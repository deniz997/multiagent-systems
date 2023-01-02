package mat.agent.reactive;

import mat.agent.reactive.model.Warehouse;

public interface OrderDistributionStrategy {
    void distribute(Warehouse warehouse);
}
