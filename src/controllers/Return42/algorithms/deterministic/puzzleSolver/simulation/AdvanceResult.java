package controllers.Return42.algorithms.deterministic.puzzleSolver.simulation;

import ontology.Types.ACTIONS;
import core.game.StateObservation;

public class AdvanceResult {
	private final StateObservation newState;
	private final ACTIONS[] actionsPerformed;
	
	public AdvanceResult(StateObservation newState,	ACTIONS[] actionsPerformed) {
		this.newState = newState;
		this.actionsPerformed = actionsPerformed;
	}
	
	public StateObservation getNewState() {
		return newState;
	}
	
	public ACTIONS[] getActionsPerformed() {
		return actionsPerformed;
	}
}
