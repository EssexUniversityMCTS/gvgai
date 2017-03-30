package shiftBufferRHCA.rhAlgo.search;

import shiftBufferRHCA.rhAlgo.strategy.Mutator;
import shiftBufferRHCA.evodef.SearchSpaceUtil;
import shiftBufferRHCA.evodef.SolutionEvaluator;
import tools.ElapsedCpuTimer;

/**
 * Created by Jialin Liu on 30/03/2017.
 * CSEE, University of Essex, UK
 * Email: jialin.liu@essex.ac.uk
 * <p>
 * Respect to Google Java Style Guide:
 * https://google.github.io/styleguide/javaguide.html
 */
public class RHCA extends RHEA {
    private Individual[] opponents;
    private int subPopSize = 1;
    private int oppPopSize = 3;

    public RHCA(int playerId) {
        super(playerId);
    }

    public RHCA(int playerId, int nSamples) {
        super(playerId, nSamples);
    }

    public RHCA(int playerId, int nSamples, boolean isShiftBuffer) {
        super(playerId, nSamples, isShiftBuffer);
    }

    public RHCA(int nSamples, boolean isShiftBuffer) {
        super(0, nSamples, isShiftBuffer);
    }


    public void initOpponent() {
        opponents = new Individual[popSize];
        for (int i=0; i<popSize; i++) {
            opponents[i] = new Individual(SearchSpaceUtil.randomPoint(searchSpace), oppId, evaluator);
        }
    }

    /**
     *
     * @param evaluator
     * @param maxEvals
     * @return: the solution coded as an array of int
     */
    @Override
    public int[] runTrial(SolutionEvaluator evaluator, int maxEvals) {
//        // Initialisation
//        init(evaluator);
//        // Evaluate and sort
//        evaluatePopulation(evaluator);
//        sortPopulationByFitness(population);
//        bestYet = population[0].getGenome();
//        while (evaluator.nEvals() < maxEvals) { //&& !evaluator.optimalFound()) {
//            Individual[] nextPop = new Individual[population.length];
//            // Keep stronger individuals
//            int i;
//            for(i = 0; i < elitism; ++i) {
//                nextPop[i] = population[i];
//            }
//            // Generate offspring
//            for (; i<popSize; i++) {
//                nextPop[i] = breed();
//                mutator.mutateIndividual(nextPop[i], probaMut);
//                nextPop[i].fitness(evaluator, opponentGenome, nSamples);
//            }
//            population = nextPop;
//            // Evaluate and sort new population
//            evaluatePopulation(evaluator);
//            sortPopulationByFitness(population);
//            bestYet = population[0].getGenome();
//        }
        return bestYet;
    }

    public int[] runTrial(SolutionEvaluator evaluator, ElapsedCpuTimer elapsedTimer) {
//        System.out.println("start init evaluator: " + elapsedTimer.remainingTimeMillis());
        // Initialisation
        init(evaluator);
//        System.out.println("start randomiseOpponent: " + elapsedTimer.remainingTimeMillis());
//        System.out.println("start evaluatePopulation: " + elapsedTimer.remainingTimeMillis());
        // Evaluate and sort
        evaluatePopulation(evaluator);
//        System.out.println("start sortPopulationByFitness: " + elapsedTimer.remainingTimeMillis());
        sortPopulationByFitness(population, true);
        sortPopulationByFitness(opponents, true);

        double avgTimeTaken = 0;
        double acumTimeTaken = 0;
        long remaining = elapsedTimer.remainingTimeMillis();
        int remainingLimit = 5;
        int numIters = 0;

        System.out.println("start optimisation WHILE block: " + elapsedTimer.remainingTimeMillis());
        while(remaining > 2*avgTimeTaken && remaining > remainingLimit){
            ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();

            Individual[] nextPop = new Individual[population.length];
            Individual[] nextOppPop = new Individual[opponents.length];

            // Keep stronger individuals
            int i;
            for(i = 0; i < elitism; ++i) {
                nextPop[i] = population[i];
            }
            // Generate offspring
            for (; i<popSize; i++) {
                nextPop[i] = breed(population);
                mutator.mutateIndividual(nextPop[i], probaMut);
            }
            for(i = 0; i < elitism; ++i) {
                nextOppPop[i] = opponents[i];
            }
            // Generate offspring
            for (; i<oppPopSize; i++) {
                nextOppPop[i] = breed(opponents);
                mutator.mutateIndividual(nextOppPop[i], probaMut);
            }
            population = nextPop;
            opponents = nextOppPop;
            // Evaluate and sort new population
            evaluatePopulation(evaluator);
            sortPopulationByFitness(population, true);
            sortPopulationByFitness(opponents, true);
            bestYet = population[0].getGenome();

            numIters++;
            acumTimeTaken += (elapsedTimerIteration.elapsedMillis()) ;
            avgTimeTaken  = acumTimeTaken/numIters;
            remaining = elapsedTimer.remainingTimeMillis();
        }
        System.out.println("finish optimisation: " + elapsedTimer.remainingTimeMillis() + " using nbIter="+numIters);
//        System.out.println("-- " + numIters + " -- ( " + avgTimeTaken + ") " + elapsedTimer.remainingTimeMillis() + "," + remaining);
        return bestYet;
    }

    @Override
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
                population[i].resetFitness();
            }
        } else {
            population = new Individual[popSize];
            for (int i=0; i<popSize; i++) {
                population[i] = new Individual(SearchSpaceUtil.randomPoint(searchSpace), playerId, evaluator);
            }
        }
        if (opponents != null && isShiftBuffer && (simulationDepth == opponents[0].getGenome().length)){
            for (int i = 0; i < oppPopSize; i++) {
                opponents[i].shiftGenome(searchSpace);
                opponents[i].resetFitness();
            }
        } else {
            opponents = new Individual[oppPopSize];
            for (int i=0; i<oppPopSize; i++) {
                opponents[i] = new Individual(SearchSpaceUtil.randomPoint(searchSpace), oppId, evaluator);
            }
        }

    }


    public void evaluatePopulation(SolutionEvaluator evaluator) {
        for (int i=0; i<population.length; i++) {
            for (int j = 0; j < subPopSize; j++) {
                int idx = rdm.nextInt(oppPopSize);
                population[i].fitness(evaluator, opponents[idx].getGenome(), nSamples);
            }
        }
    }
}

