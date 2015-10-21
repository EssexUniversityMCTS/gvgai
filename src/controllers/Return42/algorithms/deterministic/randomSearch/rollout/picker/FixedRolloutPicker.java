package controllers.Return42.algorithms.deterministic.randomSearch.rollout.picker;

import controllers.Return42.algorithms.deterministic.randomSearch.rollout.strategy.RollOutStrategy;

public class FixedRolloutPicker implements RolloutPicker {

	private final RollOutStrategy strategy;
	
	public FixedRolloutPicker(RollOutStrategy strategy) {
		this.strategy = strategy;
	}

	@Override
	public void iterationFinished(boolean didFindPlan) {
	}

	@Override
	public RollOutStrategy getCurrentRolloutStrategy() {
		return strategy;
	}

}
