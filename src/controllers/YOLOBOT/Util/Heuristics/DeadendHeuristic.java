package controllers.YOLOBOT.Util.Heuristics;

import controllers.YOLOBOT.YoloState;
import controllers.YOLOBOT.Util.Planner.KnowledgeBasedAStar;
import ontology.Types.ACTIONS;

public class DeadendHeuristic extends IHeuristic {
	
	private int[] fieldsReached;
	private int maxFieldsReached;
	
	@Override
	public double Evaluate(YoloState ys) {
		return 0;
	}

	@Override
	public HeuristicType GetType() {
		return HeuristicType.DeadendHeuristic;
	}

	@Override
	public double EvaluateWithoutNormalisation(YoloState ys) {
		return 0;
	}

	@Override
	public double GetAbsoluteMax() {
		return 1;
	}
	
	public void setFieldsReached(int[] count){
		maxFieldsReached = 0;
		fieldsReached = count;
		for (int i = 0; i < fieldsReached.length; i++) {
			if(fieldsReached[i] > maxFieldsReached){
				maxFieldsReached = fieldsReached[i];
			}
		}
	}

	public double Evaluate(ACTIONS action) {
		if(fieldsReached == null)
			return 0;
		switch (action) {
		case ACTION_DOWN:
			return fieldsReached[KnowledgeBasedAStar.BOTTOM]/(double)maxFieldsReached;
		case ACTION_UP:
			return fieldsReached[KnowledgeBasedAStar.TOP]/(double)maxFieldsReached;
		case ACTION_RIGHT:
			return fieldsReached[KnowledgeBasedAStar.RIGHT]/(double)maxFieldsReached;
		case ACTION_LEFT:
			return fieldsReached[KnowledgeBasedAStar.LEFT]/(double)maxFieldsReached;
		default:
			return 1;
		}
	}
}
