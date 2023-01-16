package mat.agent.reactive;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import mat.agent.reactive.model.Warehouse;
import mat.agent.reactive.strategy.BasicStrategy;
import mat.agent.reactive.strategy.CNPStrategy;
import mat.agent.reactive.strategy.ECNPStrategy;
import mat.agent.reactive.strategy.OrderDistributionStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;

public class App extends javafx.application.Application {
    private static final Logger logger = LogManager.getLogger(App.class);
    private Experiment experiment;
    private final int[] sizes = { 21, 41 };
    private final float[] scaling = { 1, 3.81f  };
    private final int[] agentCounts = { 20, 50, 100, 200 };
    private final float[] dropZonePercentages = { 0.25f };

    @Override
    public void start(Stage stage) throws IOException {
        logger.info("Starting application..");
        Parent root = FXMLLoader.load(Objects.requireNonNull(App.class.getResource("view.fxml")));

        experiment = new Experiment(stage, root);

        /* List<ExperimentCase> experimentCases = createExperimentCases();

        for (ExperimentCase experimentCase : experimentCases) {
            experiment.add(experimentCase);
        } */

        ExperimentCase experimentCase = new ExperimentCase();
        experimentCase
                .setAgentCount(10)
                .setIdlingZoneCount(10)
                .setSizeX(21)
                .setSizeY(21)
                .setOrderDistributionStrategy(new ECNPStrategy())
                .setIdlingZoneDistribution(Warehouse.IdlingZoneDistribution.NEAREST_BORDER);

        experiment.add(experimentCase);

        experiment.runGui();
        // experiment.runWithTimer(100);
    }

    public List<ExperimentCase> createExperimentCases() {
        List<ExperimentCase> cases = new ArrayList<>();

        for (int i = 0; i < sizes.length; i++) {
            int size = sizes[i];
            float scale = scaling[i];

            for (int agentCount : agentCounts) {
                for (float dropZonePercentage : dropZonePercentages) {
                    int idlingZoneCount = (int) (agentCount * dropZonePercentage);
                    // Iterate over all values of Warehouse.IdlingZoneDistribution
                    for (Warehouse.IdlingZoneDistribution idlingZoneDistribution : Arrays.asList(Warehouse.IdlingZoneDistribution.RANDOM, Warehouse.IdlingZoneDistribution.NEAREST_BORDER)) {
                        // Iterate over all values of OrderDistributionStrategy
                        for (OrderDistributionStrategy orderDistributionStrategy : Arrays.asList(new BasicStrategy(), new CNPStrategy(), new ECNPStrategy())) {
                            ExperimentCase experimentCase = new ExperimentCase();
                            experimentCase
                                    .setAgentCount((int) (agentCount * scale))
                                    .setIdlingZoneCount((int) (idlingZoneCount * scale))
                                    .setSizeX(size)
                                    .setSizeY(size)
                                    .setOrderDistributionStrategy(orderDistributionStrategy)
                                    .setIdlingZoneDistribution(idlingZoneDistribution);
                            cases.add(experimentCase);
                        }
                    }
                }
            }
        }

        return cases;

    }

    @Override
    public void stop() {
        experiment.stop();
    }

    public static void main(String[] args) {
        launch();
    }
}