package tracks.multiPlayer.advanced.sampleRHEA;

import core.game.StateObservationMulti;
import core.player.AbstractMultiPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Vector2d;
import tracks.multiPlayer.tools.heuristics.SimpleStateHeuristic;
import utilsUI.DrawingAgent;
import utilsUI.ParameterSetEvo;
import tracks.multiPlayer.tools.heuristics.*;

import java.awt.*;
import java.util.*;

import static utilsUI.Constants.*;

public class Agent extends AbstractMultiPlayer {

    // constants
    private final long BREAK_MS = 10;
    public static final double epsilon = 1e-6;
    static final int POINT1_CROSS = 0;
    static final int UNIFORM_CROSS = 1;

    // algorithm logic
    private Individual[] population, nextPop;
    private int NUM_INDIVIDUALS;
    private int[] N_ACTIONS;
    private HashMap<Integer, Types.ACTIONS>[] action_mapping;
    private StateHeuristicMulti heuristic;

    // others
    private ElapsedCpuTimer timer;
    private Random randomGenerator;
    private double acumTimeTakenEval = 0,avgTimeTakenEval = 0, avgTimeTaken = 0, acumTimeTaken = 0;
    private int numEvals = 0, numIters = 0;
    private boolean keepIterating = true;
    private long remaining;
    private DrawingAgent drawingAgent;

    // multi-player game parameters
    private int playerID, opponentID, noPlayers;

    /**
     * Public constructor with state observation and time due.
     *
     * @param stateObs     state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer, int playerID) {
        params = new ParameterSetEvo();
        randomGenerator = new Random();
        switch(params.HEURISTIC_TYPE){
            case HEURISTIC_SIMPLESTATE: heuristic = new SimpleStateHeuristic(stateObs); break;
            case HEURISTIC_WINSCORE:
            default: heuristic = new WinScoreHeuristic(stateObs);
        }
        this.timer = elapsedTimer;

        // Get multi player game parameters
        this.playerID = playerID;
        noPlayers = stateObs.getNoPlayers();
        opponentID = (playerID+1)%noPlayers;

        // Set up drawing
        if (DrawingAgent.drawing) {
            drawingAgent = new DrawingAgent(stateObs, playerID);
        }
    }

    @Override
    public Types.ACTIONS act(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer) {

        // Set up for drawing in this game tick
        if (DrawingAgent.drawing && drawingAgent != null) {
            drawingAgent.init(stateObs, playerID);
        }

        this.timer = elapsedTimer;
        avgTimeTaken = 0;
        acumTimeTaken = 0;
        numEvals = 0;
        acumTimeTakenEval = 0;
        numIters = 0;
        remaining = timer.remainingTimeMillis();
        NUM_INDIVIDUALS = 0;
        keepIterating = true;

        // INITIALISE POPULATION
        init_pop(stateObs);


        // RUN EVOLUTION
        remaining = timer.remainingTimeMillis();
        while (remaining > avgTimeTaken && remaining > BREAK_MS && keepIterating) {
            runIteration(stateObs);
            remaining = timer.remainingTimeMillis();
        }

        // RETURN ACTION
        Types.ACTIONS best = get_best_action(population);
        return best;
    }

    /**
     * Run evolutionary process for one generation
     * @param stateObs - current game state
     */
    private void runIteration(StateObservationMulti stateObs) {
        ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();

        ParameterSetEvo params = (ParameterSetEvo)this.params;

        if (params.REEVALUATE) {
            for (int i = 0; i < params.ELITISM; i++) {
                if (remaining > 2*avgTimeTakenEval && remaining > BREAK_MS) { // if enough time to evaluate one more individual
                    evaluate(population[i], heuristic, stateObs);
                } else {keepIterating = false;}
            }
        }

        if (NUM_INDIVIDUALS > 1) {
            for (int i = params.ELITISM; i < NUM_INDIVIDUALS; i++) {
                if (remaining > 2*avgTimeTakenEval && remaining > BREAK_MS) { // if enough time to evaluate one more individual
                    Individual newind;

                    newind = crossover();
                    newind = newind.mutate(params.MUTATION);

                    // evaluate new individual, insert into population
                    add_individual(newind, nextPop, i, stateObs);

                    remaining = timer.remainingTimeMillis();

                } else {keepIterating = false; break;}
            }
            Arrays.sort(nextPop, new Comparator<Individual>() {
                @Override
                public int compare(Individual o1, Individual o2) {
                    if (o1 == null && o2 == null) {
                        return 0;
                    }
                    if (o1 == null) {
                        return 1;
                    }
                    if (o2 == null) {
                        return -1;
                    }
                    return o1.compareTo(o2);
                }
            });
        } else if (NUM_INDIVIDUALS == 1){
            Individual newind = new Individual(params.SIMULATION_DEPTH, N_ACTIONS[playerID], randomGenerator).mutate(params.MUTATION);
            evaluate(newind, heuristic, stateObs);
            if (newind.value > population[0].value)
                nextPop[0] = newind;
        }

        population = nextPop.clone();

        numIters++;
        acumTimeTaken += (elapsedTimerIteration.elapsedMillis());
        avgTimeTaken = acumTimeTaken / numIters;
    }

