package mat.agent.reactive;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import mat.agent.reactive.model.Warehouse;
import mat.agent.reactive.strategy.BasicStrategy;
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
        int agentCount = 100;
        int idlingZoneCount = (int) (agentCount*0.5);
        experiment = new Experiment(stage, root);
        ExperimentCase experimentCase1 = new ExperimentCase();
        experimentCase1
                .setAgentCount(agentCount)
                .setIdlingZoneCount(idlingZoneCount)
                .setSizeX(21)
                .setSizeY(21)
                .setOrderDistributionStrategy(new ECNPStrategy(10000, 10))
                .setIdlingZoneDistribution(Warehouse.IdlingZoneDistribution.RANDOM);

        ExperimentCase experimentCase2 = new ExperimentCase();
        experimentCase2
                .setAgentCount(agentCount)
                .setIdlingZoneCount(idlingZoneCount)
                .setSizeX(41)
                .setSizeY(41)
                .setOrderDistributionStrategy(new ECNPStrategy(10000, 10))
                .setIdlingZoneDistribution(Warehouse.IdlingZoneDistribution.RANDOM);

        experiment.add(experimentCase1);
        experiment.add(experimentCase2);
        experiment.run();
    }

    @Override
    public void stop() {
        experiment.stop();
    }

    public static void main(String[] args) {
        launch();
    }
}