package tracks.multiPlayer.deprecated.shiftBufferRHGA.evodef;

import tools.StatSummary;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by sml on 16/08/2016.
*/

public class EvolutionLogger {

    // this keeps track of how fitness evolves

    // bestYet are the values recorded by the logger
    // but the keepBest() method is used to track what
    // the EA under test thinks is the best solution

    ArrayList<Double> fa;
    public StatSummary ss;
    int bestGen = 0;
    int[] bestYet;
    int[] finalSolution;
    double finalFitness;
    int nOptimal = 0;

    public EvolutionLogger() {
        reset();
    }

    public void log(double fitness, int[] solution, boolean isOptimal) {
        finalSolution = solution;
        finalFitness = fitness;
        if (fitness > ss.max()) {
            bestGen = fa.size() + 1;
            bestYet = solution;
        }
        if (isOptimal) {
            nOptimal++;
        }
        fa.add(fitness);
        ss.add(fitness);
    }

    public int nEvals() {
        return fa.size();
    }

    public void report() {
        // System.out.println(ss);
        System.out.println("Best solution first found at eval: " + bestGen);
        System.out.println("Best solution: " + Arrays.toString(bestYet));
        System.out.println("Best fitness: " + ss.max());
        System.out.println("Final solution: " + Arrays.toString(finalSolution));
        System.out.println("Final fitness: " + finalFitness());
        System.out.println("Number of visits to optimal: " + nOptimal());
        System.out.println("Total number of evaluations: " + ss.n());
    }

    /*
      Important to call keepBest id we want the performance
      of the algorithm to be properly measured by the logger
    */
    public void keepBest(int[] sol, double fitness) {
        this.finalSolution = sol;
        this.finalFitness = fitness;
        // System.out.println("Called keep best");
    }

    public double finalFitness() {
        return finalFitness;
    }

    public int[] finalSolution() {
        return finalSolution;
    }

    public int nOptimal() {
        return nOptimal;
    }

    public void reset() {
        fa = new ArrayList<>();
        ss = new StatSummary();
        bestYet = null;
        bestGen = 0;
        nOptimal = 0;
    }
}

