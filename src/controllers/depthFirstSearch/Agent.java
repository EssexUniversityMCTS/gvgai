package controllers.depthFirstSearch;

import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import ontology.Types.WINNER;
import tools.ElapsedCpuTimer;

public class Agent extends AbstractPlayer{

	private Random random;
	
	public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer){
		random = new Random();
	}
	
	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		double averageTime = 10;
		double totalTime = 0;
		double worstTime = 10;
		int numberOfIterations = 0;
		
		ArrayList<Types.ACTIONS> actions = stateObs.getAvailableActions();
		Stack<Node> stack = new Stack<Node>();
		stack.push(new Node(stateObs, null, Types.ACTIONS.ACTION_NIL));
		Node currentNode = null;
		while(!stack.isEmpty() && elapsedTimer.remainingTimeMillis() > 2 * averageTime && 
				elapsedTimer.remainingTimeMillis() > worstTime){
			ElapsedCpuTimer timer = new ElapsedCpuTimer();
			
			currentNode = stack.pop();
			if(currentNode.state.getGameWinner() == WINNER.PLAYER_WINS){
				break;
			}
			if(currentNode.state.getGameWinner() == WINNER.PLAYER_LOSES){
				continue;
			}
			
			for(Types.ACTIONS a:actions){
				Node nextNode = new Node(currentNode.state.copy(), currentNode, a);
				nextNode.state.advance(a);
				
				stack.push(nextNode);
			}
			
			numberOfIterations += 1;
			totalTime = timer.elapsedMillis();
			averageTime = totalTime / numberOfIterations;
		}
		
		if(currentNode == null){
			if(actions.size() > 0){
				return actions.get(random.nextInt(actions.size()));
			}
			return Types.ACTIONS.ACTION_NIL;
		}
		return currentNode.getAction();
	}
	
	class Node{
		public Types.ACTIONS action;
		public Node parent;
		public StateObservation state;
		
		public Node(StateObservation state, Node parent, Types.ACTIONS action){
			this.state = state;
			this.parent = parent;
			this.action = action;
		}
		
		public Types.ACTIONS getAction(){
			if(this.parent == null){
				return action;
			}
			if(this.parent.parent == null){
				return this.action;
			}
			
			return parent.getAction();
		}
	}

}
