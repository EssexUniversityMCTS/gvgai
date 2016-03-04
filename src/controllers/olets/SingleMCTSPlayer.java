package controllers.olets;

import core.game.StateObservation;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.util.Random;

import tools.Vector2d;

/**
 * Code written by Adrien Couetoux, acouetoux@ulg.ac.be.
 * Date: 15/12/2015
 * @author Adrien Couëtoux
 */

public class SingleMCTSPlayer {
    /**
     * Root of the tree.
     */
    private SingleTreeNode rootNode;
    /**
     * Random generator.
     */
    public static Random randomGenerator;
    /**
     * State observation at the root of the tree.
     */
    private StateObservation rootObservation;
    /**
     * At the end of each time step, we keep the node that corresponds to the chosen action.
     */
    private SingleTreeNode salvagedTree;
    /**
     * Depth of Monte Carlo rollouts.
     */
    private final int MCTSRolloutDepth;
    /**
     * Array of past avatar positions. This is used to give a bias towards exploration of new board locations.
     */
    private final Vector2d[] pastAvatarPositions;
    /**
     * Array of past avatar orientations. This is used to give a bias towards exploration of new board locations.
     */
    private final Vector2d[] pastAvatarOrientations;
    /**
     * Number of past positions and orientations that are kept in memory for the exploration bias (see above).
     */
    private static int memoryLength;
    /**
     * Index used to know where to write the next location/orientation.
     */
    private int memoryIndex;
    /**
     * Epsilon used for breaking ties
     */
    public static double epsilon;


    /**
     * Public constructor with a sampleRandom generator object.
     *
     * @param randomGenerator sampleRandom generator object.
     */
    public SingleMCTSPlayer(Random randomGenerator) {
        SingleMCTSPlayer.randomGenerator = randomGenerator;
        this.MCTSRolloutDepth = 5;
        this.rootNode = new SingleTreeNode();
        this.salvagedTree = null;
        memoryLength = 15;
        this.pastAvatarPositions = new Vector2d[memoryLength];
        this.pastAvatarOrientations = new Vector2d[memoryLength];
        this.memoryIndex = 0;
        epsilon = 0.0001;

    }

    /**
     * Initializes the tree with the new observation state in the root.
     *
     * @param gameState current state of the game.
     */
    public void init(StateObservation gameState) {
        rootObservation = gameState;
        //Set the game observation to a newly root node.
        if (salvagedTree == null) { //if there is nothing saved from a previous time step, initialize an empty tree node
            rootNode = new SingleTreeNode();
        } else {    //else, initialize the tree with the node that was chosen, and update the memory index for past position arrays
            rootNode = salvagedTree;
            pastAvatarPositions[memoryIndex] = rootObservation.getAvatarPosition();
            pastAvatarOrientations[memoryIndex] = rootObservation.getAvatarOrientation();
            if (memoryIndex < memoryLength - 1) {
                memoryIndex += 1;
            } else {
                memoryIndex = 0;
            }
        }

    }

    /**
     * Runs MCTS to decide the action to take. It does not reset the tree.
     *
     * @param elapsedTimer Timer when the action returned is due.
     * @return the action to execute in the game.
     */
    public int run(ElapsedCpuTimer elapsedTimer) {
        mctsSearch(elapsedTimer, this.rootObservation);    //Do the search within the available time.
        int action = rootNode.mostVisitedAction();  //Determine the best action to take and return it.
        salvagedTree = rootNode.children[action];
        salvagedTree.parent = null;
        salvagedTree.setNodeDepth(0);
        salvagedTree.refreshTree();
        return action;
    }

    /**
     * Builds the search tree, given a time budget and a state observation
     *
     * @param elapsedTimer  Timer when the action returned is due
     * @param rootObservation  Initial state observation
     */
    private void mctsSearch(ElapsedCpuTimer elapsedTimer, StateObservation rootObservation) {

        double avgTimeTaken = 0;
        double acumTimeTaken = 0;
        long remaining = elapsedTimer.remainingTimeMillis();
        int numIters = 0;
        StateObservation tempState;

        int remainingLimit = 5;
        while (remaining > 2 * avgTimeTaken && remaining > remainingLimit) {
            tempState = rootObservation.copy();
            ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();

            // treepolicy: navigate from the root node until either we add a new node or we reach a final state
            SingleTreeNode selected = treePolicy(tempState);

            double delta = value(tempState, selected.getNodeDepth());
//            double delta = rollOut(tempState);

            // backing up the run in the tree
            selected.backUp(selected, delta);   //TODO : I should probably make the backup method cleaner

            numIters++;
            acumTimeTaken += (elapsedTimerIteration.elapsedMillis());

            avgTimeTaken = acumTimeTaken / numIters;
            remaining = elapsedTimer.remainingTimeMillis();
        }
    }

