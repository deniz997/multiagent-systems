package mat.agent.reactive;

import mat.agent.reactive.model.Warehouse;

public class ExperimentCase {
    private int sizeX;
    private int SizeY;
    private int agentCount;
    private int idlingZoneCount;
    private Warehouse.IdlingZoneDistribution idlingZoneDistribution;
    private OrderDistributionStrategy orderDistributionStrategy;

    public int getSizeY() {
        return SizeY;
    }

    public Warehouse.IdlingZoneDistribution getIdlingZoneDistribution() {
        return idlingZoneDistribution;
    }

    public int getAgentCount() {
        return agentCount;
    }

    public int getSizeX() {
        return sizeX;
    }

    public int getIdlingZoneCount() {
        return idlingZoneCount;
    }

    public ExperimentCase setAgentCount(int agentCount) {
        this.agentCount = agentCount;

        return this;
    }

    public ExperimentCase setIdlingZoneCount(int idlingZoneCount) {
        this.idlingZoneCount = idlingZoneCount;

        return this;
    }

    public ExperimentCase setSizeX(int sizeX) {
        this.sizeX = sizeX;

        return this;
    }

    public ExperimentCase setSizeY(int sizeY) {
        SizeY = sizeY;

        return this;
    }

    public ExperimentCase setIdlingZoneDistribution(Warehouse.IdlingZoneDistribution idlingZoneDistribution) {
        this.idlingZoneDistribution = idlingZoneDistribution;

        return this;
    }

    public OrderDistributionStrategy getOrderDistributionStrategy() {
        return orderDistributionStrategy;
    }

    public ExperimentCase setOrderDistributionStrategy(OrderDistributionStrategy orderDistributionStrategy) {
        this.orderDistributionStrategy = orderDistributionStrategy;

        return this;
    }
}
