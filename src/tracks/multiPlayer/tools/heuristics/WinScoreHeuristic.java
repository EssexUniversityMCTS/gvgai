package tracks.multiPlayer.tools.heuristics;

import core.game.StateObservationMulti;
import ontology.Types;

/**
 * Created with IntelliJ IDEA.
 * User: ssamot
 * Date: 11/02/14
 * Time: 15:44
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class WinScoreHeuristic extends StateHeuristicMulti {

    private static final double HUGE_NEGATIVE = -1000.0;
    private static final double HUGE_POSITIVE =  1000.0;

    double initialNpcCounter = 0;

    public WinScoreHeuristic(StateObservationMulti stateObs) {

    }

    public double evaluateState(StateObservationMulti stateObs, int playerID) {
        boolean gameOver = stateObs.isGameOver();
        Types.WINNER win = stateObs.getMultiGameWinner()[playerID];
        Types.WINNER oppWin = stateObs.getMultiGameWinner()[(playerID + 1) % stateObs.getNoPlayers()];
        double rawScore = stateObs.getGameScore(playerID);

        if(gameOver && (win == Types.WINNER.PLAYER_LOSES || oppWin == Types.WINNER.PLAYER_WINS))
            return HUGE_NEGATIVE;

        if(gameOver && (win == Types.WINNER.PLAYER_WINS || oppWin == Types.WINNER.PLAYER_LOSES))
            return HUGE_POSITIVE;

        return rawScore;
    }


}


