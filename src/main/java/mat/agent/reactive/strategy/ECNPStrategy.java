package mat.agent.reactive.strategy;

import mat.agent.reactive.OrderBucket;
import mat.agent.reactive.model.*;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/*
 * This strategy extends the cnp strategy by trying to redistribute orders whenever possible to maximize efficiency.
 */
public class ECNPStrategy implements OrderDistributionStrategy {
    private final CNPStrategy cnpStrategy;
    private Warehouse warehouse;

    public ECNPStrategy() {

        cnpStrategy = new CNPStrategy();
    }


    @Override
    public void distribute(Warehouse warehouse) {
        if (Objects.isNull(this.warehouse)) {
            this.warehouse = warehouse;
        }

        cnpStrategy.distribute(warehouse);
    }

    @Override
    public void onReport(Warehouse.ReportType reportType, Agent agent) {
        cnpStrategy.onReport(reportType, agent);

        // At least 50% of agents should be available
        long freeAgentsCount = warehouse.getAgents().stream().filter(Agent::canReceiveOrder).count();
        if (freeAgentsCount <= (long) warehouse.getAgents().size() * 0.5) {
            return;
        }

        if (reportType == Warehouse.ReportType.GOOD_PICKED_UP) {
            Order order = agent.getOrder();
            // Split the order in half
            int halfSize = order.count() / 2;
            int rest = order.count() % 2;

            if (halfSize < 1) {
                return;
            }

            Order firstHalfOrder = new Order();
            Order secondHalfOrder = new Order();
            for (Good good : order.getNextGoods().subList(0, halfSize + rest)) {
                firstHalfOrder.add(good);
            }
            agent.setOrder(firstHalfOrder);
            for (Good good : order.getNextGoods().subList(halfSize + rest + 1, order.count())) {
                secondHalfOrder.add(good);
            }

            cnpStrategy.bidForOrder(agent.getWarehouse(), secondHalfOrder).ifPresent(nextAgent -> {
                if (!nextAgent.getId().equals(agent.getId())) {
                    // System.out.println("Reassigning order " + order.getId() + " from agent " + agent.getId() + " to agent " + nextAgent.getId());
                }
                nextAgent.setOrder(secondHalfOrder);
            });
        }
    }

    @Override
    public int getCompletedOrders() {
        return cnpStrategy.getCompletedOrders();
    }
}
