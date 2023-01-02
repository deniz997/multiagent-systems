package mat.agent.reactive;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import mat.agent.reactive.model.Warehouse;
import mat.agent.reactive.strategy.CNPStrategy;
import mat.agent.reactive.strategy.ECNPStrategy;
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
        int agentCount = 15;
        int idlingZoneCount = (int) (agentCount*0.5);
        experiment = new Experiment(stage, root);
        ExperimentCase experimentCase = new ExperimentCase();
        experimentCase
                .setAgentCount(agentCount)
                .setIdlingZoneCount(idlingZoneCount)
                .setSizeX(11)
                .setSizeY(11)
                .setOrderDistributionStrategy(new ECNPStrategy(10, 100))
                .setIdlingZoneDistribution(Warehouse.IdlingZoneDistribution.RANDOM);

        experiment.setCase(experimentCase);
        experiment.runGui();
    }

    @Override
    public void stop() {
        experiment.exit();
    }

    public static void main(String[] args) {
        launch();
    }
}