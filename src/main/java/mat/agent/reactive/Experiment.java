package mat.agent.reactive;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import mat.agent.reactive.controller.AppController;
import mat.agent.reactive.model.Agent;
import mat.agent.reactive.model.Coordinate;
import mat.agent.reactive.model.Warehouse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class Experiment {
    private static final Logger logger = LogManager.getLogger(Experiment.class);
    private static final int TIME_STEP_PERIOD_IN_MS = 2;
    private final int PASS_TROUGH_INTERVAL = 10;
    private final int STEP_COUNT_THRESHOLD = 10000;
    private int passTroughBuffer = 0;
    private int passTrough = 0;
    private int stepCount = 0;
    private final Parent root;
    private final Stage stage;
    private ExperimentCase experimentCase;
    private final Timer timer = new Timer();

    Experiment(Stage stage, Parent root) {
        this.stage = stage;
        this.root = root;
    }

    private Coordinate findFreeBorderCoordinate(Warehouse warehouse) {
        while (true) {
            Coordinate coordinate = warehouse.findRandomFreeCell();

            if ((coordinate.x == warehouse.getSizeX() - 1 || coordinate.x == 0 || coordinate.y == 0 || coordinate.y == warehouse.getSizeY() - 1) && warehouse.isProductCell(coordinate)) {
                return coordinate;
            }
        }
    }

    public Optional<Coordinate> findClosestFreeBorderCell(Warehouse warehouse, Coordinate coordinate) {
        int minDelta = Integer.MAX_VALUE;
        Coordinate minCoordinate = null;

        // Traverse top and bottom border
        for (int x = 0; x < warehouse.getSizeX(); x++) {
            Coordinate top = new Coordinate(x, 0);
            Coordinate bottom = new Coordinate(x, warehouse.getSizeY() - 1);

            if (warehouse.isProductCell(top)) {
                int delta = warehouse.getCoordinateDelta(coordinate, top);
                if (delta < minDelta) {
                    minDelta = delta;
                    minCoordinate = top;
                }
            }

            if (warehouse.isProductCell(bottom)) {
                int delta = warehouse.getCoordinateDelta(coordinate, bottom);
                if (delta < minDelta) {
                    minDelta = delta;
                    minCoordinate = bottom;
                }
            }
        }

        // Traverse left and right border
        for (int y = 0; y < warehouse.getSizeY(); y++) {
            Coordinate left = new Coordinate(0, y);
            Coordinate right = new Coordinate(warehouse.getSizeX() - 1, y);

            if (warehouse.isProductCell(left) && !(left.x == 0 && left.y == 1)) {
                int delta = warehouse.getCoordinateDelta(coordinate, left);
                if (delta < minDelta) {
                    minDelta = delta;
                    minCoordinate = left;
                }
            }

            if (warehouse.isProductCell(right) && !(right.x == warehouse.getSizeX() - 1 && left.y == 1)) {
                int delta = warehouse.getCoordinateDelta(coordinate, right);
                if (delta < minDelta) {
                    minDelta = delta;
                    minCoordinate = right;
                }
            }
        }

        return Optional.ofNullable(minCoordinate);
    }

    private void addDropZones(Warehouse warehouse) {
        for (int i = 0; i < (warehouse.getSizeX() / 2) + 1; i++) {
            warehouse.addDropZone(new Coordinate(i * 2, 0));
        }
    }

    private void addIdlingZones(Warehouse warehouse, Warehouse.IdlingZoneDistribution distribution, int count) {
        switch (distribution) {
            case RANDOM:
                for (int i = 0; i < count; i++) {
                    warehouse.addIdlingZone(warehouse.findRandomFreeCell());
                }
                break;
            case RANDOM_BORDER:
                while (count > 0) {
                    // Break early if all border cells are occupied
                    int idlingZoneCount = warehouse.getCoordinatesOf(Warehouse.GridCellType.IDLING_ZONE).size();
                    if (idlingZoneCount >= 2 * (warehouse.getSizeX() - 1) + 2 * (warehouse.getSizeY() - 1) - ((warehouse.getSizeX() / 2) + 1)) {
                        break;
                    }

                    warehouse.addIdlingZone(findFreeBorderCoordinate(warehouse));
                    count--;
                }

                for (int i = 0; i < count; i++) {
                    warehouse.addIdlingZone(warehouse.findRandomFreeCell());
                }
                break;
            case NEAREST_BORDER:
                // Place idling zones as close as possible to the drop zones

                // Fill top first
                int midX = (warehouse.getSizeX() / 2);
                int midXCounter = (warehouse.getSizeX() / 2);
                int sign = -1;
                int i = 0;

                while (midXCounter > 0) {
                    // Add top
                    warehouse.addIdlingZone(new Coordinate(midX + sign * i * 2 - 1, 0));
                    count--;

                    if (count <= 0) {
                        break;
                    }

                    sign = sign * (-1);

                    if (sign == -1) {
                        i++;
                    }

                    midXCounter--;
                }

                while (count > 0) {
                    Optional<Coordinate> coordinate = findClosestFreeBorderCell(warehouse, new Coordinate(0, 0));
                    if (coordinate.isEmpty()) {
                        break;
                    }
                    coordinate.ifPresent(warehouse::addIdlingZone);
                    count--;

                    coordinate = findClosestFreeBorderCell(warehouse, new Coordinate(warehouse.getSizeX() - 1, 0));
                    if (coordinate.isEmpty()) {
                        break;
                    }
                    coordinate.ifPresent(warehouse::addIdlingZone);
                    count--;
                }

                for (int j = 0; j < count; j++) {
                    warehouse.addIdlingZone(warehouse.findRandomFreeCell());
                }

                break;
            case DISTRIBUTED_BORDER:
                midX = (warehouse.getSizeX() / 2);
                int midY = (warehouse.getSizeY() / 2);
                i = 0;
                sign = -1;

                while (count > 0) {
                    // Add top
                    warehouse.addIdlingZone(new Coordinate(midX + sign * i * 2 - 1, 0));
                    count--;

                    if (count <= 0) {
                        break;
                    }

                    // Break early if all border cells are occupied
                    int idlingZoneCount = warehouse.getCoordinatesOf(Warehouse.GridCellType.IDLING_ZONE).size() + 1;
                    if (idlingZoneCount >= 2 * midX + 2 * midY) {
                        break;
                    }

                    // Add left
                    Coordinate left = new Coordinate(0, midY + sign * i * 2);
                    if (left.y != 0) {
                        warehouse.addIdlingZone(left);
                    }
                    count--;

                    if (count <= 0) {
                        break;
                    }

                    // Break early if all border cells are occupied
                    idlingZoneCount = warehouse.getCoordinatesOf(Warehouse.GridCellType.IDLING_ZONE).size() + 1;
                    if (idlingZoneCount >= 2 * midX + 2 * midY) {
                        break;
                    }

                    // Add right
                    Coordinate right = new Coordinate(warehouse.getSizeX() - 1, midY + sign * i * 2);
                    if (right.y != 0) {
                        warehouse.addIdlingZone(right);
                    }
                    count--;

                    if (count <= 0) {
                        break;
                    }

                    // Break early if all border cells are occupied
                    idlingZoneCount = warehouse.getCoordinatesOf(Warehouse.GridCellType.IDLING_ZONE).size() + 1;
                    if (idlingZoneCount >= 2 * midX + 2 * midY) {
                        break;
                    }

                    // Add bottom
                    warehouse.addIdlingZone(new Coordinate(midX + sign * i * 2, warehouse.getSizeY() - 1));
                    count--;

                    if (count <= 0) {
                        break;
                    }

                    // Break early if all border cells are occupied
                    idlingZoneCount = warehouse.getCoordinatesOf(Warehouse.GridCellType.IDLING_ZONE).size() + 1;
                    if (idlingZoneCount >= 2 * midX + 2 * midY) {
                        break;
                    }

                    sign = sign * (-1);

                    if (sign == -1) {
                        i++;
                    }
                }

                for (int j = 0; j < count; j++) {
                    warehouse.addIdlingZone(warehouse.findRandomFreeCell());
                }

                break;
        }
    }

    private void addAgents(Warehouse warehouse, int count) {
        for (int i = 0; i < count; i++) {
            warehouse.spawnAgent(warehouse.findRandomFreeCell());
        }
    }

    public void setCase(ExperimentCase experimentCase) {
        this.experimentCase = experimentCase;
    }

    private Warehouse setUpWarehouse() {
        Warehouse warehouse = new Warehouse(experimentCase.getSizeX(), experimentCase.getSizeY());
        warehouse.setOrderDistributionStrategy(experimentCase.getOrderDistributionStrategy());
        addDropZones(warehouse);
        addIdlingZones(warehouse, experimentCase.getIdlingZoneDistribution(), experimentCase.getIdlingZoneCount());
        // Important to add agents last
        addAgents(warehouse, experimentCase.getAgentCount());

        logger.info("Grid - Size x:" + experimentCase.getSizeX() + "," + "Size y:" + experimentCase.getSizeY() + "," +
                "Number of idling zones:" + experimentCase.getIdlingZoneCount() + "," +
                "Number of agents:" + experimentCase.getAgentCount() + "," +
                "Idling zone distribution:" + experimentCase.getIdlingZoneDistribution() + "," +
                "OrderDistributionStrategy:" + experimentCase.getOrderDistributionStrategy().getClass().toString());

        return warehouse;
    }

    public void run() {
        if (Objects.isNull(experimentCase)) {
            return;
        }

        Warehouse warehouse = setUpWarehouse();


        while (stepCount <= STEP_COUNT_THRESHOLD) {
            if (stepCount % 10000 == 0) {
                System.out.println(stepCount + " steps");
            }

            stepCount++;
            List<Agent> agents = warehouse.getAgents();
            List<Agent.Status> statusList = new LinkedList<>();

            warehouse.distributeOrders();

            for (Agent agent : agents) {
                statusList.add(agent.react());
            }

            for (Agent.Status status : statusList) {
                if (status == Agent.Status.FINISHED_ORDER) {
                    passTroughBuffer += 1;
                    passTrough += 1;
                }
            }

            if (stepCount % PASS_TROUGH_INTERVAL == 0) {
                //logger.info(passTroughBuffer + " finished orders in " + PASS_TROUGH_INTERVAL + " time steps");
                passTroughBuffer = 0;
            }
        }

        // Sum together all collisions
        logger.info("Collisions: " + warehouse.getAgents().stream().mapToInt(Agent::getCollisionCount).sum());
        exit();
    }

    public void runGui() {
        if (Objects.isNull(experimentCase)) {
            return;
        }

        Warehouse warehouse = setUpWarehouse();

        AppController appController = new AppController(root);
        Scene scene = new Scene(root, 800, 800);
        stage.setTitle("MAS Homework 1");
        stage.setScene(scene);
        stage.show();


        appController.buildGrid(warehouse);

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // Mutate warehouse state and visualize new state
                Platform.runLater(() -> {
                    stepCount++;
                    List<Agent> agents = warehouse.getAgents();
                    List<Agent.Status> statusList = new LinkedList<>();

                    warehouse.distributeOrders();

                    for (Agent agent : agents) {
                        statusList.add(agent.react());
                    }

                    appController.commitState(warehouse);

                    for (Agent.Status status : statusList) {
                        if (status == Agent.Status.FINISHED_ORDER) {
                            passTroughBuffer += 1;
                            passTrough += 1;
                        }
                    }

                    if (stepCount % PASS_TROUGH_INTERVAL == 0) {
                        //logger.info(passTroughBuffer + " finished orders in " + PASS_TROUGH_INTERVAL + " time steps");
                        passTroughBuffer = 0;
                    }

                    // Stop application if step threshold is reached
                    if (stepCount >= STEP_COUNT_THRESHOLD) {
                        // Exit
                        exit();
                    }
                });
            }
        }, 0, TIME_STEP_PERIOD_IN_MS);
    }


    public void exit() {
        //logger.info("Took " + stepCount + " steps");
        //logger.info(passTroughBuffer + " finished orders in " + PASS_TROUGH_INTERVAL + " time steps");
        float passTroughPerStepCount = (passTrough / (float) stepCount);
        logger.info("Result - Total completed orders:" + passTrough + "," + "Average completed orders:" + passTroughPerStepCount + "," + "For time steps count:" + PASS_TROUGH_INTERVAL);
        stop();
        Platform.exit();
        System.exit(0);
    }

    private void stop() {
        cleanup();
        logger.info("Stopping application..");
    }


    private void cleanup() {
        timer.cancel();
    }
}
