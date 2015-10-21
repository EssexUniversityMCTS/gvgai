package controllers.YOLOBOT.Util.Heuristics;

import controllers.YOLOBOT.YoloState;
import controllers.YOLOBOT.Util.Heatmap;

public class SectorHeatMapHeuristic extends IHeuristic {
	
	@Override
	public HeuristicType GetType() {
		return HeuristicType.SectorHeatMapHeuristic;
	}

	@Override
	public double Evaluate(YoloState ys) {
		return Math.max(-1, EvaluateWithoutNormalisation(ys) / Heatmap.instance.getMaximumAbstractHeatValue());
	}

	@Override
	public double EvaluateWithoutNormalisation(YoloState ys) {
		int agentX = ys.getAvatarX();
		int agentY = ys.getAvatarY();
		double value = 0;
		if (agentX >= 0 && agentY >= 0) {
			value = (double) Heatmap.instance.getAbstractHeatValue(agentX, agentY);
		}
		return -value;
		
	}

	@Override
	public double GetAbsoluteMax() {
		return Math.max(Double.MIN_VALUE, Heatmap.instance.getMaximumAbstractHeatValue());
	}
}