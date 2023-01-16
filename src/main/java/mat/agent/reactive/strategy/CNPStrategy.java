package mat.agent.reactive.strategy;

import mat.agent.reactive.OrderBucket;
import mat.agent.reactive.model.Agent;
import mat.agent.reactive.model.Good;
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
    private int completedOrders = 0;
    private final static int DEADLINE_STEPS = 3;
    private int deadline = 0;

    public CNPStrategy() {
        bucket = new OrderBucket(10000, 10, TimeUnit.MILLISECONDS);
    }

    public Optional<Agent> bidForOrder(Warehouse warehouse, Order order) {
        return warehouse.getAgents().stream()
                .filter(Agent::canReceiveOrder)
                .min(Comparator.comparingInt(a -> a.getBidForOrder(order).orElse(Integer.MAX_VALUE)));
    }

    @Override
    public void distribute(Warehouse warehouse) {
        long freeAgentsCount = warehouse.getAgents().stream().filter(Agent::canReceiveOrder).count();
        if (freeAgentsCount == 0) {
            return;
        }
        /* deadline++;

        if (deadline < DEADLINE_STEPS) {
            return;
        } else {
            deadline = 0;
        } */

        Order order = bucket.generateOrder(warehouse);
        bidForOrder(warehouse, order).ifPresent(agent -> agent.setOrder(order));
    }

    @Override
    public int getCompletedOrders() {
        return completedOrders;
    }

    @Override
    public void onReport(Warehouse.ReportType reportType, Agent agent) {
        if (reportType == Warehouse.ReportType.GOOD_DROPPED) {
            // Check if the underlying order of a dropped good is completed
            Optional<Good> droppedGood = agent.getOrder().getLastDroppedGood();
            droppedGood.ifPresent(good -> {
                if (good.getOrder().isCompleted()) {
                    completedOrders++;
                }
            });
        }
    }
}
