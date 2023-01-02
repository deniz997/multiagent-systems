package mat.agent.reactive.strategy;

import mat.agent.reactive.model.Agent;
import mat.agent.reactive.model.Warehouse;

public interface OrderDistributionStrategy {
    void distribute(Warehouse warehouse);

    void onReport(Warehouse.ReportType reportType, Agent agent);
}
