package mat.agent.reactive.model;

import mat.agent.reactive.App;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Agent {
	private static final Logger logger = LogManager.getLogger(Agent.class);
	public int position_x;
	public int position_y;
	
	enum Check {
		RIGHT,
		DOWN,
		LEFT,
		UP
	}
	
	enum Dodge {
		OK,
		RIGHT,
		DOWN,
		LEFT,
		TOP,
		STAY
	}
		
	int ID;
	public int dest_x;
	public int dest_y;
	public int ROBOT= 1;
	

	enum Status {
		LOADING,
		FREE,
		TO_DROP_OFF,
		TO_IDLING_ZONE
	}
	
	int leftOrRight=-1;
	
	int[][] matrix;
	public Status status;
	private int numberOfCompletedOrders = 0;
	private int numberOfAvoidedCollisions = 0;
	

	public Agent (int startX, int startY, int id) {
		this.position_x= startX;
		this.position_y = startY;
		this.status = Status.FREE;
		this.ID = id;
		
	}
	
	public Order order;
	
	public Grid move(Grid grid) {
		//TODO: case without idling zones
		if (status == Status.FREE) {
			return grid;
		}else if (status == Status.TO_DROP_OFF) {
			return moveToClosestDropOffZone(grid);
		}else if (status == Status.TO_IDLING_ZONE) {
			return moveToClosestIdlingOffZone(grid);
		}else if (status == Status.LOADING) {
				actualMove(grid, dest_x, dest_y);
			}		
		return grid;
	}
	
	private Grid moveToClosestDropOffZone(Grid grid) {
		if(position_x%2==0) {
			return actualMove(grid,position_x,0);
		}else
			leftOrRight=leftOrRight*(-1);
			return actualMove(grid,position_x+leftOrRight, 0);
	}
	
	private Grid moveToClosestIdlingOffZone(Grid grid) {
		Coordinate closestIdlingZone = new Coordinate(1000, 1000);
		for(Coordinate idlingZone : grid.idlingZones) {
			if (grid.matrix[idlingZone.x][idlingZone.y]==grid.IDLING_ZONE) { //if idlingZoneIsFree
				int distanceToCurrent = Math.abs(closestIdlingZone.x-position_x)+Math.abs(closestIdlingZone.y-position_y);
				int distanceToNew = Math.abs(idlingZone.x-position_x)+Math.abs(idlingZone.y-position_y);
				if(distanceToNew<distanceToCurrent) {
					closestIdlingZone=idlingZone;
				}
				
			}
			
		}
		System.out.println(ID +": closest Idling Zone x:" +closestIdlingZone.x +" y:" +closestIdlingZone.y);
		return actualMove(grid, closestIdlingZone.x, closestIdlingZone.y);
	}
	
	private Grid actualMove(Grid grid, int destX, int destY) {
		if(position_x<destX) {
			if (collisionChecker(Check.RIGHT, grid.matrix)){
			grid.matrix[position_x][position_y]= grid.matrixInitialStatus[position_x][position_y];
			position_x = position_x+1;
			grid.matrix[position_x][position_y]=ID;
			System.out.println(ID+ ": moved right");

			checkForStatusUpdate(destX, destY);
			logAgentMove(ID, "UP", position_x, position_y, destX, destY, status);
				return grid;
			}
		}else if(position_x>destX) {
			if (collisionChecker(Check.LEFT, grid.matrix)){
				grid.matrix[position_x][position_y]= grid.matrixInitialStatus[position_x][position_y];
				position_x = position_x-1;
				grid.matrix[position_x][position_y]=ID;
				System.out.println(ID+ ": moved left");

				checkForStatusUpdate(destX, destY);
				logAgentMove(ID, "UP", position_x, position_y, destX, destY, status);
				return grid;
			}
		}else if(position_y<destY) {
			if (collisionChecker(Check.DOWN, grid.matrix)){
			grid.matrix[position_x][position_y]= grid.matrixInitialStatus[position_x][position_y];
			position_y = position_y+1;
			grid.matrix[position_x][position_y]=ID;
			System.out.println(ID+ ": moved down");

			checkForStatusUpdate(destX, destY);
			logAgentMove(ID, "UP", position_x, position_y, destX, destY, status);
			return grid;
			}
		}else if(position_y>destY) {
			if (collisionChecker(Check.UP, grid.matrix)){
			grid.matrix[position_x][position_y]= grid.matrixInitialStatus[position_x][position_y];
			position_y = position_y-1;
			grid.matrix[position_x][position_y]=ID;
			System.out.println(ID+ ": moved up");

			checkForStatusUpdate(destX, destY);
			logAgentMove(ID, "UP", position_x, position_y, destX, destY, status);
			return grid;
			}
		}
		logAgentMove(ID, "NONE", position_x, position_y, destX, destY, status);
		return grid;

	}
	
	private void checkForStatusUpdate(int destX, int destY) {
		if(position_x==destX && position_y==destY) {
			if(status==Status.LOADING) {
				Coordinate newDest = order.coordinates.poll();
				if (newDest==null) {
					status=Status.TO_DROP_OFF;
					System.out.println(ID +  ": changed status to"+ status);

				}else{
					System.out.println(ID +  ": going to next order position x:" + newDest.x + " y:" +newDest.y);;

					dest_x=newDest.x;
					dest_y=newDest.y;
				}
			}else if(status == Status.TO_DROP_OFF) {
				status=Status.TO_IDLING_ZONE;
				numberOfCompletedOrders++;
				System.out.println(ID +  ": changed status to"+ status);

			}else if(status == Status.TO_IDLING_ZONE) {
				status=Status.FREE;
				System.out.println(ID +  ": changed status to"+ status);

			}
		}
	}
	
	public void newOrder(Order order) {
		this.status = Status.LOADING;
		this.order= order;
		Coordinate newDest = order.coordinates.poll();
		this.dest_x=newDest.x;
		this.dest_y=newDest.y;
	}
	
	
	public boolean collisionChecker (Check toCheck, int[][] matrix) {
		switch (toCheck) {
		case RIGHT:
			if (matrix[this.position_x+1][this.position_y]!=1 && matrix[this.position_x+1][this.position_y]!=2 && matrix[this.position_x+1][this.position_y]!=3) {
				return true;
			}else{
				numberOfAvoidedCollisions++;
				return false;
			}
		case DOWN:
			if (matrix[this.position_x][this.position_y+1]!=1 && matrix[this.position_x][this.position_y+1]!=2 && matrix[this.position_x][this.position_y+1]!=3 ) {
				return true;
			}else{
				numberOfAvoidedCollisions++;
				return false;
			}
		case LEFT:
			if (matrix[this.position_x-1][this.position_y]!=1 && matrix[this.position_x-1][this.position_y]!=2 && matrix[this.position_x-1][this.position_y]!=3) {
				return true;
			}else{
				numberOfAvoidedCollisions++;
				return false;
			}
		case UP:
			if (matrix[this.position_x][this.position_y-1]!=1 && matrix[this.position_x][this.position_y-1]!=2 && matrix[this.position_x][this.position_y-1]!=3) {
				return true;
			}else{
				numberOfAvoidedCollisions++;
				return false;
				}
			}
		numberOfAvoidedCollisions++;
		return false;		
	}
	
	private void logAgentMove(int agentId, String movement, int posX, int posY, int destX, int destY, Status status) {
		logger.info("Agent:" + agentId + "," +
				"Moved:" + movement + "," +
				"Position X:" + posX + "," + "Position Y:" + posY + "," +
				"Destination X:" + destX + "," + "Destination Y:" + destY + "," +
				"Status:" + status.name());
	}

	public void logExtraInfo() {
		logger.info("Agent:" + ID + "," +
				"Number of completed orders:" + numberOfCompletedOrders + "," +
				"Number of avoided collisions:" + numberOfAvoidedCollisions);
	}

}
