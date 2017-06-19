package tracks.singlePlayer.advanced.olets;

/**
 * Code written by Adrien Couetoux, acouetoux@ulg.ac.be.
 * Date: 15/12/2015
 * @author Adrien Couëtoux
 */

public class SingleTreeNode {

    /**
     * Constant used in exploration part of UCB formulas
     */
    public final double K = Math.sqrt(2.);
    /**
     * A negative constant used to approximate minus infinity
     */
    private final double HUGE_NEGATIVE = -10000000.0;
    /**
     * A constant used for the epsilon-greedy exploration/exploitation policy
     */
    private final double egreedyEpsilon = 0.05;
    /**
     * A constant used to add some exploration in the expectimax exploration/exploitation policy
     */
    private final double eMaxGreedyEpsilon = 0.05;
    /**
     * The parent node (is null for the root node of a tree)
     */
    public SingleTreeNode parent;
    /**
     * The array that stores the children nodes. It should be of fixed size equal to the number of actions
     */
    public final SingleTreeNode[] children;
    /**
     * The cumulated value of a node (i.e. the sum of all observed rewards in this node)
     */
    private double totValue;
    /**
     * The expectimax value of a node (i.e. we back up the max value from its children nodes, plus the instant values
     * observed when a simulation exited in this node, weighted proportionally to the number of exits
     */
    private double expectimax;
    /**
     * Number of simulations that passed through this node
     */
    private int nVisits;
    /**
     * Action index of the action that was chosen immediately before landing in this node. Should be null for a root node
     */
    private final int actionIndex;
    /**
     * Number of times a simulation passed through this node by calling the model (i.e. the advance method)
     */
    private int nbGenerated;
    /**
     * The bias given to a node according to the avatar location - a negative score bias is given if the location has
     * been visited many times before
     */
    private double tabooBias;
    /**
     * Depth of the node in the tree
     */
    public int nodeDepth;
    /**
     * Number of simulations that passed through this node AND ended here (game over or simulation over)
     */
    private int nbExitsHere;
    /**
     * Cumulated value of simulations that have exited in this node
     */
    private double totalValueOnExit;
    /**
     * The maximum expectimax value of this node's children
     */
    private double childrenMaxAdjEmax;
    /**
     * Like expectimax, but adjusted using the observed ratio of exits vs no exits in the node
     */
    private double adjEmax;

    /**
     * Epsilon used for breaking ties
     */
    public double epsilon = 0.0001;

    /**
     * Number of actions in this node.
     */
    public int num_actions;

    /**
     * Public constructor for nodes with no declared parent node (eg. for a root node)
     */
    public SingleTreeNode(int num_actions) {
        this(null, 0, -1, 0.0, num_actions);
        nbGenerated = 0;
        nbExitsHere = 0;
        totalValueOnExit = 0.0;
        childrenMaxAdjEmax = 0.0;
        adjEmax = 0.0;
    }

    /**
     * Public constructor for nodes with a parent node
     * @param parent    the parent node
     * @param depth    the tree depth at which the node is added
     * @param actionIndex   the index of the action that was chosen immediately before creating this node
     * @param tabooBias     the location bias of this node, computed based on the avatar location
     */
    public SingleTreeNode(SingleTreeNode parent, int depth, int actionIndex, double tabooBias, int num_actions) {
        this.parent = parent;
        children = new SingleTreeNode[num_actions];
        totValue = 0.0;
        expectimax = 0.0;
        nbGenerated = 0;
        nodeDepth = depth;
        this.actionIndex = actionIndex;
        this.tabooBias = tabooBias;
        nbExitsHere = 0;
        totalValueOnExit = 0.0;
        childrenMaxAdjEmax = 0.0;
        adjEmax = 0.0;
        this.num_actions = num_actions;

    }

    public void setNodeDepth(int newDepth) {
        this.nodeDepth = newDepth;
    }

