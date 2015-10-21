package controllers.Return42.algorithms.deterministic.randomSearch.depthControl;

/**
 * Created by Oliver on 27.04.2015.
 */
public interface DepthControl {

    public int getMaxIterationDepth();
    public void iterationFinished( boolean didFindPlan );

}
