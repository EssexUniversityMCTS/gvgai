package controllers.Return42.algorithms.deterministic.puzzleSolver.costFunction;

import core.game.StateObservation;

/**
 * Created by Oliver on 05.05.2015.
 */
public class HeatmapCostFunction implements AStarCostFunction {

    private final int[][] heatmap;

    public HeatmapCostFunction( int width, int height ) {
        heatmap = new int[width][height];
    }

    @Override
    public double evaluate(StateObservation lastState, StateObservation newState) {
        int x = (int) (newState.getAvatarPosition().x / newState.getBlockSize());
        int y = (int) (newState.getAvatarPosition().y / newState.getBlockSize());

        int visits = heatmap[x][y];
        heatmap[x][y] = visits +1;

        return visits * 0.1;
    }

}
