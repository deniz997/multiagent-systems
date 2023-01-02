package mat.agent.reactive.strategy;

import mat.agent.reactive.model.Agent;
import mat.agent.reactive.model.Order;
import mat.agent.reactive.model.Warehouse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ECNPStrategy implements OrderDistributionStrategy {
    private static final Logger logger = LogManager.getLogger(ECNPStrategy.class);
    private final CNPStrategy cnpStrategy;

    public ECNPStrategy(int maxOrders, int refillIntervalInMilliSecond) {
        this.cnpStrategy = new CNPStrategy(maxOrders, refillIntervalInMilliSecond);
    }

    @Override
    public void distribute(Warehouse warehouse) {
        cnpStrategy.distribute(warehouse);
    }

    @Override
    public void onReport(Warehouse.ReportType reportType, Agent agent) {
        // If an agent drops a good we make the order available for bidding again
        if (reportType == Warehouse.ReportType.GOOD_DROPPED) {
            Order order = agent.getOrder();
            agent.setOrder(null);
            cnpStrategy.bidForOrder(agent.getWarehouse(), order).ifPresent(nextAgent -> {
                if (!nextAgent.getId().equals(agent.getId())) {
                    // TODO: Introduce order id
                    logger.info("Reassigning order " + order + " from agent " + agent.getId() + " to agent " + nextAgent.getId());
                }
                nextAgent.setOrder(order);
            });
        }
    }
}
