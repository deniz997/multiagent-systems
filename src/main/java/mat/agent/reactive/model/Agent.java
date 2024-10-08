package mat.agent.reactive.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.Optional;
import java.util.Random;

public class Agent {
    private static final Logger logger = LogManager.getLogger(Agent.class);
    private int pendingCount = 0;
    private Coordinate freeRandomCell;
    private final String id = String.valueOf(Counter.increment());
    private int collisionCount = 0;

    public enum Status {
        FREE,
        GET_PRODUCT,
        TO_DROP_OFF,
        TO_IDLING_ZONE
    }

    private final Warehouse warehouse;
    private Status status = Status.FREE;
    private Order order;
    private Coordinate currentPos;
    private Coordinate movingTo;

    public Agent(Warehouse warehouse, Coordinate currentPos) {
        this.warehouse = warehouse;

        if (!warehouse.isCollision(currentPos)) {
            this.currentPos = currentPos;
        } else {
            logger.error("Agent was put into illegal initial position");
        }
    }

    public String getId() {
        return id;
    }

    public Status getStatus() {
        return status;
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

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public boolean canReceiveOrder() {
        return pendingCount <= 0 && status == Status.FREE;
    }

    public int getCollisionCount() {
        return collisionCount;
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
        collisionCount++;

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
        movingTo = coordinate;
        moveTo(coordinate);

        // Check if coordinate is equal to current position
        if (currentPos.x == coordinate.x && currentPos.y == coordinate.y) {
            successCallback.run();
            movingTo = null;
        }
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean hasOrder() {
        return Objects.nonNull(order) && !order.isCompleted();
    }

    public Status react() {
        switch (status) {
            // If status is free and order is pending change into get product
            case FREE:
                if (hasOrder()) {
                    // TODO: Maybe adjust this parameter
                    pendingCount = new Random().nextInt(2 * (warehouse.getSizeX() + warehouse.getSizeY()));
                    setStatus(Status.GET_PRODUCT);
                } else {
                    pendingCount--;
                }
                break;
            case GET_PRODUCT:
                order.getNextCoordinate()
                        .ifPresent(coordinate -> moveToCoordinate(coordinate, () -> {
                            warehouse.report(Warehouse.ReportType.GOOD_PICKED_UP, this);
                            status = Status.TO_DROP_OFF;
                        }));
                break;
            case TO_DROP_OFF:
                Optional<Coordinate> nextDropZone = warehouse.getClosestZoneOf(Warehouse.GridCellType.DROP_ZONE, currentPos);
                nextDropZone.ifPresent(coordinate -> moveToCoordinate(coordinate, () -> {
                    order.drop();
                    warehouse.report(Warehouse.ReportType.GOOD_DROPPED, this);
                    if (hasOrder()) {
                        status = Status.GET_PRODUCT;
                    } else {
                        status = Status.TO_IDLING_ZONE;
                    }
                }));

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

    public Optional<Integer> getBidForOrder(Order order) {
       // Get the distance of the first product in the order
        return order.getNextCoordinate()
                .map(coordinate -> warehouse.getCoordinateDelta(currentPos, coordinate));
    }

    public Optional<Coordinate> getMovingTo() {
        if (Objects.nonNull(movingTo)) {
            return Optional.of(movingTo);
        }

        return Optional.empty();
    }
}
