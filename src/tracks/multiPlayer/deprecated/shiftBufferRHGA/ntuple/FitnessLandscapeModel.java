package tracks.multiPlayer.deprecated.shiftBufferRHGA.ntuple;

/**
 * Created by sml on 19/01/2017.
 */

public interface FitnessLandscapeModel {
    void addPoint(int[] p, double value);

    // careful - this can be slow - it iterates over all points in the search space!
    int[] getBestSolution();

    int[] getBestOfSampled();

    int[] getBestOfSampledPlusNeighbours(int nNeighbours);

    // return a Double object - a null return indicates that
    // we know nothing yet;
    Double getSimple(int[] x);
}


