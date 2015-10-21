package controllers.Return42.algorithms.deterministic.randomSearch.depthControl;

/**
 * Created by Oliver on 27.04.2015.
 */
public class RollingHorizon implements DepthControl {

    private static final int[] ITERATION_LEVELS = new int[] { 5, 20, 100 };
    private static final int NUM_BAD_ITERATIONS_UNTIL_LEVEL_INCREASE = 500;
    private static final int NUM_GOOD_ITERATIONS_UNITL_LEVEL_DECREASE = 2;

    private int currentLevelIndex = 0;
    private int goodIterationsInRow = 0;
    private  int badIterationsInRow = 0;

    @Override
    public int getMaxIterationDepth() {
        return ITERATION_LEVELS[currentLevelIndex];
    }

    @Override
    public void iterationFinished(boolean didFindPlan) {
        storeResult(didFindPlan);
        adjustIterationLevel();
    }

    private void storeResult(boolean didFindPlan) {
        if (didFindPlan) {
            badIterationsInRow = 0;
            goodIterationsInRow++;
        } else {
            goodIterationsInRow = 0;
            badIterationsInRow++;
        }
    }

    private void adjustIterationLevel() {
        if (goodIterationsInRow >= NUM_GOOD_ITERATIONS_UNITL_LEVEL_DECREASE) {
            goodIterationsInRow = 0;
            currentLevelIndex = Math.max( currentLevelIndex -1, 0 );
        }

        if (badIterationsInRow >= NUM_BAD_ITERATIONS_UNTIL_LEVEL_INCREASE) {
            badIterationsInRow = 0;
            currentLevelIndex = Math.min(currentLevelIndex + 1, ITERATION_LEVELS.length - 1);
        }

    }
}
