package controllers.Return42.algorithms.deterministic.puzzleSolver.heuristic;

import core.game.Observation;
import core.game.StateObservation;

import java.util.List;

/**
 * Created by Oliver on 03.05.2015.
 */
public class NumberOfImmovablesOfType implements AStarHeuristic {

    private final int itypeOfImmovable;

    public NumberOfImmovablesOfType( int itypeOfImmovable ) {
        this.itypeOfImmovable = itypeOfImmovable;
    }

    @Override
    public double evaluate( StateObservation oldState, StateObservation newState ) {
        List<Observation>[] movables = newState.getImmovablePositions( newState.getAvatarPosition());
        if (movables == null)
            return 0;

        for( List<Observation> observationTypes: movables ) {
            for( Observation observation: observationTypes ) {
                if (observation.itype == itypeOfImmovable) {
                    return observationTypes.size();
                }
            }
        }

        return 0;
    }

}
