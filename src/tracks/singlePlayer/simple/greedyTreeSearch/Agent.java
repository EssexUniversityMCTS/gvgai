package tracks.singlePlayer.simple.greedyTreeSearch;

import java.util.Random;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

public class Agent extends AbstractPlayer {
	
	public static Random random;
	public static ACTIONS[] actions;
	public static int MAX_DEPTH;
	
	public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer) {
		random = new Random();
		actions = so.getAvailableActions().toArray(new ACTIONS[0]);
		MAX_DEPTH = 10;
	}
	
	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		
		double worstCase = 10;
		double avgTime = 10;
		double totalTime = 0;
		double iteration = 0;
		int bestAction = -1;
		TreeNode root = new TreeNode(stateObs, null);
		
		while(elapsedTimer.remainingTimeMillis() > 2 * avgTime && elapsedTimer.remainingTimeMillis() > worstCase){
			ElapsedCpuTimer temp = new ElapsedCpuTimer();
			//treeSelect
			TreeNode node = root.SelectNode();
			
			//Simulate
			double value = node.ExploreNode();
			
			//RollBack
			node.UpdateNode(value);
			
			//Get the best action
			bestAction = root.GetBestChild();
			
			totalTime += temp.elapsedMillis();
			iteration += 1;
			avgTime = totalTime / iteration;
		}
		
		if(bestAction == -1){
			System.out.println("Out of time choosing random action");
			bestAction = random.nextInt(actions.length);
		}
		
		return actions[bestAction];
	}

}
