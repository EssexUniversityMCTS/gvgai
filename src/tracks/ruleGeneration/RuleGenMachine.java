package tracks.ruleGeneration;

import core.vgdl.VGDLFactory;
import core.vgdl.VGDLParser;
import core.vgdl.VGDLRegistry;
import core.competition.CompetitionParameters;
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
                String state = "none";
                for(String line:gameLines){
                    if(line.contains("BasicGame")){
                        state = "none";
                        writer.write(line.trim() + "\n");
                        continue;
                    }
                    if(line.contains("InteractionSet")){
                        state="interaction";
                        for(int i=0; i<rules[0].length; i++){
                            writer.write("   " + rules[0][i] + "\n");
                        }
                        continue;
                    }
                    if(line.contains("TerminationSet")){
                        state="termination";
                        for(int i=0; i<rules[1].length; i++){
                            writer.write("   " + rules[1][i] + "\n");
                        }
                        continue;
                    }
                    if(line.contains("SpriteSet") || line.contains("LevelMapping")){
                        state = "none";
                        writer.write("   " + line.trim() + "\n");
                        continue;
                    }

                    switch(state){
                        case "interaction":
                            continue;
                        case "termination":
                            continue;
                        case "none":
                            writer.write("    " + line + "\n");
                            break;
                    }
                }
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
