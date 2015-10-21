package controllers.Return42.algorithms.deterministic.puzzleSolver.heuristic;

import core.game.Observation;
import core.game.StateObservation;

import java.util.List;

import controllers.Return42.util.DistanceUtils;

/**
 * Created by Oliver on 03.05.2015.
 */
public class DistanceToNextMovableHeuristic implements AStarHeuristic {

    private final int itypeOfMovable;

    public DistanceToNextMovableHeuristic( int itypeOfMovable ) {
        this.itypeOfMovable = itypeOfMovable;
    }

    @Override
    public double evaluate( StateObservation oldState, StateObservation newState ) {
        List<Observation>[] immovables = newState.getMovablePositions( newState.getAvatarPosition() );
        
        if (immovables == null)
            return 0;

        for( List<Observation> observationTypes: immovables ) {
            for( Observation observation: observationTypes ) {
                if (observation.itype == itypeOfMovable) {
                    return DistanceUtils.manhattanDistance( observation.position, newState.getAvatarPosition(), newState.getBlockSize());
                }
            }
        }

        return 0;
    }

}
