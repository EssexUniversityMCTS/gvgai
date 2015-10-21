package controllers.YOLOBOT.SubAgents.HandleMCTS;

import java.util.ArrayList;
import java.util.Iterator;

import controllers.YOLOBOT.Agent;
import controllers.YOLOBOT.YoloState;
import controllers.YOLOBOT.Util.Wissensdatenbank.YoloKnowledge;
import ontology.Types.ACTIONS;
import ontology.Types.WINNER;
import tools.Utils;
import tools.Vector2d;
import core.game.Observation;


public abstract class MCTNode {
	
	protected ACTIONS action;
	protected MCTNode[] children;
	protected double[] averageRewards;
	protected MCTNode parent;
	protected MCTNode lastSelectedChild;
	protected int visits;
	protected int depth;
	protected int childrenFreeCount;
	private boolean neverMinusScore;
	private boolean sometimesMinusScore;
	public boolean isLeaf;
	
	/**
	 * Actions, die noch nie in den Tod gefuehrt haben (Auch nicht geschaetzt)
	 */
	protected ArrayList<ACTIONS> validActions;
	private boolean forceValidActionsDontChange;
	
	public MCTNode(ACTIONS action, YoloState state){
		setState(state);
		this.action = action;
		lastSelectedChild = null;
		depth = 0;
		doFullInit(state);
	}
	
	public MCTNode(ACTIONS action, MCTNode parent) {
		isLeaf = true;
		lastSelectedChild = null;
		this.action = action;
		this.parent = parent;
		if(this.parent == null){
			depth = 0;
		}else{
			depth = parent.depth + 1;
		}
		
		int x = parent.getState().getAvatarX();
		int y = parent.getState().getAvatarY();
		boolean noMove = false;
		Vector2d orientation = parent.getState().getAvatarOrientation();
		switch (action) {
		case ACTION_DOWN:
			if(!orientation.equals(YoloKnowledge.ORIENTATION_NULL) && !orientation.equals(YoloKnowledge.ORIENTATION_DOWN))
				noMove = true;
			y++;
			break;
		case ACTION_UP:
			if(!orientation.equals(YoloKnowledge.ORIENTATION_NULL) && !orientation.equals(YoloKnowledge.ORIENTATION_UP))
				noMove = true;
			y--;
			break;
		case ACTION_RIGHT:
			if(!orientation.equals(YoloKnowledge.ORIENTATION_NULL) && !orientation.equals(YoloKnowledge.ORIENTATION_RIGHT))
				noMove = true;
			x++;
			break;
		case ACTION_LEFT:
			if(!orientation.equals(YoloKnowledge.ORIENTATION_NULL) && !orientation.equals(YoloKnowledge.ORIENTATION_LEFT))
				noMove = true;
			x--;
			break;
		default:
			//TODO: Action use auf singleton checken! Wenn schon geschossen, dann true!
			noMove = true;
		}
		
		if(noMove){
			x = parent.getState().getAvatarX();
			y = parent.getState().getAvatarY();
		}
		averageRewards = MCTHandler.heuristics.EvaluateAll(parent.getState());
		if(MCTHandler.heuristicToUse != null){
			averageRewards[MCTHandler.heuristics.getIndexOfHeuristic(MCTHandler.heuristicToUse.GetType())] = MCTHandler.heuristicToUse.getModdedHeuristic(parent.getState(), x, y, orientation);
		}
		neverMinusScore = true;
		sometimesMinusScore = false;
	}

	public MCTNode expandOrSelect(){
		lastSelectedChild = null;
		while (!getState().isGameOver() && depth < MCTHandler.ROLLOUT_DEPTH) {
			
			//Is fully Expanded
//				if(forceValidActionsDontChange)
//					System.out.print("!!!");
			MCTNode bestChild = uSBSelectChild();
			if(bestChild == null){
				//have no good child (none at all)
				if(MCTHandler.DEBUG_TRACE)
					System.out.print("Me -> ");
				return this;
			}else{
				if(bestChild.isLeaf){
					YoloState state = bestChild.parent.getState().copyAdvanceLearn(bestChild.action);
					bestChild.doFullInit(state);
					if(MCTHandler.DEBUG_TRACE)
						System.out.print(bestChild.action.toString());
					return bestChild;
				}
				if(MCTHandler.DEBUG_TRACE)
					System.out.print(bestChild.action.toString() + "  ->  ");
				lastSelectedChild = bestChild;
				return bestChild.expandOrSelect();
			}
		}
		if(MCTHandler.DEBUG_TRACE)
			System.out.print("Endloop -> ");
		return this;
	}
	
