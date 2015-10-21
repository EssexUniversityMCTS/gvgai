package controllers.YOLOBOT;

import java.util.ArrayList;
import java.util.LinkedList;

import controllers.YOLOBOT.Helper;
import controllers.YOLOBOT.YoloState;
import ontology.Types;
import ontology.Types.ACTIONS;
import ontology.Types.WINNER;
import tools.Vector2d;
import core.game.Observation;

public class OwnHistory {

	public YoloState state;
	public LinkedList<ACTIONS> actions;
	private double priority;
	private double distanceResAva;
	private double anzRes, anzMoveable, anzStatic;
	private int erreichbareFelder;
	private int winBonus;
	private int heatmapValue;
	private boolean pickedUp;
	private int tick;
	private static int[][] heatmap;
	
	public OwnHistory(OwnHistory before, ACTIONS newAction){
		state = before.state.copy();
		state.advance(newAction);
		actions = new LinkedList<ACTIONS>(before.actions);
		actions.add(newAction);
		if(state != null){
			/*if(state.getResourcesPositions() != null)
				priority += state.getResourcesPositions().length*2;
			if(state.getMovablePositions() != null)
				priority += state.getMovablePositions().length;*/
			

			ArrayList<Observation>[][] grid = state.getObservationGrid();
			//Hier soll geprueft werden ob auf dem Feld des Avatars etwas liegt
			Vector2d avatarPosition = state.getAvatarPosition();
			Observation agent = Helper.getAgent(grid, state);
			int agentX, agentY;
			if(agent != null){
				agentX = (int) (agent.position.x / state.getBlockSize());
				agentY = (int) (agent.position.y / state.getBlockSize());
			}else {
				agentX = 0;
				agentY = 0;
			}
			heatmapValue = heatmap[agentX][agentY]--;
			ArrayList<Observation>[] resObs = state.getResourcesPositions(avatarPosition);
			
			distanceResAva = Double.MAX_VALUE;
			anzRes = 0;
			if(resObs != null){
				for (int resTypeNr = 0; resTypeNr < resObs.length; resTypeNr++) {
					if(!resObs[resTypeNr].isEmpty()){
						//Typ im Spiel vorhanden
						anzRes += resObs[resTypeNr].size();
						double distance = resObs[resTypeNr].get(0).sqDist/state.getBlockSize();
						if(distance < distanceResAva)
							distanceResAva = distance;
					}
				}
			}
			winBonus = 0;
			if(state.isGameOver()){
				winBonus = (state.getGameWinner()==WINNER.PLAYER_WINS?1:-1);
			}

			//OneTypeAStar aStern = new OneTypeAStar(state);
			//List<Observation> obs = aStern.calculate(new int[0]);
			erreichbareFelder = 0;// aStern.getMoveableFieldCount();
			
			tick = state.getGameTick();
			
			for (int x = 0; x < grid.length; x++) {
				for (int y = 0; y < grid[x].length; y++) {
					for (Observation ob : grid[x][y]) {
						if(ob.category == Types.TYPE_MOVABLE)
							anzMoveable++;
						else if(ob.category == Types.TYPE_STATIC)
							anzStatic++;
					}
				}
			}
		}
			
		
		pickedUp = anzRes < before.anzRes;
	}

	public OwnHistory(YoloState so) {
		actions = new LinkedList<ACTIONS>();
		state = so;
		if(heatmap == null)
			heatmap = new int[so.getWorldDimension().height][so.getWorldDimension().width];
	}
	
	public OwnHistory(YoloState stateObs,
			LinkedList<ACTIONS> durchgefuehrteAktionen) {
		actions = durchgefuehrteAktionen;
		state = stateObs;
	}

	public double getPriority(){
		double history = Math.max(30,state.getEventsHistory().size());
		double distToRes = anzRes==0?0:(pickedUp?0:0-distanceResAva);
		double resCount = anzRes;
		double score = state.getGameScore();
		double moveFields = erreichbareFelder;
		double win = winBonus;

		double scoreWeight = 100;
		//if(tick < 70)
		//	scoreWeight = 0;
		
		double historyWeight = 0.1;
		if(score*scoreWeight == 0)
			historyWeight = 0;
		
		priority = 100_0000_000*win + scoreWeight*score + moveFields*0 + history*historyWeight + 0.05 * distToRes -1* resCount;// - heat;
			
		if(tick<100)
			return -win;
			
		//return -100* -state.getGameScore()-erreichbareFelder - 100*winBonus;//priority;
		//priority = -erreichbareFelder;
		return -priority;//(erreichbareFelder + (100*(priority + winBonus)) +state.getGameScore());

	}

	public boolean toPrune() {
		
		return false;
	}
	
	@Override
	public String toString() {
		return "Tiefe: " + actions.size() + "\tPrio: " + priority;
	}
}
