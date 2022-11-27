package mat.agent.reactive.model;
import java.util.LinkedList;

public class Order{
	 
	LinkedList<Coordinate >coordinates = new LinkedList<Coordinate>();
	
	public Order() {
		coordinates.add(new Coordinate(4,4));
		coordinates.add(new Coordinate(3,3));
		
	}
	
}