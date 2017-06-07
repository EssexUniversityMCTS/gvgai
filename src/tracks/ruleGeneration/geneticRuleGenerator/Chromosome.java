package tracks.ruleGeneration.geneticRuleGenerator;
import java.util.*;

import core.game.SLDescription;
import core.game.StateObservation;
import core.game.Event;
import core.game.GameDescription.SpriteData;
import core.game.Observation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.LevelAnalyzer;
import tools.Vector2d;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import java.util.Arrays;

public class Chromosome implements Comparable<Chromosome>{
	/**
	 * current chromosome fitness if its a feasible
	 */
	private ArrayList<Double> fitness;
	/**
	 * current chromosome fitness if its an infeasible
	 */
	private double constrainFitness;
	
	/**
	 * keeps track of how many bad frames there are during playthoughs
	 */
	private int badFrames;
	
	/*
	 * contains how many errors came out of the build test
	 */
	private int errorCount;
	
	/**
	 * the ruleset this chromosome contains
	 */
	private String[][] ruleset;
	/**
	 * the SL description of this chromosome
	 */
	private SLDescription sl;
	/**
	 * elapsed time
	 */
	private ElapsedCpuTimer time;
	/**
	 * amount of steps allowed for the naive agent to sit around
	 */
	private int FEASIBILITY_STEP_LIMIT = 40;

	private int doNothingLength;
	StateObservation doNothingState;
	StateObservation bestState;
	ArrayList<Types.ACTIONS> bestSol;
	
	/**
	 * Chromosome constructor.  Holds the ruleset and initializes agents within
	 * @param ruleset	the ruleset the chromosome contains
	 * @param sl		the game description
	 * @param time		elapsed time
	 */

