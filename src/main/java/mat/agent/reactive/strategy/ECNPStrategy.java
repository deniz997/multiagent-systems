package mat.agent.reactive.strategy;

import mat.agent.reactive.model.Agent;
import mat.agent.reactive.model.Coordinate;
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
        if (reportType == Warehouse.ReportType.GOOD_PICKED_UP) {
            Order order = agent.getOrder();
            // Split up the order
            Order firstProductOnly = new Order(order.pop());
            Order restOrder = new Order();
            for (Coordinate coordinate : order.getCoordinates()) {
                restOrder.add(coordinate);
            }

            agent.setOrder(firstProductOnly);

            if (restOrder.getCoordinates().size() == 0) {
                return;
            }

            cnpStrategy.bidForOrder(agent.getWarehouse(), restOrder).ifPresent(nextAgent -> {
                if (!nextAgent.getId().equals(agent.getId())) {
                    // TODO: Introduce order id
                    // logger.info("Reassigning order " + order + " from agent " + agent.getId() + " to agent " + nextAgent.getId());
                }
                nextAgent.setOrder(order);
            });
        }
    }
}
