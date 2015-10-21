package controllers.Return42.algorithms.deterministic.randomSearch.rollout.heuristic;

import ontology.Types.WINNER;
import tools.Vector2d;
import controllers.Return42.knowledgebase.GameInformation;
import controllers.Return42.knowledgebase.KnowledgeBase;
import controllers.Return42.knowledgebase.ScoreHeightMapGenerator;
import controllers.Return42.util.StateObservationUtils;
import core.game.StateObservation;

public class GameScoreAndHeightMapHeuristic implements RolloutHeuristic {

	private ScoreHeightMapGenerator scoreHeightMapGenerator;
	private GameInformation gameInformation;
	private int inventoryItemsBefore;

	private final static int GAME_OVER_SCORE = 100000;

	public GameScoreAndHeightMapHeuristic( KnowledgeBase knowledge ) {
		this.scoreHeightMapGenerator = knowledge.getScoreHeightMapGenerator();
		this.gameInformation = knowledge.getGameInformation();
	}

	@Override
	public void preStep(StateObservation state) {
		inventoryItemsBefore = StateObservationUtils.count( state.getAvatarResources() );
	}

	@Override
	public double postStep(StateObservation state) {
		double[][] heightMap = scoreHeightMapGenerator.getHeightMap(state);
		Vector2d gridPosition = gameInformation.gamePositionToGridPosition(state.getAvatarPosition());

		Double heightMapScore = heightMap[(int) gridPosition.x][(int) gridPosition.y];

		double gameScore = state.getGameScore();
		double finalScore = 0;

		if (state.isGameOver() && state.getGameWinner() == WINNER.PLAYER_LOSES)
			finalScore = -GAME_OVER_SCORE;
		else if (state.isGameOver() && state.getGameWinner() == WINNER.PLAYER_WINS)
			// winning plans should only be overwritten by other winning plans.
			// otherwise hunting meaningless points e.g. painter
			finalScore = GAME_OVER_SCORE + gameScore;
		else {
			int inventoryNow = StateObservationUtils.count( state.getAvatarResources() );
			
			finalScore = gameScore * 1000;
			finalScore += (inventoryNow - inventoryItemsBefore) * 50;
			finalScore += heightMapScore;
		}

		return finalScore;
	}

}
