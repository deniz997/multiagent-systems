package mat.agent.reactive.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.Random;

public class Agent {
    private static final Logger logger = LogManager.getLogger(Agent.class);

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

    // WIP
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


        // In case of collision in both x and y we try 5 times to move randomly
        int counter = 5;

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

    public void react() {
        switch (status) {
            // If status is free and order is pending change into get product
            case FREE:
                if (hasOrder()) {
                    setStatus(Status.GET_PRODUCT);
                }
                break;
            case GET_PRODUCT:
                order.getNextCoordinate()
                        .ifPresent(coordinate -> moveToCoordinate(coordinate, () -> {
                            status = Status.TO_DROP_OFF;
                        }));
                break;
            case TO_DROP_OFF:
                // TODO: Maybe go to free status if no drop zone is available
                warehouse.getClosestZoneOf(Warehouse.GridCellType.DROP_ZONE, currentPos)
                        .ifPresent(coordinate -> moveToCoordinate(coordinate, () -> {
                            order.pop();

                            if (hasOrder()) {
                                status = Status.GET_PRODUCT;
                            } else {
                                status = Status.TO_IDLING_ZONE;
                            }
                        }));
                break;
            case TO_IDLING_ZONE:
                warehouse.getClosestZoneOf(Warehouse.GridCellType.IDLING_ZONE, currentPos)
                        .ifPresent(coordinate -> moveToCoordinate(coordinate, () -> {
                            status = Status.FREE;
                        }));
                break;
        }
    }

    public Coordinate getCoordinate() {
        return currentPos;
    }
}
