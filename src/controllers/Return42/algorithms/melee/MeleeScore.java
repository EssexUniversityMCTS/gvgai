package controllers.Return42.algorithms.melee;

import java.util.List;

import controllers.Return42.util.StateObservationUtils;
import ontology.Types.WINNER;
import core.game.Observation;
import core.game.StateObservation;

public class MeleeScore {

	public static double evaluate(StateObservation startState, StateObservation finalState ) {
		if (finalState.isGameOver() && finalState.getGameWinner() == WINNER.PLAYER_LOSES )
			return -1;

		double score = 0;
		
		if (finalState.isGameOver() && finalState.getGameWinner() == WINNER.PLAYER_WINS )
			score += 1;
	
		if (finalState.getGameScore() > startState.getGameScore() )
			score += 0.5;
		
		int startRes = StateObservationUtils.count( startState.getAvatarResources() );
		int finalRes = StateObservationUtils.count( finalState.getAvatarResources() );
		score += (finalRes - startRes);
		
		List<Observation> startNpcs = StateObservationUtils.flatten( startState.getNPCPositions() );
		List<Observation> finalNpcs = StateObservationUtils.flatten( finalState.getNPCPositions() );
		if ( finalNpcs.size() < startNpcs.size() )
			score += 0.7;
		
		return score;
	}
	
}
