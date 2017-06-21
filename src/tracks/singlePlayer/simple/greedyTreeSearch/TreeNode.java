package tracks.singlePlayer.simple.greedyTreeSearch;

import core.game.StateObservation;
import ontology.Types.WINNER;

public class TreeNode {
	public double HUGE_NUMBER = 1000000.0;
	
	private TreeNode parent;
	private TreeNode[] children;
	private StateObservation state;
	private double value;
	private int depth;
	
	public TreeNode(StateObservation state, TreeNode parent) {
		this.parent = parent;
		this.state = state;
		if(parent != null){
			this.depth = parent.depth + 1;
		}
		else{
			this.depth = 0;
		}
		this.children = new TreeNode[Agent.actions.length];
		this.value = 0;
	}
	
	public TreeNode SelectNode(){
		TreeNode current = this;
		while(!current.state.isGameOver() && current.depth < Agent.MAX_DEPTH){
			int index = current.GetUnexplored();
			if(index < 0){
				current = current.children[GetBestChild()];
			}
			else{
				StateObservation newState = current.state.copy();
				newState.advance(Agent.actions[index]);
				current.children[index] = new TreeNode(newState, current);
				return current.children[index];
			}	
		}
		
		return current;
	}
	
	public double ExploreNode(){
		StateObservation future = state.copy();
		int depth = this.depth;
		while(depth < Agent.MAX_DEPTH && !future.isGameOver())
		{
			future.advance(Agent.actions[GetRandomActionIndex()]);
			depth+=1;
		}
		
		return EvaluateState(future);
	}
	
	public void UpdateNode(double value){
		TreeNode current = this;
		while(current != null){
			current.value += value;
			current = current.parent;
		}
	}
	
	private double EvaluateState(StateObservation state){
		if(state.isGameOver()){
			if(state.getGameWinner() == WINNER.PLAYER_WINS){
				return HUGE_NUMBER;
			}
			else if(state.getGameWinner() == WINNER.PLAYER_LOSES){
				return -HUGE_NUMBER;
			}
			else{
				return state.getGameScore() / 2;
			}
		}
		return state.getGameScore();
	}
	
	private int GetRandomActionIndex(){
		return Agent.random.nextInt(Agent.actions.length);
	}
	
	private int GetUnexplored(){
		for(int i=0;i<Agent.actions.length;i++){
			if(children[i] == null){
				return i;
			}
		}
		
		return -1;
	}
	
	public int GetBestChild(){
		int bestIndex = -1;
		double bestValue = -Double.MAX_VALUE;
		for(int i=0;i<children.length;i++){
			if(children[i] != null && children[i].value > bestValue){
				bestValue = children[i].value;
				bestIndex = i;
			}
		}
		
		return bestIndex;
	}
}
