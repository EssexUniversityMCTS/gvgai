package tracks.ruleGeneration;

import core.vgdl.Node;
import core.vgdl.VGDLFactory;
import core.vgdl.VGDLParser;
import core.vgdl.VGDLRegistry;
import core.competition.CompetitionParameters;
import core.content.InteractionContent;
import core.content.SpriteContent;
import core.content.TerminationContent;
import core.game.Game;
import core.game.GameDescription.SpriteData;
import core.game.SLDescription;
import core.generator.AbstractRuleGenerator;
import core.logging.Logger;
import core.logging.Message;
import core.player.AbstractMultiPlayer;
import core.player.AbstractPlayer;
import core.player.Player;
import tools.ElapsedCpuTimer;
import tools.IO;
import tracks.ArcadeMachine;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by dperez on 19/03/2017.
 */
public class RuleGenMachine
{
	/**
	 * Reads and launches a game for a human to be played. Graphics always on.
	 *
	 * @param original_game
	 * 		  original game description file.
	 * @param generated_game
	 *            generated game description file.
	 * @param level_file
	 *            file with the level to be played.
	 */
	public static double[] playOneGame(String original_game, String generated_game, String level_file, String actionFile, int randomSeed) {
		String agentName = "tracks.singlePlayer.tools.human.Agent";
		boolean visuals = true;
		return runOneGame(original_game, generated_game, level_file, visuals, agentName, actionFile, randomSeed, 0);
	}

	/**
	 * Reads and launches a game for a bot to be played. Graphics can be on or
	 * off.
	 *
	 * @param original_game
	 * 		  original game description file.
	 * @param generated_game
	 *            generated game description file.
	 * @param level_file
	 *            file with the level to be played.
	 * @param visuals
	 *            true to show the graphics, false otherwise.
	 * @param agentNames
	 *            names (inc. package) where the tracks are otherwise.
	 *            Names separated by space.
	 * @param actionFile
	 *            filename of the files where the actions of these players, for
	 *            this game, should be recorded.
	 * @param randomSeed
	 *            sampleRandom seed for the sampleRandom generator.
	 * @param playerID
	 *            ID of the human player
	 */
	public static double[] runOneGame(String original_game, String generated_game, String level_file, boolean visuals, String agentNames,
									  String actionFile, int randomSeed, int playerID) {
		VGDLFactory.GetInstance().init(); // This always first thing to do.
		VGDLRegistry.GetInstance().init();

		if (CompetitionParameters.OS_WIN)
		{
			System.out.println(" * WARNING: Time limitations based on WALL TIME on Windows * ");
		}

		// First, we create the game to be played..
		Game toPlay = new VGDLParser().parseGame(generated_game);
		Node n = new VGDLParser().indentTreeParser(new tools.IO().readFile(original_game));
		for(Node c:n.children){
			if(c.content instanceof SpriteContent){
				new VGDLParser().modifyTheSpriteRender(toPlay, c.children);
				break;
			}
		}
		toPlay.buildLevel(level_file, randomSeed);

		// Warm the game up.
		ArcadeMachine.warmUp(toPlay, CompetitionParameters.WARMUP_TIME);

		// Create the players.
		String[] names = agentNames.split(" ");
		int no_players = toPlay.no_players;
		if (no_players > 1 && no_players != names.length) {
			// We fill with more human players
			String[] newNames = new String[no_players];
			System.arraycopy(names, 0, newNames, 0, names.length);
			for (int i = names.length; i < no_players; ++i)
				newNames[i] = "tracks.multiPlayer.tools.human.Agent";
			names = newNames;
		}

		boolean humans[] = new boolean[no_players];
		boolean anyHuman = false;

		// System.out.println("Number of players: " + no_players);

		Player[] players;
		if (no_players > 1) {
			// multi player games
			players = new AbstractMultiPlayer[no_players];
		} else {
			// single player games
			players = new AbstractPlayer[no_players];
		}

		for (int i = 0; i < no_players; i++) {

			humans[i] = isHuman(names[i]);
			anyHuman |= humans[i];

			if (no_players > 1) {
				// multi player
				players[i] = ArcadeMachine.createMultiPlayer(names[i], actionFile, toPlay.getObservationMulti(i),
						randomSeed, i, humans[i]);
			} else {
				// single player
				players[i] = ArcadeMachine.createPlayer(names[i], actionFile, toPlay.getObservation(), randomSeed,
						humans[i]);
			}

			if (players[i] == null) {
				// Something went wrong in the constructor, controller
				// disqualified
				if (no_players > 1) {
					// multi player
					toPlay.getAvatars()[i].disqualify(true);
				} else {
					// single player
					toPlay.disqualify();
				}

				// Get the score for the result.
				return toPlay.handleResult();
			}
		}

		// Then, play the game.
		double[] score;
		if (visuals)
			score = toPlay.playGame(players, randomSeed, anyHuman, playerID);
		else
			score = toPlay.runGame(players, randomSeed);

		// Finally, when the game is over, we need to tear the players down.
		ArcadeMachine.tearPlayerDown(toPlay, players, actionFile, randomSeed, true);

		// This, the last thing to do in this method, always:
		toPlay.handleResult();
		toPlay.printResult();

		return toPlay.getFullResult();
	}

