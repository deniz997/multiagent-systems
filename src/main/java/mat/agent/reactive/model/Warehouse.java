package mat.agent.reactive.model;

import mat.agent.reactive.strategy.OrderDistributionStrategy;
import mat.agent.reactive.strategy.BasicStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

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

    public enum ReportType {
        GOOD_DROPPED,
        GOOD_PICKED_UP,
    }

    private final int sizeX;
    private final int sizeY;
    private final GridCellType[][] grid;
    private final List<Agent> agents = new LinkedList<>();
    private static final Logger logger = LogManager.getLogger(Warehouse.class);
    // Basic strategy by default
    private OrderDistributionStrategy orderDistributionStrategy = new BasicStrategy();

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

    public void setOrderDistributionStrategy(OrderDistributionStrategy orderDistributionStrategy) {
        this.orderDistributionStrategy = orderDistributionStrategy;
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

    public void distributeOrders() {
        if (Objects.nonNull(orderDistributionStrategy)) {
            orderDistributionStrategy.distribute(this);
        } else {
            logger.warn("No order distribution strategy set");
        }
    }

    public int getCoordinateDelta(Coordinate coordinate1, Coordinate coordinate2) {
        return Math.abs((coordinate1.x - coordinate2.x)) +  Math.abs((coordinate1.y - coordinate2.y));
    }

    // The warehouse can get different types of reports from the agents
    public void report(ReportType reportType, Agent agent) {
        if (Objects.nonNull(orderDistributionStrategy)) {
            orderDistributionStrategy.onReport(reportType, agent);
        }
    }

    public Agent getAgentByCoordinate(Coordinate coordinate) {
        for (Agent agent : getAgents()) {
            if (agent.getCoordinate().x == coordinate.x && agent.getCoordinate().y == coordinate.y) {
                return agent;
            }
        }

        return null;
    }

    public int getCompletedOrders() {
        return orderDistributionStrategy.getCompletedOrders();
    }

    public List<Good> findOptimalPath(Order order) {
        // Find the optimal path for the order
        List<Good> goods = order.getGoods();
        List<Good> optimalPath = new LinkedList<>();

        if (goods.size() == 0) {
            return optimalPath;
        }

        // Find the closest good to the starting good
        Good currentGood = goods.get(0);
        optimalPath.add(currentGood);
        goods.remove(currentGood);

        while (!goods.isEmpty()) {
            Good closestGood = null;
            int currentDelta = Integer.MAX_VALUE;

            for (Good good : goods) {
                assert currentGood != null;
                int delta = getCoordinateDelta(currentGood.getCoordinate(), good.getCoordinate());

                if (delta < currentDelta) {
                    currentDelta = delta;
                    closestGood = good;
                }
            }

            optimalPath.add(closestGood);
            goods.remove(closestGood);
            currentGood = closestGood;
        }

        return optimalPath;
    }

}
