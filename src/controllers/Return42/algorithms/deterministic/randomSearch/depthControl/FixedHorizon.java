package controllers.Return42.algorithms.deterministic.randomSearch.depthControl;

/**
 * Created by Oliver on 27.04.2015.
 */
public class FixedHorizon implements DepthControl {

    @Override
    public int getMaxIterationDepth() {
        return 40;
    }

    @Override
    public void iterationFinished(boolean didFindPlan) {
    }
}
