package mat.agent.reactive.strategy;

import mat.agent.reactive.OrderBucket;
import mat.agent.reactive.model.Agent;
import mat.agent.reactive.model.Order;
import mat.agent.reactive.model.Warehouse;

import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/*
 * This strategy is based on contract net protocol. It is used to distribute orders to agents.
 * The agents bid for the order and the one with the highest bid gets the order.
 * We also introduce a token bucket to limit the number of orders generated.
 */
public class CNPStrategy implements OrderDistributionStrategy {
    private final OrderBucket bucket;

    public CNPStrategy(int maxOrders, int refillIntervalInMilliSecond) {
        bucket = new OrderBucket(maxOrders, refillIntervalInMilliSecond, TimeUnit.MILLISECONDS);
    }

    public Optional<Agent> bidForOrder(Warehouse warehouse, Order order) {
        return warehouse.getAgents().stream()
                .filter(Agent::canReceiveOrder)
                .min(Comparator.comparingInt(a -> a.getBidForOrder(order).orElse(Integer.MAX_VALUE)));
    }

    @Override
    public void distribute(Warehouse warehouse) {
        // At least one agent should be able to receive the order
        if (warehouse.getAgents().stream().noneMatch(Agent::canReceiveOrder)) {
            return;
        }

        Order order = bucket.generateOrder(warehouse);
        bidForOrder(warehouse, order).ifPresent(agent -> agent.setOrder(order));
    }

    @Override
    public void onReport(Warehouse.ReportType reportType, Agent agent) {
    }
}
