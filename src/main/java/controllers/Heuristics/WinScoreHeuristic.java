package controllers.Heuristics;

import core.game.StateObservation;
import ontology.Types;

/**
 * Created with IntelliJ IDEA. User: ssamot Date: 11/02/14 Time: 15:44 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class WinScoreHeuristic implements StateHeuristic {

	private static final double HUGE_NEGATIVE = -10000000.0;
	private static final double HUGE_POSITIVE = 10000000.0;

	double initialNpcCounter;

	public WinScoreHeuristic(StateObservation stateObs) {

	}

	@Override
	public double evaluateState(StateObservation stateObs) {
		boolean gameOver = stateObs.isGameOver();
		Types.WINNER win = stateObs.getGameWinner();
		double rawScore = stateObs.getGameScore();

		if (gameOver && Types.WINNER.PLAYER_LOSES == win)
			return WinScoreHeuristic.HUGE_NEGATIVE;

		if (gameOver && Types.WINNER.PLAYER_WINS == win)
			return WinScoreHeuristic.HUGE_POSITIVE;

		return rawScore;
	}

}
