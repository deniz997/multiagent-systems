package mat.agent.reactive.strategy;

import mat.agent.reactive.OrderBucket;
import mat.agent.reactive.OrderDistributionStrategy;
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

    @Override
    public void distribute(Warehouse warehouse) {
        // At least one agent should be able to receive the order
        if (warehouse.getAgents().stream().noneMatch(Agent::canReceiveOrder)) {
            return;
        }

        Optional<Order> order = bucket.tryAcquire(warehouse);
        order.ifPresent(value -> warehouse.getAgents().stream()
                .filter(Agent::canReceiveOrder)
                .max(Comparator.comparingInt(a -> a.getBidForOrder(value)))
                .ifPresent(agent -> agent.setOrder(value)));
    }
}
