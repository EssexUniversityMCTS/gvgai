package controllers.YOLOBOT.Util.Heuristics;

import controllers.YOLOBOT.YoloState;
import controllers.YOLOBOT.Util.Wissensdatenbank.YoloKnowledge;

public class ScoreHeuristic extends IHeuristic {

	public static double max = Double.MIN_VALUE;
	
	@Override
	public double Evaluate(YoloState ys) {
		return EvaluateWithoutNormalisation(ys)/max;

	}

	@Override
	public HeuristicType GetType() {
		return HeuristicType.ScoreHeuristic;
	}

	@Override
	public double EvaluateWithoutNormalisation(YoloState ys) {
		if(ys != null) {
			double deltaScore = ys.getGameScore() - YoloState.currentGameScore;
			
			if(deltaScore<0 && !YoloKnowledge.instance.isMinusScoreBad())
				deltaScore = 0;
			
			if(max < Math.abs(deltaScore)) {
				max = Math.abs(deltaScore);				
			}
			
			return deltaScore;
		}
		return 0;
	}

	@Override
	public double GetAbsoluteMax() {
		return max;
	}
}