    /**
     * Evaluates an individual by rolling the current state with the actions in the individual
     * and returning the value of the resulting state; random action chosen for the opponent
     * @param individual - individual to be valued
     * @param heuristic - heuristic to be used for state evaluation
     * @param state - current state, root of rollouts
     * @return - value of last state reached
     */
    private double evaluate(Individual individual, StateHeuristicMulti heuristic, StateObservationMulti state) {
        ParameterSetEvo params = (ParameterSetEvo)this.params;
        ElapsedCpuTimer elapsedTimerIterationEval = new ElapsedCpuTimer();

        StateObservationMulti st = state.copy();
        int i;
        for (i = 0; i < params.SIMULATION_DEPTH; i++) {
            double acum = 0, avg;
            if (! st.isGameOver()) {
                ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();

                // Multi player advance method
                Types.ACTIONS[] advanceActs = new Types.ACTIONS[noPlayers];
                for (int k = 0; k < noPlayers; k++) {
                    if (k == playerID)
                        advanceActs[k] = action_mapping[k].get(individual.actions[i]);
                    else advanceActs[k] = action_mapping[k].get(randomGenerator.nextInt(N_ACTIONS[k]));
                }
                st.advance(advanceActs);

                // Draw simulations
                if (DrawingAgent.drawing && drawingAgent != null) {
                    Vector2d pos = state.getAvatarPosition(playerID);
                    drawingAgent.updatePosThinking(pos);
                }

                acum += elapsedTimerIteration.elapsedMillis();
                avg = acum / (i+1);
                remaining = timer.remainingTimeMillis();
                if (remaining < 2*avg || remaining < BREAK_MS) break;
            } else {
                break;
            }
        }

        StateObservationMulti first = st.copy();
        double value = heuristic.evaluateState(first, playerID);

        // Apply discount factor
        value *= Math.pow(params.DISCOUNT_FACTOR,i);

        individual.value = value;

        numEvals++;
        acumTimeTakenEval += (elapsedTimerIterationEval.elapsedMillis());
        avgTimeTakenEval = acumTimeTakenEval / numEvals;
        remaining = timer.remainingTimeMillis();

        return value;
    }

