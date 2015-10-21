package controllers.YOLOBOT.Util.Heuristics;

import controllers.YOLOBOT.Agent;
import controllers.YOLOBOT.YoloState;
import controllers.YOLOBOT.SubAgents.HandleMCTS.MCTHandler;
import ontology.Types.WINNER;

public class SimulateDepthHeuristic extends IHeuristic{
	
	private static final double BAD_VALUE = 50000;
	private double curMax = -Double.MAX_VALUE; 
	
	@Override
	public HeuristicType GetType() {
		// TODO Auto-generated method stub
		return HeuristicType.SimulateDepthHeuristic;
	}

	@Override
	public double Evaluate(YoloState ys) {
		// TODO Auto-generated method stub
		return EvaluateWithoutNormalisation(ys)/curMax;
	}

	@Override
	public double EvaluateWithoutNormalisation(YoloState ys) {
		// TODO Auto-generated method stub
		double newDepth = ys.getTargetReachedDepth();
		if(newDepth == 0 && ys.isGameOver() && ys.getGameWinner() != WINNER.PLAYER_WINS){
			//Didnt Simulate at all --> Bad value!
			newDepth = BAD_VALUE;
		}
		if(newDepth > curMax){
			curMax = newDepth;
		}
		return -newDepth;
	}

	@Override
	public double GetAbsoluteMax() {
		// TODO Auto-generated method stub
		return MCTHandler.ROLLOUT_DEPTH;
	}

}