	public Chromosome(String[][] ruleset, SLDescription sl) {
		this.ruleset = ruleset;
		this.sl = sl;
		this.fitness = new ArrayList<Double>();
		fitness.add(0.0);
		fitness.add(0.0);
		this.badFrames = 0;
	}
	/**
	 * Flips a coin to see if we mutate on termination or interaction
	 */
	public void mutate() {
		// loop through as many times as we want to mutate
		int mutationCount = SharedData.random.nextInt(SharedData.MUTATION_AMOUNT) + 1;
		for(int i = 0; i < mutationCount; i++) {
			int mutateR = SharedData.random.nextInt(2);
			if(mutateR == 0){
				//mutate interaction set
				mutateInteraction();
			} else {
				// mutate termination
				mutateTermination();
			}
		}
	}
	/**
	 * performs a mutation on a random interaction in the set
	 * 4 types of mutation: insert a new rule, delete an old rule, change a rule, and change rule parameters (but keep the rule)
	 * the interaction ruleset will shift back and forth between an array and an arraylist depending on the circumstances
	 * according to what is easiest to manipulate at the time
	 */
	public void mutateInteraction() {
		ArrayList<String> interactionSet = new ArrayList<>( Arrays.asList(ruleset[0]));
		double mutationType = SharedData.random.nextDouble();
		// we do an insertion
		if(mutationType < SharedData.INSERTION_PROB) {
			// roll dice to see if we will insert a new rule altogether or a new parameter into an existing rule
			double roll = SharedData.random.nextDouble();
			// insert a new parameter onto an existing rule
			if(roll < SharedData.INSERT_PARAM_PROB) {
				// grab a random existing rule
				int point = SharedData.random.nextInt(interactionSet.size());
				String addToMe = interactionSet.get(point);
				// insert a new parameter into it
				String nParam = SharedData.interactionParams[SharedData.random.nextInt(SharedData.interactionParams.length)];
				nParam += "=";
				
				// there are two types of parameters, ones that take sprites and ones that take values
				if(nParam.equals("scoreChange=") || nParam.equals("limit=") || nParam.equals("value=") || nParam.equals("geq=")
						|| nParam.equals("leq=")) {
					int val = SharedData.random.nextInt(SharedData.NUMERICAL_VALUE_PARAM) - 1000;
					nParam += val;
				} else {
					String nSprite = SharedData.usefulSprites.get(SharedData.random.nextInt(SharedData.usefulSprites.size()));
					nParam += nSprite;
				}
				addToMe += " " + nParam;
				// replace the old rule with the modified one
				ruleset[0][point] = addToMe;
			}
			// insert an entirely new rule, possibly with a parameter in it
			else {
				String nInteraction = SharedData.interactions[SharedData.random.nextInt(SharedData.interactions.length)];
				int i1 = SharedData.random.nextInt(SharedData.usefulSprites.size());
			    int i2 = (i1 + 1 + SharedData.random.nextInt(SharedData.usefulSprites.size() - 1)) % SharedData.usefulSprites.size();
			    
			    String newInteraction = SharedData.usefulSprites.get(i1) + " " + SharedData.usefulSprites.get(i2) + " > " + nInteraction;
			    // roll to see if you insert a parameter into this interaction
			    roll = SharedData.random.nextDouble();
			    
			    if(roll < SharedData.INSERT_PARAM_PROB) {
			    	String nParam = SharedData.interactionParams[SharedData.random.nextInt(SharedData.interactionParams.length)];
					nParam += "=";
					
					// there are two types of parameters, ones that take sprites and ones that take values
					if(nParam.equals("scoreChange=") || nParam.equals("limit=") || nParam.equals("value=") || nParam.equals("geq=")
							|| nParam.equals("leq=")) {
						int val = SharedData.random.nextInt(SharedData.NUMERICAL_VALUE_PARAM) - 1000;
						nParam += val;
					} else {
						String nSprite = SharedData.usefulSprites.get(SharedData.random.nextInt(SharedData.usefulSprites.size()));
						nParam += nSprite;
					}
					newInteraction += " " + nParam;
			    }
			    // add the new interaction to the interaction set
			    interactionSet.add(newInteraction);
			    // remove weird space from the arrayList
			    interactionSet.removeIf(s -> s == null);
			    // stream the list back into itself to avoid duplicate rules from having been created
				interactionSet = (ArrayList<String>) interactionSet.stream().distinct().collect(Collectors.toList());
				// redefine the interaction array with the interaction array list
				ruleset[0] = new String[interactionSet.size()];
				ruleset[0] = interactionSet.toArray(ruleset[0]);
			}
		} 
		// we do a deletion
		else if(mutationType < SharedData.DELETION_PROB + SharedData.INSERTION_PROB) {
			// roll dice to see if we will delete a rule altogether or a parameter of an existing rule
			double roll = SharedData.random.nextDouble();
			// delete a parameter from an existing rule
			if(roll < SharedData.DELETE_PARAM_PROB) {
				int point = SharedData.random.nextInt(interactionSet.size());
				String deleteFromMe = interactionSet.get(point);
				// find all parameters for this rule, note: there may be none.  In that case we do nothing.
				String[] splitDeleteFromMe = deleteFromMe.split("\\s+");
				ArrayList<String> params = new ArrayList<String>();
				for(String param : splitDeleteFromMe) {
					// we can assume that if one of the split strings contains an = sign that it is a parameter
					if(param.contains("=")){
						params.add(param);
					}
				}
				// if no params do nothing
				if(params.size() == 0) {
					
				} 
				// if one param, remove it
				else if(params.size() == 1) {
					String fixedRule = "";
					for(String part : splitDeleteFromMe) {
						if(!part.contains("=")) {
							fixedRule += part + " ";
						}
					}
					interactionSet.set(point, fixedRule);
				}
				else {
					// pick one of the rules and don't include it, but include the others
					int rule = SharedData.random.nextInt(params.size());
					String fixedRule = "";
					for(String part : splitDeleteFromMe) {
						if(!part.equals(params.get(rule))) {
							fixedRule += part + " ";
						}
					}
					interactionSet.set(point, fixedRule);
				}
			    // remove weird space from the arrayList
			    interactionSet.removeIf(s -> s == null);
			    // stream the list back into itself to avoid duplicate rules from having been created
				interactionSet = (ArrayList<String>) interactionSet.stream().distinct().collect(Collectors.toList());
				// redefine the interaction array with the interaction array list
				ruleset[0] = new String[interactionSet.size()];
				ruleset[0] = interactionSet.toArray(ruleset[0]);
			}
			// delete an entire rule from the interaction set
			else{
				int point = SharedData.random.nextInt(interactionSet.size());
				// dont try to delete from an empty interaction set
				if (interactionSet.size() > 1) {
					interactionSet.remove(point);
				}
			    // remove weird space from the arrayList
			    interactionSet.removeIf(s -> s == null);
			    // stream the list back into itself to avoid duplicate rules from having been created
				interactionSet = (ArrayList<String>) interactionSet.stream().distinct().collect(Collectors.toList());
				// redefine the interaction array with the interaction array list
				ruleset[0] = new String[interactionSet.size()];
				ruleset[0] = interactionSet.toArray(ruleset[0]);
			}
		} 
		// modify a rule from the interaction set by changing its parameters
		else if (mutationType < SharedData.MODIFY_RULE_PROB + SharedData.DELETION_PROB + SharedData.INSERTION_PROB) {
			// pick our modified rule
			int point = SharedData.random.nextInt(interactionSet.size());
			
			// roll to see what kind of modification, either a rule change or a parameter change
			double roll = SharedData.random.nextDouble();
			// modify a parameter of a rule completely
			if(roll < SharedData.MODIFY_PARAM_PROB) {
				String modifyFromMe = interactionSet.get(point);
				// find all parameters for this rule, note: there may be none.  In that case we do nothing.
				String[] splitModifyFromMe = modifyFromMe.split("\\s+");
				ArrayList<String> ps = new ArrayList<String>();
				for(String param : splitModifyFromMe) {
					// we can assume that if one of the split strings contains an = sign that it is a parameter
					if(param.contains("=")){
						ps.add(param);
					}
				}
				// if no params do nothing
				if(ps.size() == 0) {
					
				} else {
					// pick one of the rules and don't include it, but include the others
					int rule = SharedData.random.nextInt(ps.size());
					String fixedRule = "";
					for(String part : splitModifyFromMe) {
						if(!part.equals(ps.get(rule))) {
							fixedRule += part + " ";
						} 
						// we are on the parameter we want to replace
						else {
							String nParam = SharedData.interactionParams[SharedData.random.nextInt(SharedData.interactionParams.length)];
							nParam += "=";
							// there are two types of parameters, ones that take sprites and ones that take values
							if(nParam.equals("scoreChange=") || nParam.equals("limit=") || nParam.equals("value=") || nParam.equals("geq=")
									|| nParam.equals("leq=")) {
								int val = SharedData.random.nextInt(SharedData.NUMERICAL_VALUE_PARAM) - 1000;
								nParam += val;
							} else {
								String nSprite = SharedData.usefulSprites.get(SharedData.random.nextInt(SharedData.usefulSprites.size()));
								nParam += nSprite;
							}
							
							fixedRule += nParam + " ";
						}
					}
					interactionSet.set(point, fixedRule);
				}
			    // remove weird space from the arrayList
			    interactionSet.removeIf(s -> s == null);
			    // stream the list back into itself to avoid duplicate rules from having been created
				interactionSet = (ArrayList<String>) interactionSet.stream().distinct().collect(Collectors.toList());
				// redefine the interaction array with the interaction array list
				ruleset[0] = new String[interactionSet.size()];
				ruleset[0] = interactionSet.toArray(ruleset[0]);
			} 
			// modify a rule, but leave the parameters and sprites
			else {
				String newRule = SharedData.interactions[SharedData.random.nextInt(SharedData.interactions.length)];
				String modRule = ruleset[0][point];
				
				String[] splitModRule = modRule.split("\\s+");
				// replace old rule with new one
				splitModRule[3] = newRule;
				newRule = "";
				for(String part : splitModRule) {
					newRule += part + " ";
				}
				ruleset[0][point] = newRule;
			}
		} 
		// we should never ever reach this point
		else {
			System.err.println("What?! How did we even get here!?");
		}
	}
	/**
	 * performs a mutation on a random termination in the set
	 * 4 types of mutation: insert a new rule, delete an old rule, change a rule, and change rule parameters (but keep the rule)
	 * the termination ruleset will shift back and forth between an array and an arraylist depending on the circumstances
	 * according to what is easiest to manipulate at the time. 
	 */
	public void mutateTermination() {
		ArrayList<String> terminationSet = new ArrayList<>( Arrays.asList(ruleset[1]));
		double mutationType = SharedData.random.nextDouble();
		// we do an insertion
		if(mutationType < SharedData.INSERTION_PROB) {
			// roll dice to see if we will insert a new rule altogether or a new parameter into an existing rule
			double roll = SharedData.random.nextDouble();
			// insert a new parameter onto an existing rule
			if(roll < SharedData.INSERT_PARAM_PROB) {
				// grab a random existing rule
				int point = SharedData.random.nextInt(terminationSet.size());
				String addToMe = terminationSet.get(point);
				// insert a new parameter into it
				String nParam = SharedData.terminationParams[SharedData.random.nextInt(SharedData.terminationParams.length)];
				nParam += "=";
				// add either a number or a sprite to the parameter
				double roll1 = SharedData.random.nextDouble();
				// insert a sprite
				if(roll1 < SharedData.PARAM_NUM_OR_SPRITE_PROB) {
					String nSprite = SharedData.usefulSprites.get(SharedData.random.nextInt(SharedData.usefulSprites.size()));
					nParam += nSprite;
				}
				// insert a numerical value
				else {
					int val = SharedData.random.nextInt(SharedData.NUMERICAL_VALUE_PARAM);
					nParam += val;
				}
				addToMe += " " + nParam;
				// replace the old rule with the modified one
				ruleset[1][point] = addToMe;
				
				// DEBUG CODE loop through terminations and find a bug
				for(int i = 0; i < this.ruleset[1].length; i++) {
					if(ruleset[1][i].contains("limit= ")) {
						System.out.println("Broken");
					}
				
				}
			}
			// insert an entirely new rule, possibly with a parameter in it
			else {
				String nTermination = SharedData.terminations[SharedData.random.nextInt(SharedData.terminations.length)];    
				
				
				// roll to see if we include a parameter from the termination parameter set
				double roll1 = SharedData.random.nextDouble();
				if(roll < SharedData.INSERT_PARAM_PROB) {
					String nParam = SharedData.terminationParams[SharedData.random.nextInt(SharedData.terminationParams.length)];
					nParam += "=";
					// add either a number or a sprite to the parameter only two types
					double roll2 = SharedData.random.nextDouble();
					// insert a sprite
					String nSprite = SharedData.usefulSprites.get(SharedData.random.nextInt(SharedData.usefulSprites.size()));
					nParam += nSprite;
					
					nTermination+= " " + nParam;
				}
				// add win and limit
				nTermination += " win=";
				
				double roll2 = SharedData.random.nextDouble();
				if(roll2 < SharedData.WIN_PARAM_PROB){
					nTermination += "True";
				} else {
					nTermination += "False";
				}
				// special rules for Timeout rule
				if(nTermination.contains("Timeout")) {
					int val = SharedData.random.nextInt(SharedData.TERMINATION_LIMIT_PARAM) + 500;
					nTermination += " limit="+val;
				} else{
					int val = SharedData.random.nextInt(SharedData.TERMINATION_LIMIT_PARAM);
					nTermination += " limit="+val;
				}
			    // add the new termination to the termination set
			    terminationSet.add(nTermination);
			    // remove weird space from the arrayList
			    terminationSet.removeIf(s -> s == null);
			    // stream the list back into itself to avoid duplicate rules from having been created
				terminationSet = (ArrayList<String>) terminationSet.stream().distinct().collect(Collectors.toList());
				// redefine the termination array with the termination array list
				ruleset[1] = new String[terminationSet.size()];
				ruleset[1] = terminationSet.toArray(ruleset[1]);
				
				
				// DEBUG CODE loop through terminations and find a bug
				for(int i = 0; i < this.ruleset[1].length; i++) {
					if(ruleset[1][i].contains("limit= ")) {
						System.out.println("Broken");
					}
				
				}
			}
		} 
		// we do a deletion
		else if(mutationType < SharedData.DELETION_PROB + SharedData.INSERTION_PROB) {
			// roll dice to see if we will delete a rule altogether or a parameter of an existing rule
			double roll = SharedData.random.nextDouble();
			// delete a parameter from an existing rule
			if(roll < SharedData.DELETE_PARAM_PROB) {
				int point = SharedData.random.nextInt(terminationSet.size());
				String deleteFromMe = terminationSet.get(point);
				// find all parameters for this rule, note: there may be none.  In that case we do nothing.
				String[] splitDeleteFromMe = deleteFromMe.split("\\s+");
				ArrayList<String> params = new ArrayList<String>();
				for(String param : splitDeleteFromMe) {
					// we can assume that if one of the split strings contains an = sign that it is a parameter
					// the extra rule here is that it is not a "limit" or a "win" param. We cannot remove those!
					if(param.contains("=") && !param.contains("limit") && !param.contains("win")){
						params.add(param);
					}
				}
				// if no params do nothing
				if(params.size() == 0) {
					
				} 
				else {
					// pick one of the rules and don't include it, but include the others
					int rule = SharedData.random.nextInt(params.size());
					String fixedRule = "";
					for(String part : splitDeleteFromMe) {
						if(!part.equals(params.get(rule))) {
							fixedRule += part + " ";
						}
					}
					terminationSet.set(point, fixedRule);
				}
			    // remove weird space from the arrayList
				terminationSet.removeIf(s -> s == null);
			    // stream the list back into itself to avoid duplicate rules from having been created
				terminationSet = (ArrayList<String>) terminationSet.stream().distinct().collect(Collectors.toList());
				// redefine the interaction array with the interaction array list
				ruleset[1] = new String[terminationSet.size()];
				ruleset[1] = terminationSet.toArray(ruleset[1]);
				
				// DEBUG CODE loop through terminations and find a bug
				for(int i = 0; i < this.ruleset[1].length; i++) {
					if(ruleset[1][i].contains("limit= ")) {
						System.out.println("Broken");
					}
				
				}
			}
			// delete an entire rule from the interaction set
			else{
				int point = SharedData.random.nextInt(terminationSet.size());
				// dont try to delete from an empty interaction set
				if (terminationSet.size() > 1) {
					terminationSet.remove(point);
				}
			    // remove weird space from the arrayList
				terminationSet.removeIf(s -> s == null);
			    // stream the list back into itself to avoid duplicate rules from having been created
				terminationSet = (ArrayList<String>) terminationSet.stream().distinct().collect(Collectors.toList());
				// redefine the interaction array with the interaction array list
				ruleset[1] = new String[terminationSet.size()];
				ruleset[1] = terminationSet.toArray(ruleset[1]);
				
				// DEBUG CODE loop through terminations and find a bug
				for(int i = 0; i < this.ruleset[1].length; i++) {
					if(ruleset[1][i].contains("limit= ")) {
						System.out.println("Broken");
					}
				
				}
			}
		} 
		// modify a rule from the interaction set by changing its parameters
		else if (mutationType < SharedData.MODIFY_RULE_PROB + SharedData.DELETION_PROB + SharedData.INSERTION_PROB) {
			// pick our modified rule
			int point = SharedData.random.nextInt(terminationSet.size());
			
			// roll to see what kind of modification, either a rule change or a parameter change
			double roll = SharedData.random.nextDouble();
			// modify a parameter of a rule completely
			if(roll < SharedData.MODIFY_PARAM_PROB) {
				String modifyFromMe = terminationSet.get(point);
				// find all parameters for this rule, note: there may be none.  In that case we do nothing.
				String[] splitModifyFromMe = modifyFromMe.split("\\s+");
				ArrayList<String> ps = new ArrayList<String>();
				for(String param : splitModifyFromMe) {
					// we can assume that if one of the split strings contains an = sign that it is a parameter
					// we can change limit and win parameters now (but this will cause us to have special rules)!
					if(param.contains("=")){
						ps.add(param);
					}
				}
				// if no params do nothing
				if(ps.size() == 0) {
					
				} else {
					// pick one of the rules and don't include it, but include the others
					int rule = SharedData.random.nextInt(ps.size());
					String fixedRule = "";
					for(String part : splitModifyFromMe) {
						if(!part.equals(ps.get(rule))) {
							fixedRule += part + " ";
						} 
						// we are on the parameter we want to modify
						else {
							String nParam = ""; 
							if(part.contains("win")) {
								nParam = "win=";
								// roll dice to see if true or false
								double roll2 = SharedData.random.nextDouble();
								if(roll2 < SharedData.WIN_PARAM_PROB) {
									nParam += "True";
								} else {
									nParam += "False";
								}
							} else if(part.contains("limit")) {
								nParam = "limit=";
								// if this is a timeout rule, special conditions apply,  make so limit is at least 500
								if(fixedRule.contains("Timeout")) {
									int roll2 = SharedData.random.nextInt(SharedData.TERMINATION_LIMIT_PARAM) + 500;
									nParam += roll2;
								}
								else{
									// roll dice to see how high the new limit is
									int roll2 = SharedData.random.nextInt(SharedData.TERMINATION_LIMIT_PARAM);
									nParam += roll2;
								}
							} else {
								// pick a new parameter
								nParam = SharedData.terminationParams[SharedData.random.nextInt(SharedData.terminationParams.length)] + "=";
								// insert a sprite
								String nSprite = SharedData.usefulSprites.get(SharedData.random.nextInt(SharedData.usefulSprites.size()));
								nParam += nSprite;
							}
							fixedRule += nParam + " ";
						}
					}
					terminationSet.set(point, fixedRule);
				}
			    // remove weird space from the arrayList
				terminationSet.removeIf(s -> s == null);
			    // stream the list back into itself to avoid duplicate rules from having been created
				terminationSet = (ArrayList<String>) terminationSet.stream().distinct().collect(Collectors.toList());
				// redefine the interaction array with the interaction array list
				ruleset[1] = new String[terminationSet.size()];
				ruleset[1] = terminationSet.toArray(ruleset[1]);
				
				// DEBUG CODE loop through terminations and find a bug
				for(int i = 0; i < this.ruleset[1].length; i++) {
					if(ruleset[1][i].contains("limit= ")) {
						System.out.println("Broken");
					}
				
				}
			} 
			// modify a rule, but leave the parameters and sprites
			else {
				String newRule = SharedData.terminations[SharedData.random.nextInt(SharedData.terminations.length)];
				String modRule = ruleset[1][point];
				
				String[] splitModRule = modRule.split("\\s+");
				// replace old rule with new one
				splitModRule[0] = newRule;
				newRule = "";
				for(String part : splitModRule) {
					newRule += part + " ";
				}
				ruleset[1][point] = newRule;
				
				// DEBUG CODE loop through terminations and find a bug
				for(int i = 0; i < this.ruleset[1].length; i++) {
					if(ruleset[1][i].contains("limit= ")) {
						System.out.println("Broken");
					}
				
				}
			}
		} 
		// we should never ever reach this point
		else {
			System.err.println("What?! Howd we even get here!?");
		}
		

	}
	/**
	 * clone the chromosome data
	 */
	public Chromosome clone(){
		// copy ruleset into nRuleset. Two for loops, in case 2d array is jagged
		String[][] nRuleset = new String[ruleset.length][];
		nRuleset[0] = new String[ruleset[0].length];
		nRuleset[1] = new String[ruleset[1].length];
		for(int i = 0; i < ruleset[0].length; i++) {
			nRuleset[0][i] = ruleset[0][i];
		}
		for(int i = 0; i < ruleset[1].length; i++) {
			nRuleset[1][i] = ruleset[1][i];
		}
		Chromosome c = new Chromosome(nRuleset, sl);
		return c;
	}

