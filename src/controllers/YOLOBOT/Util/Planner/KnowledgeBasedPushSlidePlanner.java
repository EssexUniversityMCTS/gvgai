package controllers.YOLOBOT.Util.Planner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.PriorityQueue;

import controllers.YOLOBOT.Agent;
import controllers.YOLOBOT.YoloState;
import controllers.YOLOBOT.Util.Wissensdatenbank.PlayerEvent;
import controllers.YOLOBOT.Util.Wissensdatenbank.YoloEvent;
import controllers.YOLOBOT.Util.Wissensdatenbank.YoloKnowledge;
import ontology.Types;
import core.game.Observation;

public class KnowledgeBasedPushSlidePlanner {

	private int directionChangeMalus;
	private int[] malusByIgnoringItype;
	public final static byte LEFT = 0;
	public final static byte RIGHT = 1;
	public final static byte TOP = 2;
	public final static byte BOTTOM = 3;
	private static final boolean DEBUG = false;
	private HashSet<Integer> seen;
	private KnowledgeBasedAStar aStar;
	private YoloState state;
	private ArrayList<Observation>[][] grid;
	
	public KnowledgeBasedPushSlidePlanner(YoloState state, int directionChangeMalus){
		this.state = state;
		this.grid = state.getObservationGrid();
		this.directionChangeMalus = directionChangeMalus;
		this.malusByIgnoringItype = new int[YoloKnowledge.ITYPE_MAX_COUNT];
		aStar = new KnowledgeBasedAStar(state);
	}
	
	public void setDirectionChangeMalus(int directionChangeMalus) {
		this.directionChangeMalus = directionChangeMalus;
	}
	public void setIgnoreMalus(int itype, int malus){
		malusByIgnoringItype[itype] = malus;
	}
	
	public Zustand compute(int object_itype, int toX, int toY, boolean slides, int[] collisionItypes, int agentX, int agentY, int choosePossibilityNr){
		if(!Agent.UPLOAD_VERSION)
			System.out.println("Suche: " + object_itype);
		boolean[] slideStop = new boolean[YoloKnowledge.ITYPE_MAX_COUNT];
		for (int i = 0; i < collisionItypes.length; i++) {
			slideStop[collisionItypes[i]] = true;
		}
		seen = new HashSet<Integer>();
		PriorityQueue<Zustand> zustaende = new PriorityQueue<Zustand>();
		zustaende.add(new Zustand(toX, toY, toX, toY, (byte) 127, 0, null));
		int stepCount = 0;
		int possibilitiesFound = 0;
		Zustand neuerZustand = null;
		while(!zustaende.isEmpty()){
			Zustand z = (Zustand) zustaende.poll();
			for (byte direction = 0; direction < 4; direction++) {
				int malus = 0;
				int xNew = getXinDirection(z.lastX, direction);
				int yNew = getYinDirection(z.lastY, direction);
				int xPlayerNew = getXinDirection(xNew, direction);
				int yPlayerNew = getYinDirection(yNew, direction);
				
				if(posIsOk(xPlayerNew, yPlayerNew) && canMoveHere(xNew, yNew, slideStop)){
					//Objekt kann sich hier befinden
					if(playerCanPush(xPlayerNew, yPlayerNew, xNew, yNew, state.getInventoryArray())){
						//Spieler kann auf beiden noetigen steinen stehen.

						
						if(!playerCanMove(xNew, yNew,z.lastPlayerX, z.lastPlayerY, z.lastX, z.lastY, object_itype, state.getInventoryArray()))
							continue;
						else{
							//Save aStarMove for later use
							++stepCount;
						}

						if(z.lastDirection != direction){
							//Richtung wurde geaendert! Malus!
							malus += directionChangeMalus;
							
							//Steinposition wurde noch nicht bertreten:
							malus += getMalus(xNew, yNew);
						}
						//Spielerposition wurde noch nie betreten:
						malus += getMalus(xPlayerNew, yPlayerNew);
						
						
						neuerZustand = new Zustand(xNew, yNew, xPlayerNew, yPlayerNew, direction, z.malus + malus, z);
						
						//HashSet
						int key = neuerZustand.key();
						if(seen.contains(key))
							continue;
						seen.add(key);
						
						//Check end
						if(istZiel(xNew, yNew, object_itype) && playerCanMove(agentX, agentY, xPlayerNew, yPlayerNew, xNew, yNew, object_itype, state.getInventoryArray())){

							if(!Agent.UPLOAD_VERSION)
								System.out.println("Push Slide Planner found solution after " + stepCount + " steps!");
							possibilitiesFound++;
							if(possibilitiesFound > choosePossibilityNr)
								return neuerZustand;
						}
						//Add in queue
						zustaende.add(neuerZustand);
					}
				}
			}
			
		}

		if(!Agent.UPLOAD_VERSION)
			System.out.println("Push Slide Planner found solution canceled after " + stepCount + " steps!");
		return null;
	}

