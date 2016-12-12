package ruleGenerators.geneticRuleGenerator;

import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.Collection;
import java.util.*;

import core.game.SLDescription;
import core.game.StateObservation;
import core.game.Event;
import core.game.GameDescription.SpriteData;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.LevelAnalyzer;
import tools.StepController;
import tools.Vector2d;

public class Chromosome implements Comparable<Chromosome>{
	private ArrayList<String>[][] level;
	/**
	 * current chromosome fitness if its a feasible
	 */
	private ArrayList<Double> fitness;
	/**
	 * Parameters to initialize constraints
	 */
	HashMap<String, Object> parameters;
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
	 * the random agent
	 */
	private AbstractPlayer randomAgent;
	/**
	 * The current stateObservation of the level
	 */
	private StateObservation stateObs;
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
	 * List of all useful sprites in the game
	 */
	private ArrayList<String> usefulSprites;
	/**
	 * Level analyzer for the game
	 */
	private LevelAnalyzer levAl;

	/**
	 * Array contains all the simple interactions
	 */
	private String[] interactions = new String[]{
			"killSprite", "killAll", "killIfHasMore", "killIfHasLess", "killIfFromAbove",
			"killIfOtherHasMore", "spawnBehind", "stepBack",
			"spawnIfHasMore", "spawnIfHasLess", "cloneSprite", "transformTo", "undoAll",
			"flipDirection", "transformToRandomChild", "updateSpawnType",
			"removeScore", "addHealthPoints", "addHealthPointsToMax", "reverseDirection",
			"subtractHealthPoints", "increaseSpeedToAll", "decreaseSpeedToAll", "attractGaze",
			"align", "turnAround", "wrapAround", "pullWithIt", "bounceForward", "teleportToExit",
			"collectResource", "setSpeedForAll", "undoAll", "reverseDirection", "changeResource"};
	private String[] terminations = new String[] {
		"SpriteCounter", "SpriteCounterMore", "MultiSpriteCounter",
		"StopCounter", "Timeout"};
	/**
	 * Chromosome constructor.  Holds the ruleset and initializes agents within
	 * @param ruleset	the ruleset the chromosome contains
	 * @param sl		the game description
	 * @param time		elapsed time
	 */

