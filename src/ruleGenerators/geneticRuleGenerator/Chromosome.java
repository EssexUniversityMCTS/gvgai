package ruleGenerators.geneticRuleGenerator;

import java.util.ArrayList;

import core.game.StateObservation;
import core.player.AbstractPlayer;

public class Chromosome {
	private ArrayList<String>[][] level;
	/**
	 * current chromosome fitness if its a feasible
	 */
	private ArrayList<Double> fitness;
	/**
	 * current chromosome fitness if its an infeasible
	 */
	private double constrainFitness;
	/**
	 * if the fitness is calculated before (no need to recalculate)
	 */
	private boolean calculated;
	/**
	 * the best automated agent
	 */
	private AbstractPlayer automatedAgent;
	/**
	 * the naive automated agent
	 */
	private AbstractPlayer naiveAgent;
	/**
	 * the do nothing automated agent
	 */
	private AbstractPlayer doNothingAgent;
	/**
	 * The current stateObservation of the level
	 */
	private StateObservation stateObs;
	private String[][] ruleset;
	
	public Chromosome(String[][] ruleset) {
		this.ruleset = ruleset;
	}
}
