package mat.agent.reactive.strategy;

import mat.agent.reactive.OrderBucket;
import mat.agent.reactive.model.*;

import java.util.LinkedList;

public class BasicStrategy implements OrderDistributionStrategy {
    private static final int ORDER_SIZE = 4;
    private int completedOrders = 0;

    private Order generateOrder(Warehouse warehouse) {
        Order order = new Order();

        for (int i = 0; i < ORDER_SIZE; i++) {
            Good good = new Good();
            good.setCoordinate(warehouse.findRandomFreeCell());
            order.add(good);
        }

        return order;
    }

    @Override
    public void distribute(Warehouse warehouse) {
        if (warehouse.getAgents().stream().noneMatch(Agent::canReceiveOrder)) {
            return;
        }

        Order order = generateOrder(warehouse);
        warehouse.getAgents().stream()
                .filter(Agent::canReceiveOrder)
                .findFirst()
                .ifPresent(agent -> agent.setOrder(order));
    }


    @Override
    public void onReport(Warehouse.ReportType reportType, Agent agent) {
        if (reportType == Warehouse.ReportType.GOOD_DROPPED && agent.getOrder().isCompleted()) {
            completedOrders++;
        }
    }

    @Override
    public int getCompletedOrders() {
        return completedOrders;
    }
}