	public void cleanseChromosome() {
		Set<String> cleanser = new HashSet<String>();
		// read the rulest into the Set cleanser
		for(int i = 0; i < ruleset[0].length; i++) {
			cleanser.add(ruleset[0][i]);
		}
		ruleset[0] = new String[0];

		// read the cleanser back into the ruleset
		ruleset[0] = cleanser.toArray(ruleset[0]);
		
		// read the termination set into the Set cleanser
		cleanser = new HashSet<String>();
		for(int i = 0; i < ruleset[1].length; i++) {
			cleanser.add(ruleset[1][i]);
		}
		ruleset[1] = new String[0];
		// read the cleanser back into the ruleset
		ruleset[1] = cleanser.toArray(ruleset[1]);
		
		// check termination set for an end if player dies
		boolean hasCondition = false;
		SpriteData[] avatarName = SharedData.la.getAvatars(false);
		for(int i = 0; i < ruleset[1].length; i++) {
			if(ruleset[1][i].contains("SpriteCounter") && ruleset[1][i].contains("stype="+avatarName[0].name) && ruleset[1][i].contains("limit=0")) {
				hasCondition = true;
				break;
			}
		}
		// if the condition doesnt exist, make it so
		if(!hasCondition) {
			String[] tempTerm = new String[ruleset[1].length + 1];
			for(int j = 0; j < ruleset[1].length; j++) {
				tempTerm[j] = ruleset[1][j];
			}
			// add condition
			String termy = "SpriteCounter stype=" + avatarName[0].name + " limit=0 win=";
			// roll for win or lose condition
			int roll = SharedData.random.nextInt(2);
			if(roll == 1) {
				termy += "True";
			} else {
				termy += "False";
			}
			tempTerm[tempTerm.length - 1] = termy;
			// replace ruleset with updated one
			ruleset[1] = tempTerm;
		}
	}
	/**
	 * first checks to see if there are no build errors, if there are, this is infeasible. 
	 * Otherwise, it will check to see if a do nothing agent dies within the first 40 steps of playing. 
	 * if it does, this is infeasible.
	 * @return
	 */
	private StateObservation feasibilityTest() {
		HashMap<String, ArrayList<String>> spriteSetStruct = SharedData.constGen.getSpriteSetStructure();
		StateObservation state = sl.testRules(ruleset[0], ruleset[1], spriteSetStruct);		
		errorCount = sl.getErrors().size();
		constrainFitness = 0;
		constrainFitness += (0.5) * 1.0 / (errorCount + 1.0);	
		if(constrainFitness >= 0.5) {
			doNothingLength = Integer.MAX_VALUE;
			for(int i = 0; i < SharedData.REPETITION_AMOUNT; i++) {
				int temp = this.getAgentResult(state.copy(), FEASIBILITY_STEP_LIMIT, SharedData.doNothingAgent);
				if(temp < doNothingLength){
					doNothingLength = temp;
				}
			}
			constrainFitness += 0.2 * (doNothingLength / (40.0));
			
			this.fitness.set(0, constrainFitness);

		}
		return state;
	}
	
	
	/**
	 * calculates the fitness, by comparing the scores of a naiveAI and a smart AI
	 * @param time	how much time to evaluate the chromosome
	 */
	public void calculateFitness(long time) {
		
		// reset bad frames
		this.badFrames = 0;
		// unique events that occurred in all the game simulations
		Set<String> events = new HashSet<String>();
		StateObservation stateObs = feasibilityTest();
		if(constrainFitness < 0.7) {
			// failed feasibility
			this.fitness.set(0, constrainFitness);
		}
		else {					
			//Play the game using the best agent
			double score = -200;
			ArrayList<Vector2d> SOs = new ArrayList<>();
			// protects the fitness evaluation from looping forever
	
			// big vars
			// keeps track of total number of simulated frames
			int frameCount = 0;
			// Best Agent
			double agentBestScore = Double.NEGATIVE_INFINITY;
			double automatedScoreSum = 0.0;
			double automatedWinSum = 0.0;
			int bestSolutionSize = 0;
			for(int i=0; i<SharedData.REPETITION_AMOUNT; i++){
				StateObservation tempState = stateObs.copy();
				cleanOpenloopAgents();
				int temp = getAgentResult(tempState, SharedData.EVALUATION_STEP_COUNT, SharedData.automatedAgent);
				// add temp to framesCount
				frameCount += temp;
				
				if(tempState.getGameScore() > agentBestScore) {
					agentBestScore = tempState.getGameScore();
					bestState = tempState;
					bestSolutionSize = temp;
				}
				
				score = tempState.getGameScore();
				automatedScoreSum += score;
				if(tempState.getGameWinner() == Types.WINNER.PLAYER_WINS){
					automatedWinSum += 1;
				} else if(tempState.getGameWinner() == Types.WINNER.NO_WINNER) {
					automatedWinSum += 0.5;
				}
				
				TreeSet s1 = tempState.getEventsHistory();
				Iterator<Event> iter1 = s1.iterator();
				while(iter1.hasNext()) {
					Event e = iter1.next();
					events.add(e.activeTypeId + "" + e.passiveTypeId);
				}
				score = -200;
			}
			 
			// Random Agent
			score = -200;
			 
			double randomScoreSum = 0.0;
			double randomWinSum = 0.0;
			StateObservation randomState = null;
			for(int i=0; i<SharedData.REPETITION_AMOUNT; i++){
				StateObservation tempState = stateObs.copy();
				int temp = getAgentResult(tempState, bestSolutionSize, SharedData.randomAgent);
				// add temp to framesCount
				frameCount += temp;
				randomState = tempState;
				
				score = randomState.getGameScore();
				
				randomScoreSum += score;
				if(randomState.getGameWinner() == Types.WINNER.PLAYER_WINS){
					randomWinSum += 1;
				} else if(randomState.getGameWinner() == Types.WINNER.NO_WINNER) {
					randomWinSum += 0.5;
				}
				
				// gather all unique interactions between objects in the naive agent
				TreeSet s1 = randomState.getEventsHistory();
				Iterator<Event> iter1 = s1.iterator();
				while(iter1.hasNext()) {
					Event e = iter1.next();
					events.add(e.activeTypeId + "" + e.passiveTypeId);
				}
				score = -200;
			}
			
			// Naive agent
			score = -200;
			StateObservation naiveState = null;
			double naiveScoreSum = 0.0;
			double naiveWinSum = 0.0;
			//playing the game using the naive agent
			for(int i=0; i<SharedData.REPETITION_AMOUNT; i++){
				StateObservation tempState = stateObs.copy();
				int temp = getAgentResult(tempState, bestSolutionSize, SharedData.naiveAgent);
				// add temp to framesCount
				frameCount += temp;
				naiveState = tempState;
				
				score = naiveState.getGameScore();
				if(score > -100) {
					naiveScoreSum += score;
					if(naiveState.getGameWinner() == Types.WINNER.PLAYER_WINS){
						naiveWinSum += 1;
					} else if(naiveState.getGameWinner() == Types.WINNER.NO_WINNER) {
						naiveWinSum += 0.5;
					}
				}
				
				// gather all unique interactions between objects in the best agent
				TreeSet s1 = naiveState.getEventsHistory();
				Iterator<Event> iter1 = s1.iterator();
				while(iter1.hasNext()) {
					Event e = iter1.next();
					events.add(e.activeTypeId + "" + e.passiveTypeId);
					}
				score = -200;
			}
			double badFramePercent = badFrames / (1.0 * frameCount);
//			if(badFramePercent > .3) {
//				// if we have bad frames, this is still not a good game
//				constrainFitness += 0.3 * (1 - badFrames / (1.0 * frameCount));
//				this.fitness.set(0, constrainFitness);
//			}
//			else {
				// find average scores and wins across playthroughs
				double avgBestScore = automatedScoreSum / SharedData.REPETITION_AMOUNT;
				double avgNaiveScore = naiveScoreSum / SharedData.REPETITION_AMOUNT;
				double avgRandomScore = randomScoreSum / SharedData.REPETITION_AMOUNT;
				
				double avgBestWin = automatedWinSum / SharedData.REPETITION_AMOUNT;
				double avgNaiveWin = naiveWinSum / SharedData.REPETITION_AMOUNT;
				double avgRandomWin = randomWinSum / SharedData.REPETITION_AMOUNT;
				
				// calc sigmoid function with the score as "t"
				double sigBest = 1 / (1 + Math.pow(Math.E, (0.1) * -avgBestScore));
				double sigNaive = 1 / (1 + Math.pow(Math.E, (0.1) * -avgNaiveScore));
				double sigRandom = 1 / (1 + Math.pow(Math.E, (0.1) * -avgRandomScore));
				
				// sum weighted win and sig-score values
				double summedBest = 0.9 * avgBestWin + 0.1 * sigBest;
				double summedNaive = 0.9 * avgNaiveWin + 0.1 * sigNaive;
				double summedRandom = 0.9 * avgRandomWin + 0.1 * sigRandom;
	
				// calc game score differences
				double gameScore = (summedBest - summedNaive) * (summedNaive - summedRandom);
				
				// allows rounding up due to weird scores
				if(gameScore > -0.0005) {
					
					gameScore = 0;
				}
				// reward fitness for each unique interaction triggered
				int uniqueCount = events.size();
				// add a normalized unique count to the fitness
				double rulesTriggered = uniqueCount / (ruleset[0].length * 1.0f + 1);
				
				// fitness is calculated by weight summing the 2 variables together
				
				double fitness = (gameScore + 1) * (rulesTriggered);
				constrainFitness = 1.0;
				this.fitness.set(0, constrainFitness);
				this.fitness.set(1, fitness);
		} 
	}
	/**
	 * Play the current level using the naive player
	 * @param stateObs	the current stateObservation object that represent the level
	 * @param steps		the maximum amount of steps that it shouldn't exceed it
	 * @param agent		current agent to play the level
	 * @return			the number of steps that the agent stops playing after (<= steps)
	 */
	private int getAgentResult(StateObservation stateObs, int steps, AbstractPlayer agent){
		int i =0;
		int k = 0;
		for(i=0;i<steps;i++){
			if(stateObs.isGameOver()){
				break;
			}
			ElapsedCpuTimer timer = new ElapsedCpuTimer();
			timer.setMaxTimeMillis(SharedData.EVALUATION_STEP_TIME);
			Types.ACTIONS bestAction = agent.act(stateObs, timer);
			stateObs.advance(bestAction);
			k += checkIfOffScreen(stateObs);

		}
		if(k > 0) {
			// add k to global var keeping track of this
			this.badFrames += k;
		}
		return i;
	}
	
