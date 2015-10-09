package controllers.greedySearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import ontology.Types.WINNER;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

public class Agent extends AbstractPlayer{

	public int[][] tiles;
	public int totalMoves;
	
	private Node bestNode;
	private int exploredStates;
	private Random random;
	
	public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer){
		bestNode = null;
		random = new Random();
	}
	
	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		float avgTime = 10;
		float worstTime = 10;
		float totalTime = 0;
		int numberOfIterations = 0;
		
		int d1 = stateObs.getObservationGrid().length;
		int d2 = stateObs.getObservationGrid()[0].length;
		
		tiles = new int[d1][d2];
		totalMoves = 0;
		
		bestNode = null;
		exploredStates = 0;
		
		Node root = new Node(stateObs, null, Types.ACTIONS.ACTION_NIL, this);
		ArrayList<Node> queue = new ArrayList<Node>();
		ArrayList<Types.ACTIONS> actions = stateObs.getAvailableActions();
		queue.add(root);
		Node currentNode = null;
		
		while(queue.size() > 0 && elapsedTimer.remainingTimeMillis() > 2 * avgTime 
				&& elapsedTimer.remainingTimeMillis() > worstTime){
			ElapsedCpuTimer methodTime = new ElapsedCpuTimer();
			
			Collections.sort(queue);
			currentNode = queue.remove(0);
			exploredStates += 1;
			
			if(bestNode == null || bestNode.fitness < currentNode.fitness){
				bestNode = currentNode;
			}
			
			for(Types.ACTIONS a:actions){
				StateObservation temp = currentNode.stateObs.copy();
				temp.advance(a);
				Node newNode = new Node(temp, currentNode, a, this);
				queue.add(newNode);
			}
			
			numberOfIterations += 1;
			totalTime += methodTime.elapsedMillis();
			avgTime = totalTime / numberOfIterations;
		}
		
		ArrayList<Types.ACTIONS> solution = bestNode.getSolution();
		if(solution.size() > 0){
			return bestNode.getSolution().get(0);
		}
		
		return actions.get(random.nextInt(actions.size()));
	}

	public double getBestScore(){
		return bestNode.fitness;
	}
	
	public ArrayList<Types.ACTIONS> getBestSolution(){
		return bestNode.getSolution();
	}
	
	public int getNumberOfEvents(){
		return bestNode.getNumberOfEvents();
	}
	
	public int getNumberOfStates(){
		return exploredStates;
	}
	
	public static class Node implements Comparable<Node>{
		public static final double HUGE_VALUE = 1000;
		public static double getFitness(StateObservation stateObs){
			if(stateObs.isGameOver()){
				if(stateObs.getGameWinner() == WINNER.PLAYER_WINS){
					return HUGE_VALUE;
				}
				else{
					return -HUGE_VALUE;
				}
			}
			
			return stateObs.getGameScore();
		}
		
		public Node parent;
		public StateObservation stateObs;
		public Types.ACTIONS action;
		public double fitness;
		public double heuristic;
		
		public Node(StateObservation stateObs, Node parent, Types.ACTIONS action, Agent agent){
			this.stateObs = stateObs;
			this.parent = parent;
			this.action = action;
			
			Vector2d avatarPostion = stateObs.getAvatarPosition().mul(1.0 / stateObs.getBlockSize());
			agent.tiles[(int)avatarPostion.x][(int)avatarPostion.y] += 1;
			agent.totalMoves += 1;
			
			calculateFitness();
			calculateHeuristic(agent);
		}
		
		private void calculateHeuristic(Agent agent){
			if(stateObs.isGameOver()){
				if(stateObs.getGameWinner() == WINNER.PLAYER_WINS){
					heuristic = HUGE_VALUE;
					return;
				}
				else{
					heuristic = -HUGE_VALUE;
					return;
				}
			}
			
			Vector2d point = stateObs.getAvatarPosition().mul(1.0/stateObs.getBlockSize());
			agent.totalMoves += 1;
			agent.tiles[(int)point.x][(int)point.y] += 1;
			
			double value = (agent.totalMoves - agent.tiles[(int)point.x][(int)point.y]) / (agent.totalMoves * 1.0);
			heuristic = stateObs.getGameScore() + 0.5 * value;
		}
		
		private void calculateFitness(){
			if(stateObs.isGameOver()){
				if(stateObs.getGameWinner() == WINNER.PLAYER_WINS){
					fitness = HUGE_VALUE;
					return;
				}
				else{
					fitness = -HUGE_VALUE;
					return;
				}
			}
			
			fitness = stateObs.getGameScore();
		}
		
		public int getNumberOfEvents(){
			return stateObs.getEventsHistory().size();
		}
		
		public ArrayList<Types.ACTIONS> getSolution(){
			if(parent == null){
				return new ArrayList<Types.ACTIONS>();
			}
			
			ArrayList<Types.ACTIONS> solution= new ArrayList<Types.ACTIONS>();
			solution.addAll(parent.getSolution());
			solution.add(action);
			
			return solution;
		}

		@Override
		public int compareTo(Node n) {
			if(this.heuristic < n.heuristic){
				return 1;
			}
			if(this.heuristic > n.heuristic){
				return -1;
			}
			return 0;
		}
	}
}
