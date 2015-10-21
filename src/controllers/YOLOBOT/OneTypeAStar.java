package controllers.YOLOBOT;

import java.util.LinkedList;
import java.util.List;

import ontology.Types;
import core.game.Observation;
import core.game.StateObservation;

public class OneTypeAStar {

	private List<Observation>[][] grid;
	public int[][] distance;
	public int itype_ground;
	/***
	 * Direction from where it got reached.<br/>
	 * 0 = Left<br/>
	 * 1 = Right<br/>
	 * 2 = Top<br/>
	 * 3 = Bottom
	 */
	public byte[][] from;
	public final static byte LEFT = 0;
	public final static byte RIGHT = 1;
	public final static byte TOP = 2;
	public final static byte BOTTOM = 3;
	private static boolean[] markedItypes;
	public int moveableFieldCount;
	private int agentX, agentY;

	public OneTypeAStar(YoloState so) {
		Helper.max_itype = 30;
		List<Observation>[][] grid = so.getObservationGrid();
		this.grid = grid;
		Observation agent = Helper.getAgent(grid, so);
		
		if(agent != null){
			agentX = (int) (agent.position.x / so.getBlockSize());
			agentY = (int) (agent.position.y / so.getBlockSize());
		}else {
			agentX = 0;
			agentY = 0;
		}
		itype_ground = -1;
		for (Observation ob : grid[agentX][agentY]) {
			if(ob.category == Types.TYPE_STATIC){
				//Statisches Objekt auf dem der Spieler steht!
				itype_ground = ob.itype;
			}
		}
	}
	public int calculateReachable() {
		calculate(new int[0]);
		return moveableFieldCount;
	}
	public List<Observation> calculate(int[] interestingItypes) {
		LinkedList<Observation> retVal = new LinkedList<Observation>();
		moveableFieldCount = 0;
		markedItypes = new boolean[Helper.max_itype];
		for (int i = 0; i < interestingItypes.length; i++) {
			markedItypes[interestingItypes[i]] = true;
		}

		distance = new int[grid.length][grid[0].length];
		distance[agentX][agentY] = 1;
		from = new byte[grid.length][grid[0].length];
		LinkedList<Integer> xQueue = new LinkedList<Integer>();
		LinkedList<Integer> yQueue = new LinkedList<Integer>();
		xQueue.add(agentX);
		yQueue.add(agentY);

		while (xQueue.size() > 0) {
			int x = xQueue.removeFirst();
			int y = yQueue.removeFirst();
			int newDistance = distance[x][y] + 1;
			for (int xNew = x - 1; xNew <= x + 1; xNew++) {
				if (xNew >= 0 && xNew < grid.length)
					for (int yNew = y - 1; yNew <= y + 1; yNew++){
						if (yNew >= 0 && yNew < grid[xNew].length && (xNew == x || yNew == y)){
							//Gueltiges Feld
							//System.out.println("Gueltig:"+xNew+ "-"+yNew);
							if(distance[xNew][yNew] == 0){
								//System.out.println("Neu gefunden:"+xNew+ "-"+yNew);
								//Neu gefunden!
								distance[xNew][yNew] = newDistance;
								from[xNew][yNew] = (byte)((xNew < x?0:(xNew > x?1:(yNew < y?2:3))));
								
								boolean moveAvailable = grid[xNew][yNew].size() == 0;	//Bei keiner Observation ist es begehbar!
								
								for (Observation obs : grid[xNew][yNew]) {
									moveAvailable |= obs.itype == itype_ground;
									if(markedItypes[obs.itype])
										retVal.add(obs);
								}
								
								if(moveAvailable){
									moveableFieldCount++;
									xQueue.add(xNew);
									yQueue.add(yNew);
								}
									
							}
						}
					}
			}
		}

		return retVal;
	}
	
	public int getMoveableFieldCount() {
		return moveableFieldCount;
	}

}