	/**
	 * create a new game file using the new generated rules
	 * @param gameFile		current game file
	 * @param levelFile		current level file
	 * @param ruleGenerator	current rule generator
	 * @param modifiedFile	the resulted game file
	 * @param randomSeed	random seed used in encoding game sprites
	 * @return			true if everything worked fine, false otherwise
	 */
	public static boolean generateRules(String gameFile, String levelFile, String ruleGenerator, String modifiedFile, int randomSeed) {
		VGDLFactory.GetInstance().init();
		VGDLRegistry.GetInstance().init();

		// First, we create the game to be played..
		Game toPlay = new VGDLParser().parseGame(gameFile);
		String[] lines = new IO().readFile(levelFile);

		try {
			SLDescription sl = new SLDescription(toPlay, lines, randomSeed);
			AbstractRuleGenerator generator = createRuleGenerator(ruleGenerator, sl);
			String[][] rules = getGeneratedRules(sl, toPlay, generator);
			HashMap<String, ArrayList<String>> spriteSetStructure = generator.getSpriteSetStructure();
			rules = sl.modifyRules(rules[0], rules[1], randomSeed);

			SpriteData[] data = sl.getGameSprites();
			HashMap<String, String> msprites = new HashMap<String, String>();
			for(int i=0; i<data.length; i++){
				String decodedLine = sl.modifyRules(new String[]{data[i].toString()}, new String[]{}, randomSeed)[0][0];
				msprites.put(sl.decodeName(data[i].name, randomSeed), decodedLine);
			}
			HashMap<String, ArrayList<String>> msetStructure = new HashMap<String, ArrayList<String>>();
			if(spriteSetStructure != null){
				for(String key:spriteSetStructure.keySet()){
					msetStructure.put(key, new ArrayList<String>());
					for(int i=0; i<spriteSetStructure.get(key).size(); i++){
						String decodedName = sl.decodeName(spriteSetStructure.get(key).get(i), randomSeed);
						if(decodedName.length() > 0){
							msetStructure.get(key).add(decodedName);
						}
					}
				}
			}

			saveGame(gameFile, modifiedFile, rules, msetStructure, msprites);
		} catch (Exception e) {
			toPlay.disqualify();
			toPlay.handleResult();
			toPlay.printResult();
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return true;
	}


	/// PRIVATE METHODS

	/**
	 * @param ruleGenerator rule generatord
	 * @param sl Level Description
	 * @return The rule generator created
	 * @throws RuntimeException
	 */
	protected static AbstractRuleGenerator createRuleGenerator(String ruleGenerator, SLDescription sl)
			throws RuntimeException {
		AbstractRuleGenerator generator = null;
		try {
			// Get the class and the constructor with arguments
			// (StateObservation, long).
			Class<? extends AbstractRuleGenerator> controllerClass = Class.forName(ruleGenerator)
					.asSubclass(AbstractRuleGenerator.class);
			Class[] gameArgClass = new Class[] { SLDescription.class, ElapsedCpuTimer.class };
			Constructor controllerArgsConstructor = controllerClass.getConstructor(gameArgClass);

			// Determine the time due for the controller creation.
			ElapsedCpuTimer ect = new ElapsedCpuTimer();
			ect.setMaxTimeMillis(CompetitionParameters.RULE_INITIALIZATION_TIME);

			// Call the constructor with the appropriate parameters.
			Object[] constructorArgs = new Object[] { sl, ect.copy() };
			generator = (AbstractRuleGenerator) controllerArgsConstructor.newInstance(constructorArgs);

			// Check if we returned on time, and act in consequence.
			long timeTaken = ect.elapsedMillis();
			if (ect.exceededMaxTime()) {
				long exceeded = -ect.remainingTimeMillis();
				System.out.println("Generator initialization time out (" + exceeded + ").");

				return null;
			} else {
				System.out.println("Generator initialization time: " + timeTaken + " ms.");
			}

			// This code can throw many exceptions (no time related):

		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			System.err
					.println("Constructor " + ruleGenerator + "(StateObservation,long) not found in controller class:");
			System.exit(1);

		} catch (ClassNotFoundException e) {
			System.err.println("Class " + ruleGenerator + " not found for the controller:");
			e.printStackTrace();
			System.exit(1);

		} catch (InstantiationException e) {
			System.err.println("Exception instantiating " + ruleGenerator + ":");
			e.printStackTrace();
			System.exit(1);

		} catch (IllegalAccessException e) {
			System.err.println("Illegal access exception when instantiating " + ruleGenerator + ":");
			e.printStackTrace();
			System.exit(1);
		} catch (InvocationTargetException e) {
			System.err.println("Exception calling the constructor " + ruleGenerator + "(StateObservation,long):");
			e.printStackTrace();
			System.exit(1);
		}

		return generator;
	}


	/**
	 * run the generator to get new rules
	 * @param sl	current game sprites and level description object
	 * @param game	current game object
	 * @param generator	current rule generator
	 * @return		the new interaction rules and termination conditions
	 */
	private static String[][] getGeneratedRules(SLDescription sl, Game game, AbstractRuleGenerator generator) {
		ElapsedCpuTimer ect = new ElapsedCpuTimer();
		ect.setMaxTimeMillis(CompetitionParameters.RULE_ACTION_TIME);

		String[][] rules = generator.generateRules(sl, ect.copy());

		if (ect.exceededMaxTime()) {
			long exceeded = -ect.remainingTimeMillis();

			if (ect.elapsedMillis() > CompetitionParameters.LEVEL_ACTION_TIME_DISQ) {
				// The agent took too long to replay. The game is over and the
				// agent is disqualified
				System.out.println("Too long: " + "(exceeding " + (exceeded) + "ms): controller disqualified.");
				rules = new String[1][1];
			} else {
				System.out.println("Overspent: " + "(exceeding " + (exceeded) + "ms): applying Empty Level.");
				rules = new String[1][1];
			}
		}

		return rules;
	}

	/**
	 * Recursive function to save game tree and replace the old rules with the new rules
	 * @param n			current Node that need to be printed
	 * @param level		current level in the tree
	 * @param w			current writer object
	 * @param rules		array of interaction rules or terminations
	 * @throws IOException	thrown when a problem happens during writing
	 */
	private static void saveTree(Node n, int level, BufferedWriter w, String[][] rules, HashMap<String, ArrayList<String>> setStructure, HashMap<String, String> sprites) throws IOException{
		String template = "    ";
		String message = "";
		for(int i=0; i<level; i++){
			message += template;
		}
		w.write(message + n.content.line.trim() + "\n");
		if(n.content instanceof InteractionContent){
			for(int i=0; i<rules[0].length; i++){
				w.write(message + template + rules[0][i].trim() + "\n");
			}
		}
		else if(n.content instanceof TerminationContent){
			for(int i=0; i<rules[1].length; i++){
				w.write(message + template + rules[1][i].trim() + "\n");
			}
		}
		else if(n.content instanceof SpriteContent){
			ArrayList<String> msprites = new ArrayList<String>();
			for(String key:setStructure.keySet()){
				msprites.add(template + key + " >");
				for(int i=0; i<setStructure.get(key).size(); i++){
					if(sprites.containsKey(setStructure.get(key).get(i).trim())){
						msprites.add(template + template + sprites.get(setStructure.get(key).get(i).trim()).trim());
						sprites.remove(setStructure.get(key).get(i).trim());
					}
					else{
						Logger.getInstance().addMessage(new Message(Message.ERROR, "Undefined " + setStructure.get(key).get(i) + " in the provided sprite set."));
					}
				}
			}
			for(String value:sprites.values()){
				msprites.add(template + value.trim());
			}
			for(String value:msprites){
				w.write(message + value + "\n");
			}
		}
		else{
			for (int i = 0; i < n.children.size(); i++) {
				saveTree(n.children.get(i), level + 1, w, rules, setStructure, sprites);
			}
		}
	}

	/**
	 * Save the result of the rule generations
	 * @param gameFile		current game file
	 * @param modifiedFile	current new game file
	 * @param rules		the generated rules
	 */
	private static void saveGame(String gameFile, String modifiedFile, String[][] rules, HashMap<String, ArrayList<String>> setStructure, HashMap<String, String> sprites) {
		try {
			if (modifiedFile != null) {
				BufferedWriter writer = new BufferedWriter(new FileWriter(modifiedFile));
				String[] gameLines = new tools.IO().readFile(gameFile);
				Node n = new VGDLParser().indentTreeParser(gameLines);
				saveTree(n, 0, writer, rules, setStructure, sprites);
				writer.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static final boolean isHuman(String agentName) {
		if (agentName.equalsIgnoreCase("tracks.multiPlayer.tools.human.Agent")
				|| agentName.equalsIgnoreCase("tracks.singlePlayer.tools.human.Agent"))
			return true;
		return false;
	}

}