	private void doFullInit(YoloState state) {
		validActions = new ArrayList<ACTIONS>(MCTHandler.rootState.getAvailableActions(true));
		getValidActions(state);
		if(validActions.isEmpty()){
			if(depth ==0)
				forceValidActionsDontChange = true;
			validActions = new ArrayList<ACTIONS>(MCTHandler.rootState.getAvailableActions(true));
		}
		
		childrenFreeCount = validActions.size();
		children = new MCTNode[childrenFreeCount];
		averageRewards = new double[MCTHandler.heuristics.Length()];
		neverMinusScore = true;
		sometimesMinusScore = false;
		isLeaf = false;
		
		setState(state);
		createPseudoChildren();
		
	}

	protected abstract void createPseudoChildren();

	protected abstract void setState(YoloState state) ;

	
	
	public YoloState simulate(boolean useScoreLookahead, boolean forceNoRandom) {
		int currentDepth = 0;
		ACTIONS nextAction;
		YoloState oldState = getState();
		YoloState newState = oldState;
		int backtrackCount = 0;
		boolean targetReached = false;
		double summedHeuristicUntilTargetReached = 0;
		ArrayList<ACTIONS> forbiddenActions = new ArrayList<ACTIONS>();
		
		
		//Check Reach Target:
		if(YoloKnowledge.instance.positionAufSpielfeld(newState.getAvatarX(), newState.getAvatarY())){			
			if(MCTHandler.heuristicToUse != null && MCTHandler.heuristicToUse.EvaluateWithoutNormalisation(newState) == 0){
				//Already at target!
				targetReached = true;
			}
		}
		
		while (!oldState.isGameOver() && currentDepth < MCTHandler.ROLLOUT_DEPTH) {
			if(!targetReached){
				//Didnt reach the target yet
				if(useScoreLookahead)
					nextAction = MCTHandler.scoreLookaheadPolicy.nextAction(oldState, forbiddenActions,true);
				else
					nextAction = MCTHandler.rolloutPolicy.nextAction(oldState, forbiddenActions, forceNoRandom);
			}else{
				nextAction = MCTHandler.randomPolicy.nextAction(oldState, forbiddenActions);
			}
				
			newState = oldState.copyAdvanceLearn(nextAction);
			
			boolean shouldBackTrack = newState.isGameOver() && newState.getGameWinner() != WINNER.PLAYER_WINS;
//			shouldBackTrack |= !YoloKnowledge.instance.haveEverGotScoreWithoutWinning() && rootScore > newState.getGameScore();
			if(shouldBackTrack){
				//Random walk in den Tod!
				//Backtracke und nimm andere Aktion!
				forbiddenActions.add(nextAction);
				if(MCTHandler.DEBUG_TRACE)
					System.out.print(" (" + nextAction.toString() + ") ");
				if(backtrackCount>4){
					newState.setTargetReachedCost(summedHeuristicUntilTargetReached);
					return newState;
				}
				backtrackCount++;
				newState = oldState;
			}else{
				//Random walk lebt!
				
				//Check A-Star Target Reached:
				

				if(!targetReached && MCTHandler.heuristicToUse != null && YoloKnowledge.instance.positionAufSpielfeld(newState.getAvatarX(), newState.getAvatarY())){
					double heuristicValue = MCTHandler.heuristicToUse.EvaluateWithoutNormalisation(newState);
					summedHeuristicUntilTargetReached -= heuristicValue;
					if(heuristicValue == 0){
						//Reached Target!
						if(MCTHandler.DEBUG_TRACE)
							System.out.print(" ->" + nextAction.toString() + "<- ");
						//Save targetReached:
						targetReached = true;
					}
				}
				
				
				forbiddenActions.clear();
				oldState = newState;
				currentDepth++;
				if(MCTHandler.DEBUG_TRACE)
					System.out.print(" " + nextAction.toString() + " ");
			}
		}
		newState.setTargetReachedCost(summedHeuristicUntilTargetReached);
		return newState;		
	}