	/**
	 * crossover the current chromosome with the input chromosome
	 * @param c	the other chromosome to crossover with
	 * @return	the current children from the crossover process
	 */
	public ArrayList<Chromosome> crossover(Chromosome c){
		ArrayList<Chromosome> children = new ArrayList<Chromosome>();
		children.add(this.clone());
		children.add(c.clone());



		// make new rulesets to represent the new rules
		String[][] nRuleSetOne;
		String[][] nRuleSetTwo;



		// interaction set
		//crossover points
		int pointOne = SharedData.random.nextInt(ruleset[0].length);
		int pointTwo = SharedData.random.nextInt(c.getRuleset()[0].length);

		// calculate new sizes of the rulesets
		int nSizeOne = pointOne + (c.getRuleset()[0].length - pointTwo);
		int nSizeTwo = pointTwo + (ruleset[0].length - pointOne);

		// finalize construction
		nRuleSetOne = new String[2][];
		nRuleSetOne[0] = new String[nSizeOne];
		nRuleSetTwo = new String[2][];
		nRuleSetTwo[0] = new String[nSizeTwo];

		// swapping interaction for ruleset one
		for(int i = 0; i < pointOne; i++) {
			nRuleSetOne[0][i] = ruleset[0][i];
		}
		int counter = pointTwo;
		for(int i = pointOne; i < nSizeOne; i++) {
			nRuleSetOne[0][i] = c.getRuleset()[0][counter];
			counter++;
		}
		// swapping for ruleset two
		for(int i = 0; i < pointTwo; i++) {
			nRuleSetTwo[0][i] = c.getRuleset()[0][i];
		}
		counter = pointOne;
		for(int i = pointTwo; i < nSizeTwo; i++) {
			nRuleSetTwo[0][i] = ruleset[0][counter];
		}

		// termination set
		// crossover points
		pointOne = SharedData.random.nextInt(ruleset[1].length);
		pointTwo = SharedData.random.nextInt(c.getRuleset()[1].length);

		// calculate new sizes of the rulesets
		nSizeOne = pointOne + (c.getRuleset()[1].length - pointTwo);
		nSizeTwo = pointTwo + (ruleset[1].length - pointOne);

		// finalize construction
		nRuleSetOne[1] = new String[nSizeOne];
		nRuleSetTwo[1] = new String[nSizeTwo];

		// give the children their rulesets
		children.get(0).setRuleset(nRuleSetOne);
		children.get(1).setRuleset(nRuleSetTwo);

		// swapping terminations for ruleset one
		for(int i = 0; i < pointOne; i++) {
			nRuleSetOne[1][i] = ruleset[1][i];
		}
		counter = pointTwo;
		for(int i = pointOne; i < nSizeOne; i++) {
			nRuleSetOne[1][i] = c.getRuleset()[1][counter];
			counter++;
		}
		// swapping for ruleset two
		for(int i = 0; i < pointTwo; i++) {
			nRuleSetTwo[1][i] = c.getRuleset()[1][i];
		}
		counter = pointOne;
		for(int i = pointTwo; i < nSizeTwo; i++) {
			nRuleSetTwo[1][i] = ruleset[1][counter];
		}
		for(int i = 0; i < nRuleSetOne.length; i++) {
			ArrayList<String> temp = new ArrayList<> (Arrays.asList(nRuleSetOne[i]));
			temp = (ArrayList<String>) temp.stream().distinct().collect(Collectors.toList());
			nRuleSetOne[i] = new String[temp.size()];
			nRuleSetOne[i] = temp.toArray(nRuleSetOne[i]);

			temp = new ArrayList<> (Arrays.asList(nRuleSetTwo[i]));
			temp = (ArrayList<String>) temp.stream().distinct().collect(Collectors.toList());
			nRuleSetTwo[i] = new String[temp.size()];
			nRuleSetTwo[i] = temp.toArray(nRuleSetTwo[i]);
			}

		return children;
	}

	
	private void cleanOpenloopAgents() {
		((tracks.singlePlayer.advanced.olets.Agent)SharedData.automatedAgent).mctsPlayer = 
			new tracks.singlePlayer.advanced.olets.SingleMCTSPlayer(new Random(), 
				(tracks.singlePlayer.advanced.olets.Agent) SharedData.automatedAgent);
	}
	
