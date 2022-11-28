package mat.agent.reactive.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class Warehouse {
    public enum GridCellType {
        FREE,
        DROP_ZONE,
        IDLING_ZONE,
    }

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

    public void setDropZones(Coordinate... coordinates) {
        for (Coordinate coordinate : coordinates) {
            grid[coordinate.y][coordinate.x] = GridCellType.DROP_ZONE;
        }
    }

    public void setIdlingZones(Coordinate... coordinates) {
        for (Coordinate coordinate : coordinates) {
            grid[coordinate.y][coordinate.x] = GridCellType.IDLING_ZONE;
        }
    }

    public void spawnAgents(Coordinate... coordinates) {
        for (Coordinate coordinate : coordinates) {
            Agent agent = new Agent(this, coordinate);
            agents.add(agent);
        }
    }

    // The order distribution is probably subject to change
    public void placeOrders(Order... orders) {
        // Ensure order count is less or equal than agent count
        if (orders.length > agents.size()) {
            return;
        }
        // abort current process and set new order for each agent
        for (int i = 0; i < orders.length; i++) {
            Agent agent = getAgents().get(i);
            agent.setStatus(Agent.Status.FREE);
            agent.setOrder(orders[i]);
        }
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

    public Optional<Coordinate> getClosestZoneOf(GridCellType cellType, Coordinate coordinate) {
        List<Coordinate> dropOffZones = getCoordinatesOf(cellType);
        Coordinate currentClosestDropOffZone = null;
        int currentDelta = Integer.MAX_VALUE;

        for (Coordinate dropOffZone : dropOffZones) {
            int delta = Math.abs((dropOffZone.x - coordinate.x)) +  Math.abs((dropOffZone.y - coordinate.y));

            if (delta < currentDelta) {
                currentDelta = delta;
                currentClosestDropOffZone = dropOffZone;
            }
        }

        return Optional.ofNullable(currentClosestDropOffZone);
    }

    public boolean isCollision(Coordinate coordinate) {
        for (Agent agent : getAgents()) {
            if (agent.getCoordinate().x == coordinate.x && agent.getCoordinate().y == coordinate.y) {
                return true;
            }
        }

        return false;
    }
}
