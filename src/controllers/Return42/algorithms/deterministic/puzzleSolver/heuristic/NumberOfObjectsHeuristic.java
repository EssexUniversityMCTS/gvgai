package controllers.Return42.algorithms.deterministic.puzzleSolver.heuristic;

import core.game.Observation;
import core.game.StateObservation;

import java.util.ArrayList;

import controllers.Return42.util.StateObservationUtils;

/**
 * Created by Oliver on 05.05.2015.
 */
public class NumberOfObjectsHeuristic implements AStarHeuristic {

    @Override
    public double evaluate( StateObservation oldState, StateObservation newState ) {
        int movables = countElements( newState.getMovablePositions() );
        int statics = countElements( newState.getImmovablePositions() );

        return movables + statics;
    }

    private int countElements(ArrayList<Observation>[] observations) {
        return StateObservationUtils.flatten( observations ).size();
    }
}
