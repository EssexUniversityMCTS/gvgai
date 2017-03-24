package tracks.ruleGeneration;

import core.vgdl.Node;
import core.vgdl.VGDLFactory;
import core.vgdl.VGDLParser;
import core.vgdl.VGDLRegistry;
import core.competition.CompetitionParameters;
import core.content.InteractionContent;
import core.content.TerminationContent;
import core.game.Game;
import core.game.SLDescription;
import core.generator.AbstractRuleGenerator;
import tools.ElapsedCpuTimer;
import tools.IO;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by dperez on 19/03/2017.
 */
public class RuleGenMachine
{
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
            rules = sl.modifyRules(rules[0], rules[1], randomSeed);
            if (rules.length < 2) {
                System.out.println("Missing either interaction rules or termination rules.");
            }
            saveGame(gameFile, modifiedFile, rules);
        } catch (Exception e) {
            toPlay.disqualify();
            toPlay.handleResult();
            toPlay.printResult();
            System.out.println(e.getMessage());

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
    private static void saveTree(Node n, int level, BufferedWriter w, String[][] rules) throws IOException{
	String template = "    ";
	String message = "";
	for(int i=0; i<level; i++){
	    message += template;
	}
	w.write(message + n.content.line.trim() + "\n");
	if(n.content instanceof InteractionContent){
	    for(int i=1; i<rules[0].length; i++){
		w.write(message + template + rules[0][i].trim() + "\n");
	    }
	}
	else if(n.content instanceof TerminationContent){
	    for(int i=1; i<rules[1].length; i++){
		w.write(message + template + rules[1][i].trim() + "\n");
	    }
	}
	else{
	    for (int i = 0; i < n.children.size(); i++) {
		saveTree(n.children.get(i), level + 1, w, rules);
	    }
	}
    }
    
    /**
     * Save the result of the rule generations
     * @param gameFile		current game file
     * @param modifiedFile	current new game file
     * @param rules		the generated rules
     */
    private static void saveGame(String gameFile, String modifiedFile, String[][] rules) {
        try {
            if (modifiedFile != null) {
                BufferedWriter writer = new BufferedWriter(new FileWriter(modifiedFile));
                String[] gameLines = new tools.IO().readFile(gameFile);
                Node n = new VGDLParser().indentTreeParser(gameLines);
                saveTree(n, 0, writer, rules);
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
