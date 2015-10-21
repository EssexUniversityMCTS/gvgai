package controllers.Return42.algorithms.deterministic.randomSearch.rollout.strategy;

import java.util.ArrayList;
import java.util.Random;

import ontology.Types.ACTIONS;
import core.game.StateObservation;

public class RandomRollOut implements RollOutStrategy {

	private Random random = new Random();
	

	@Override
	public ACTIONS getNextAction(StateObservation state) {
		ArrayList<ACTIONS> allActions = state.getAvailableActions();
		return allActions.get( random.nextInt( allActions.size() ) );
	}


	@Override
	public void beginnRollOutPhase( StateObservation startState ) {		
	}

}