	public MCTNode uSBSelectChild() {
		
		boolean ignoreDead = validActions.isEmpty();
		
		double bestValue = -Double.MAX_VALUE;
		MCTNode bestNode = null;
		
		double[] hvVals = getHeuristicValues();

		for (int i = 0; i < hvVals.length; i++) {
			MCTNode child = children[i];
			if(child == null || !(ignoreDead || validActions.contains(children[i].action)))
				continue;
			hvVals[i] = (hvVals[i]+1)/2;

			double uctValue = hvVals[i]
					+ Math.sqrt(2)
					* Math.sqrt(Math.log(this.visits + 1)
							/ (child.visits + MCTHandler.epsilon));

			// small sampleRandom numbers: break ties in unexpanded nodes
			uctValue = Utils.noise(uctValue, MCTHandler.epsilon,
					MCTHandler.rnd.nextDouble()); // break ties randomly

			if (uctValue > bestValue) {
				bestNode = child;
				bestValue = uctValue;
			}

		}

		if (bestNode == null) {
			if(!Agent.UPLOAD_VERSION)
				System.out.println("Warning! returning null: " + bestValue
					+ " : " + this.children.length);
			return null;
		}
		postSelectChild(bestNode);
		return bestNode;
	}

	public ArrayList<ACTIONS> getUntriedValidActions() {
		ArrayList<ACTIONS> untriedActions = getValidActions(getState());

		for (MCTNode child : children) {
			if (child != null) {
				untriedActions.remove(child.action);
			} else {
				break;
			}
		}
		
		return untriedActions;
		
	}

	public ACTIONS bestAction(){
		int mostVisits = -1;
		ACTIONS actionToTake = ACTIONS.ACTION_NIL;
		double heuristicValueOfbest = 0;
		double[] hvVals = getHeuristicValues();
		boolean bestHasScoreLoss = false;

		int mostVisitsWithoutScoreLoss = -1;
		double heuristicValueOfBestWithoutScoreLoss = 0;
		ACTIONS bestActionWithoutScoreLoss = ACTIONS.ACTION_NIL;
		boolean haveActionWithoutScoreLoss = false;
		
		for (int i = 0; i < children.length; i++) {

			if (children[i] != null) {
				
				//Best
				if (children[i].visits > mostVisits) {
					mostVisits = children[i].visits;
					actionToTake = children[i].action;
					heuristicValueOfbest = hvVals[i];
					bestHasScoreLoss = children[i].sometimesMinusScore;
					
				}else if(children[i].visits == mostVisits){
					if(hvVals[i] >= heuristicValueOfbest){
						mostVisits = children[i].visits;
						actionToTake = children[i].action;
						heuristicValueOfbest = hvVals[i];
						bestHasScoreLoss = children[i].sometimesMinusScore;
						
					}
						
				}
				
				//Best without score Loss
				if (children[i].neverMinusScore && children[i].visits > mostVisitsWithoutScoreLoss) {
					mostVisitsWithoutScoreLoss = children[i].visits;
					bestActionWithoutScoreLoss = children[i].action;
					heuristicValueOfBestWithoutScoreLoss = hvVals[i];
					haveActionWithoutScoreLoss = true;
				}else if(children[i].neverMinusScore && children[i].visits == mostVisitsWithoutScoreLoss){
					if(hvVals[i] >= heuristicValueOfBestWithoutScoreLoss){
						mostVisitsWithoutScoreLoss = children[i].visits;
						bestActionWithoutScoreLoss = children[i].action;
						heuristicValueOfBestWithoutScoreLoss = hvVals[i];
						haveActionWithoutScoreLoss = true;
						
					}
						
				}
			}
		}
//		if(MCTHandler.useNonNegativeScore && bestHasScoreLoss){
//			if(haveActionWithoutScoreLoss){
//				System.out.println("Other action");
//				return bestActionWithoutScoreLoss;
//			}
//		}
		
		return actionToTake;
	}
	
