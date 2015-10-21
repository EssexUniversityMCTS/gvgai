package controllers.sampleUCT;

import java.util.ArrayList;

import core.game.StateObservation;
import ontology.Types;
import ontology.Types.WINNER;
import tools.Utils;

public class Node{
	public final double constant = Math.sqrt(2);
	public final double epsilon = 1e-6;
	
	public Node parent;
	public Node[] children;
	public StateObservation state;
	public double numberOfVisits;
	public double totalScore;
	
	public Node(Node parent, StateObservation state){
		this.parent = parent;
		this.state = state;
		this.children = new Node[Agent.actions.size()];
		this.numberOfVisits = 0;
		this.totalScore = 0;
	}
	
	public double parentNumberOfVisits(){
		if(parent == null){
			return 0;
		}
		
		return parent.numberOfVisits;
	}
	
	public void updateStats(double score){
		numberOfVisits += 1;
		totalScore += score;
		if(parent != null){
			parent.updateStats(score);
		}
	}
	
	public int checkChildren(){
		for(int i=0;i<children.length;i++){
			if(children[i] == null){
				return i;
			}
		}
		
		return -1;
	}
	
	public Node getHighestScore(){
		int maxVisited = -1;
		for(int i=0;i<children.length;i++){
			if(children[i] != null && (maxVisited == -1 || 
					this.children[i].getAverageScore() > this.children[maxVisited].getAverageScore())){
				maxVisited = i;
			}
		}
		
		if(maxVisited == -1){
			return this;
		}
		
		return children[maxVisited].getHighestScore();
	}
	
	public Node getMostVisited(){
		int maxVisited = -1;
		for(int i=0;i<children.length;i++){
			if(children[i] != null && (maxVisited == -1 || 
					this.children[i].numberOfVisits > this.children[maxVisited].numberOfVisits)){
				maxVisited = i;
			}
		}
		
		if(maxVisited == -1){
			return this;
		}
		
		return children[maxVisited].getMostVisited();
	}
	
	public double getUCTValue(){
		double value = totalScore / (numberOfVisits + epsilon) + 2 * constant * Math.sqrt(2 * Math.log(parentNumberOfVisits() + 1) / (numberOfVisits + epsilon));
		value = Utils.noise(value, this.epsilon, Agent.random.nextDouble());
		return value;
	}
	
	public ArrayList<Types.ACTIONS> getSolution(){
		if(this.parent == null){
			return new ArrayList<Types.ACTIONS>();
		}
		
		Types.ACTIONS action = null;
		for(int i=0; i<this.parent.children.length; i++){
			if(this.parent.children[i] == this){
				action = Agent.actions.get(i);
				break;
			}
		}
		
		ArrayList<Types.ACTIONS> list = this.parent.getSolution();
		list.add(action);
		return list;
	}
	
	public double getAverageScore(){
		return state.getGameScore();
	}
	
	public WINNER getWins(){
		return state.getGameWinner();
	}
	
	public double getEvents(){
		return state.getEventsHistory().size();
	}
	
	public double getNumChildren(){
		int totalChildren = 0;
		for(int i=0; i<children.length; i++){
			if(children[i] != null){
				totalChildren += children[i].getNumChildren();
			}
		}
		
		return totalChildren + 1;
	}
}
