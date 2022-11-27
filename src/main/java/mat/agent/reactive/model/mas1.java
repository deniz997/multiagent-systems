package mat.agent.reactive.model;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;


public class mas1{
	private static final Logger logger = LogManager.getLogger(mas1.class);
	LinkedList<Agent> agents = new LinkedList<Agent>();	
	LinkedList<Order> orders = new LinkedList<Order>();
	int numberOfTimesteps;
	public Grid grid;
	
	public mas1() {
		this.grid= new Grid(5,5,Grid.IdlingZones.RANDOM, 5);
		this.numberOfTimesteps = 50;
		grid.init();
//		orders.add(new Order());
//		for (int i = 0; i < 1; i++) {
//			agentsFree.add(new Agent(8,8));
//		}
		
	}
	

	
	public static void main(String[] args) {
		mas1 mas = new mas1();

		mas.orders.add(new Order());
		mas.orders.add(new Order());

		mas.agents.add(new Agent(1, 1,1));
		mas.agents.add(new Agent(2, 2,2));
		mas.agents.add(new Agent(3, 3,3));

		mas.logGeneralInfo();
		for (int i = 0; i < mas.numberOfTimesteps; i++) {
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

		for(Agent agent : mas.agents) {
			agent.logExtraInfo();
		}
	}

	public void logGeneralInfo() {
		logger.info("Experiment - Number of agents:" + agents.size() + "," +
				"Number of orders:" + orders.size() + "," +
				"Number of timesteps:" + numberOfTimesteps);
	}
}
