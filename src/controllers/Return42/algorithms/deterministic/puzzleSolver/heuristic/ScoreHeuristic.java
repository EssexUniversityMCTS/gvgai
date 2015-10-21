package controllers.Return42.algorithms.deterministic.puzzleSolver.heuristic;

import core.game.StateObservation;

/**
 * Created by Oliver on 05.05.2015.
 */
public class ScoreHeuristic implements AStarHeuristic {

    @Override
    public double evaluate( StateObservation oldState, StateObservation newState ) {
        return -newState.getGameScore();
    }
}
