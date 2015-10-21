package controllers.YOLOBOT.Util.Heuristics;

import controllers.YOLOBOT.YoloState;

public class InventoryCountHeuristic extends IHeuristic {

	public static double max = 0;
	
	@Override
	public HeuristicType GetType() {
		return HeuristicType.InventoryCountHeuristic;
	}

	@Override
	public double Evaluate(YoloState ys) {
		return EvaluateWithoutNormalisation(ys) /max;
	}

	@Override
	public double EvaluateWithoutNormalisation(YoloState ys) {
		if(ys == null) return 0;
		
		double tmp = Math.abs(ys.getInventoryArray().length);
		if(max < tmp) max = tmp;
		
		return ys.getInventoryArray().length;
	}

	@Override
	public double GetAbsoluteMax() {
		return max;
	}

	

}
