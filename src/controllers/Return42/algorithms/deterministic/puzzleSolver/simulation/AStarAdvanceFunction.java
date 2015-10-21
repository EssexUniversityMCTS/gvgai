package controllers.Return42.algorithms.deterministic.puzzleSolver.simulation;

import ontology.Types.ACTIONS;
import core.game.StateObservation;

public interface AStarAdvanceFunction {
	
	public AdvanceResult advance( StateObservation oldState, StateObservation freeCopy, ACTIONS action );
	
}
