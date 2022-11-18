package mat.agent.reactive.model;

public class Agent {
	
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
		
	
	public int dest_x;
	public int dest_y;
	public int ROBOT= 1;
	
	public boolean busy;
	public boolean loading;
	
	int[][] matrix;
	

	public Agent (Order order) {
		this.order = order;
		
	}
	
	public Order order;
	
	public Grid move(Grid grid) {
		
		if(position_x<dest_x) {
			if (!collisionChecker(Check.RIGHT)){
			grid.matrix[position_x][position_y]= grid.matrixInitialStatus[position_x][position_y];
			position_x = position_x+1;
			grid.matrix[position_x][position_y]=ROBOT;
			return grid;
			}
		}else if(position_y<dest_y) {
			if (!collisionChecker(Check.DOWN)){
			grid.matrix[position_x][position_y]= grid.matrixInitialStatus[position_x][position_y];
			position_y = position_y+1;
			grid.matrix[position_x][position_y]=ROBOT;
			return grid;
			}
		}else if(position_x>dest_x) {
			if (!collisionChecker(Check.LEFT)){
			grid.matrix[position_x][position_y]= grid.matrixInitialStatus[position_x][position_y];
			position_x = position_x-1;
			grid.matrix[position_x][position_y]=ROBOT;
			return grid;
			}
		}else if(position_y>dest_y) {
			if (!collisionChecker(Check.UP)){
			grid.matrix[position_x][position_y]= grid.matrixInitialStatus[position_x][position_y];
			position_y = position_y-1;
			grid.matrix[position_x][position_y]=ROBOT;
			return grid;
			}
		}
		return grid;		
	}
	
	
	public boolean collisionChecker (Check toCheck) {
		switch (toCheck) {
		case RIGHT:
			if (this.matrix[this.position_x+1][this.position_y]!=ROBOT) {
				return true;
			}else{ 
				return false;
			}
		case DOWN:
			if (this.matrix[this.position_x][this.position_y+1]!=ROBOT) {
				return true;
			}else{ 
				return false;
			}
		case LEFT:
			if (this.matrix[this.position_x-1][this.position_y]!=ROBOT) {
				return true;
			}else{ 
				return false;
			}
		case UP:
		if (this.matrix[this.position_x][this.position_y-1]!=ROBOT) {
			return true;
		}else{ 
			return false;
			}
		}
		return false;		
	}
	
	

}
