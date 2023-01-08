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
        int agentCount =761;
        int size = 41;
        int idlingZoneCount25 = (int) (agentCount*0.25);
        int idlingZoneCount5 = (int) (agentCount*0.5);
        experiment = new Experiment(stage, root);
        ExperimentCase experimentCase1 = new ExperimentCase();
        experimentCase1
                .setAgentCount(agentCount)
                .setIdlingZoneCount(idlingZoneCount25)
                .setSizeX(size)
                .setSizeY(size)
                .setOrderDistributionStrategy(new CNPStrategy(10000,10))
                .setIdlingZoneDistribution(Warehouse.IdlingZoneDistribution.NEAREST_BORDER);

        ExperimentCase experimentCase2 = new ExperimentCase();
        experimentCase2
                .setAgentCount(agentCount)
                .setIdlingZoneCount(idlingZoneCount25)
                .setSizeX(size)
                .setSizeY(size)
                .setOrderDistributionStrategy(new CNPStrategy(10000,10))
                .setIdlingZoneDistribution(Warehouse.IdlingZoneDistribution.RANDOM);

        ExperimentCase experimentCase3 = new ExperimentCase();
        experimentCase3
                .setAgentCount(agentCount)
                .setIdlingZoneCount(idlingZoneCount5)
                .setSizeX(size)
                .setSizeY(size)
                .setOrderDistributionStrategy(new CNPStrategy(10000,10))
                .setIdlingZoneDistribution(Warehouse.IdlingZoneDistribution.NEAREST_BORDER);

        ExperimentCase experimentCase4 = new ExperimentCase();
        experimentCase4
                .setAgentCount(agentCount)
                .setIdlingZoneCount(idlingZoneCount5)
                .setSizeX(size)
                .setSizeY(size)
                .setOrderDistributionStrategy(new CNPStrategy(10000,10))
                .setIdlingZoneDistribution(Warehouse.IdlingZoneDistribution.RANDOM);


        experiment.add(experimentCase1);
        experiment.add(experimentCase2);
        experiment.add(experimentCase3);
        experiment.add(experimentCase4);
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