    public int getNodeDepth() {
        return (this.nodeDepth);
    }

    public int getActionIndex() { return (this.actionIndex); }

    public int getNbGenerated() { return (this.nbGenerated); }

    public void setTabooBias(double tabooBias) {this.tabooBias = tabooBias;}

    /**
     * Updates nodes attributes in a tree; mostly used to reset number of simulations to 1 to reduce the weight of past
     * simulations when salvaging a tree branch from one time step to the next
     */
    public void refreshTree() {
        for (SingleTreeNode aChildren : this.children) {
            if (!(aChildren == null)) {
                aChildren.setNodeDepth(this.getNodeDepth() + 1);
                aChildren.totValue = aChildren.totValue / aChildren.nVisits;
                aChildren.nVisits = 1;
                aChildren.expectimax = aChildren.totValue / aChildren.nVisits;
                aChildren.adjEmax = aChildren.totValue / aChildren.nVisits;
                aChildren.totalValueOnExit = aChildren.totalValueOnExit / aChildren.nVisits;
                aChildren.nbExitsHere = 1;
                aChildren.refreshTree();
                aChildren.nbGenerated = 0;
            }
        }
    }


//    public SingleTreeNode uct(StateObservation _currentObservation) {
//        SingleTreeNode selected = null;
//        double bestValue = -Double.MAX_VALUE;
//        int selectedIdx = 0;
//
//        for (int i = 0; i < this.children.length; i++) {
//            SingleTreeNode child = children[i];
//            double hvVal = child.totValue;
//            double childValue = hvVal / (child.nVisits + SingleMCTSPlayer.epsilon);
//            double uctValue = childValue +
//                    Agent.K * Math.sqrt(Math.log(this.nVisits + 1) / (child.nVisits + SingleMCTSPlayer.epsilon)) +
//                    SingleMCTSPlayer.randomGenerator.nextDouble() * SingleMCTSPlayer.epsilon;
//
//            // small sampleRandom numbers: break ties in unexpanded nodes
//            if (uctValue > bestValue) {
//                selected = child;
//                selectedIdx = i;
//                bestValue = uctValue;
//            }
//        }
//        if (selected == null) {
//            throw new RuntimeException("Warning! returning null: " + bestValue + " : " + this.children.length);
//        }
//        _currentObservation.advance(Agent.actions[selectedIdx]);
//        return selected;
//    }

//    public SingleTreeNode egreedy(StateObservation _currentObservation) {
//        SingleTreeNode selected = null;
//        int selectedIdx = 0;
//
//        if (SingleMCTSPlayer.randomGenerator.nextDouble() < egreedyEpsilon) {
//            //Choose randomly
//            selectedIdx = SingleMCTSPlayer.randomGenerator.nextInt(children.length);
//            selected = this.children[selectedIdx];
//
//        } else {
//            //pick the best Q.
//            double bestValue = -Double.MAX_VALUE;
//            for (int i = 0; i < this.children.length; i++) {
//                SingleTreeNode child = children[i];
//                double hvVal = child.totValue;
//
//                // small sampleRandom numbers: break ties in unexpanded nodes
//                if (hvVal > bestValue) {
//                    selected = child;
//                    selectedIdx = i;
//                    bestValue = hvVal;
//                }
//            }
//
//        }
//        if (selected == null) {
//            throw new RuntimeException("Warning! returning null: " + this.children.length);
//        }
//
//        _currentObservation.advance(Agent.actions[selectedIdx]);
//        return selected;
//    }

//    public SingleTreeNode eMaxGreedy(StateObservation _currentObservation) {
//        SingleTreeNode selected = null;
//        double bestValue = -Double.MAX_VALUE;
//        int selectedIdx = 0;
//
//        if (SingleMCTSPlayer.randomGenerator.nextDouble() < eMaxGreedyEpsilon) {
//            //Choose randomly
//            selectedIdx = SingleMCTSPlayer.randomGenerator.nextInt(children.length);
//            selected = this.children[selectedIdx];
//
//        } else {
//            //pick the best Q.
//            for (int i = 0; i < this.children.length; i++) {
//
//                SingleTreeNode child = children[i];
//                double hvVal = child.expectimax + Agent.K * Math.sqrt(Math.log(this.nVisits + 1) / (child.nVisits + SingleMCTSPlayer.epsilon)) + SingleMCTSPlayer.randomGenerator.nextDouble() * SingleMCTSPlayer.epsilon - this.tabooBias;
//
//                // small sampleRandom numbers: break ties in unexpanded nodes
//                if (hvVal > bestValue) {
//                    selected = child;
//                    selectedIdx = i;
//                    bestValue = hvVal;
//                }
//            }
//        }
//        if (selected == null) {
//            throw new RuntimeException("Warning! returning null: " + this.children.length);
//        }
//        _currentObservation.advance(Agent.actions[selectedIdx]);
//        return selected;
//    }

//    public double getUCTscore() {
//        return totValue / nVisits + Agent.K * Math.sqrt(Math.log(parent.nVisits + 1) / (nVisits + SingleMCTSPlayer.epsilon)) - tabooBias;
//    }

//    private double getEmaxScore() {
//        return expectimax + Agent.K * Math.sqrt(Math.log(parent.nVisits + 1) / (nVisits + SingleMCTSPlayer.epsilon)) - tabooBias;
//    }

