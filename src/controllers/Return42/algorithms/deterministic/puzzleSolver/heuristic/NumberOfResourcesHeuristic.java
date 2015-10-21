package controllers.Return42.algorithms.deterministic.puzzleSolver.heuristic;

import controllers.Return42.util.StateObservationUtils;
import core.game.StateObservation;

public class NumberOfResourcesHeuristic implements AStarHeuristic {

	@Override
	public double evaluate(StateObservation oldState, StateObservation newState) {
		return StateObservationUtils.count( newState.getResourcesPositions() );
	}

}
