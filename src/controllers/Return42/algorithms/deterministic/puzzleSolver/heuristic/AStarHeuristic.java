package controllers.Return42.algorithms.deterministic.puzzleSolver.heuristic;

import core.game.StateObservation;

/**
 * Created by Oliver on 03.05.2015.
 */
public interface AStarHeuristic {

    double evaluate( StateObservation oldState, StateObservation newState );

}
