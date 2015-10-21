package controllers.Return42.algorithms.GA;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import controllers.Return42.GameStateCache;
import controllers.Return42.algorithms.GA.genomes.EmergencyGenome;
import controllers.Return42.algorithms.GA.genomes.Genome;
import controllers.Return42.algorithms.GA.genomes.MultiStepGenome;
import controllers.Return42.algorithms.GA.genomes.NextStepMultiGenome;
import controllers.Return42.heuristics.CompareHeuristic;
import controllers.Return42.heuristics.ComplexHeuristic;
import controllers.Return42.heuristics.HeuristicExplorer;
import controllers.Return42.heuristics.NextStepHeuristic;
import controllers.Return42.heuristics.features.CompareFeature;
import controllers.Return42.heuristics.features.controller.FeatureController;

public class Agent extends AbstractPlayer {

    private static final long BREAK_HEURISTICS = 200;      // abort heuristic cache threshold
    private static final int HEURISTICS_SIZE = 1024;
    private static final int HEURISTICS_DEPTH = 100;

    private static int SIMULATION_DEPTH = 4;         // TODO find reasonable number, min 1
    private static final int SIMULATION_DEPTH_MAX = 16;          // TODO find reasonable number, min 1
    private static final int POPULATION_SIZE = 2;          // TODO find reasonable number, min 2

    private final Types.ACTIONS[] actions;
    private final Genome genomes[] = new Genome[POPULATION_SIZE];

    private static final Random rnd = new Random();

    private ElapsedCpuTimer timer;
    private double initialTime;
    private int numSimulations;
    private boolean lastAdjust = false;
    private Genome emergency;

    private boolean debug = false;

    private ComplexHeuristic heuristic;

    public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        //SIMULATION_DEPTH = (int)Math.sqrt(Math.sqrt(stateObs.getWorldDimension().getWidth() * stateObs.getWorldDimension().getHeight())) / 2; //does not work as intended
        int N_ACTIONS = stateObs.getAvailableActions().size();
        actions = new Types.ACTIONS[N_ACTIONS];
        stateObs.getAvailableActions().toArray(actions);
        GameStateCache c = new GameStateCache(stateObs);

        Types.ACTIONS[] aNull = new Types.ACTIONS[N_ACTIONS + 1];
        stateObs.getAvailableActions(true).toArray(aNull);
        emergency = new EmergencyGenome(1, aNull);

        timer = elapsedTimer;
        initGenome(c);