	private boolean playerCanMove(int xStart, int yStart,
			int xZiel, int yZiel, int xVerbot, int yVerbot, int itypeVerbot, byte[] inventory) {
		if(xZiel == xVerbot && yZiel == yVerbot)
			return true;	//Initale Queue eintraege!
		
		for (int player_itype : YoloKnowledge.instance.getPossiblePlayerItypes()) {
			boolean error = false;
			for (Observation obs : grid[xStart][yStart]) {
				if(obs.category != Types.TYPE_AVATAR){
					PlayerEvent event = YoloKnowledge.instance.getPlayerEvent(player_itype, obs.itype, true);
					if(event.willCancel(inventory)){
						if(!Agent.UPLOAD_VERSION && DEBUG)
							System.out.println(obs.itype + " blocks Player " + player_itype);
						error |= event.willCancel(inventory);
					}else{
						//Event ist durchfuehrbar: Vielleicht aber aendert es den Itype:
						int iTypeChange = event.getEvent(inventory).getIType();
						if(iTypeChange != -1)
							player_itype = YoloKnowledge.instance.indexToItype(iTypeChange);
					}
				}
			}
			
			if(!error){
				//Player can stand here in player_itype type
				aStar.setIllegalMove(xVerbot, yVerbot, itypeVerbot);
				
				aStar.setStopEarly(xZiel, yZiel);
				aStar.setIgnoreOneMoveableOfType(itypeVerbot);
				aStar.calculate(xStart, yStart, player_itype, new int[]{}, false);
				if((!aStar.interpretedAsWall[xZiel][yZiel] || istZiel(xZiel, yZiel, itypeVerbot)) && aStar.distance[xZiel][yZiel] > 0) 	
					//TODO: aStar.interpretedAsWall[xZiel][yZiel] nicht betrachten, wenn es der push stein ist!
					return true;
			}
		}
		return false;
	}

	private boolean istZiel(int x, int y, int object_itype) {
		for (Observation obs : grid[x][y]) {
			if(object_itype == obs.itype)
				return true;
		}
		return false;
	}

	private int getMalus(int x, int y) {
		if(grid[x][y] == null)
			return 0;
		
		int malus = 0;
		for (Observation obs : grid[x][y]) {
			malus += malusByIgnoringItype[obs.itype];
		}
		return malus;
	}

	/** Der Spieler koennte theoretisch von A nach B gehen
	 * @param fromX	A.x
	 * @param fromY	A.y
	 * @param toX	B.x
	 * @param toY	B.y
	 * @return	Der Spieler koennte theoretisch von A nach B gehen
	 */
	private boolean playerCanPush(int fromX, int fromY, int toX,
			int toY, byte[] inventory) {
		
		for (int player_itype : YoloKnowledge.instance.getPossiblePlayerItypes()) {
			boolean error = false;
			for (Observation obs : grid[fromX][fromY]) {
				//Kann dieser IType auf dem Startfeld stehen?
				if(obs.category != Types.TYPE_AVATAR){
					PlayerEvent event = YoloKnowledge.instance.getPlayerEvent(player_itype, obs.itype, true);
					YoloEvent triggeringEvent = event.getEvent(inventory);
					if(triggeringEvent.getIType() == -1 || YoloKnowledge.instance.indexToItype(triggeringEvent.getIType()) == player_itype)
						error |= event.willCancel(inventory) && event.getObserveCount() > 0;
					else
						error = true;
				}
			}
			
			if(!error){
				//Kann er zusaetzlich auch auf das Zielfeld gehen? 
				for (Observation obs2 : grid[toX][toY]) {
					if(obs2.category != Types.TYPE_AVATAR){
						PlayerEvent event = YoloKnowledge.instance.getPlayerEvent(player_itype, obs2.itype, true);
						YoloEvent triggeringEvent = event.getEvent(inventory);
						error |= event.willCancel(inventory) && event.getObserveCount() > 0;
					}
				}
			}
			
			if(!error)
				return true;
		}
		
		return false;
	}

	private boolean canMoveHere(int x, int y, boolean[] collisionItypes) {
		for (Observation obs : grid[x][y]) {
			if(collisionItypes[obs.itype])
				return false;
		}
		return true;
	}
	
	private int getXinDirection(int x, byte direction){
		switch (direction) {
		case LEFT:
			return x-1;
		case RIGHT:
			return x+1;
		default:
			return x;
		}
	}
	
	private int getYinDirection(int y, byte direction){
		switch (direction) {
		case TOP:
			return y-1;
		case BOTTOM:
			return y+1;
		default:
			return y;
		}
	}
	
	private boolean posIsOk(int x, int y){
		return x>=0 && y>=0 && x < grid.length && y < grid[0].length;
	}
	
}
