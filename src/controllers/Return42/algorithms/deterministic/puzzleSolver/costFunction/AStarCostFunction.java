package controllers.Return42.algorithms.deterministic.puzzleSolver.costFunction;

import core.game.StateObservation;

/**
 * Created by Oliver on 03.05.2015.
 */
public interface AStarCostFunction {

    double evaluate( StateObservation lastState, StateObservation newState );

}
