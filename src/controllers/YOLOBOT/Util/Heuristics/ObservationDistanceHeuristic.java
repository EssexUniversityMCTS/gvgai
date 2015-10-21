package controllers.YOLOBOT.Util.Heuristics;

import controllers.YOLOBOT.YoloState;
import core.game.Observation;

public class ObservationDistanceHeuristic extends IHeuristic {

	
	Observation observation1 = null;
	Observation observation2 = null;
	public static double max = 0;
	
	public ObservationDistanceHeuristic(Observation o1, Observation o2) {
		observation1 = o1;
		observation2 = o2;
	}
	
	@Override
	public HeuristicType GetType() {
		return HeuristicType.ObservationDistanceHeuristic;
	}

	@Override
	public double Evaluate(YoloState ys) {
		return EvaluateWithoutNormalisation(ys)/max;
		}

	@Override
	public double EvaluateWithoutNormalisation(YoloState ys) {
		double tmp = Math.sqrt(Math.pow((observation1.position.x - observation2.position.x),2) + Math.pow((observation1.position.y - observation2.position.y),2));
		if(max < tmp) max = tmp;
		return tmp;
	}

	@Override
	public double GetAbsoluteMax() {
		return max;
	}
}
