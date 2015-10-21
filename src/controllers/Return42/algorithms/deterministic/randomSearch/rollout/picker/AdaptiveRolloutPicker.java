package controllers.Return42.algorithms.deterministic.randomSearch.rollout.picker;

import controllers.Return42.algorithms.deterministic.randomSearch.rollout.strategy.GuidedRollout;
import controllers.Return42.algorithms.deterministic.randomSearch.rollout.strategy.RollOutStrategy;
import controllers.Return42.algorithms.deterministic.randomSearch.rollout.strategy.SoftMaxHeuristicRollOut;
import controllers.Return42.heuristics.action.HeightMapWithUseAndNilHeuristic;
import controllers.Return42.knowledgebase.KnowledgeBase;

public class AdaptiveRolloutPicker implements RolloutPicker {

	private static final int ITERATIONS_UNTIL_SCORE_IS_USED = 50;
	private final RollOutStrategy random;
	private final RollOutStrategy heightmap;
	
	private int iterationsSincePlan;
	private RollOutStrategy currentStrategy;
	
	
	public AdaptiveRolloutPicker( KnowledgeBase knowledge ) {
		random = new GuidedRollout( knowledge );
		heightmap = new SoftMaxHeuristicRollOut( new HeightMapWithUseAndNilHeuristic(knowledge), 0.03 );
		currentStrategy = random;
	}
	
	public void iterationFinished( boolean didFindPlan ) {
		if (didFindPlan) {
			iterationsSincePlan = 0;
			currentStrategy = random;
		} else {
			iterationsSincePlan++;
			
			if (iterationsSincePlan > ITERATIONS_UNTIL_SCORE_IS_USED) {
				currentStrategy = (iterationsSincePlan % 2 == 0) ? random : heightmap;
			}
		}
	}
	
	public RollOutStrategy getCurrentRolloutStrategy() {
		return currentStrategy;
	}
	
}
