package controllers.Return42.util;

import controllers.Return42.hashing.ObservationHasher;
import core.game.StateObservation;
import ontology.Types;

/**
 * Utility class to check, if a move has "no" consequences.
 * Consequences are:
 *  - change in score
 *  - change in winning (win/lose)
 *  - change in movable objects.
 */
public class NilMoveChecker {

    public static boolean advanceStateAndCheckIfIsNilMove( StateObservation state, Types.ACTIONS action ) {
        double scoreBefore = state.getGameScore();
        int movableObjectStateBefore = ObservationHasher.hash( state.getMovablePositions() );

        state.advance( action );

        if (state.isGameOver())
            return false;

        double scoreAfter = state.getGameScore();
        if (scoreAfter != scoreBefore)
            return false;

        int movableObjectStateAfter = ObservationHasher.hash(state.getMovablePositions());
        if (movableObjectStateAfter != movableObjectStateBefore)
            return false;

        return true;
    }

}
