package mat.agent.reactive.model;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;


public class Grid {
	private static final Logger logger = LogManager.getLogger(Grid.class);
	public int size_x;
	public int size_y;
	public int[][] matrix;
	public int[][] matrixInitialStatus;
	enum IdlingZones {
		NONE,
		RANDOM_BORDER,
		RANDOM,
		NEAREST_BORDER,
		DISTRIBUTED_BORDER
	}
	public IdlingZones pPosition;
	public int numberOfIdlingZones;
	public int DROP_ZONE = 9;
	public int IDLING_ZONE = 5;
	public int FREE = 0;
	public int ROBOT= 1;	
	public ArrayList<Coordinate> idlingZones;
	
	
	
	public Grid (int size_x, int size_y, IdlingZones pPosition , int numberOfIdlingZones) {
		this.size_x = size_x;
		this.size_y = size_y;
		this.matrix = new int[size_x][size_y]; 
		this.matrixInitialStatus = new int[size_x][size_y]; 
		this.pPosition = pPosition;
		this.numberOfIdlingZones = numberOfIdlingZones;
		logGridData(size_x, size_y, numberOfIdlingZones, pPosition.name());
		this.init();
		this.fillIdlingZonesList();
		this.printMatrix();
		this.copyToInitial();
	}
	
	private void fillIdlingZonesList() {
		idlingZones = new ArrayList<Coordinate>();
			for (int y = 0; y < size_y; y++) {
			    for (int x = 0; x < size_x; x++) {
			        if (matrix[x][y]==IDLING_ZONE) {
			        	this.idlingZones.add(new Coordinate(x, y));
			        }
			        
			    }		
			}
			for(Coordinate idlingZones : idlingZones) {
				System.out.println("x:" + idlingZones.x + " y: "+idlingZones.y);
				logZones("IDLING_ZONE", idlingZones.x, idlingZones.y);
			}
		}
	

