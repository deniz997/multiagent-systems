package mat.agent.reactive;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import mat.agent.reactive.model.Warehouse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;

public class App extends javafx.application.Application {
    private static final Logger logger = LogManager.getLogger(App.class);
    private Experiment experiment;

    @Override
    public void start(Stage stage) throws IOException {
        logger.info("Starting application..");
        Parent root = FXMLLoader.load(Objects.requireNonNull(App.class.getResource("view.fxml")));

        experiment = new Experiment(stage, root);
        ExperimentCase experimentCase = new ExperimentCase();
        experimentCase
                .setAgentCount(100)
                .setIdlingZoneCount(50)
                .setSizeX(21)
                .setSizeY(21)
                .setIdlingZoneDistribution(Warehouse.IdlingZoneDistribution.RANDOM);
        experiment.setCase(experimentCase);

        experiment.run();
    }

    @Override
    public void stop() {
        experiment.exit();
    }

    public static void main(String[] args) {
        launch();
    }
}