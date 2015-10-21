package controllers.Return42.algorithms.deterministic.puzzleSolver.heuristic;

import core.game.Observation;
import core.game.StateObservation;

import java.util.LinkedList;
import java.util.List;

import controllers.Return42.util.DistanceUtils;
import controllers.Return42.util.StateObservationUtils;

/**
 * Created by Oliver on 03.05.2015.
 */
public class DistanceBetweenMovablesAndSinksHeuristic implements AStarHeuristic {

    private final int itypeOfMovable;
    private final int itypeOfSink;
    private final boolean areSinksExclusive;

    public DistanceBetweenMovablesAndSinksHeuristic(int itypeOfMovable, int itypeOfSink, boolean areSinksExclusive) {
        this.itypeOfMovable = itypeOfMovable;
        this.itypeOfSink = itypeOfSink;
        this.areSinksExclusive = areSinksExclusive;
    }

    @Override
    public double evaluate( StateObservation oldState, StateObservation newState ) {
        List<Observation> movables = StateObservationUtils.getMovablesOfType( newState, itypeOfMovable );
        List<Observation> sinks = StateObservationUtils.getImmovablesOfType( newState, itypeOfSink);

        double dist = calcDistBetweenMovablesAndSinks( movables, sinks, newState.getBlockSize() );
        return dist;
    }

    private double calcDistBetweenMovablesAndSinks( List<Observation> movables, List<Observation> sinks, int blocksize ) {
        List<Observation> remainingSinks = new LinkedList<>( sinks );
        double totalDist = 0;

        for( Observation movable: movables ) {
            if (remainingSinks.isEmpty())
                break;

            Observation closestSink = StateObservationUtils.selectClosest( remainingSinks, movable.position );
            double distance = DistanceUtils.manhattanDistance( closestSink.position, movable.position, blocksize );
            totalDist += distance;

            if (areSinksExclusive) {
                remainingSinks.remove( closestSink );
            }
        }

        return totalDist;
    }
}
