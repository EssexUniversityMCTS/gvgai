package controllers.Return42.algorithms.deterministic.puzzleSolver.heuristic;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import core.game.StateObservation;

/**
 * Created by Oliver on 03.05.2015.
 */
public class CombinedHeuristic implements AStarHeuristic {

    private final List<AStarHeuristic> heuristics;

    public CombinedHeuristic( AStarHeuristic... heuristics ) {
        this.heuristics = new LinkedList<>( Arrays.asList( heuristics ) );
    }

    @Override
    public double evaluate( StateObservation oldState, StateObservation newState ) {
        double total = 0;

        for( AStarHeuristic heuristic: heuristics) {
            total += heuristic.evaluate( oldState, newState );
        }

        return total;
    }

	public void addHeuristic( AStarHeuristic heuristic) {
		heuristics.add( heuristic );
	}
}