	public Chromosome(String[][] ruleset, SLDescription sl, ElapsedCpuTimer time, ArrayList<String> usefulSprites) {
		this.ruleset = ruleset;
		this.sl = sl;
		this.fitness = new ArrayList<Double>();
		fitness.add(0.0);
		fitness.add(0.0);
		this.calculated = false;
		this.stateObs = null;
		this.usefulSprites = usefulSprites;
	    this.levAl = new LevelAnalyzer(sl);
	    this.parameters = new HashMap<String, Object>();
		constructAgent();
	}
	/**
	 * mutate the current chromosome interaction set
	 */
	public void mutateInteraction() {
		ArrayList<String> interaction = new ArrayList<>( Arrays.asList(ruleset[0]));
		ArrayList<String> tempInteractions = new ArrayList<> ( Arrays.asList(interactions));
		ArrayList<String> tempSprites = (ArrayList<String>) SharedData.usefulSprites.clone();
		
		tempSprites.remove(SharedData.la.getImmovables(0.8, 1, false)[0].name);
		for(SpriteData sd : levAl.getSpawners(0, 1, true)) {
			tempSprites.remove(sd.name);
		}
		SpriteData[] resourceSpriteData = levAl.getResources(0, 1, false);
		SpriteData[] spawnerSpriteData = levAl.getSpawners(0, 1, false);
		SpriteData[] portalSpriteData = levAl.getPortals(0, 1, false);
		// remove resource-dependent interactions from the choices
		if(resourceSpriteData.length == 0) {
			tempInteractions.remove("killIfHasMore");
			tempInteractions.remove("killIfHasLess");
			tempInteractions.remove("killIfOtherHasMore");
			tempInteractions.remove("spawnIfHasMore");
			tempInteractions.remove("spawnIfHasLess");
			tempInteractions.remove("changeResource");
		}
		if(spawnerSpriteData.length == 0) {
			tempInteractions.remove("updateSpawnType");
		}
		if(portalSpriteData.length == 0) {
			tempInteractions.remove("teleportToExit");
		}
		for(int i = 0; i < SharedData.MUTATION_AMOUNT; i++)
		{
			int point = SharedData.random.nextInt(ruleset[0].length);
			String nInteraction = "";
			//insert new random rule
			if(SharedData.random.nextDouble() < SharedData.INSERTION_PROB){
				nInteraction = tempInteractions.get(SharedData.random.nextInt(tempInteractions.size()));
				String officialInteraction = nInteraction;
				int i1 = SharedData.random.nextInt(tempSprites.size());
			    int i2 = (i1 + 1 + SharedData.random.nextInt(tempSprites.size() - 1)) % tempSprites.size();
			    
			    officialInteraction = tempSprites.get(i1) + " " +
					    tempSprites.get(i2) + " > " + nInteraction;

				// the killIfHasMore, killIfHasLess, and KillIfOtherHasMore rules
				if(nInteraction.equals("killIfHasMore") || nInteraction.equals("killIfHasLess")
						|| nInteraction.equals("killIfOtherHasMore")) {
					// lazy version of "if we have no resources, redo interaction selection loop"
					if(resourceSpriteData.length == 0) {
						i--;
						continue;
					}
				    String resourceS = " resource=" + resourceSpriteData[SharedData.random.nextInt(resourceSpriteData.length)].name;
				    officialInteraction += resourceS;
				    String limit = " limit=" + SharedData.random.nextInt(10);
				    officialInteraction += limit;
				}
				// the changeResource rule
				else if(nInteraction.equals("changeResource")) {
					// lazy version of "if we have no resources, redo interaction selection loop"
					if(resourceSpriteData.length == 0) {
						i--;
						continue;
					}
				    String resourceS = " resource=" + resourceSpriteData[SharedData.random.nextInt(resourceSpriteData.length)].name;
				    officialInteraction += resourceS;
				    String value = " value=" + SharedData.random.nextInt(15);
				    officialInteraction += value;
				}
				// the spawnIfHasMore and spawnIfHasLess rules
				else if(nInteraction.equals("spawnIfHasMore") || nInteraction.equals("spawnIfHasLess")) {
				    int i3 = SharedData.random.nextInt(tempSprites.size());
				 // lazy version of "if we have no resources, redo interaction selection loop"
					if(resourceSpriteData.length == 0) {
						i--;
						continue;
					}
				    String stype = " stype=" + tempSprites.get(i3);
				    officialInteraction += stype;
				    String resourceS = " resource=" + resourceSpriteData[SharedData.random.nextInt(resourceSpriteData.length)].name;
				    officialInteraction += resourceS;
				    String limit = " limit=" + SharedData.random.nextInt(25);
				    officialInteraction += limit;
				}
				// the transformTo rule
				else if(nInteraction.equals("transformTo")) {
					tempSprites.remove(levAl.getAvatars(false)[0].name);
				    int i3 = SharedData.random.nextInt(tempSprites.size());
				    
				    String stype = " stype=" + tempSprites.get(i3);
				    officialInteraction += stype;
				    boolean force = SharedData.random.nextBoolean();
				    String forceOrientation = " forceOrientation=" + force;
				    officialInteraction += forceOrientation;
				}
				// the killAll, spawnBehind, and transformToRandomChild rules
				else if(nInteraction.equals("killAll") || nInteraction.equals("spawnBehind")
						|| nInteraction.equals("transformToRandomChild")) {
					if(nInteraction.equals("transformToRandomChild")){
						tempSprites.remove(levAl.getAvatars(true)[0].name);
					}
				    int i3 = SharedData.random.nextInt(tempSprites.size());
				    String stype = " stype=" + tempSprites.get(i3);
				    officialInteraction += stype;
				}
				// addHealthPoints and addHealthPointsToMax rules
				else if(nInteraction.equals("addHealthPoints") ||
						nInteraction.equals("addHealthPointsToMax")) {
					String value = " value=" + SharedData.random.nextInt(15);
					officialInteraction += value;
				}
				// subtractHealthPoints, increaseSpeedToAll, decreaseSpeedToAll, and setSpeedForAll rules
				else if(nInteraction.equals("subtractHealthPoints") || nInteraction.equals("increaseSpeedToAll") ||
						nInteraction.equals("decreaseSpeedToAll") || nInteraction.equals("setSpeedForAll")) {
					for(SpriteData sd : SharedData.la.getPortals(0, 1, false)) {
						tempSprites.remove(sd.name);
					}
					String value = " value=" + SharedData.random.nextInt(25);
					officialInteraction += value;
				    int i3 = SharedData.random.nextInt(tempSprites.size());
				    String stype = " stype=" + tempSprites.get(i3);
				    officialInteraction += stype;
				}
				// the updateSpawnType rule
				else if(nInteraction.equals("updateSpawnType")) {
					// lazy version of "if we have no resources, redo interaction selection loop"
					if(spawnerSpriteData.length == 0) {
						i--;
						continue;
					}
					int i3 = SharedData.random.nextInt(tempSprites.size());
				    String stype = " stype=" + tempSprites.get(i3);
				    officialInteraction += stype;
				    String spawnType = " spawnPoint=" + spawnerSpriteData[SharedData.random.nextInt(spawnerSpriteData.length)].name;
				    officialInteraction += spawnType;
				}
				// teleportToExit rule
				else if(nInteraction.equals("teleportToExit")) {
					// second sprite needs to be a portal
					// lazy version of "if we have no resources, redo interaction selection loop"
					if(portalSpriteData.length == 0) {
						i--;
						continue;
					}
					i2 = SharedData.random.nextInt(portalSpriteData.length);
					officialInteraction = tempSprites.get(i1) + " " +
							portalSpriteData[i2].name + " > " + nInteraction;
				}
				// the transformToSingleton rule
				else if(nInteraction.equals("transformToSingleton")) {
					tempSprites.remove(levAl.getAvatars(true)[0].name);
					int i3 = SharedData.random.nextInt(tempSprites.size());
					officialInteraction += " stype=" + tempSprites.get(i3);
				    int i4 = (i3 + 1 + SharedData.random.nextInt(tempSprites.size() - 1)) % tempSprites.size();
					officialInteraction += " stype_other=" + tempSprites.get(i4);
				}
				// simple rules that follow the same pattern dont have a special case
				else {
					String fffff = "";
				}
				String score = "";
				boolean isScore = SharedData.random.nextBoolean();
				if(isScore) {
					score = " scoreChange=" + (SharedData.random.nextInt(6) - 3);
					officialInteraction += score;
				}
				interaction.add(officialInteraction);
			}
			//clear any random rule
			else if(SharedData.random.nextDouble() < SharedData.INSERTION_PROB + SharedData.DELETION_PROB){
				if (interaction.size() > 1) {
					interaction.remove(point);
				}
			}
			//change a random rule
			else{
				int choice = SharedData.random.nextInt(4);
				String[] changeMe = ruleset[0][point].split(" ");
				String officialInteraction = "";
				// change the first sprite of a rule
				if(choice == 0) {
					int i1 = SharedData.random.nextInt(tempSprites.size());
					changeMe[0] = tempSprites.get(i1);
					for(int a = 0; a < changeMe.length; a++) { 
						officialInteraction += changeMe[a] + " ";
					}
				}
				// change the second sprite of a rule
				else if(choice == 1) {
					int i1 = SharedData.random.nextInt(tempSprites.size());
					changeMe[1] = tempSprites.get(i1);
					for(int a = 0; a < changeMe.length; a++) { 
						officialInteraction += changeMe[a] + " ";
					}
				}
				// change the rule and its parameters
				else if(choice == 2) {
					nInteraction = tempInteractions.get(SharedData.random.nextInt(tempInteractions.size()));

					officialInteraction = changeMe[0] + " " + changeMe[1] + " > " + nInteraction; 
					if(nInteraction.equals("killIfHasMore") || nInteraction.equals("killIfHasLess")
							|| nInteraction.equals("killIfOtherHasMore")) {
						// lazy version of "if we have no resources, redo interaction selection loop"
						if(resourceSpriteData.length == 0) {
							i--;
							continue;
						}
					    String resourceS = " resource=" + resourceSpriteData[SharedData.random.nextInt(resourceSpriteData.length)].name;
					    officialInteraction += resourceS;
					    String limit = " limit=" + SharedData.random.nextInt(10);
					    officialInteraction += limit;
					}
					// the changeResource rule
					else if(nInteraction.equals("changeResource")) {
						// lazy version of "if we have no resources, redo interaction selection loop"
						if(resourceSpriteData.length == 0) {
							i--;
							continue;
						}
					    String resourceS = " resource=" + resourceSpriteData[SharedData.random.nextInt(resourceSpriteData.length)].name;
					    officialInteraction += resourceS;
					    String value = " value=" + SharedData.random.nextInt(15);
					    officialInteraction += value;
					}
					// the spawnIfHasMore and spawnIfHasLess rules
					else if(nInteraction.equals("spawnIfHasMore") || nInteraction.equals("spawnIfHasLess")) {
					    int i3 = SharedData.random.nextInt(tempSprites.size());
					 // lazy version of "if we have no resources, redo interaction selection loop"
						if(resourceSpriteData.length == 0) {
							i--;
							continue;
						}
					    String stype = " stype=" + tempSprites.get(i3);
					    officialInteraction += stype;
					    String resourceS = " resource=" + resourceSpriteData[SharedData.random.nextInt(resourceSpriteData.length)].name;
					    officialInteraction += resourceS;
					    String limit = " limit=" + SharedData.random.nextInt(25);
					    officialInteraction += limit;
					}
					// the transformTo rule
					else if(nInteraction.equals("transformTo")) {
						tempSprites.remove(levAl.getAvatars(true)[0].name);
					    int i3 = SharedData.random.nextInt(tempSprites.size());
					    String stype = " stype=" + tempSprites.get(i3);
					    officialInteraction += stype;
					    boolean force = SharedData.random.nextBoolean();
					    String forceOrientation = " forceOrientation=" + force;
					    officialInteraction += forceOrientation;
					}
					// the killAll, spawnBehind, and transformToRandomChild rules
					else if(nInteraction.equals("killAll") || nInteraction.equals("spawnBehind")
							|| nInteraction.equals("transformToRandomChild")) {
						if(nInteraction.equals("transformToRandomChild")) {
							tempSprites.remove(levAl.getAvatars(true)[0].name);
						}
					    int i3 = SharedData.random.nextInt(tempSprites.size());
					    String stype = " stype=" + tempSprites.get(i3);
					    officialInteraction += stype;
					}
					// addHealthPoints and addHealthPointsToMax rules
					else if(nInteraction.equals("addHealthPoints") ||
							nInteraction.equals("addHealthPointsToMax")) {
						String value = " value=" + SharedData.random.nextInt(15);
						officialInteraction += value;
					}
					// subtractHealthPoints, increaseSpeedToAll, decreaseSpeedToAll, and setSpeedForAll rules
					else if(nInteraction.equals("subtractHealthPoints") || nInteraction.equals("increaseSpeedToAll") ||
							nInteraction.equals("decreaseSpeedToAll") || nInteraction.equals("setSpeedForAll")) {
						for(SpriteData sd : SharedData.la.getPortals(0, 1, true)) {
							tempSprites.remove(sd.name);
						}
						String value = " value=" + SharedData.random.nextInt(25);
						officialInteraction += value;
					    int i3 = SharedData.random.nextInt(tempSprites.size());
					    String stype = " stype=" + tempSprites.get(i3);
					    officialInteraction += stype;
					}
					// the updateSpawnType rule
					else if(nInteraction.equals("updateSpawnType")) {
						// lazy version of "if we have no resources, redo interaction selection loop"
						if(spawnerSpriteData.length == 0) {
							i--;
							continue;
						}
						int i3 = SharedData.random.nextInt(tempSprites.size());
					    String stype = " stype=" + tempSprites.get(i3);
					    officialInteraction += stype;
					    String spawnType = " spawnPoint=" + spawnerSpriteData[SharedData.random.nextInt(spawnerSpriteData.length)].name;
					    officialInteraction += spawnType;
					}
					// teleportToExit rule
					else if(nInteraction.equals("teleportToExit")) {
						// second sprite needs to be a portal
						// lazy version of "if we have no resources, redo interaction selection loop"
						if(portalSpriteData.length == 0) {
							i--;
							continue;
						}
						int i5 = SharedData.random.nextInt(portalSpriteData.length);
						officialInteraction = changeMe[0] + " " +
								portalSpriteData[i5].name + " > " + nInteraction;
					}
					// the transformToSingleton rule
					else if(nInteraction.equals("transformToSingleton")) {
						tempSprites.remove(levAl.getAvatars(true)[0].name);
						int i3 = SharedData.random.nextInt(tempSprites.size());
						officialInteraction += " stype=" + tempSprites.get(i3);
					    int i4 = (i3 + 1 + SharedData.random.nextInt(tempSprites.size() - 1)) % tempSprites.size();
						officialInteraction += " stype_other=" + tempSprites.get(i4);
					}
					// simple rules that follow the same pattern dont have a special case
					else {
						String fff = "";
					}
					String score = "";
					boolean isScore = SharedData.random.nextBoolean();
					if(isScore) {
						score = " scoreChange=" + (SharedData.random.nextInt(6) - 3);
						officialInteraction += score;
					}
					
				}
				// change the parameters for the rule
				else if(choice == 3) {
					officialInteraction = changeMe[0] + " " + changeMe[1] + " > " + changeMe[3]; 
					if(changeMe[3].equals("killIfHasMore") || changeMe[3].equals("killIfHasLess")
							|| changeMe[3].equals("killIfOtherHasMore")) {
					    String resourceS = " resource=" + resourceSpriteData[SharedData.random.nextInt(resourceSpriteData.length)].name;
					    officialInteraction += resourceS;
					    String limit = " limit=" + SharedData.random.nextInt(10);
					    officialInteraction += limit;
					}
					// the changeResource rule
					else if(changeMe[3].equals("changeResource")) {
					    String resourceS = " resource=" + resourceSpriteData[SharedData.random.nextInt(resourceSpriteData.length)].name;
					    officialInteraction += resourceS;
					    String value = " value=" + SharedData.random.nextInt(15);
					    officialInteraction += value;
					}
					// the spawnIfHasMore and spawnIfHasLess rules
					else if(changeMe[3].equals("spawnIfHasMore") || changeMe[3].equals("spawnIfHasLess")) {
					    int i3 = SharedData.random.nextInt(tempSprites.size());
					    String stype = " stype=" + tempSprites.get(i3);
					    officialInteraction += stype;
					    String resourceS = " resource=" + resourceSpriteData[SharedData.random.nextInt(resourceSpriteData.length)].name;
					    officialInteraction += resourceS;
					    String limit = " limit=" + SharedData.random.nextInt(25);
					    officialInteraction += limit;
					}
					// the transformTo rule
					else if(changeMe[3].equals("transformTo")) {
					    int i3 = SharedData.random.nextInt(tempSprites.size());
					    String stype = " stype=" + tempSprites.get(i3);
					    officialInteraction += stype;
					    boolean force = SharedData.random.nextBoolean();
					    String forceOrientation = " forceOrientation=" + force;
					    officialInteraction += forceOrientation;
					}
					// the killAll, spawnBehind, and transformToRandomChild rules
					else if(changeMe[3].equals("killAll") || changeMe[3].equals("spawnBehind")
							|| changeMe[3].equals("transformToRandomChild")) {
					    int i3 = SharedData.random.nextInt(tempSprites.size());
					    String stype = " stype=" + tempSprites.get(i3);
					    officialInteraction += stype;
					}
					// addHealthPoints and addHealthPointsToMax rules
					else if(changeMe[3].equals("addHealthPoints") ||
							changeMe[3].equals("addHealthPointsToMax")) {
						String value = " value=" + SharedData.random.nextInt(15);
						officialInteraction += value;
					}
					// subtractHealthPoints, increaseSpeedToAll, decreaseSpeedToAll, and setSpeedForAll rules
					else if(changeMe[3].equals("subtractHealthPoints") || changeMe[3].equals("increaseSpeedToAll") ||
							changeMe[3].equals("decreaseSpeedToAll") || changeMe[3].equals("setSpeedForAll")) {
						for(SpriteData sd : SharedData.la.getPortals(0, 1, true)) {
							tempSprites.remove(sd.name);
						}
						String value = " value=" + SharedData.random.nextInt(25);
						officialInteraction += value;
					    int i3 = SharedData.random.nextInt(tempSprites.size());
					    String stype = " stype=" + tempSprites.get(i3);
					    officialInteraction += stype;
					}
					// the updateSpawnType rule
					else if(changeMe[3].equals("updateSpawnType")) {
						int i3 = SharedData.random.nextInt(tempSprites.size());
					    String stype = " stype=" + tempSprites.get(i3);
					    officialInteraction += stype;
					    String spawnType = " spawnPoint=" + spawnerSpriteData[SharedData.random.nextInt(spawnerSpriteData.length)].name;
					    officialInteraction += spawnType;
					}
					// teleportToExit rule
					else if(changeMe[3].equals("teleportToExit")) {
						int i5 = SharedData.random.nextInt(portalSpriteData.length);
						officialInteraction = changeMe[0] + " " +
								portalSpriteData[i5].name + " > " + changeMe[3];
					}
					// the transformToSingleton rule
					else if(changeMe[3].equals("transformToSingleton")) {
						tempSprites.remove(levAl.getAvatars(true)[0].name);
						int i3 = SharedData.random.nextInt(tempSprites.size());
						officialInteraction += " stype=" + tempSprites.get(i3);
					    int i4 = (i3 + 1 + SharedData.random.nextInt(tempSprites.size() - 1)) % tempSprites.size();
						officialInteraction += " stype_other=" + tempSprites.get(i4);
					}
					// simple rules that follow the same pattern dont have a special case
					else {
						String fff = "";
					}
					String score = "";
					boolean isScore = SharedData.random.nextBoolean();
					if(isScore) {
						score = " scoreChange=" + (SharedData.random.nextInt(6) - 3);
						officialInteraction += score;
					}
				}				
				interaction.add(officialInteraction);
				if(officialInteraction.contains("pullWithIt stype=")) {
					int flag = 0;
				}
				if(officialInteraction.contains("attractGaze=") || officialInteraction.contains("forceOrientation=") || officialInteraction.contains("stepBack") || officialInteraction.contains("killSprite")) {
					int flag = 0;
				}
			}
			
			interaction.removeIf(s -> s == null);
			interaction = (ArrayList<String>) interaction.stream().distinct().collect(Collectors.toList());
			ruleset[0] = new String[interaction.size()];
			ruleset[0] = interaction.toArray(ruleset[0]);		
		}

	}
	/**
	 * mutate the current chromosome interaction set
	 */
	public void mutateTermination(){
		for(int i = 0; i < SharedData.MUTATION_AMOUNT; i++)
		{
			int point = SharedData.random.nextInt(ruleset[1].length);
			ArrayList<String> termination = new ArrayList<>( Arrays.asList(ruleset[1]));
			// a sprite list where the avatar is not included
			ArrayList<String> tempSprites = (ArrayList<String>) SharedData.usefulSprites.clone();
			tempSprites.remove(levAl.getAvatars(true)[0].name);
			//insert new random rule
		    String nTermString = "";
			if(SharedData.random.nextDouble() < SharedData.INSERTION_PROB){
				String nTermination = terminations[SharedData.random.nextInt(this.terminations.length)];
				
				// SpriteCounter termination
				if(nTermination.equals("SpriteCounter")) {
					String sprite1 =  tempSprites.get(SharedData.random.nextInt(tempSprites.size()));
					// special case where sprite1 is the avatar
					int count = SharedData.random.nextInt(25);
					if(sprite1.equals(levAl.getAvatars(true)[0].name)) {
						count = 0;
					}
					boolean isWin = SharedData.random.nextBoolean();
					String win = "";
					if(isWin) {
						win = "True";
					} else {
						win = "False";
					}
					nTermString = nTermination + " stype=" + sprite1 + " limit=" + count + " win=" + win;
				// SpriteCounterMore termination
				} else if(nTermination.equals("SpriteCounterMore")) {
					tempSprites.remove(levAl.getImmovables(0.8, 1.0, false)[0].name);
					String sprite1 = tempSprites.get(SharedData.random.nextInt(tempSprites.size()));
					int count = SharedData.random.nextInt(25);
					boolean isWin = SharedData.random.nextBoolean();
					String win = "";
					if(isWin) {
						win = "True";
					} else {
						win = "False";
					}
					nTermString = nTermination + " stype=" + sprite1 + " limit=" + count + " win=" + win;
				} else if(nTermination.equals("MultiSpriteCounter") || nTermination.equals("StopCounter")) {
					String sprite1 = tempSprites.get(SharedData.random.nextInt(tempSprites.size()));
					String sprite2 = tempSprites.get(SharedData.random.nextInt(tempSprites.size()));
					String sprite3 = tempSprites.get(SharedData.random.nextInt(tempSprites.size()));

					int count = SharedData.random.nextInt(1000);
					boolean isWin = SharedData.random.nextBoolean();
					String win = "";
					if(isWin) {
						win = "True";
					} else {
						win = "False";
					}

					nTermString = nTermination + " stype1=" + sprite1 + " stype2=" + sprite2 + " stype3="
							+ sprite3 + " limit=" + count + " win=" + win;
				} else if(nTermination.equals("MultiSpriteCounterSubTypes")) {
					for(SpriteData sd : SharedData.la.getPortals(0, 1, true)) {
						tempSprites.remove(sd.name);
					}
					for(SpriteData sd : SharedData.la.getSpawners(0, 1, true)) {
						tempSprites.remove(sd.name);
					}
					String sprite1 = tempSprites.get(SharedData.random.nextInt(tempSprites.size()));
					String sprite2 = "";
					String sprite3 = "";

					nTermString = nTermination + " stype1=" + sprite1;

					boolean moreSprites = SharedData.random.nextBoolean();
					if(moreSprites) {
						sprite2 = tempSprites.get(SharedData.random.nextInt(tempSprites.size()));
						nTermString += " stype2=" + sprite2;
						moreSprites = SharedData.random.nextBoolean();
						if(moreSprites) {
							sprite3 = tempSprites.get(SharedData.random.nextInt(tempSprites.size()));
							nTermString += " stype3=" + sprite3;
						}
					}
					
					int count = SharedData.random.nextInt(25);

					boolean isWin = SharedData.random.nextBoolean();
					String win = "";
					if(isWin) {
						win = "True";
					} else {
						win = "False";
					}
					nTermString += " limit=" + count + " win=" + win;
				} else {
					int count = SharedData.random.nextInt(25);
					boolean isWin = SharedData.random.nextBoolean();
					String win = "";
					if(isWin) {
						win = "True";
					} else {
						win = "False";
					}
					nTermString = nTermination + " limit=" + count + " win=" + win;;
				}
				termination.add(nTermString);
			}
			//clear any random rule
			else if(SharedData.random.nextDouble() < SharedData.INSERTION_PROB + SharedData.DELETION_PROB){
				if(termination.size() > 1){
					termination.remove(point);
				}
			}
			//change a random rule
			else{
				termination.remove(point);
				String nTermination = terminations[SharedData.random.nextInt(this.terminations.length)];
				// SpriteCounter termination
				if(nTermination.equals("SpriteCounter")) {
					String sprite1 = tempSprites.get(SharedData.random.nextInt(tempSprites.size()));
					int count = SharedData.random.nextInt(25);
					// special case where sprite1 is the avatar
					if(sprite1.equals(levAl.getAvatars(true)[0].name)) {
						count = 0;
					}
					boolean isWin = SharedData.random.nextBoolean();
					String win = "";
					if(isWin) {
						win = "True";
					} else {
						win = "False";
					}
					nTermString = nTermination + " stype=" + sprite1 + " limit=" + count + " win=" + win;
				// SpriteCounterMore termination
				} else if(nTermination.equals("SpriteCounterMore")) {
					tempSprites.remove(levAl.getImmovables(0.8, 1.0, false)[0].name);
					String sprite1 = tempSprites.get(SharedData.random.nextInt(tempSprites.size()));
					int count = SharedData.random.nextInt(25);
					boolean isWin = SharedData.random.nextBoolean();
					String win = "";
					if(isWin) {
						win = "True";
					} else {
						win = "False";
					}
					nTermString = nTermination + " stype=" + sprite1 + " limit=" + count + " win=" + win;
				} else if(nTermination.equals("MultiSpriteCounter") || nTermination.equals("StopCounter")) {
					String sprite1 = tempSprites.get(SharedData.random.nextInt(tempSprites.size()));
					String sprite2 = tempSprites.get(SharedData.random.nextInt(tempSprites.size()));
					String sprite3 = tempSprites.get(SharedData.random.nextInt(tempSprites.size()));

					int count = SharedData.random.nextInt(1000);
					boolean isWin = SharedData.random.nextBoolean();
					String win = "";
					if(isWin) {
						win = "True";
					} else {
						win = "False";
					}

					nTermString = nTermination + " stype1=" + sprite1 + " stype2=" + sprite2 + " stype3="
							+ sprite3 + " limit=" + count + " win=" + win;
				} else if(nTermination.equals("MultiSpriteCounterSubTypes")) {
					for(SpriteData sd : SharedData.la.getPortals(0, 1, false)) {
						tempSprites.remove(sd.name);
					}
					for(SpriteData sd : SharedData.la.getSpawners(0, 1, false)) {
						tempSprites.remove(sd.name);
					}
					String sprite1 = tempSprites.get(SharedData.random.nextInt(tempSprites.size()));
					
					String sprite2 = "";
					String sprite3 = "";
					int count = SharedData.random.nextInt(25);

					nTermString = nTermination + " stype1=" + sprite1;

					boolean moreSprites = SharedData.random.nextBoolean();
					if(moreSprites) {
						sprite2 = tempSprites.get(SharedData.random.nextInt(tempSprites.size()));
						nTermString += " stype2=" + sprite2;
						moreSprites = SharedData.random.nextBoolean();
						if(moreSprites) {
							sprite3 = tempSprites.get(SharedData.random.nextInt(tempSprites.size()));
							nTermString += " stype3=" + sprite3;
						}
					}
					
					boolean isWin = SharedData.random.nextBoolean();
					String win = "";
					if(isWin) {
						win = "True";
					} else {
						win = "False";
					}
					nTermString += " limit=" + count + " win=" + win;
				} else {
					int count = SharedData.random.nextInt(25);
					boolean isWin = SharedData.random.nextBoolean();
					String win = "";
					if(isWin) {
						win = "True";
					} else {
						win = "False";
					}
					nTermString = nTermination + " limit=" + count + " win=" + win;;
				}
				termination.add(nTermString);
				if(nTermString.contains(" limit=")) {
					int flag = 1;
				}
			}
		    // fold the interactions list back into the ruleset
			termination.removeIf(s -> s == null);
			ruleset[1] = new String[termination.size()];
			ruleset[1] = termination.toArray(ruleset[1]);
		}
	}
	/**
	 * runs the mutation methods on this chromosome
	 */
	public void mutate() {
		mutateInteraction();
		mutateTermination();
		// Resolving potential constraints if mutations make malformed sets
		resolveDeathConstraint();
	}


