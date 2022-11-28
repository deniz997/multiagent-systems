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
    private static final int TIME_STEP_PERIOD_IN_MS = 1000;

    private static final Logger logger = LogManager.getLogger(App.class);
    private final Timer timer = new Timer();

    @Override
    public void start(Stage stage) throws IOException {
        logger.info("Starting application..");
        Optional<Warehouse> warehouse = ConfigLoader.getWarehouseSetup();

        if (warehouse.isEmpty()) {
            logger.error("Warehouse config could not be loaded");
            this.stop();
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
                    List<Agent> agents = warehouse.get().getAgents();

                    // TODO? warehouse.get().distributeOrders();

                    for (Agent agent: agents) {
                        agent.react();
                    }

                    appController.commitState(warehouse.get());
                });
            }
        }, 0, TIME_STEP_PERIOD_IN_MS);
    }

    @Override
    public void stop() {
        cleanup();
        logger.info("Stopping application..");
    }

    public static void main(String[] args) {
        launch();
    }

    private void cleanup() {
        timer.cancel();
    }
}