package controllers.sampleUCT;

import java.util.ArrayList;
import java.util.Random;

import controllers.Heuristics.WinScoreHeuristic;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;

public class Agent extends AbstractPlayer{
	public static ArrayList<Types.ACTIONS> actions;
	public static Random random;
	
	public int maxDepth;
	public Node root;
	
	public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer){
		actions = stateObs.getAvailableActions();
		maxDepth = 10;
		random = new Random();
	}
	
	private void simulateNode(Node node){
		StateObservation state = node.state.copy();
		for(int i=0;i<maxDepth;i++){
			if(state.isGameOver()){
				break;
			}
			state.advance(actions.get(random.nextInt(actions.size())));
		}
		WinScoreHeuristic heuristic = new WinScoreHeuristic(state);
		node.updateStats(heuristic.evaluateState(state));
	}
	
	private Node selectNode(Node node){
		Node curNode = null;
		int index = node.checkChildren();
		if(index == -1){
			int maxIndex = -1;
			for(int i=0; i<node.children.length; i++){
				if(maxIndex == -1 || node.children[i].getUCTValue() > node.children[maxIndex].getUCTValue()){
					maxIndex = i;
				}
			}
			if(node.children[maxIndex].state.isGameOver()){
				curNode = node.children[maxIndex];
			}
			else{
				curNode = selectNode(node.children[maxIndex]);
			}
		}
		else{
			Types.ACTIONS act = actions.get(index);
			StateObservation state = node.state.copy();
			state.advance(act);
			curNode = new Node(node, state);
			node.children[index] = curNode;
		}
		return curNode;
	}
	
	public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer){
		float avgTime = 10;
		float worstTime = 10;
		float totalTime = 0;
		int numberOfIterations = 0;
		
		root = new Node(null, stateObs);
		
		while(elapsedTimer.remainingTimeMillis() > 2 * avgTime 
				&& elapsedTimer.remainingTimeMillis() > worstTime){
			ElapsedCpuTimer methodTime = new ElapsedCpuTimer();
			
			Node node = selectNode(root);
			simulateNode(node);
			
			numberOfIterations += 1;
			totalTime += methodTime.elapsedMillis();
			avgTime = totalTime / numberOfIterations;
		}
		
		int maxVisited = -1;
		for(int i=0;i<root.children.length;i++){
			if(root.children[i] != null && (maxVisited == -1 || 
					root.children[i].numberOfVisits > root.children[maxVisited].numberOfVisits)){
				maxVisited = i;
			}
		}
		
		if(maxVisited >= 0){
			return actions.get(maxVisited);
		}
		
		return actions.get(random.nextInt(actions.size()));
	}
}
