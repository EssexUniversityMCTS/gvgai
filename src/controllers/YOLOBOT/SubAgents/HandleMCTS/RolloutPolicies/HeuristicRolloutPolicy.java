package controllers.YOLOBOT.SubAgents.HandleMCTS.RolloutPolicies;

import java.util.ArrayList;
import java.util.Iterator;

import controllers.YOLOBOT.YoloState;
import controllers.YOLOBOT.SubAgents.HandleMCTS.MCTHandler;
import controllers.YOLOBOT.Util.Heuristics.IModdableHeuristic;
import controllers.YOLOBOT.Util.Wissensdatenbank.YoloKnowledge;
import ontology.Types.ACTIONS;
import tools.Vector2d;

public class HeuristicRolloutPolicy extends RolloutPolicy {

	RandomNotDeadRolloutPolicy randomNotDeadPolicy;
	private boolean useScoreLookahead;
	
	public HeuristicRolloutPolicy() {
		this(false);
	}
	
	public HeuristicRolloutPolicy(boolean useScoreLookahead) {
		this.useScoreLookahead = useScoreLookahead;
		randomNotDeadPolicy = new RandomNotDeadRolloutPolicy();
	}
	
	@Override
	public ArrayList<ACTIONS> possibleNextActions(YoloState state,
			ArrayList<ACTIONS> forbiddenAction, boolean forceNotEpsilon) {
		
		ArrayList<ACTIONS> validActions = randomNotDeadPolicy.possibleNextActions(state, forbiddenAction, forceNotEpsilon);
		IModdableHeuristic heuristicToUse = null;
		if(useScoreLookahead){
			if(MCTHandler.scoreLookaheadHeuristic.isActive())
				heuristicToUse = MCTHandler.scoreLookaheadHeuristic;
			else
				return validActions;
		}else{
			if(MCTHandler.aSDH.isActive())
				heuristicToUse = MCTHandler.aSDH;
			else if (MCTHandler.npcH.isActive())
				heuristicToUse = MCTHandler.npcH;
			else
				return validActions;
		}
		
		ArrayList<ACTIONS> bestActions = new ArrayList<ACTIONS>();
		int currentBestHeuristic = Integer.MIN_VALUE;
		Vector2d orientation = state.getAvatarOrientation();
		int x = state.getAvatarX();
		int y = state.getAvatarY();
		for (Iterator<ACTIONS> iterator = validActions.iterator(); iterator.hasNext();) {
			ACTIONS action = (ACTIONS) iterator.next();
			
			int myX = x;
			int myY = y;
			switch (action) {
			case ACTION_DOWN:
//				if(orientation.equals(YoloKnowledge.ORIENTATION_NULL) || orientation.equals(YoloKnowledge.ORIENTATION_DOWN))
					myY++;
				break;
			case ACTION_UP:
//				if(orientation.equals(YoloKnowledge.ORIENTATION_NULL) || orientation.equals(YoloKnowledge.ORIENTATION_UP))
					myY--;
				break;
			case ACTION_RIGHT:
//				if(orientation.equals(YoloKnowledge.ORIENTATION_NULL) || orientation.equals(YoloKnowledge.ORIENTATION_RIGHT))
					myX++;
				break;
			case ACTION_LEFT:
//				if(orientation.equals(YoloKnowledge.ORIENTATION_NULL) || orientation.equals(YoloKnowledge.ORIENTATION_LEFT))
					myX--;
				break;
			default:
			}
			if(YoloKnowledge.instance.positionAufSpielfeld(myX, myY) && heuristicToUse.canStepOn(myX, myY)){
				//Ziel ist auf dem Spielfeld
				int myHeuristicValue = (int) heuristicToUse.getModdedHeuristic(state, myX, myY, orientation);
				if(myHeuristicValue > currentBestHeuristic){
					//Hab bessere Aktion gefunden!
					bestActions.clear();
					bestActions.add(action);
					currentBestHeuristic = myHeuristicValue;
					
				}else if(myHeuristicValue == currentBestHeuristic){
					bestActions.add(action);
				}
			}else{
				//Fuehrt aus dem Spielfeld
			}
			
		}
		if(!useScoreLookahead && !orientation.equals(YoloKnowledge.ORIENTATION_NULL)){
			ACTIONS onlyAction = null;
			for (Iterator<ACTIONS> iterator = bestActions.iterator(); iterator.hasNext();) {
				ACTIONS action = (ACTIONS) iterator.next();
				switch (action) {
				case ACTION_DOWN:
					if(orientation.equals(YoloKnowledge.ORIENTATION_DOWN)){
						onlyAction = action;
					}
					break;
				case ACTION_UP:
					if(orientation.equals(YoloKnowledge.ORIENTATION_UP))
						onlyAction = action;
					break;
				case ACTION_RIGHT:
					if(orientation.equals(YoloKnowledge.ORIENTATION_RIGHT))
						onlyAction = action;
					break;
				case ACTION_LEFT:
					if(orientation.equals(YoloKnowledge.ORIENTATION_LEFT))
						onlyAction = action;
					break;
				default:
				}
			}
			if(onlyAction != null){
				bestActions.clear();
				bestActions.add(onlyAction);
			}
		}
		
		
		if(bestActions.isEmpty())
			bestActions = validActions;


		if(validActions.contains(ACTIONS.ACTION_USE) && YoloKnowledge.instance.canUseInteractWithSomethingAt(state)){
			bestActions.remove(ACTIONS.ACTION_NIL);
			if(currentBestHeuristic == 0)
				bestActions.clear();
			if(!bestActions.contains(ACTIONS.ACTION_USE))
				bestActions.add(ACTIONS.ACTION_USE);
		}
		return bestActions;
	}

}