    /**
     * @return - the individual resulting from crossover applied to the specified population
     */
    private Individual crossover() {
        ParameterSetEvo params = (ParameterSetEvo)this.params;
        Individual newind = null;
        if (NUM_INDIVIDUALS > 1) {
            newind = new Individual(params.SIMULATION_DEPTH, N_ACTIONS[playerID], randomGenerator);
            Individual[] tournament = new Individual[params.TOURNAMENT_SIZE];
            Individual[] parents = new Individual[params.NO_PARENTS];

            ArrayList<Individual> list = new ArrayList<>();
            if (NUM_INDIVIDUALS > params.TOURNAMENT_SIZE) {
                list.addAll(Arrays.asList(population).subList(params.ELITISM, NUM_INDIVIDUALS));
            } else {
                list.addAll(Arrays.asList(population));
            }

            //Select a number of random distinct individuals for tournament and sort them based on value
            for (int i = 0; i < params.TOURNAMENT_SIZE; i++) {
                int index = randomGenerator.nextInt(list.size());
                tournament[i] = list.get(index);
                list.remove(index);
            }
            Arrays.sort(tournament);

            //get best individuals in tournament as parents
            if (params.NO_PARENTS <= params.TOURNAMENT_SIZE) {
                for (int i = 0; i < params.NO_PARENTS; i++) {
                    parents[i] = list.get(i);
                }
                newind.crossover(parents, params.CROSSOVER_TYPE);
            } else {
                System.out.println("WARNING: Number of parents must be LESS than tournament size.");
            }
        }
        return newind;
    }

    /**
     * Insert a new individual into the population at the specified position by replacing the old one.
     * @param newind - individual to be inserted into population
     * @param pop - population
     * @param idx - position where individual should be inserted
     * @param stateObs - current game state
     */
    private void add_individual(Individual newind, Individual[] pop, int idx, StateObservationMulti stateObs) {
        evaluate(newind, heuristic, stateObs);
        pop[idx] = newind.copy();
    }

    /**
     * Initialize population
     * @param stateObs - current game state
     */
    @SuppressWarnings("unchecked")
    private void init_pop(StateObservationMulti stateObs) {
        ParameterSetEvo params = (ParameterSetEvo)this.params;
        double remaining = timer.remainingTimeMillis();

        N_ACTIONS = new int[noPlayers];
        action_mapping = new HashMap[noPlayers];
        for (int i = 0; i < noPlayers; i++) {
            ArrayList<Types.ACTIONS> actions = stateObs.getAvailableActions(i);
            N_ACTIONS[i] = actions.size() + 1;
            action_mapping[i] = new HashMap<>();
            int k = 0;
            for (Types.ACTIONS action : actions) {
                action_mapping[i].put(k, action);
                k++;
            }
            action_mapping[i].put(k, Types.ACTIONS.ACTION_NIL);
        }

        population = new Individual[params.POPULATION_SIZE];
        nextPop = new Individual[params.POPULATION_SIZE];
        for (int i = 0; i < params.POPULATION_SIZE; i++) {
            if (i == 0 || remaining > avgTimeTakenEval && remaining > BREAK_MS) {
                population[i] = new Individual(params.SIMULATION_DEPTH, N_ACTIONS[playerID], randomGenerator);
                evaluate(population[i], heuristic, stateObs);
                remaining = timer.remainingTimeMillis();
                NUM_INDIVIDUALS = i+1;
            } else {break;}
        }

        if (NUM_INDIVIDUALS > 1)
            Arrays.sort(population, new Comparator<Individual>() {
                @Override
                public int compare(Individual o1, Individual o2) {
                    if (o1 == null && o2 == null) {
                        return 0;
                    }
                    if (o1 == null) {
                        return 1;
                    }
                    if (o2 == null) {
                        return -1;
                    }
                    return o1.compareTo(o2);
                }});
        for (int i = 0; i < NUM_INDIVIDUALS; i++) {
            if (population[i] != null)
                nextPop[i] = population[i].copy();
        }

    }

    /**
     * @param pop - last population obtained after evolution
     * @return - first action of best individual in the population (found at index 0)
     */
    private Types.ACTIONS get_best_action(Individual[] pop) {
        int bestAction = pop[0].actions[0];
        return action_mapping[playerID].get(bestAction);
    }

    public void draw(Graphics2D g) {
        if (DrawingAgent.drawing) {
            drawingAgent.draw(g, DRAW_ET);
        }
    }

}
