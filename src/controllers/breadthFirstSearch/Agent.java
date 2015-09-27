package controllers.breadthFirstSearch;

import java.util.ArrayList;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import ontology.Types.WINNER;
import tools.ElapsedCpuTimer;

public class Agent extends AbstractPlayer{

	public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer){
		
	}
	
	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		ArrayList<Node> queue = new ArrayList<Node>();
		queue.add(new Node(null, Types.ACTIONS.ACTION_NIL, stateObs));
		
		float avgTime = 10;
		float worstTime = 10;
		float totalTime = 0;
		int numberOfIterations = 0;
		Node currentNode = null;
		ArrayList<Types.ACTIONS> possibleActions = stateObs.getAvailableActions();
		
		while(!queue.isEmpty() && elapsedTimer.remainingTimeMillis() > 2 * avgTime 
				&& elapsedTimer.remainingTimeMillis() > worstTime){
			ElapsedCpuTimer methodTime = new ElapsedCpuTimer();
			
			currentNode = queue.remove(0);
			if(currentNode.state.getGameWinner() == WINNER.PLAYER_WINS){
				break;
			}
			if(currentNode.state.getGameWinner() == WINNER.PLAYER_LOSES){
				continue;
			}
			for(int i=0;i<possibleActions.size();i++){
				StateObservation newState = stateObs.copy();
				newState.advance(possibleActions.get(i));
				queue.add(new Node(currentNode, possibleActions.get(i), newState));
			}
			
			numberOfIterations += 1;
			totalTime += methodTime.elapsedMillis();
			avgTime = totalTime / numberOfIterations;
		}
		
		if(currentNode == null){
			return Types.ACTIONS.ACTION_NIL;
		}
		
		return currentNode.getAction();
	}

	public class Node {
		public Node parent;
		public Types.ACTIONS action;
		public StateObservation state;
		
		public Node(Node parent, Types.ACTIONS action, StateObservation state){
			this.parent = parent;
			this.action = action;
			this.state = state;
		}
		
		public Types.ACTIONS getAction(){
			if(this.parent == null){
				return action;
			}
			if(this.parent.parent == null){
				return action;
			}
			
			return parent.getAction();
		}
	}
}


