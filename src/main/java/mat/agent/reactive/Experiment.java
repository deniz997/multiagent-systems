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
    private static final int TIME_STEP_PERIOD_IN_MS = 10;
    private final int PASS_TROUGH_INTERVAL = 10;
    private final int STEP_COUNT_THRESHOLD = 1000;
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

    private Optional<Coordinate> findClosestFreeBorderCellWithAtLeastOneDelta(Warehouse warehouse, Coordinate coordinate) {
        int minDelta = Integer.MAX_VALUE;
        Coordinate minCoordinate = null;

        // Traverse top and bottom border
        for (int x = 0; x < warehouse.getSizeX(); x++) {
            Coordinate top = new Coordinate(x, 0);
            Coordinate bottom = new Coordinate(x, warehouse.getSizeY() - 1);

            if (warehouse.isProductCell(top)) {
                int delta = warehouse.getCoordinateDelta(coordinate, top);

                if (delta < minDelta && delta > 1 && top.x > coordinate.x && top.y == coordinate.y) {
                    minDelta = delta;
                    minCoordinate = top;
                }
            }

            if (warehouse.isProductCell(bottom)) {
                int delta = warehouse.getCoordinateDelta(coordinate, bottom);

                if (delta < minDelta && delta > 1 && bottom.x > coordinate.x && bottom.y == coordinate.y) {
                    minDelta = delta;
                    minCoordinate = bottom;
                }
            }
        }

        // Traverse left and right border
        for (int y = 0; y < warehouse.getSizeY(); y++) {
            Coordinate left = new Coordinate(0, y);
            Coordinate right = new Coordinate(warehouse.getSizeX() - 1, y);

            if (warehouse.isProductCell(left)) {
                int delta = warehouse.getCoordinateDelta(coordinate, left);
                if (delta < minDelta && delta > 1 && left.y > coordinate.y && left.x == coordinate.x) {
                    minDelta = delta;
                    minCoordinate = left;
                }
            }

            if (warehouse.isProductCell(right)) {
                int delta = warehouse.getCoordinateDelta(coordinate, right);
                if (delta < minDelta && delta > 1 && right.y > coordinate.y && right.x == coordinate.x) {
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
                    warehouse.addIdlingZone(findFreeBorderCoordinate(warehouse));
                    count--;
                }
                break;
            case NEAREST_BORDER:
                // Place idling zones as close as possible to the drop zones
                while (count > 0) {
                    Optional<Coordinate> coordinate = findClosestFreeBorderCell(warehouse, new Coordinate(0, 0));
                    coordinate.ifPresent(warehouse::addIdlingZone);
                    count--;

                    coordinate = findClosestFreeBorderCell(warehouse, new Coordinate(warehouse.getSizeX() - 1, 0));
                    coordinate.ifPresent(warehouse::addIdlingZone);
                    count--;
                }

                break;
            case DISTRIBUTED_BORDER:
                int zonesPerBorder = count / 3;
                int rest = count % 3;
                int startX = (int) Math.floor((warehouse.getSizeX() - 1.0) / zonesPerBorder - 1);
                Optional<Coordinate> lastCoordinate = findClosestFreeBorderCellWithAtLeastOneDelta(warehouse, new Coordinate(startX, 0));
                lastCoordinate.ifPresent(warehouse::addIdlingZone);

                // top
                for (int i = 1; i < zonesPerBorder + rest; i++) {
                    if (lastCoordinate.isPresent()) {
                        lastCoordinate = findClosestFreeBorderCellWithAtLeastOneDelta(warehouse, lastCoordinate.get());
                        lastCoordinate.ifPresent(warehouse::addIdlingZone);
                 }
                }

                lastCoordinate = findClosestFreeBorderCellWithAtLeastOneDelta(warehouse, new Coordinate(startX, warehouse.getSizeY() - 1));
                lastCoordinate.ifPresent(warehouse::addIdlingZone);

                // bottom
                for (int i = 1; i < zonesPerBorder + rest; i++) {
                    if (lastCoordinate.isPresent()) {
                        lastCoordinate = findClosestFreeBorderCellWithAtLeastOneDelta(warehouse, lastCoordinate.get());
                        lastCoordinate.ifPresent(warehouse::addIdlingZone);
                    }
                }

                int startY = (int) Math.floor((warehouse.getSizeY() - 1.0) / zonesPerBorder - 1);
                lastCoordinate = findClosestFreeBorderCellWithAtLeastOneDelta(warehouse, new Coordinate(0, startY));
                lastCoordinate.ifPresent(warehouse::addIdlingZone);

                // left
                for (int i = 1; i < zonesPerBorder; i++) {
                    if (lastCoordinate.isPresent()) {
                        lastCoordinate = findClosestFreeBorderCellWithAtLeastOneDelta(warehouse, lastCoordinate.get());
                        lastCoordinate.ifPresent(warehouse::addIdlingZone);
                    }
                }

                lastCoordinate = findClosestFreeBorderCellWithAtLeastOneDelta(warehouse, new Coordinate(warehouse.getSizeX() - 1, startY));
                lastCoordinate.ifPresent(warehouse::addIdlingZone);

                // right
                for (int i = 1; i < zonesPerBorder; i++) {
                    if (lastCoordinate.isPresent()) {
                        lastCoordinate = findClosestFreeBorderCellWithAtLeastOneDelta(warehouse, lastCoordinate.get());
                        lastCoordinate.ifPresent(warehouse::addIdlingZone);
                    }
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

    public void run() {
        AppController appController = new AppController(root);
        Scene scene = new Scene(root, 800, 800);
        stage.setTitle("MAS Homework 1");
        stage.setScene(scene);
        stage.show();

        if (Objects.isNull(experimentCase)) {
            return;
        }

        Warehouse warehouse = new Warehouse(experimentCase.getSizeX(), experimentCase.getSizeY());
        addDropZones(warehouse);
        addIdlingZones(warehouse, experimentCase.getIdlingZoneDistribution(), experimentCase.getIdlingZoneCount());
        // Important to add agents last
        addAgents(warehouse, experimentCase.getAgentCount());

        logger.info("Grid - Size x:" + experimentCase.getSizeX() + "," + "Size y:" + experimentCase.getSizeY() + "," +
                "Number of idling zones:" + experimentCase.getIdlingZoneCount() + "," +
                "Number of agents:" + experimentCase.getAgentCount() + "," +
        "Idling zone distribution:" + experimentCase.getIdlingZoneDistribution());

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
        logger.info("Result - Total completed orders:" + passTrough + "," + "Average completed orders:" + PASS_TROUGH_INTERVAL + "," + "For time steps count:" + passTroughPerStepCount);
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
