package mat.agent.reactive.model;
import java.util.LinkedList;


public class mas1{
	
	LinkedList<Agent> agents = new LinkedList<Agent>();	
	LinkedList<Order> orders = new LinkedList<Order>();
	public Grid grid;
	
	public mas1() {
		this.grid= new Grid(5,5,Grid.IdlingZones.RANDOM, 5);
		grid.init();
//		orders.add(new Order());
//		for (int i = 0; i < 1; i++) {
//			agentsFree.add(new Agent(8,8));
//		}
		
	}
	

	
	public static void main(String[] args) {
		mas1 mas = new mas1();
		mas.grid.matrixInitialStatus=mas.grid.matrix;
		
		mas.orders.add(new Order());
		mas.orders.add(new Order());

		mas.agents.add(new Agent(1, 1,1));
//		mas.agents.add(new Agent(2, 2,2));


		
		
		for (int i = 0; i < 50; i++) {
			//give free agents orders
			for (Agent agent : mas.agents) {
				if(agent.status==Agent.Status.FREE || agent.status==Agent.Status.TO_IDLING_ZONE) {
					if(!mas.orders.isEmpty()) {
						System.out.println(agent.ID +  ": Agent gets new order");
						agent.status=Agent.Status.LOADING;
						agent.newOrder(mas.orders.remove());
					}	
				}
			}
			for (Agent agent : mas.agents) {
				mas.grid = agent.move(mas.grid);
				mas.grid.printMatrix();
				System.out.println();
			}
		}
	}
}
