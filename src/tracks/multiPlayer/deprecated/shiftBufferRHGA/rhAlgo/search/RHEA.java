package tracks.multiPlayer.deprecated.shiftBufferRHGA.rhAlgo.search;

import tracks.multiPlayer.deprecated.shiftBufferRHGA.evodef.EvoAlg;
import tracks.multiPlayer.deprecated.shiftBufferRHGA.evodef.SearchSpace;
import tracks.multiPlayer.deprecated.shiftBufferRHGA.evodef.SearchSpaceUtil;
import tracks.multiPlayer.deprecated.shiftBufferRHGA.evodef.SolutionEvaluator;
import tracks.multiPlayer.deprecated.shiftBufferRHGA.ntuple.NTupleSystem;
import tracks.multiPlayer.deprecated.shiftBufferRHGA.rhAlgo.strategy.*;

import java.util.Random;

/**
 * Created by Jialin Liu on 28/03/2017.
 * CSEE, University of Essex, UK
 * Email: jialin.liu@essex.ac.uk
 * <p>
 * Respect to Google Java Style Guide:
 * https://google.github.io/styleguide/javaguide.html
 */

public abstract class RHEA implements EvoAlg {
    protected int playerId;
    protected int popSize = 5;
    protected int elitism = 2;
    protected int tournament = 1;
    protected int nSamples = 2;
    protected int simulationDepth;
    protected double probaMut;
    protected Individual[] population;
    protected int[] bestYet;
    protected int[] seed;
    protected boolean isShiftBuffer;
    protected SearchSpace searchSpace;
    protected SolutionEvaluator evaluator;
    protected NTupleSystem model;
    static Random rdm;
    protected ISelection selector;
    protected ICrossover cross;
    protected Mutator mutator;

    protected int id, oppId, no_players;

    // should not be a static, just did it quick and dirty
    static boolean noisy = false;
    static double epsilon = 1.0;
    static boolean accumulateBestYetStats = false;
    // this is only checked if not resampling parent
    static boolean resampleParent = false;

    public RHEA(int playerId) {
        this(playerId, 1);
    }

    public RHEA(int playerId, int nSamples) {
        this(playerId, nSamples, false);
    }

    public RHEA(int playerId, int nSamples, boolean isShiftBuffer) {
        this.playerId = playerId;
        this.nSamples = nSamples;
        this.isShiftBuffer = isShiftBuffer;
        this.rdm = new Random();
        this.selector = new TournamentSelection(rdm, tournament);
        this.cross = new UniformCrossover(rdm);
    }

    @Override
    public void setInitialSeed(int[] seed) {
        this.seed = seed;
    }

    @Override
    public void setModel(NTupleSystem nTupleSystem) {
        this.model = nTupleSystem;
    }

    @Override
    public NTupleSystem getModel() {
        return model;
    }

    protected void setEvaluator(SolutionEvaluator evaluator) {
        this.evaluator = evaluator;
    }
    /**
     * Init search space
     */
    public void init(SolutionEvaluator evaluator) {
        this.evaluator = evaluator;
        this.searchSpace = evaluator.searchSpace();
        this.mutator = new Mutator(searchSpace);
        this.simulationDepth = searchSpace.nDims();
//        System.out.println("simulationDepth of this RHEA is " + simulationDepth);
        this.probaMut = (double ) 1/simulationDepth;
        if (population != null && isShiftBuffer && (simulationDepth == population[0].getGenome().length)){
            for (int i = 0; i < popSize; i++) {
                population[i].shiftGenome(searchSpace);
            }
        } else {
            population = new Individual[popSize];
            for (int i=0; i<popSize; i++) {
                population[i] = new Individual(SearchSpaceUtil.randomPoint(searchSpace), playerId, evaluator);
            }
        }
    }

    private void printGenome(int []path)
    {
        for(int p : path)
        {
            System.out.print(p);
        }
        System.out.println();
    }

    protected void sortPopulationByFitness(Individual[] population) {
        for (int i = 0; i < population.length; i++) {
            for (int j = i + 1; j < population.length; j++) {
                if (population[i].getFitness() < population[j].getFitness()) {
                    Individual gcache = population[i];
                    population[i] = population[j];
                    population[j] = gcache;
                }
            }
        }
    }

    protected Individual breed() {
        Individual gai1 = selector.getParent(population, null);        //First parent.
        Individual gai2 = selector.getParent(population, gai1);        //Second parent.
        return cross.uniformCross(gai1, gai2);
    }
}

