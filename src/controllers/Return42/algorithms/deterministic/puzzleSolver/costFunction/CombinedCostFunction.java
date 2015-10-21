package controllers.Return42.algorithms.deterministic.puzzleSolver.costFunction;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import core.game.StateObservation;

/**
 * Created by Oliver on 03.05.2015.
 */
public class CombinedCostFunction implements AStarCostFunction {

    private final List<AStarCostFunction> costFunctions;

    public CombinedCostFunction(AStarCostFunction... costFunctions) {
        this.costFunctions = new LinkedList<>( Arrays.asList( costFunctions ) );
    }

    @Override
    public double evaluate( StateObservation lastState, StateObservation newState ) {
        double total = 0;

        for(AStarCostFunction costFunction: costFunctions ) {
            total += costFunction.evaluate( lastState, newState );
        }

        return total;
    }

	public void addCostFunction( AStarCostFunction costFunction ) {
		costFunctions.add( costFunction );
	}

}
