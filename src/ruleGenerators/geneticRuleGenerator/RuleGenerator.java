package ruleGenerators.geneticRuleGenerator;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import core.ArcadeMachine;
import core.game.SLDescription;
import core.game.StateObservation;
import core.game.GameDescription.SpriteData;
import core.generator.AbstractRuleGenerator;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.LevelAnalyzer;
import tools.LevelMapping;

public class RuleGenerator extends AbstractRuleGenerator{

	
	/** Some necessary tools to make our generator work **/
	/** Random object to help in generation **/
	private Random random;
	/** A constructor to help speed up the creation of random generators **/
	private static ruleGenerators.randomRuleGenerator.RuleGenerator randomGen;
	/** A constructor to help speed up the creation of constructive generators */
	private static ruleGenerators.constructiveRuleGenerator.RuleGenerator constructGen;
	/** contains info about sprites and current level **/
	private SLDescription sl;
	/** amount of time allowed **/
	private ElapsedCpuTimer time;
	/** Level mapping of the best chromosome **/
	private String[][] bestChromosomeRuleset;
	/** The best chromosome fitness across generations **/
	private ArrayList<Double> bestFitness;
	/** number of feasible chromosomes across generations **/
	private ArrayList<Integer> numOfFeasible;
	/** number of infeasible chromosomes across generations **/
	private ArrayList<Integer> numOfInFeasible;
	
	/**
	 * This is an evolutionary rule generator
	 * @param sl	contains information about sprites and current level
	 * @param time	amount of time allowed to generate
	 */
	public RuleGenerator(SLDescription sl, ElapsedCpuTimer time) {
		this.sl = sl;
		this.time = time;
		SharedData.usefulSprites = new ArrayList<String>();
		SharedData.random = new Random();
		SharedData.la = new LevelAnalyzer(sl);
		try {
			SharedData.output = new PrintWriter(SharedData.filename);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String[][] currentLevel = sl.getCurrentLevel();
		//Just get the useful sprites from the current level
		for(SpriteData sd : sl.getGameSprites()) {
			SharedData.usefulSprites.add(sd.name);
		}
	}
	@Override
	public String[][] generateRules(SLDescription sl, ElapsedCpuTimer time) {
		// TODO Auto-generated method stub
		return null;
	}

}
