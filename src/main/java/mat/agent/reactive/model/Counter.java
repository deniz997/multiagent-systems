package mat.agent.reactive.model;

public class Counter {
    private static int value = 0;

    public static int getValue() {
        return value;
    }

    public static void setValue(int value) {
        Counter.value = value;
    }

    public static int increment() {
        Counter.value++;
        return getValue();
    }

    public static int decrement() {
        Counter.value--;
        return getValue();
    }
}
