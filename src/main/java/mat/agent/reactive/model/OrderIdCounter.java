package mat.agent.reactive.model;

public class OrderIdCounter {
    private static int value = 0;

    public static int getValue() {
        return value;
    }

    public static void setValue(int value) {
        OrderIdCounter.value = value;
    }

    public static int increment() {
        OrderIdCounter.value++;
        return getValue();
    }

    public static int decrement() {
        OrderIdCounter.value--;
        return getValue();
    }
}