	/**
	 * tests to make sure the game is playable, meaning a DoNothing AI won't die in the first 40 frames.
	 */
	public boolean feasibilityTest() {
		doNothingLength = Integer.MAX_VALUE;
		for(int i = 0; i < SharedData.REPETITION_AMOUNT; i++) {
			int temp = this.getAgentResult(getStateObservation().copy(), FEASIBILITY_STEP_LIMIT, this.naiveAgent);
			if(temp < doNothingLength){
				doNothingLength = temp;
				
			}
		}
		if(doNothingLength < 40) {
			return false;
		}
		return true;
	}


	/**
	 * calculates the fitness, by comparing the scores of a naiveAI and a smart AI
	 * @param time	how much time to evaluate the chromosome
	 */
	public void calculateFitness(long time) {

		boolean isFeasible = false;
		isFeasible = feasibilityTest();
		constrainFitness = 0;
		this.fitness.set(0, 0.0);
		if(isFeasible) {
			// unique events that occured in all the game simluations
			Set<String> events = new HashSet<String>();

			this.fitness.set(0, 1.0);
			constrainFitness = 1;
			//Play the game using the best agent
			double score = -200;
			ArrayList<Vector2d> SOs = new ArrayList<>();
			// protects the fitness evaluation from looping forever
			int protectionCounter = 0;

			// Best Agent
			while(score < -100) {
				StepController stepAgent = new StepController(automatedAgent, SharedData.EVALUATION_STEP_TIME);
				ElapsedCpuTimer elapsedTimer = new ElapsedCpuTimer();
				elapsedTimer.setMaxTimeMillis(time);
		
				stepAgent.playGame(stateObs.copy(), elapsedTimer, SOs);
				bestState = stepAgent.getFinalState();
				bestSol = stepAgent.getSolution();
				score = bestState.getGameScore();
				protectionCounter++;
				if(protectionCounter > SharedData.PROTECTION_COUNTER) {
					break;
				}
			}
			
			TreeSet s = bestState.getEventsHistory();
			Iterator<Event> iter = s.iterator();
			while(iter.hasNext()) {
				Event e = iter.next();
				events.add(e.activeTypeId + "" + e.passiveTypeId);
			}
			// Naive agent
			score = -200;
			StateObservation naiveState = null;
			int naiveLength = Integer.MAX_VALUE;
			double bestNaiveScore = Double.MIN_VALUE;
			protectionCounter = 0;
			//playing the game using the donothing agent and naive agent
			for(int i=0; i<SharedData.REPETITION_AMOUNT; i++){
				while(score < -100) {
					StateObservation tempState = stateObs.copy();
					int temp = getAgentResult(tempState, bestSol.size(), naiveAgent);
					if(temp < naiveLength){
						naiveLength = temp;
						naiveState = tempState;
					}
					score = naiveState.getGameScore();
					if(score > bestNaiveScore) {
						bestNaiveScore = score;
					}
					protectionCounter++;
					if(protectionCounter > SharedData.PROTECTION_COUNTER) {
						break;
					}
				}
				// gather all unique interactions between objects in the naive agent
				TreeSet s1 = naiveState.getEventsHistory();
				Iterator<Event> iter1 = s.iterator();
				while(iter.hasNext()) {
					Event e = iter.next();
					events.add(e.activeTypeId + "" + e.passiveTypeId);				}
			}
			// Random Agent
			StateObservation randomState = null;
			int randomLength = Integer.MAX_VALUE;
			double bestRandomScore = Double.MIN_VALUE;
			score = -200;
			protectionCounter = 0;
			for(int i=0; i<SharedData.REPETITION_AMOUNT; i++){
				while(score < -100) {
					StateObservation tempState = stateObs.copy();
					
					StepController stepAgent = new StepController(randomAgent, SharedData.EVALUATION_STEP_TIME);
					ElapsedCpuTimer elapsedTimer = new ElapsedCpuTimer();
					elapsedTimer.setMaxTimeMillis(time);
			
					stepAgent.playGame(stateObs.copy(), elapsedTimer, SOs);
					tempState = stepAgent.getFinalState();
					int temp = stepAgent.getSolution().size();

					if(temp < randomLength){
						randomLength = temp;
						randomState = tempState;
					}
					score = randomState.getGameScore();
					if(score > bestRandomScore) {
						bestRandomScore = score;
					}
					if(protectionCounter > SharedData.PROTECTION_COUNTER) {
						break;
					}
				}
				// gather all unique interactions between objects in the naive agent
				TreeSet s1 = randomState.getEventsHistory();
				Iterator<Event> iter1 = s.iterator();
				while(iter.hasNext()) {
					Event e = iter.next();
					events.add(e.activeTypeId + "" + e.passiveTypeId);				}
			}
			// calc summation of differences between best agent and the naive and random agents
			double difference = (bestState.getGameScore() - bestNaiveScore) + bestState.getGameScore() - bestRandomScore;
			double fit = difference;
			// penalize the fitness if both agents score 0
			if(bestState.getGameScore() == 0 && naiveState.getGameScore() == 0) {
				fit += -50;
			}
			
			// reward the fitness if smart agent wins the game
			if(bestState.getGameWinner() != Types.WINNER.PLAYER_WINS) {
				fit += Math.abs(difference) * 1/2;
			}
			this.fitness.set(1, difference);
			resolveOffscreenConstraint(SOs, bestState.getWorldDimension());
			
			// reward fitness for each unique interaction triggered
			int uniqueCount = events.size();
			fit += uniqueCount * 2;
			this.fitness.set(1, fit);
		}
	}

