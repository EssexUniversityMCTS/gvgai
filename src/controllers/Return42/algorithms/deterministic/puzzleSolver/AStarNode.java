package controllers.Return42.algorithms.deterministic.puzzleSolver;

import ontology.Types;
import ontology.Types.ACTIONS;

/**
 * Created by Oliver on 03.05.2015.
 */
public class AStarNode implements Comparable<AStarNode> {

	private final double h;
	
    private AStarNode prev;
    private Types.ACTIONS[] lastActions;
    private double g;
    private int depth;

    public AStarNode( AStarNode prev, Types.ACTIONS[] lastActions, double g, double h, int depth ) {
        this.prev = prev;
        this.lastActions = lastActions;
        this.g = g;
        this.h = h;
        this.depth = depth;
    }

    public double getF() {
        return g+h;
    }

    public double getG() {
        return g;
    }

    public AStarNode getPrev() {
        return prev;
    }

    public Types.ACTIONS[] getLastActions() {
        return lastActions;
    }

    public int getDepth() {
        return depth;
    }

    @Override
    public int compareTo( AStarNode other ) {
        return Double.compare( this.getF(), other.getF() );
    }

	public void updatePredecessor( AStarNode prev, ACTIONS[] lastActions, double g, int depth ) {
        this.prev = prev;
        this.lastActions = lastActions;
        this.g = g;
        this.depth = depth;
	}

}
