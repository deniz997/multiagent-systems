package mat.agent.reactive.strategy;

import mat.agent.reactive.ConsoleColor;
import mat.agent.reactive.OrderBucket;
import mat.agent.reactive.model.Agent;
import mat.agent.reactive.model.Good;
import mat.agent.reactive.model.Order;
import mat.agent.reactive.model.Warehouse;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
        List<Optional<Integer>> allBids = warehouse.getAgents().stream()
                .filter(Agent::canReceiveOrder)
                .map(agent -> agent.getBidForOrder(order)).collect(Collectors.toList());

        // Print all bids
        System.out.print("All Bids: ");
        for (Optional<Integer> bid : allBids) {
            System.out.print((bid.isPresent() ? bid.get() : "N/A") + " ");
        }
        System.out.print("\n");

        Optional<Agent> agent = warehouse.getAgents().stream()
                .filter(Agent::canReceiveOrder)
                .min(Comparator.comparingInt(a -> a.getBidForOrder(order).orElse(Integer.MAX_VALUE)));

        agent.ifPresent(value -> {
            if (value.getBidForOrder(order).isEmpty()) {
                return;
            }

            if (Objects.equals(value.getId(), "1")) {
                System.out.println(
                        ConsoleColor.RED + "Agent " + value.getId() + " got order " + order.getId() + " with bid of " + value.getBidForOrder(order).get() +
                        ConsoleColor.RESET
                );
            } else {
                System.out.println("Agent " + value.getId() + " got order " + order.getId() + " with bid of " + value.getBidForOrder(order).get());
            }
        });

        return agent;
    }

    @Override
    public void distribute(Warehouse warehouse) {
        if (warehouse.getAgents().stream().noneMatch(Agent::canReceiveOrder)) {
            return;
        }
        /* deadline++;

        if (deadline < DEADLINE_STEPS) {
            return;
        } else {
            deadline = 0;
        } */

        Order order = bucket.generateOrder(warehouse);
        bidForOrder(warehouse, order).ifPresent(agent -> {
            agent.setOrder(order);
        });
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
                if (Objects.equals(agent.getId(), "1")) {
                    System.out.println(ConsoleColor.RED + "Agent " + agent.getId() + " dropped good" +  ConsoleColor.RESET);
                } else {
                    System.out.println("Agent " + agent.getId() + " dropped good");
                }

                if (good.getOrder().isCompleted()) {
                    if (Objects.equals(agent.getId(), "1")) {
                        System.out.println(ConsoleColor.RED + "Agent " + agent.getId() + " completed order with id " + good.getOrder().getId() + ConsoleColor.RESET);
                    } else {
                        System.out.println("Agent " + agent.getId() + " completed order with id " + good.getOrder().getId());
                    }
                    completedOrders++;
                }
            });
        } else if (reportType == Warehouse.ReportType.GOOD_PICKED_UP){
            if (Objects.equals(agent.getId(), "1")) {
                System.out.println(ConsoleColor.RED + "Agent " + agent.getId() + " picked up good" + ConsoleColor.RESET);
            } else {
                System.out.println("Agent " + agent.getId() + " picked up good");
            }
        }
    }
}
