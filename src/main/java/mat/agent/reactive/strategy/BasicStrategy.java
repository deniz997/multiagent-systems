package mat.agent.reactive.strategy;

import mat.agent.reactive.model.Agent;
import mat.agent.reactive.model.Coordinate;
import mat.agent.reactive.model.Order;
import mat.agent.reactive.model.Warehouse;

import java.util.LinkedList;

public class BasicStrategy implements OrderDistributionStrategy {
    private static final int ORDER_SIZE = 3;

    private Order generateOrder(Warehouse warehouse) {
        Order order = new Order();

        for (int i = 0; i < ORDER_SIZE; i++) {
            order.add(warehouse.findRandomFreeCell());
        }

        return order;
    }

    @Override
    public void distribute(Warehouse warehouse) {
        for (Agent agent : warehouse.getAgents()) {
            if (agent.canReceiveOrder()) {
                agent.setOrder(generateOrder(warehouse));
            }
        }
    }

    @Override
    public void onReport(Warehouse.ReportType reportType, Agent agent) {
    }
}
