package controllers.Return42.algorithms.deterministic.randomSearch.rollout.strategy;

import core.game.StateObservation;
import ontology.Types.ACTIONS;

public interface RollOutStrategy {

	void beginnRollOutPhase(StateObservation startState);

	ACTIONS getNextAction(StateObservation state);

}