	/**
	 * initialize the agents used during evaluating the chromosome
	 */
	private void constructAgent(){
		try{
			Class agentClass = Class.forName(SharedData.AGENT_NAME);
			Constructor agentConst = agentClass.getConstructor(new Class[]{StateObservation.class, ElapsedCpuTimer.class});
			automatedAgent = (AbstractPlayer)agentConst.newInstance(getStateObservation().copy(), null);
		}
		catch(Exception e){
			e.printStackTrace();
		}

		try{
			Class agentClass = Class.forName(SharedData.NAIVE_AGENT_NAME);
			Constructor agentConst = agentClass.getConstructor(new Class[]{StateObservation.class, ElapsedCpuTimer.class});
			naiveAgent = (AbstractPlayer)agentConst.newInstance(getStateObservation().copy(), null);
		}
		catch(Exception e){
			e.printStackTrace();
		}

		try{
			Class agentClass = Class.forName(SharedData.DO_NOTHING_AGENT_NAME);
			Constructor agentConst = agentClass.getConstructor(new Class[]{StateObservation.class, ElapsedCpuTimer.class});
			doNothingAgent = (AbstractPlayer)agentConst.newInstance(getStateObservation().copy(), null);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		try{
			Class agentClass = Class.forName(SharedData.RANDOM_AGENT_NAME);
			Constructor agentConst = agentClass.getConstructor(new Class[]{StateObservation.class, ElapsedCpuTimer.class});
			randomAgent = (AbstractPlayer)agentConst.newInstance(getStateObservation().copy(), null);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * get game state observation for the current level
	 * @return	StateObservation for the current level
	 */
	private StateObservation getStateObservation(){
//		try (FileWriter fw = new FileWriter(SharedData.filename, true);
//				BufferedWriter bw = new BufferedWriter(fw);
//				PrintWriter out = new PrintWriter(bw)) {
//			out.println("StateObservations");
//			out.println("Interaction");
//			for (int i = 0; i < ruleset[0].length; ++i) {
//				out.println(ruleset[0][i]);
//			}
//			out.println("Termination");
//			for (int i = 0; i < ruleset[1].length; ++i) {
//				out.println(ruleset[1][i]);
//			}
//		} catch (Exception ex) {
//			System.out.println(ex.getMessage());
//		}
		if(stateObs != null){
			return stateObs;
		}
		stateObs = sl.testRules(ruleset[0], ruleset[1]);
		return stateObs;
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
		Chromosome c = new Chromosome(nRuleset, sl, time, usefulSprites);
		return c;
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
		for(i=0;i<steps;i++){
			if(stateObs.isGameOver()){
				break;
			}
			ElapsedCpuTimer timer = new ElapsedCpuTimer();
			timer.setMaxTimeMillis(SharedData.EVALUATION_STEP_TIME);
			Types.ACTIONS bestAction = agent.act(stateObs, timer);
			stateObs.advance(bestAction);
		}
		
		return i;
	}

	public ArrayList<Double> getFitness() {
		return fitness;
	}
	
	/**
	 * Attempts to resolve rules that kill off the avatar
	 * so that we will have a termination that ensures the game
	 * ends at that point.
	 */
	public void resolveDeathConstraint() {
		String avatarName = levAl.getAvatars(true)[0].name;
		String endGameIfAvatarDead = "SpriteCounter stype=" + avatarName + " limit=0";
		ArrayList<String> killList = 
				new ArrayList<>(Arrays.asList(ruleset[0]));
		killList = new ArrayList<>(
				killList.stream().filter(s->s.contains("kill")).collect(Collectors.toList())
				);
		ArrayList<String> endCountList =
				new ArrayList<>(Arrays.asList(ruleset[1]));
		endCountList = new ArrayList<>(
				endCountList.stream().filter(s->s.contains(endGameIfAvatarDead)).collect(Collectors.toList())
				);
		for (String t : killList) {
			if (t.contains(avatarName) &&
				t.indexOf(avatarName) == 0 &&
				endCountList.isEmpty()
				) {
				killList = new ArrayList<>(Arrays.asList(ruleset[1]));
				killList.add(endGameIfAvatarDead + " win=False");
				ruleset[1] = killList.toArray(new String[0]);
				break;
			}
		}

	}
	
	/**
	 * Attempt to prevent games where avatar goes off screen
	 */
	public void resolveOffscreenConstraint(ArrayList<Vector2d> SOs, Dimension world) {
		//System.out.println(SOs.size());
		Vector2d pos = SOs.get(SOs.size() - 2);
		if (pos.x < 0 || pos.y > world.width ||
			pos.y < 0 || pos.y > world.height) {
			//constrainFitness *= 0.9;
			fitness.set(1, fitness.get(1) * (4.0/5.0));
		}
	}
	
	/**
	 * Get constraint fitness for infeasible chromosome
	 * @return	1 if its feasible and less than 1 if not
	 */
	public double getConstrainFitness(){
		return constrainFitness;
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

	/**
	 * Get the average value of the fitness
	 * @return	average value of the fitness array
	 */
	public double getCombinedFitness(){
		double result = 0;
		for(double v: this.fitness){
			result += v;
		}
		return result / this.fitness.size();
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