        //act(stateObs, elapsedTimer);
        microbial(c);
    }


    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        timer = elapsedTimer;
        if(stateObs.getAvatarLastAction() != Types.ACTIONS.ACTION_NIL) {
            advanceGenomes();
        }

        GameStateCache gs = new GameStateCache(stateObs);

        for(FeatureController c : heuristic.getController()) {
            c.check(gs);
        }

        Types.ACTIONS a = microbial(gs);

        adjustGenomeDepth();

        if(debug) {
            System.out.println(a + ";" + (initialTime - timer.remainingTimeMillis()) / numSimulations + ";" + numSimulations + ";" + genomes[0].getDepth());

            return a;
        } else {
            return a;
        }
    }

    private void adjustGenomeDepth() {
        if(numSimulations > 1000 && SIMULATION_DEPTH < SIMULATION_DEPTH_MAX) {
            if(lastAdjust) {
                SIMULATION_DEPTH += 2;
                for(Genome genome : genomes) {
                    genome.addDepth(2);
                }
            } else {
                lastAdjust = true;
            }
        } else {
            lastAdjust = false;
        }
    }


    private Types.ACTIONS microbial(GameStateCache stateObs) {

        Genome bestGenome;
        initialTime = timer.remainingTimeMillis();
        numSimulations = 0;

        do {
            bestGenome = microbial_tournament(stateObs);
        } while(timeForAnotherRun());

        if(bestGenome.getScore(stateObs, stateObs) == Double.NEGATIVE_INFINITY) {
            bestGenome = emergency;
            if(bestGenome.getScore(stateObs, stateObs) > 0) {
                System.out.println("Emergency genome got this");
            } else {
                System.out.println("We are doomed");
            }

            //System.out.println("We are gonna die. Emergency depth cut");
            //resetGenomeDepth();
        }

        return bestGenome.getNextAction();
    }

    private boolean timeForAnotherRun() {
        double remaining = timer.remainingTimeMillis();
        double avg = (initialTime - remaining) / numSimulations;
        return remaining > 1 && remaining > avg * 3.0;
    }


    private Genome microbial_tournament(GameStateCache stateObs) {
        int a, b, W, L;
        numSimulations++;

        /*a = rnd.nextInt(POPULATION_SIZE);
        do {
            b = rnd.nextInt(POPULATION_SIZE);
        } while(a == b);*/
        a = 0;
        b = rnd.nextInt(POPULATION_SIZE-1) + 1;

        // evaluate
        double score_a = genomes[a].getScore(stateObs, stateObs);
        if(timer.remainingTimeMillis() < 4) {
            return genomes[a];
        }
        double score_b = genomes[b].getScore(stateObs, stateObs);

        if(score_a > score_b) {
            W = a;
            L = b;
        } else {
            W = b;
            L = a;
        }

        genomes[L].adapt(genomes[W]);

        return genomes[W];
    }

    ////

    private void advanceGenomes() {
        for(int j = 0; j < POPULATION_SIZE; j++) {
            genomes[j].advance();
        }
    }

    private void resetGenomeDepth() {
        for(Genome genome : genomes) {
            genome.addDepth(-SIMULATION_DEPTH + 4);
        }
        SIMULATION_DEPTH = 4;
    }

    private void initGenome(GameStateCache stateObs) {
        heuristic = new ComplexHeuristic(stateObs, false);//heuristicTournament(stateObs);
        CompareHeuristic next = new NextStepHeuristic(stateObs, false);//heuristicTournament(stateObs);
        if(next.getFeatures().size() > 0) {
            for(int j = 0; j < POPULATION_SIZE; j++) {
                genomes[j] = new NextStepMultiGenome(SIMULATION_DEPTH, actions, heuristic, next);
            }
        } else {
            for(int j = 0; j < POPULATION_SIZE; j++) {
                genomes[j] = new MultiStepGenome(SIMULATION_DEPTH, actions, heuristic);
            }
        }
    }

    /**
     * Create heuristics with random weights and simulate them
     *
     * @param stateObs current gamestate
     * @return best heuristic
     */
    private CompareHeuristic heuristicTournament(GameStateCache stateObs) {
        List<CompareHeuristic> heuristics = new ArrayList<>();
        heuristics.add(new ComplexHeuristic(stateObs, false));
        //heuristics.add(new SimpleHeuristic());

        CompareHeuristic bestHeuristic = heuristics.get(0);
        double highscore = Double.NEGATIVE_INFINITY;

        for(int i = 0; i < HEURISTICS_SIZE; i++) {
            HeuristicExplorer explorer = new HeuristicExplorer(heuristics.get(i));
            double s = explorer.getScoreAfter(stateObs, HEURISTICS_DEPTH);// + rnd.nextDouble() * 0.001;
            if(s > highscore) {
                highscore = s;
                bestHeuristic = heuristics.get(i);
                System.out.println(i + " is better " + s);
            }

            heuristics.add(new ComplexHeuristic(stateObs, true));

            if(timer.remainingTimeMillis() < BREAK_HEURISTICS) {
                if(debug) {
                    System.out.println("----");
                    System.out.println("Heuristics simulated: " + i + " - " + timer.remainingTimeMillis());
                    for(CompareFeature f : bestHeuristic.getFeatures()) {
                        System.out.println(f.getWeight());
                    }
                    System.out.println("Normal:");
                    for(CompareFeature f : heuristics.get(0).getFeatures()) {
                        System.out.println(f.getWeight());
                    }
                    System.out.println("----");
                }
                break;
            }
        }
        return bestHeuristic;
    }
}