	/***
	 * Checks to see if sprites are off screen
	 * @param stateObs the temporary state observation of the game
	 * @return the number of times sprites were off screen
	 */
	private int checkIfOffScreen(StateObservation stateObs) {
		ArrayList<Observation> allSprites = new ArrayList<Observation>();
		ArrayList<Observation>[] temp = stateObs.getNPCPositions();
		if(temp != null) {
			for(ArrayList<Observation> list : temp) {
				allSprites.addAll(list);
			}	
		}
		temp = stateObs.getImmovablePositions();
		if(temp != null) {
			for(ArrayList<Observation> list : temp) {
				allSprites.addAll(list);
			}
		}
		
		temp = stateObs.getMovablePositions();
		if(temp != null) {
			for(ArrayList<Observation> list : temp) {
				allSprites.addAll(list);
			}
		}
		
		// calculate screen size
		int xMin = -1 * stateObs.getBlockSize();
		int yMin = -1 * stateObs.getBlockSize();
		
		// add a 1 pixel buffer
		int xMax = (SharedData.la.getWidth()+1) * stateObs.getBlockSize();
		int yMax = (SharedData.la.getLength()+1) * stateObs.getBlockSize();
		int counter = 0;
		// check to see if any sprites are out of screen
		boolean frameBad = false;
		for(Observation s : allSprites) {
			if(s.position.x < xMin || s.position.x > xMax || s.position.y < yMin || s.position.y > yMax) {
				if(!frameBad) {
					counter++;
					frameBad = true;
				}
			}
		}
		return counter;
		
	}
	
