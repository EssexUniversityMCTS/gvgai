package controllers.Return42.algorithms.deterministic.randomSearch.rollout.heuristic;

import core.game.StateObservation;

public interface RolloutHeuristic {
	public void preStep( StateObservation state );
	public double postStep( StateObservation state );
}