    /**
     * The policy that navigates through the tree.
     * @param currentObservation    the initial state observation, used as the root node
     * @return  the tree node where the tree navigation has ended (it can be a final node/state, or just the node where the policy exited the tree.
     */
    private SingleTreeNode treePolicy(StateObservation currentObservation) {
        SingleTreeNode currentNode = rootNode;
        int localDepth = 0;
        double _tabooBias;
        int i;
        boolean stateFound;

        while (!(currentObservation.isGameOver()))
        {
            if (currentNode.notFullyExpanded()) {
                return expand(currentNode, currentObservation);
            } else {
                SingleTreeNode next = currentNode.selectChild();
                currentObservation.advance(Agent.actions[next.getActionIndex()]);

                currentNode = next;
                if (currentNode.getNbGenerated() == 0) {
                    _tabooBias = 0.0;
                    i = 0;
                    stateFound = false;
                    while ((!stateFound) && (i < memoryLength) && (this.pastAvatarPositions[i] != null)) {
                        if (this.pastAvatarPositions[i].equals(currentObservation.getAvatarPosition())) {
                            _tabooBias += 0.5;
                            stateFound = true;
                        }
                        i++;
                    }
                    currentNode.setTabooBias(_tabooBias);
                    if (localDepth == 0) {
                        return currentNode;
                    }
                }
            }
            localDepth += 1;
        }
        return currentNode;
    }

    /**
     * Expands the tree, from a given node, and given a certain state observation
     * @param fatherNode    the node from which we are expanding the tree
     * @param currentObservation   the state observation that we are currently in (for *this* particular simulation)
     * @return  the new tree node that resulted from the expansion
     */
    private SingleTreeNode expand(SingleTreeNode fatherNode, StateObservation currentObservation) {
        int bestAction = 0;
        double bestValue = -1;

        for (int i = 0; i < fatherNode.children.length; i++) {
            double x = SingleMCTSPlayer.randomGenerator.nextDouble();
            if (x > bestValue && fatherNode.children[i] == null) {
                bestAction = i;
                bestValue = x;
            }
        }
        currentObservation.advance(Agent.actions[bestAction]);
        int newDepth = fatherNode.nodeDepth + 1;
        double _tabooBias = 0.0;
        int i = 0;
        boolean stateFound = false;
        while ((!stateFound) && (i < SingleMCTSPlayer.memoryLength) && (this.pastAvatarPositions[i] != null)) {
            if (this.pastAvatarPositions[i].equals(currentObservation.getAvatarPosition())) {
                //if(this.midLevelManager.pastAvatarOrientations[i].equals(nextState.getAvatarOrientation())) {
                _tabooBias += 0.5;
                stateFound = true;
                //}
            }
            i++;
        }

        SingleTreeNode tn = new SingleTreeNode(fatherNode, newDepth, bestAction, _tabooBias);
        fatherNode.children[bestAction] = tn;
        return tn;
    }

    /**
     * Computes the value associated with a state observation and a tree depth
     * @param a_gameState   the state observation that is evaluated
     * @param treeDepth    the depth in the tree where this evaluation is made
     * @return  the value of the state
     */
    private double value(StateObservation a_gameState, int treeDepth) {

        boolean gameOver = a_gameState.isGameOver();
        Types.WINNER win = a_gameState.getGameWinner();
        double rawScore = a_gameState.getGameScore();

        if (gameOver && win == Types.WINNER.PLAYER_LOSES) {
            return (rawScore - (2000.0 / Math.pow(1.0 + treeDepth, 2)) * (1.0 + Math.abs(rawScore)));
        }

        if (gameOver && win == Types.WINNER.PLAYER_WINS)
            return (rawScore + 100.0 * (1.0 + Math.abs(rawScore)));

        return rawScore;
    }

    /**
     * Plays random moves until the simulation ends (either because we do MCTSRolloutDepth moves, or the game ends)
     *
     * @param _currentObservation   the initial state observation
     * @return  the final value after playing the random moves
     */
    public double rollOut(StateObservation _currentObservation)
    {
        int rolloutDepth = 0;
        while (!finishRollout(_currentObservation,rolloutDepth)) {
            int action = randomGenerator.nextInt(Agent.NUM_ACTIONS);
            _currentObservation.advance(Agent.actions[action]);
            rolloutDepth++;
        }

        return value(_currentObservation, rolloutDepth);
    }

    /**
     * Checks if a rollout should end or not
     * @param rollerState   the current state observation
     * @param depth     the current depth of the rollout
     * @return  the value in the last reached state
     */
    private boolean finishRollout(StateObservation rollerState, int depth)
    {
        if(depth >= MCTSRolloutDepth )      //rollout end condition.
            return true;
        return rollerState.isGameOver();
    }


}