    /**
     * Computes the weighted expectimax of a node, minus a location bias to increase the value of nodes in locations that
     * have not been visited often in the past
     * @return  the weighted expectimax with location bias
     */
    private double getAdjustedEmaxScore() {
        return (adjEmax + K * Math.sqrt(Math.log(parent.nVisits + 1) / (nVisits + epsilon)) - tabooBias);
    }

    /**
     * Backtracks along the visited branch of the tree, to update the stored data, including the expectimax values
     * @param node  the initial node of the backup (usually a tree leaf)
     * @param result    the value measured before back tracking (eg. the score when the simulation ended)
     */
    public void backUp(SingleTreeNode node, double result) {
        SingleTreeNode n = node;
        int backUpDepth = 0;
        while (n != null) {
            n.nVisits++;
            n.nbGenerated++;
            n.totValue += result;
            if (backUpDepth > 0) {
                double bestExpectimax = HUGE_NEGATIVE;
                double bestAdjustedExpectimax = HUGE_NEGATIVE;
                for (int i = 0; i < n.children.length; i++) {
                    if (n.children[i] != null) {
                        if (n.children[i].expectimax > bestExpectimax) {
                            bestExpectimax = n.children[i].expectimax;
                        }
                        if (n.children[i].adjEmax > bestAdjustedExpectimax) {
                            bestAdjustedExpectimax = n.children[i].adjEmax;
                        }
                    }
                }

                n.expectimax = bestExpectimax;
                n.childrenMaxAdjEmax = bestAdjustedExpectimax;
                n.adjEmax = (((float) n.nbExitsHere) / n.nVisits) * (n.totalValueOnExit / n.nbExitsHere) + (1.0 - (((float) n.nbExitsHere) / n.nVisits)) * n.childrenMaxAdjEmax;
            } else {
                n.nbExitsHere += 1;
                n.totalValueOnExit += result;

                n.adjEmax = (((float) n.nbExitsHere) / n.nVisits) * (n.totalValueOnExit / n.nbExitsHere) + (1.0 - (((float) n.nbExitsHere) / n.nVisits)) * n.childrenMaxAdjEmax;
                n.expectimax = n.totValue / n.nVisits;
            }

            n = n.parent;
            backUpDepth += 1;
        }
    }