	public double[] getHeuristicValues() {
		double[][] childrenAverageRewards = new double[children.length][averageRewards.length];
		for (int i = 0; i < children.length; i++) {
			if (children[i] != null) {
				childrenAverageRewards[i] = children[i].averageRewards;				
			}
		}
		return MCTHandler.heuristics.Evaluate(childrenAverageRewards);
	}
	

	public void update(double[] rewards) {
		for (int i = 0; i < rewards.length; i++) {
			averageRewards[i] = (averageRewards[i]*visits + rewards[i])/(visits + 1);
		}
		visits++;
	}
	

	public ArrayList<ACTIONS> getValidActions(YoloState curState) {
		if(forceValidActionsDontChange)
			return new ArrayList<ACTIONS>(validActions);
		Observation stochasticKiller = YoloKnowledge.instance.getPossibleStochasticKillerAt(curState, curState.getAvatarX(), curState.getAvatarY());
		boolean shouldMove = stochasticKiller != null;
		boolean canUse = shouldMove && validActions.contains(ACTIONS.ACTION_USE) && YoloKnowledge.instance.canInteractWithUse(curState.getAvatar().itype, stochasticKiller.itype) && observationIsInFrontOfAvatar(curState, stochasticKiller);
		
		for (Iterator<ACTIONS> iterator = validActions.iterator(); iterator.hasNext();) {
			ACTIONS actions = (ACTIONS) iterator.next();
			if(YoloKnowledge.instance.actionsLeadsOutOfBattlefield(curState, actions) || YoloKnowledge.instance.moveWillCancel(curState,actions, true, false) || couldGetKilledByEnemyIfIUseAction(curState, actions, shouldMove, canUse)) {
				iterator.remove();
				childrenFreeCount--;
			}
		}
		
		if(validActions.isEmpty())
			return new ArrayList<ACTIONS>(curState.getAvailableActions());
		
		return new ArrayList<ACTIONS>(validActions);
	}

	public MCTNode getParent() {
		return parent;
	}


	protected abstract YoloState getState();

	protected abstract void postSelectChild(MCTNode bestNode);
	

	
	private boolean observationIsInFrontOfAvatar(YoloState curState,
			Observation observation) {
		
		Vector2d orientation = curState.getAvatarOrientation();
		
		if(orientation.equals(YoloKnowledge.ORIENTATION_NULL))
			return false;
		
		int x = curState.getAvatarX();
		int y = curState.getAvatarY();

		if (orientation.equals(YoloKnowledge.ORIENTATION_DOWN))
				y++;
		else if (orientation.equals(YoloKnowledge.ORIENTATION_UP))
				y--;
		else if (orientation.equals(YoloKnowledge.ORIENTATION_RIGHT))
				x++;
		else if (orientation.equals(YoloKnowledge.ORIENTATION_LEFT))
				x--;
		
		
		if(!YoloKnowledge.instance.positionAufSpielfeld(x,y))
			return false;
		return  curState.getObservationGrid()[x][y].contains(observation);
			
	}


	private boolean couldGetKilledByEnemyIfIUseAction(YoloState curState,
			ACTIONS action, boolean shouldMove, boolean canUse) {
		if(shouldMove){
			if(canUse && action == ACTIONS.ACTION_USE)
				return false;
			else{
				Vector2d orientation = curState.getAvatarOrientation();
				if(!orientation.equals(YoloKnowledge.ORIENTATION_NULL)){
					if(shouldMove){
						switch (action) {
						case ACTION_DOWN:
							return !orientation.equals(YoloKnowledge.ORIENTATION_DOWN);
						case ACTION_UP:
							return !orientation.equals(YoloKnowledge.ORIENTATION_UP);
						case ACTION_RIGHT:
							return !orientation.equals(YoloKnowledge.ORIENTATION_RIGHT);
						case ACTION_LEFT:
							return !orientation.equals(YoloKnowledge.ORIENTATION_LEFT);
						default:
						}
					}
					return true;
				}
			}			
		}
		return false;		
	}
}
