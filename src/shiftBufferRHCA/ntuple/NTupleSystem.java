package shiftBufferRHCA.ntuple;

import shiftBufferRHCA.evodef.SearchSpace;
import shiftBufferRHCA.evodef.SearchSpaceUtil;
import shiftBufferRHCA.tools.Picker;
import tools.StatSummary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by simonmarklucas on 13/11/2016.
 * <p>
 * Also need to consider how to handle a larger number of picks
 * - though this may not be so important ...
 */
public class NTupleSystem implements FitnessLandscapeModel {


    static int minTupleSize = 1;

    List<int[]> sampledPoints;


    SearchSpace searchSpace;
    ArrayList<NTuple> tuples;
    UrnSystem urnSystem;

    public NTupleSystem(SearchSpace searchSpace) {
        this.searchSpace = searchSpace;
        tuples = new ArrayList<>();
        sampledPoints = new ArrayList<>();
    }

    // currently just comment / uncomment the following to decide which
    // n-tuples to add
    public NTupleSystem addTuples() {
        add1Tuples();
        // add2Tuples();
        // add3Tuples();
        //
        //
        addNTuple();
        // urnSystem = new UrnSystem();
        return this;
    }

    @Override
    public void addPoint(int[] p, double value) {
        for (NTuple tuple : tuples) {
            tuple.add(p, value);
        }
        sampledPoints.add(p);
    }

    public void addSummary(int[] p, StatSummary ss) {
        for (NTuple tuple : tuples) {
            tuple.add(p, ss);
        }
    }



    // careful - this can be slow - it iterates over all points in the search space!
    @Override
    public int[] getBestSolution() {
        Picker<int[]> picker = new Picker<int[]>(Picker.MAX_FIRST);
        for (int i = 0; i < SearchSpaceUtil.size(searchSpace); i++) {
            int[] p = SearchSpaceUtil.nthPoint(searchSpace, i);
            picker.add(getSimple(p), p);
        }
        // System.out.println("Best solution: " + Arrays.toString(picker.getBest()) + "\t: " + picker.getBestScore());
        return picker.getBest();
    }

    @Override
    public int[] getBestOfSampled() {
        Picker<int[]> picker = new Picker<int[]>(Picker.MAX_FIRST);
        for (int[] p : sampledPoints) {
            picker.add(getSimple(p), p);
        }
        // System.out.println("Best solution: " + Arrays.toString(picker.getBest()) + "\t: " + picker.getBestScore());
        return picker.getBest();
    }

    @Override
    public int[] getBestOfSampledPlusNeighbours(int nNeighbours) {
        // evaluate choices with zero exploration factor - want to exploit best
        EvaluateChoices evc = new EvaluateChoices(this, 0);
        for (int[] p : sampledPoints) {
            evc.add(p);
        }
        // System.out.println("Best solution: " + Arrays.toString(picker.getBest()) + "\t: " + picker.getBestScore());
        return evc.picker.getBest();
    }

    public Double getB(int[] x) {
        // we could get an average ...
        double[] probVec = urnSystem.getPrior();
        for (NTuple tuple : tuples) {
            StatSummary ss = tuple.getStats(x);
            if (ss != null) {
                if (tuple.tuple.length >= minTupleSize) {
                    System.out.println(Arrays.toString(x) + "\t " + ss.mean() + "\t " + ss.n());
                    double nWins = ss.mean() * ss.n();
                    double[] pVec = urnSystem.pVec(nWins, ss.n());
                    // System.out.println(Arrays.toString(pVec));
                    // System.out.println(nWins + " : " + ss.n() + " : "  + pWIn(pVec));
                    multiplyAndNorm(probVec, pVec);
                }
            }
        }
        // BarChart.display(probVec, "Prob Vec: " + Arrays.toString(x) + " : " + pWIn(probVec));

        // return rand.nextDouble();
        return pWIn(probVec);
    }

    @Override
    public Double getSimple(int[] x) {
        // we could get an average ...

        StatSummary ssTot = new StatSummary();
        for (NTuple tuple : tuples) {
            StatSummary ss = tuple.getStats(x);
            if (ss != null) {
                if (tuple.tuple.length >= minTupleSize) {
                    double mean = ss.mean();
                    if (!Double.isNaN(mean))
                        ssTot.add(mean);
                }
            }
        }
        // BarChart.display(probVec, "Prob Vec: " + Arrays.toString(x) + " : " + pWIn(probVec));

        // return rand.nextDouble();
        // System.out.println("Returning: " + ssTot.mean() + " : " + ssTot.n());

        double ret = ssTot.mean();
        if (Double.isNaN(ret)) {
            return 0.0;
        } else {
            return ret;
        }

    }

