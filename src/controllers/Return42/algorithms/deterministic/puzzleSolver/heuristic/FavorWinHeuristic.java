package controllers.Return42.algorithms.deterministic.puzzleSolver.heuristic;

import core.game.StateObservation;
import ontology.Types;

public class FavorWinHeuristic implements AStarHeuristic {

    @Override
    public double evaluate(  StateObservation oldState, StateObservation newState ) {
        if ( newState.getGameWinner() == Types.WINNER.PLAYER_WINS)
            return 0;
        else 
        	return 2;
    }

}