    /**
     * Selects a child node, from the current node. It currently selects based on an epsilon-greedy, the greedy part
     * being made according to adjusted expectimax values
     * @return  the selected child node
     */
    public SingleTreeNode selectChild() {
        SingleTreeNode selected = null;
        double bestValue = -Double.MAX_VALUE;
        int selectedIdx;

        if (SingleMCTSPlayer.randomGenerator.nextDouble() < eMaxGreedyEpsilon) {
            //Choose randomly
            selectedIdx = SingleMCTSPlayer.randomGenerator.nextInt(children.length);
            selected = this.children[selectedIdx];
        } else {
            //pick the best Q.
            for (SingleTreeNode child : this.children) {
                //double score = child.getEmaxScore();
                double score = child.getAdjustedEmaxScore();
                //double score = child.getUCTscore();
                // small sampleRandom numbers: break ties in unexpanded nodes
                if (score > bestValue) {
                    selected = child;
//                    selectedIdx = i;
                    bestValue = score;
                }
            }
        }

        if (selected == null) {
            throw new RuntimeException("Warning! returning null: " + this.children.length);
        }
        return selected;
    }

    /**
     * Finds the action that was selected the most times
     * @return  the most selected action from the current node
     */
    public int mostVisitedAction() {
        int selected = -1;
        double bestValue = -Double.MAX_VALUE;
        boolean allEqual = true;
        double first = -1;

        for (int i = 0; i < children.length; i++) {
            if (children[i] != null) {
                if (first == -1)
                    first = children[i].nVisits;
                else if (first != children[i].nVisits) {
                    allEqual = false;
                }
                double challengerValue = children[i].nVisits + SingleMCTSPlayer.randomGenerator.nextDouble() * epsilon;
                if (challengerValue > bestValue) {
                    bestValue = challengerValue;
                    selected = i;
                }
            }
        }

        if (selected == -1) {
            System.out.println("Unexpected selection!");
            selected = 0;
        } else if (allEqual) {
            //If all are equal, we opt to choose for the one with the best Q.
            selected = bestAction();
        }
        return selected;
    }

    /**
     * Finds the action with the highest cumulative value. Used in case of a tie when comparing the number of simulations
     * @return  the action with the highest cumulative value.
     */
    private int bestAction() {
        int selected = -1;
        double bestValue = -Double.MAX_VALUE;
        for (int i = 0; i < children.length; i++) {
            if (children[i] != null && children[i].totValue + SingleMCTSPlayer.randomGenerator.nextDouble() * epsilon > bestValue) {
                bestValue = children[i].totValue;
                selected = i;
            }
        }
        if (selected == -1) {
            System.out.println("Unexpected selection!");
            selected = 0;
        }
        return selected;
    }

//    public int bestEMaxAction() {
//        int selected = -1;
//        double bestValue = -Double.MAX_VALUE;
//        for (int i = 0; i < children.length; i++) {
//
//            if (children[i] != null && children[i].getEmaxScore() > bestValue) {
//                bestValue = children[i].getEmaxScore();
//                selected = i;
//            }
//        }
//        if (selected == -1) {
//            System.out.println("Unexpected selection!");
//            selected = 0;
//        }
//        return selected;
//    }

    /**
     * Checks if the current node is fully expanded, i.e. if all actions have been selected at least once.
     * @return  true if there is an action that has not been tried yet, false otherwise.
     */
    public boolean notFullyExpanded() {
        for (SingleTreeNode tn : children) {
            if (tn == null) {
                return true;
            }
        }
        return false;
    }

//    public void printChildrenStats() {
//        for (int i = 0; i < children.length; i++) {
//
//            if (children[i] != null) {
//                System.out.format("Child number: %d%n ", i);
//                System.out.format("has a nb visit of: %d%n", children[i].nVisits);
//                System.out.format("has an expectimax of : %f%n", children[i].expectimax);
//                System.out.format("has an avg reward of : %f%n", children[i].totValue / children[i].nVisits);
//            }
//        }
//    }
}
