package tracks.multiPlayer.deprecated.shiftBufferRHGA.rhAlgo.strategy;


import tracks.multiPlayer.deprecated.shiftBufferRHGA.rhAlgo.search.Individual;
import tracks.multiPlayer.deprecated.shiftBufferRHGA.evodef.SearchSpace;
import tracks.multiPlayer.deprecated.shiftBufferRHGA.evodef.SearchSpaceUtil;

import java.util.Random;

/**
 * Created by sml on 17/01/2017.
 */
public class Mutator {

//    public static void main(String[] args) {
//
//        // check operation on a simple case
//
//        Mutator mutator = new Mutator(new EvalMaxM(3, 2));
//
//        System.out.println(Arrays.toString(mutator.randMut(new int[]{0, 0, 0})));
//        System.out.println(Arrays.toString(mutator.randMut(new int[]{1, 1, 1})));
//
//        // check operation of this
//        totalRandomChaosMutation = false;
//        mutator = new Mutator(new EvalMaxM(50, 10));
//        System.out.println(Arrays.toString(mutator.randMut(new int[mutator.searchSpace.nDims()])));
//        totalRandomChaosMutation = true;
//        System.out.println(Arrays.toString(mutator.randMut(new int[mutator.searchSpace.nDims()])));
//    }

    public double pointProb = 1.0;
    static Random random = new Random();

    public static boolean totalRandomChaosMutation = false;

    SearchSpace searchSpace;

    public Mutator(SearchSpace searchSpace) {
        this.searchSpace = searchSpace;
    }

    public int[] randMut(int[] v) {
        // note: the algorithm ensures that at least of the bits is different in the returned array
        if (totalRandomChaosMutation) {
            return SearchSpaceUtil.randomPoint(searchSpace);
        }
        // otherwise do a proper mutator
        int n = v.length;
        int[] x = new int[n];
        // pointwise probability of additional mutations
        double mutProb = pointProb / n;
        // choose element of vector to mutate
        int ix = random.nextInt(n);
        // copy all the values fauthfully apart from the chosen one
        for (int i=0; i<n; i++) {
            if (i == ix || random.nextDouble() < mutProb) {
                x[i] = mutateValue(v[i], searchSpace.nValues(i));
            } else {
                x[i] = v[i];
            }
        }
        return x;
    }

    int mutateValue(int cur, int nPossible) {
        // the range is nPossible-1, since we
        // selecting the current value is not allowed
        // therefore we add 1 if the randomly chosen
        // value is greater than or equal to the current value
        int rx = random.nextInt(nPossible-1);
        return rx >= cur ? rx+1 : rx;
    }

    int mutateWithProba(double probaMut, int cur, int nPossible) {
        if (random.nextDouble() < probaMut) {
            return mutateValue(cur, nPossible);
        }
        return cur;
    }

    public void mutateIndividual(Individual individual, double probaMut) {
        for (int i = 0; i < individual.getGenome().length; i++) {
            if (random.nextDouble() < probaMut) {
                int cur = individual.getGenome()[i];
                individual.getGenome()[i] = mutateValue(cur, searchSpace.nValues(i));
            }
        }
    }
}
