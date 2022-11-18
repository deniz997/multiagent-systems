package mat.agent.reactive.model;
import java.util.List;

public class Order{
	 
	public List<Coordinate> coordinates;
	public Order() {
		coordinates.add(new Coordinate(7,7));
		coordinates.add(new Coordinate(5,5));
		coordinates.add(new Coordinate(3,3));
			
	}
	
}