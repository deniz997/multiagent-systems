package mat.agent.reactive.model;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class Order{
	 
	private final List<Coordinate> coordinates;
	
	public Order(Coordinate... coordinates) {
		this.coordinates = new LinkedList<>(Arrays.asList(coordinates));
	}

	public int count() {
		return coordinates.size();
	}

	public Coordinate pop() {
		Coordinate coordinate = coordinates.get(0);
		coordinates.remove(0);
		return coordinate;
	}

	public Optional<Coordinate> getNextCoordinate() {
		if (coordinates.size() > 0) {
			return Optional.of(coordinates.get(0));
		}
		return Optional.empty();
	}
}