    public double getExplorationEstimate(int[] x) {
        // just takes the average of the exploration vector
        double[] vec = getExplorationVector(x);
        double tot = 0;
        for (double e : vec) tot += e;
        return tot / vec.length;
    }

    static double epsilon = 0.1;

    public double[] getExplorationVector(int[] x) {
        // idea is simple: we just provide a summary over all
        // the samples, comparing each to the maximum in that N-Tuple

        double[] vec = new double[tuples.size()];
        for (int i = 0; i < tuples.size(); i++) {
            NTuple tuple = tuples.get(i);
            StatSummary ss = tuple.getStats(x);
            if (ss != null) {
                vec[i] = Math.log(1 + tuple.nSamples()) / (epsilon + ss.n());
            } else {
                vec[i] = Math.log(1 + tuple.nSamples) / epsilon;
            }
        }
        return vec;
    }

    public double pWIn(double[] pVec) {
        double nBalls = pVec.length - 1;
        double denom = 0;
        double p = 0;
        for (int i = 0; i < pVec.length; i++) {
            // for each Urn look up the probability given the number of wins
            // and multiply it by the win prob for that Urn
            denom += pVec[i];
            p += pVec[i] * ((double) i / nBalls);
        }
        // System.out.println(p + "\t " + p / denom);
        return p / denom;
    }

    public void multiplyAndNorm(double[] a, double[] b) {
        if (a.length != b.length) {
            throw new RuntimeException("Vec lengths not equal" + a.length + " : " + b.length);
        }
        double tot = 0;
        for (int i = 0; i < a.length; i++) {
            a[i] = a[i] * b[i];
            tot += a[i];
        }
        for (int i = 0; i < a.length; i++) {
            a[i] /= tot;
        }
    }

    public Double getOld(int[] x) {
        // we could get an average ...
        double tot = 0;
        double denom = 0;
        int nNull = 0;
        int nValid = 0;
        StatSummary ave = new StatSummary();

        for (NTuple tuple : tuples) {
            StatSummary ss = tuple.getStats(x);
            if (ss != null) {
                double dInc = 1; // (ss.n() > 1) ? ss.stdErr() : defaultErr;

                if (Double.isNaN(dInc)) {
                    System.out.println(ss);
                }

                tot += ss.mean() / (1 + dInc);
                denom += (1 + dInc);
                nValid++;
                // experiment with only counting longer tuples
                if (tuple.tuple.length > 2) {
                    ave.add(ss.mean());
                }
                System.out.println(tuple + "\t " + ss.mean() + "\t " + ss.n());
            } else {
                nNull++;
            }
        }
        if (denom > 0) {
            // return tot / denom;
            double ret = ave.mean();
            if (Double.isNaN(ret)) {
                return 0.0;
            } else {
                return ave.mean();
            }
        } else {
            System.out.println("Null return: " + nNull + " versus " + nValid + " : " + (denom == Double.NaN) + " : " + denom);
            return null;
        }
    }


    public void printSummaryReport() {
        System.out.format("Search space has %d dimensions\n", searchSpace.nDims());
        for (NTuple nt : tuples) {
            System.out.println(nt);
        }
        System.out.format("Model has %d tuples.\n", tuples.size());

    }

    public void printDetailedReport() {
        System.out.format("Search space has %d dimensions\n", searchSpace.nDims());
        for (NTuple nt : tuples) {
            nt.printNonEmpty();
            System.out.println();
        }
        System.out.format("Model has %d tuples.\n", tuples.size());

    }

    // note that there is a smarter way to add different n-tuples, but this way is easiest

    public void add1Tuples() {
        for (int i = 0; i < searchSpace.nDims(); i++) {
            int[] a = new int[]{i};
            tuples.add(new NTuple(searchSpace, a));
        }
    }

    public void add2Tuples() {
        for (int i = 0; i < searchSpace.nDims() - 1; i++) {
            for (int j = i + 1; j < searchSpace.nDims(); j++) {
                int[] a = new int[]{i, j};
                tuples.add(new NTuple(searchSpace, a));
            }
        }
    }

    public void add3Tuples() {
        for (int i = 0; i < searchSpace.nDims() - 2; i++) {
            for (int j = i + 1; j < searchSpace.nDims() - 1; j++) {
                for (int k = j + 1; k < searchSpace.nDims(); k++) {
                    int[] a = new int[]{i, j, k};
                    tuples.add(new NTuple(searchSpace, a));
                }
            }
        }
    }

    public void addNTuple() {
        // adds the entire one
        int[] a = new int[searchSpace.nDims()];
        for (int i = 0; i < a.length; i++) {
            a[i] = i;
        }
        tuples.add(new NTuple(searchSpace, a));
    }

}
