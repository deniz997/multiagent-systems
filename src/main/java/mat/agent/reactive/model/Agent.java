package mat.agent.reactive.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class Agent {
    private static final Logger logger = LogManager.getLogger(Agent.class);
    private int pendingCount = 0;
    private Coordinate freeRandomCell;

    public enum Status {
        FREE,
        GET_PRODUCT,
        TO_DROP_OFF,
        FINISHED_ORDER,
        TO_IDLING_ZONE
    }

    private final Warehouse warehouse;
    private Status status = Status.FREE;
    private Order order;
    private Coordinate currentPos;

    public Agent(Warehouse warehouse, Coordinate currentPos) {
        this.warehouse = warehouse;

        if (!warehouse.isCollision(currentPos)) {
            this.currentPos = currentPos;
        } else {
            logger.error("Agent was put into illegal initial position");
        }
    }

    public void setCurrentPosX(int x) {
        if (warehouse.isInBounds(new Coordinate(x, currentPos.y))) {
            this.currentPos.x = x;
        }
    }

    public void setCurrentPosY(int y) {
        if (warehouse.isInBounds(new Coordinate(currentPos.x, y))) {
            this.currentPos.y = y;
        }
    }

    public boolean canReceiveOrder() {
        return pendingCount <= 0;
    }

    public void moveTo(Coordinate coordinate) {
        int moveX = 0;
        int moveY = 0;

        if (currentPos.x > coordinate.x && !warehouse.isCollision(new Coordinate(currentPos.x - 1, currentPos.y))) {
            moveX -= 1;
        } else if (currentPos.x < coordinate.x && !warehouse.isCollision(new Coordinate(currentPos.x + 1, currentPos.y))) {
            moveX += 1;
        }
        if (currentPos.y > coordinate.y && !warehouse.isCollision(new Coordinate(currentPos.x, currentPos.y - 1))) {
            moveY -= 1;
        } else if (currentPos.y < coordinate.y && !warehouse.isCollision(new Coordinate(currentPos.x, currentPos.y + 1))) {
            moveY += 1;
        }

        // If agent can move in x but also in y we choose randomly what to do
        if (moveX != 0 && moveY != 0) {
            int move = new Random().nextInt(2);

            switch (move) {
                case 0:
                    setCurrentPosX(currentPos.x + moveX);
                    break;
                case 1:
                    setCurrentPosY(currentPos.y + moveY);
                    break;
            }
            return;
        } else if (moveX != 0) {
            setCurrentPosX(currentPos.x + moveX);
            return;
        } else if (moveY != 0) {
            setCurrentPosY(currentPos.y + moveY);
            return;
        }
        //logger.warn("Collision");

        // In case of collision in both x and y we try 10 times to move randomly
        int counter = 10;

        while (counter > 0) {
            int move = new Random().nextInt(4);

            switch (move) {
                case 0:
                    if (!warehouse.isCollision(new Coordinate(currentPos.x - 1, currentPos.y))) {
                        setCurrentPosX(currentPos.x - 1);
                        return;
                    }
                    break;
                case 1:
                    if (!warehouse.isCollision(new Coordinate(currentPos.x + 1, currentPos.y))) {
                        setCurrentPosX(currentPos.x + 1);
                        return;
                    }
                    break;
                case 2:
                    if (!warehouse.isCollision(new Coordinate(currentPos.x, currentPos.y - 1))) {
                        setCurrentPosY(currentPos.y - 1);
                        return;
                    }
                    break;
                case 3:
                    if (!warehouse.isCollision(new Coordinate(currentPos.x, currentPos.y + 1))) {
                        setCurrentPosY(currentPos.y + 1);
                        return;
                    }
                    break;
            }

            counter--;
        }

        //logger.info("Stuck");
    }

    private void moveToCoordinate(Coordinate coordinate, Runnable successCallback) {
        moveTo(coordinate);

        // Check if coordinate is equal to current position
        if (currentPos.x == coordinate.x && currentPos.y == coordinate.y) {
            successCallback.run();
        }
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean hasOrder() {
        return Objects.nonNull(order) && order.count() > 0;
    }

    public Status react() {
        switch (status) {
            // If status is free and order is pending change into get product
            case FREE:
                if (hasOrder()) {
                    // TODO: Maybe adjust this parameter
                    pendingCount = new Random().nextInt(3 * (warehouse.getSizeX() + warehouse.getSizeY()));
                    setStatus(Status.GET_PRODUCT);
                } else {
                    pendingCount--;
                }
                break;
            case GET_PRODUCT:
                order.getNextCoordinate()
                        .ifPresent(coordinate -> moveToCoordinate(coordinate, () -> {
                            status = Status.TO_DROP_OFF;
                        }));
                break;
            case TO_DROP_OFF:
                AtomicBoolean isFinishedOrder = new AtomicBoolean(false);
                Optional<Coordinate> nextDropZone = warehouse.getClosestZoneOf(Warehouse.GridCellType.DROP_ZONE, currentPos);

                nextDropZone.ifPresent(coordinate -> moveToCoordinate(coordinate, () -> {
                    order.pop();

                    if (hasOrder()) {
                        status = Status.GET_PRODUCT;
                    } else {
                        isFinishedOrder.set(true);
                        status = Status.TO_IDLING_ZONE;
                    }
                }));

                if (isFinishedOrder.get()) {
                    isFinishedOrder.set(false);
                    return Status.FINISHED_ORDER;
                }
                break;
            case TO_IDLING_ZONE:
                pendingCount--;
                Optional<Coordinate> nextIdlingZone = warehouse.getClosestZoneOf(Warehouse.GridCellType.IDLING_ZONE, currentPos);

                if (nextIdlingZone.isPresent()) {
                    moveToCoordinate(nextIdlingZone.get(), () -> {
                        //logger.info("In idling zone");
                        status = Status.FREE;
                    });
                } else {
                    //logger.info("No idling zone available");
                    // Move to random free coordinate when no idling zone is available, constantly check if coordinate is free
                    if (Objects.nonNull(freeRandomCell) && warehouse.isProductCell(freeRandomCell)) {
                        moveToCoordinate(freeRandomCell, () -> {
                            status = Status.FREE;
                            freeRandomCell = null;
                        });
                    } else {
                        freeRandomCell = warehouse.findRandomFreeCell();
                    }
                }

                break;
        }

        return status;
    }

    public Coordinate getCoordinate() {
        return currentPos;
    }

    public int getBidForOrder(Order order) {
        // Accumulate the distance of all products in the order
        Coordinate currentCoordinate = getCoordinate();

        int bid = 0;

        for (Coordinate coordinate : order.getCoordinates()) {
            bid += warehouse.getCoordinateDelta(currentCoordinate, coordinate);
            currentCoordinate = coordinate;
        }

        return bid;
    }
}
