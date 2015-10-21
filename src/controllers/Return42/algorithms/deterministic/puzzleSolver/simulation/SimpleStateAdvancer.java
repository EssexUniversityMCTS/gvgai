package controllers.Return42.algorithms.deterministic.puzzleSolver.simulation;

import ontology.Types.ACTIONS;
import core.game.StateObservation;

public class SimpleStateAdvancer implements AStarAdvanceFunction {

	@Override
	public AdvanceResult advance(StateObservation oldState,	StateObservation freeCopy, ACTIONS action) {
		freeCopy.advance(action);
		
		return new AdvanceResult( 
				freeCopy,
				new ACTIONS[] { action }
		);
	}

}
