package shiftBufferRHCA.rhAlgo.search;

import shiftBufferRHCA.evodef.SearchSpace;
import shiftBufferRHCA.evodef.SearchSpaceUtil;
import shiftBufferRHCA.evodef.SolutionEvaluator;
import tools.StatSummary;

/**
 * Created by Jialin Liu on 27/03/2017.
 * CSEE, University of Essex, UK
 * Email: jialin.liu@essex.ac.uk
 * <p/>
 * Respect to Google Java Style Guide:
 * https://google.github.io/styleguide/javaguide.html
 */
public class Individual {
    private int[] genome;
    private double probaMut = 0.2;
    private double fitness;
    public int playerId;
    private StatSummary accumFit;
    public SolutionEvaluator evaluator;


    public Individual(int simulationDepth, int playerId, SolutionEvaluator evaluator) {
        this.genome = new int[simulationDepth];
        this.fitness = 0;
        accumFit = new StatSummary();
        this.playerId = playerId;
        this.evaluator = evaluator;
    }


    public Individual(int[] genome, int playerId, SolutionEvaluator evaluator) {
        this.genome = genome;
        this.fitness = 0;
        accumFit = new StatSummary();
        this.playerId = playerId;
        this.evaluator = evaluator;
    }

    public void setGenome(int[] genome) {
        this.genome = genome;
//        this.probaMut = 1/genome.length;
    }

    public int[] getGenome() {
        return genome;
    }

    public boolean shiftGenome(SearchSpace searchSpace) {
        if (genome !=null) {
            genome = SearchSpaceUtil.shiftLeftAndRandomAppend(genome, searchSpace);
            return true;
        }
        return false;
    }

    /**
     * Randomly generate/reset chromosomes
     */
    public void randomize(SearchSpace searchSpace)
    {
        genome = SearchSpaceUtil.randomPoint(searchSpace);
    }

    public void setFitness(double fit)
    {
        fitness = fit;
    }

    public double getFitness()
    {
        return fitness;
    }

    /**
     * Return the mean fitness value
     */
    public double getMeanFitness()
    {
        if(accumFit.n() == 0)
            return fitness;

        return accumFit.mean();
    }

    public void accumFitness(double fit)
    {
        accumFit.add(fit);
    }

    public void resetFitness() {
        this.accumFit.reset();
        this.fitness = 0;
    }

    public Individual copy()
    {
        Individual gai = new Individual(this.genome.length, this.playerId, this.evaluator);
        for(int i = 0; i < this.genome.length; ++i)
        {
            gai.genome[i] = this.genome[i];
        }
        return gai;
    }

    public String toString()
    {
        String st = new String();
        for(int i = 0; i < genome.length; ++i)
            st += genome[i];
        return st;
    }

    public void print()
    {
        String genome = this.toString();
        genome += "; n: " + accumFit.n() + ", fitness: " + ((accumFit.n() == 0) ? fitness : accumFit.mean());
        System.out.println(genome);
    }

    public void fitness(SolutionEvaluator evaluator, int[] opponent, int nSamples) {
        double acc = 0.0;
        for (int i=0; i<nSamples; i++) {
            double fitness = evaluator.pairEvaluate(this.genome, opponent);
            acc += fitness;
            accumFitness(fitness);
        }
        setFitness(acc/nSamples);
    }
}
