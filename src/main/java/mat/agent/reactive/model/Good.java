package mat.agent.reactive.model;

public class Good {
    private Coordinate coordinate;
    private Order order;
    private boolean pickedUp = false;

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public void pickup() {
        pickedUp = true;
    }

    public boolean isPickedUp() {
        return pickedUp;
    }
}
