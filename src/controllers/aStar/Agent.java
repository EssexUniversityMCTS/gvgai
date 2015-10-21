package controllers.aStar;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;

import controllers.Heuristics.WinScoreHeuristic;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import ontology.Types.WINNER;
import tools.ElapsedCpuTimer;

public class Agent extends AbstractPlayer{

	private ArrayList<Types.ACTIONS> actions;
	private Random random;
	
	public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer){
		actions = stateObs.getAvailableActions();
		random = new Random();
	}
	
	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		double worstTime = 10;
		double avgTime = 10;
		double totalTime = 0;
		double numberOfTime = 0;
		
		PriorityQueue<Node> queue = new PriorityQueue<Node>();
		queue.add(new Node(stateObs, null, ACTIONS.ACTION_NIL));
		Node currentNode = null;
		
		while(!queue.isEmpty() && elapsedTimer.remainingTimeMillis() > avgTime && 
				elapsedTimer.remainingTimeMillis() > worstTime){
			ElapsedCpuTimer time = new ElapsedCpuTimer();
			
			currentNode = queue.remove();
			if(currentNode.stateObs.getGameWinner() == WINNER.PLAYER_WINS){
				break;
			}
			if(currentNode.stateObs.getGameWinner() == WINNER.PLAYER_LOSES){
				continue;
			}
			
			for(Types.ACTIONS a:actions){
				StateObservation newState = currentNode.stateObs.copy();
				newState.advance(a);
				queue.add(new Node(newState, currentNode, a));
			}
			
			totalTime += time.elapsedMillis();
			numberOfTime += 1;
			avgTime = totalTime / numberOfTime;
		}
		
		return currentNode.getAction();
	}
	
	private class Node implements Comparable<Node>{
		public StateObservation stateObs;
		public Node parent;
		public Types.ACTIONS action;
		public double depth;
		public double cost;
		public double heuristic;
		
		public Node(StateObservation stateObs, Node parent, Types.ACTIONS act){
			this.stateObs = stateObs;
			this.parent = parent;
			this.action = act;
			this.depth = 1;
			if(this.parent != null){
				this.depth += this.parent.depth;
			}
			this.cost = 1 / depth;
			WinScoreHeuristic heuristic = new WinScoreHeuristic(stateObs);
			this.heuristic = heuristic.evaluateState(stateObs);
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
		
		@Override
		public int compareTo(Node n) {
			if(this.cost + this.heuristic > n.cost + n.heuristic){
				return -1;
			}
			
			if(this.cost + this.heuristic <= n.cost + n.heuristic){
				return 1;
			}
			
			return 0;
		}
	}
}
