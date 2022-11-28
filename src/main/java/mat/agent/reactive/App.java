package mat.agent.reactive;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import mat.agent.reactive.controller.AppController;
import mat.agent.reactive.model.Agent;
import mat.agent.reactive.model.Warehouse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;

public class App extends javafx.application.Application {
    private static final int TIME_STEP_PERIOD_IN_MS = 500;
    private final int PASS_TROUGH_INTERVAL = 10;
    private int passTroughBuffer = 0;
    private int passTrough = 0;
    private int stepCount = 0;

    private static final Logger logger = LogManager.getLogger(App.class);
    private final Timer timer = new Timer();

    @Override
    public void start(Stage stage) throws IOException {
        logger.info("Starting application..");
        Optional<Warehouse> warehouse = ConfigLoader.getWarehouseSetup();

        if (warehouse.isEmpty()) {
            logger.error("Warehouse config could not be loaded");
            stop();
            return;
        }

        // TODO: Logging warehouse setup for analysis of logs

        Parent root = FXMLLoader.load(Objects.requireNonNull(App.class.getResource("view.fxml")));
        AppController appController = new AppController(root);
        appController.buildGrid(warehouse.get());
        Scene scene = new Scene(root, 800, 600);
        stage.setTitle("MAS Homework 1");
        stage.setScene(scene);
        stage.show();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // Mutate warehouse state and visualize new state
                Platform.runLater(() -> {
                    stepCount++;
                    List<Agent> agents = warehouse.get().getAgents();
                    List<Agent.Status> statusList = new LinkedList<>();

                    // TODO? warehouse.get().distributeOrders();

                    for (Agent agent: agents) {
                        statusList.add(agent.react());
                    }

                    appController.commitState(warehouse.get());

                    for (Agent.Status status : statusList) {
                        if (status == Agent.Status.DROPPED_OFF) {
                            passTroughBuffer += 1;
                            passTrough += 1;
                        }
                    }

                    if (stepCount % PASS_TROUGH_INTERVAL == 0) {
                        logger.info(passTroughBuffer + " dropped off products in " + PASS_TROUGH_INTERVAL + " time steps");
                        passTroughBuffer = 0;
                    }

                    if (statusList.stream().allMatch(status -> status == Agent.Status.FREE)) {
                        logger.info(passTroughBuffer + " dropped off products in " + (stepCount % PASS_TROUGH_INTERVAL) + " time steps");
                        Platform.exit();
                        stop();
                        System.exit(0);
                    }
                });
            }
        }, 0, TIME_STEP_PERIOD_IN_MS);
    }

    @Override
    public void stop() {
        cleanup();
        logger.info("Took " + stepCount + " steps");
        float passTroughPerStepCount = (passTrough / (float) stepCount);
        logger.info(passTroughPerStepCount + " dropped of orders per " + PASS_TROUGH_INTERVAL + " time steps");
        logger.info("Stopping application..");
    }

    public static void main(String[] args) {
        launch();
    }

    private void cleanup() {
        timer.cancel();
    }
}