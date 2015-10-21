package controllers.Return42.algorithms.deterministic.puzzleSolver.costFunction;

import core.game.StateObservation;

/**
 * Created by Oliver on 05.05.2015.
 */
public class ConstantCostFunction implements AStarCostFunction {

	private final double cost;
	
    public ConstantCostFunction(double cost) {
    	this.cost = cost;
	}

	@Override
    public double evaluate(StateObservation lastState, StateObservation newState) {
      	return cost;
    }

}
