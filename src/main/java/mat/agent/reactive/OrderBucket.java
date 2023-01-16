package mat.agent.reactive;

import mat.agent.reactive.model.Good;
import mat.agent.reactive.model.Order;
import mat.agent.reactive.model.Warehouse;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

// Token bucket for orders
public class OrderBucket {
    private final long maxTokens;
    private final long refillInterval;
    private final TimeUnit refillUnit;
    private long availableTokens;
    private long lastRefillTime;

    public OrderBucket(long maxTokens, long refillInterval, TimeUnit refillUnit) {
        this.maxTokens = maxTokens;
        this.refillInterval = refillInterval;
        this.refillUnit = refillUnit;
        this.availableTokens = maxTokens;
        this.lastRefillTime = System.nanoTime();
    }

    private static final int ORDER_SIZE = 3;;

    public Order generateOrder(Warehouse warehouse) {
        Order order = new Order();

        for (int i = 0; i < ORDER_SIZE; i++) {
            Good good = new Good();
            good.setCoordinate(warehouse.findRandomFreeCell());
            order.add(good);
        }

        return order;
    }

    public Optional<Order> tryAcquire(Warehouse warehouse) {
        refill();

        if (availableTokens > 0) {
            availableTokens--;
            return Optional.of(generateOrder(warehouse));
        }
        return Optional.empty();
    }

    private void refill() {
        long currentTime = System.nanoTime();
        long elapsedTime = currentTime - lastRefillTime;
        long refillAmount = elapsedTime / refillUnit.toNanos(refillInterval);
        if (refillAmount > 0) {
            availableTokens = Math.min(maxTokens, availableTokens + refillAmount);
            lastRefillTime = currentTime;
        }
    }
}

