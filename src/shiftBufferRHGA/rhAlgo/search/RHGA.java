package shiftBufferRHGA.rhAlgo.search;


import tools.ElapsedCpuTimer;
import shiftBufferRHGA.evodef.SearchSpaceUtil;
import shiftBufferRHGA.evodef.SolutionEvaluator;

/**
 * Created by Jialin Liu on 27/03/2017.
 * CSEE, University of Essex, UK
 * Email: jialin.liu@essex.ac.uk
 * <p/>
 * Respect to Google Java Style Guide:
 * https://google.github.io/styleguide/javaguide.html
 */
public class RHGA extends RHEA {
    private int[] opponentGenome;

    public RHGA(int playerId) {
        super(playerId);
    }

    public RHGA(int playerId, int nSamples) {
        super(playerId, nSamples);
    }

    public RHGA(int playerId, int nSamples, boolean isShiftBuffer) {
        super(playerId, nSamples, isShiftBuffer);
    }

    public RHGA(int nSamples, boolean isShiftBuffer) {
        super(0, nSamples, isShiftBuffer);
    }


    public void randomiseOpponent() {
        this.opponentGenome = SearchSpaceUtil.randomPoint(searchSpace);
    }

    /**
     *
     * @param evaluator
     * @param maxEvals
     * @return: the solution coded as an array of int
     */
    @Override
    public int[] runTrial(SolutionEvaluator evaluator, int maxEvals) {
        // Initialisation
        init(evaluator);
        randomiseOpponent();
        // Evaluate and sort
        evaluatePopulation(evaluator);
        sortPopulationByFitness(population, true);
        bestYet = population[0].getGenome();
        while (evaluator.nEvals() < maxEvals) { //&& !evaluator.optimalFound()) {
            randomiseOpponent();
            Individual[] nextPop = new Individual[population.length];
            // Keep stronger individuals
            int i;
            for(i = 0; i < elitism; ++i) {
                nextPop[i] = population[i];
            }
            // Generate offspring
            for (; i<popSize; i++) {
                nextPop[i] = breed(population);
                mutator.mutateIndividual(nextPop[i], probaMut);
                nextPop[i].fitness(evaluator, opponentGenome, nSamples);
            }
            population = nextPop;
            // Evaluate and sort new population
            evaluatePopulation(evaluator);
            sortPopulationByFitness(population, true);
            bestYet = population[0].getGenome();
        }
        return bestYet;
    }

    public int[] runTrial(SolutionEvaluator evaluator, ElapsedCpuTimer elapsedTimer) {
//        System.out.println("start init evaluator: " + elapsedTimer.remainingTimeMillis());
        // Initialisation
        init(evaluator);
//        System.out.println("start randomiseOpponent: " + elapsedTimer.remainingTimeMillis());
        randomiseOpponent();
//        System.out.println("start evaluatePopulation: " + elapsedTimer.remainingTimeMillis());
        // Evaluate and sort
        evaluatePopulation(evaluator);
//        System.out.println("start sortPopulationByFitness: " + elapsedTimer.remainingTimeMillis());
        sortPopulationByFitness(population, true);

        double avgTimeTaken = 0;
        double acumTimeTaken = 0;
        long remaining = elapsedTimer.remainingTimeMillis();
        int remainingLimit = 5;
        int numIters = 0;

//        System.out.println("start optimisation WHILE block: " + elapsedTimer.remainingTimeMillis());
        while(remaining > 2*avgTimeTaken && remaining > remainingLimit){
            ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();

            randomiseOpponent();
            Individual[] nextPop = new Individual[population.length];
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
            population = nextPop;
            // Evaluate and sort new population
            evaluatePopulation(evaluator);
            sortPopulationByFitness(population, true);
            bestYet = population[0].getGenome();

            numIters++;
            acumTimeTaken += (elapsedTimerIteration.elapsedMillis()) ;
            avgTimeTaken  = acumTimeTaken/numIters;
            remaining = elapsedTimer.remainingTimeMillis();
        }
//        System.out.println("finish optimisation: " + elapsedTimer.remainingTimeMillis() + " using nbIter="+numIters);
//        System.out.println("-- " + numIters + " -- ( " + avgTimeTaken + ") " + elapsedTimer.remainingTimeMillis() + "," + remaining);
        return bestYet;
    }

    public void evaluatePopulation(SolutionEvaluator evaluator) {
        for (int i=0; i<population.length; i++) {
            population[i].fitness(evaluator, opponentGenome, nSamples);
        }
    }
}