	/**
	 * Compare two chromosome with each other based on their
	 * constrained fitness and normal fitness
	 */
	@Override
	public int compareTo(Chromosome o) {
		if(this.constrainFitness < 1 || o.constrainFitness < 1){
			if(this.constrainFitness < o.constrainFitness){
				return 1;
			}
			if(this.constrainFitness > o.constrainFitness){
				return -1;
			}
			return 0;
		}

		double firstFitness = 0;
		double secondFitness = 0;
		for(int i=0; i<this.fitness.size(); i++){
			firstFitness += this.fitness.get(i);
			secondFitness += o.fitness.get(i);
		}

		if(firstFitness > secondFitness){
			return -1;
		}

		if(firstFitness < secondFitness){
			return 1;
		}

		return 0;
	}
	/**
	 * Returns the fitness of the chromosome
	 * @return fitness the fitness of the chromosome
	 */
	public ArrayList<Double> getFitness() {
		return fitness;
	}
	/**
	 * Get constraint fitness for infeasible chromosome
	 * @return	1 if its feasible and less than 1 if not
	 */
	public double getConstrainFitness(){
		return constrainFitness;
	}
	/**
	 * returns the ruleset of this chromosome
	 * @return
	 */
	public String[][] getRuleset() {
		return ruleset;
	}
	/**
	 * sets the ruleset
	 * @param nRuleset	the new ruleset
	 */
	public void setRuleset(String[][] nRuleset) {
		this.ruleset = nRuleset;
	}
}
