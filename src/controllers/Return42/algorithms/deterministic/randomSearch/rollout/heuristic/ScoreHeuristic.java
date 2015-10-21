package controllers.Return42.algorithms.deterministic.randomSearch.rollout.heuristic;

import controllers.Return42.util.StateObservationUtils;
import core.game.StateObservation;
import ontology.Types.WINNER;

public class ScoreHeuristic implements RolloutHeuristic {

	private int inventoryItemsBefore;
	
	@Override
	public void preStep(StateObservation state) {
		inventoryItemsBefore = StateObservationUtils.count( state.getAvatarResources() );
	}

	@Override
	public double postStep(StateObservation state) {
		double rawScore = state.getGameScore();
		int inventoryNow = StateObservationUtils.count( state.getAvatarResources() );
		
		if (state.isGameOver() && state.getGameWinner() == WINNER.PLAYER_LOSES)
			rawScore = -1000;
        else if (state.isGameOver() && state.getGameWinner() == WINNER.PLAYER_WINS)
            rawScore *= 10; //winning plans should only be overwritten by other winning plans. otherwise hunting meaningless points e.g. painter
		
		if (inventoryNow > inventoryItemsBefore )
			rawScore += 1;
		
        return rawScore;
	}

}
