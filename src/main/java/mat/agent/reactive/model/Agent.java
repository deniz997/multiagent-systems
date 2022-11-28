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

    // TODO: More intelligent path finding, maybe Dijkstra
    public void moveTo(Coordinate coordinate) {
        Coordinate proposedCoordinate = new Coordinate(currentPos.x, currentPos.y);

        if (currentPos.x > coordinate.x) {
            proposedCoordinate.x -= 1;
        } else if (currentPos.x < coordinate.x) {
            proposedCoordinate.x += 1;
        } else if (currentPos.y > coordinate.y) {
            proposedCoordinate.y -= 1;
        } else if (currentPos.y < coordinate.y) {
            proposedCoordinate.y += 1;
        }

        if (warehouse.isCollision(proposedCoordinate)) {
            // In case of collision try 10 times to move randomly
            int counter = 10;

            while (counter > 0) {
                int move = new Random().nextInt(4);

                switch (move) {
                    case 0:
                        if (!warehouse.isCollision(new Coordinate(currentPos.x - 1, currentPos.y))) {
                            currentPos = new Coordinate(currentPos.x - 1, currentPos.y);
                            return;
                        }
                        break;
                    case 1:
                        if (!warehouse.isCollision(new Coordinate(currentPos.x + 1, currentPos.y))) {
                            currentPos = new Coordinate(currentPos.x + 1, currentPos.y);
                            return;
                        }
                        break;
                    case 2:
                        if (!warehouse.isCollision(new Coordinate(currentPos.x, currentPos.y - 1))) {
                            currentPos = new Coordinate(currentPos.x, currentPos.y - 1);
                            return;
                        }
                        break;
                    case 3:
                        if (!warehouse.isCollision(new Coordinate(currentPos.x , currentPos.y + 1))) {
                            currentPos = new Coordinate(currentPos.x, currentPos.y + 1);
                            return;
                        }
                        break;
                }

                counter--;
            }
        } else {
            currentPos = proposedCoordinate;
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
                        .ifPresent(coordinate ->  moveToCoordinate(coordinate, () -> {
                            status = Status.TO_DROP_OFF;
                        }));
                break;
            case TO_DROP_OFF:
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