	public void init() {
		int currentIdlingZones = 0;
//		drop of zones
		for (int i = 0; i < this.size_x/2+1; i++) {
			matrix[i*2][0]= DROP_ZONE;
			logZones("DROP_ZONE", i*2, 0);
		}
//			

//		fill in parking positions
		switch (pPosition) {
		case NONE:
			break;
		case DISTRIBUTED_BORDER:
			for (int x = 1; x<size_x/2+1; x++) {
				if(currentIdlingZones<numberOfIdlingZones) {
					matrix[x*2][0] = IDLING_ZONE;
					currentIdlingZones++;
				}
				if(currentIdlingZones<numberOfIdlingZones) {
					matrix[x*2][size_y-1] = IDLING_ZONE;
					currentIdlingZones++;
				}
			}
//			fill bottom if needed
			for (int y = 1; y<size_y/2+1; y++) {
				if(currentIdlingZones<numberOfIdlingZones) {
					matrix[size_x-1][y*2] = IDLING_ZONE;
					currentIdlingZones++;
				}
			}
			if(currentIdlingZones<numberOfIdlingZones) {
				System.err.println("more idling zones than " + currentIdlingZones+ " are not possible");
			}

			break;
		case NEAREST_BORDER:
//			fill top
			for (int x = 1; x<size_x/2+1; x++) {
				if(currentIdlingZones<numberOfIdlingZones) {
					matrix[0][x*2-1] = IDLING_ZONE;
					currentIdlingZones++;
				}
			}
				
//			fill sides if needed
				for (int x = 2; x<size_y; x++) {
					if(currentIdlingZones<numberOfIdlingZones) {
						matrix[x][0] = IDLING_ZONE;
						currentIdlingZones++;
					}
					if(currentIdlingZones<numberOfIdlingZones) {
						matrix[x][size_y-1] = IDLING_ZONE;
						currentIdlingZones++;
					}
				}
				
//				fill bottom if needed
				for (int y = 1; y<size_y; y++) {
					if(currentIdlingZones<numberOfIdlingZones) {
						matrix[size_x-1][y] = IDLING_ZONE;
						currentIdlingZones++;
					}
				}
				if(currentIdlingZones<numberOfIdlingZones) {
					System.err.println("more idling zones than " + currentIdlingZones+ " are not possible");
				}						
			break;
		case RANDOM:
			while(currentIdlingZones<numberOfIdlingZones) {
				int random_x = (int)Math.floor(Math.random()*(size_x)); //random number between 0 and size_x
				int random_y = (int)Math.floor(Math.random()*(size_y));
				if (matrix[random_x][random_y]!=DROP_ZONE && matrix[random_x][random_y]!=IDLING_ZONE) {
					matrix[random_x][random_y] = IDLING_ZONE;
					currentIdlingZones++;
				}
			}
			break;
		case RANDOM_BORDER:
//			first check if the border space is big enough
			int maxNumberOfIdlingZones=(((size_x-1)/2))+(2*(size_y-2))+size_x-2;
			if (numberOfIdlingZones>maxNumberOfIdlingZones) {
				System.err.println("more idling zones than " + maxNumberOfIdlingZones + " are not possible");
			}
			while(currentIdlingZones<numberOfIdlingZones) {				
				//first decide on which side the idling zone should go
				int random_side = (int)Math.floor(Math.random()*(4));
				switch(random_side) {
//				top:
				case 1:
					int y = (int)Math.floor(Math.random()*size_x); //random number which cant be a corner
					if (matrix[0][y]!=DROP_ZONE && matrix[0][y]!=IDLING_ZONE && y<size_y-1){
						matrix[0][y] = IDLING_ZONE;
						currentIdlingZones++;
					}
//				right
				case 2: 
					int x = (int)Math.floor(Math.random()*size_y);
					if (matrix[x][size_y-1]!=DROP_ZONE && matrix[x][size_y-1]!=IDLING_ZONE && x<size_x-1){
						matrix[x][size_y-1] = IDLING_ZONE;
						currentIdlingZones++;
					}
//				bottom	
				case 3: 
					int y_b = 1 + (int)(Math.random() * ((size_y - 3) + 1)); //to make sure its not filling the corners
					if (matrix[size_x-1][y_b]!=DROP_ZONE && matrix[size_x-1][y_b]!=IDLING_ZONE){
						matrix[size_x-1][y_b] = IDLING_ZONE;
						currentIdlingZones++;
					}
//				left
					
				case 4: 
					int x_l = (int)Math.floor(Math.random()*size_y);
					if (matrix[x_l][0]!=DROP_ZONE && matrix[x_l][0]!=IDLING_ZONE && x_l<size_x-1){
						matrix[x_l][0] = IDLING_ZONE;
						currentIdlingZones++;
					}
					
				
				
			}
		}
			break;
			
		}
	}
	
		
		
	public void printMatrix() {
		
		for (int y = 0; y < size_y; y++) {
		    for (int x = 0; x < size_x; x++) {
		        System.out.print(matrix[x][y] + " ");
		    }
		    System.out.println();
		}
	}

	public void copyToInitial() {
		for (int y = 0; y < size_y; y++) {
			for (int x = 0; x < size_x; x++) {
				int field;
				field = matrix[x][y];
				matrixInitialStatus[x][y] = field;
			}
		}
	}

	private void logGridData(int sizeX, int sizeY, int numberOfIdlingZones, String distribution) {
		logger.info("Grid - Size x:" + sizeX + "," + "Size y:" + sizeY + "," +
		"Number of idling zones:" + numberOfIdlingZones + "," +
				"Idling zone distribution:" + distribution);
	}

	private void logZones(String zoneType, int posX, int posY) {
		logger.info("Zone type:" + zoneType + "," + "Position x:" + posX + "," + "Position y:" + posY);
	}
		
	

}
