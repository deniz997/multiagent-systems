package mat.agent.reactive.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class Warehouse {
    public enum IdlingZoneDistribution {
        RANDOM_BORDER,
        RANDOM,
        NEAREST_BORDER,
        DISTRIBUTED_BORDER
    }

    public enum GridCellType {
        FREE,
        DROP_ZONE,
        IDLING_ZONE,
    }

    private static final int ORDER_SIZE = 3;
    private final int sizeX;
    private final int sizeY;
    private final GridCellType[][] grid;
    private final List<Agent> agents = new LinkedList<>();

    public Warehouse(int sizeX, int sizeY) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.grid = new GridCellType[sizeX][sizeY];

        // Initialize empty grid
        for (int y = 0; y < sizeY; y++) {
            for (int x = 0; x < sizeX; x++) {
                grid[y][x] = GridCellType.FREE;
            }
        }
    }

    public int getSizeX() {
        return sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }

    public GridCellType[][] getSetup() {
        return grid;
    }

    public void addDropZone(Coordinate coordinate) {
        if (isInBounds(coordinate)) {
            grid[coordinate.y][coordinate.x] = GridCellType.DROP_ZONE;
        }
    }

    public void addIdlingZone(Coordinate coordinate) {
        if (isInBounds(coordinate)) {
            grid[coordinate.y][coordinate.x] = GridCellType.IDLING_ZONE;
        }
    }

    public void spawnAgent(Coordinate coordinate) {
        Agent agent = new Agent(this, coordinate);
        agents.add(agent);
    }

    public List<Agent> getAgents() {
        return agents;
    }

    public List<Coordinate> getAgentCoordinates() {
        List<Coordinate> coordinates = new LinkedList<>();

        for (Agent agent : getAgents()) {
            coordinates.add(agent.getCoordinate());
        }

        return coordinates;
    }

    public List<Coordinate> getCoordinatesOf(GridCellType cellType) {
        List<Coordinate> coordinates = new LinkedList<>();

        for (int y = 0; y < sizeY; y++) {
            for (int x = 0; x < sizeX; x++) {
                if (grid[y][x] == cellType) {
                    coordinates.add(new Coordinate(x, y));
                }
            }
        }

        return coordinates;
    }

    // Gets the closest zone of given cell type which is not already occupied
    public Optional<Coordinate> getClosestZoneOf(GridCellType cellType, Coordinate coordinate) {
        List<Coordinate> zones = getCoordinatesOf(cellType);
        Coordinate currentClosestZone = null;
        int currentDelta = Integer.MAX_VALUE;

        for (Coordinate zone : zones) {
             if (isCollision(zone)) {
                continue;
            }

            int delta = getCoordinateDelta(coordinate, zone);

            if (delta < currentDelta) {
                currentDelta = delta;
                currentClosestZone = zone;
            }
        }

        return Optional.ofNullable(currentClosestZone);
    }

    public boolean isCollision(Coordinate coordinate) {
        if (!isInBounds(coordinate)) {
            return false;
        }

        for (Agent agent : getAgents()) {
            if (agent.getCoordinate().x == coordinate.x && agent.getCoordinate().y == coordinate.y) {
                return true;
            }
        }

        return false;
    }

    public boolean isInBounds(Coordinate coordinate) {
        return coordinate.x >= 0 && coordinate.x < sizeX && coordinate.y >= 0 && coordinate.y < sizeY;
    }

    public boolean isProductCell(Coordinate coordinate) {
        return grid[coordinate.y][coordinate.x] == GridCellType.FREE && !isCollision(coordinate);
    }

    public Coordinate findRandomFreeCell() {
        while (true) {
            int x = new Random().nextInt(sizeX);
            int y = new Random().nextInt(sizeY);
            Coordinate coordinate = new Coordinate(x, y);

            if (isProductCell(coordinate)) {
                return coordinate;
            }
        }
    }

    private Order generateOrder() {
        LinkedList<Coordinate> orderCoordinates = new LinkedList<>();

        for (int i = 0; i < ORDER_SIZE; i++) {
            orderCoordinates.add(findRandomFreeCell());
        }

        return new Order(orderCoordinates);
    }

    public void distributeOrders() {
        for (Agent agent : getAgents()) {
            if (agent.canReceiveOrder()) {
                agent.setOrder(generateOrder());
            }
        }
    }

    public int getCoordinateDelta(Coordinate coordinate1, Coordinate coordinate2) {
        return Math.abs((coordinate1.x - coordinate2.x)) +  Math.abs((coordinate1.y - coordinate2.y));
    }
}
