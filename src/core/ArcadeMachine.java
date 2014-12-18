package core;

import core.competition.CompetitionParameters;
import core.game.Game;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.StatSummary;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 06/11/13
 * Time: 11:24
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class ArcadeMachine
{
    public static final boolean VERBOSE = false;

    /**
     * Reads and launches a game for a human to be played. Graphics always on.
     * @param game_file game description file.
     * @param level_file file with the level to be played.
     */
    public static double playOneGame(String game_file, String level_file, String actionFile, int randomSeed)
    {
        String agentName = "controllers.human.Agent";
        boolean visuals = true;
        return runOneGame(game_file, level_file, visuals, agentName, actionFile, randomSeed);
    }

    /**
     * Reads and launches a game for a bot to be played. Graphics can be on or off.
     * @param game_file game description file.
     * @param level_file file with the level to be played.
     * @param visuals true to show the graphics, false otherwise.
     * @param agentName name (inc. package) where the controller is otherwise.
     * @param actionFile filename of the file where the actions of this player, for this game, should be recorded.
     * @param randomSeed sampleRandom seed for the sampleRandom generator.
     */
    public static double runOneGame(String game_file, String level_file, boolean visuals,
                                    String agentName, String actionFile, int randomSeed)
    {
        VGDLFactory.GetInstance().init(); //This always first thing to do.
        VGDLRegistry.GetInstance().init();

        System.out.println(" ** Playing game " + game_file + ", level " + level_file + " **");

        // First, we create the game to be played..
        Game toPlay = new VGDLParser().parseGame(game_file);
        toPlay.buildLevel(level_file);

        //Warm the game up.
        ArcadeMachine.warmUp(toPlay, CompetitionParameters.WARMUP_TIME);

        //Create the player.
        AbstractPlayer player = ArcadeMachine.createPlayer(agentName, actionFile, toPlay.getObservation(), randomSeed);

        if(player == null)
        {
            //Something went wrong in the constructor, controller disqualified
            toPlay.disqualify();

            //Get the score for the result.
            return toPlay.handleResult();

        }

        //Then, play the game.
        double score = 0.0;
        if(visuals)
            score = toPlay.playGame(player, randomSeed);
        else
            score = toPlay.runGame(player, randomSeed);

        //Finally, when the game is over, we need to tear the player down.
        ArcadeMachine.tearPlayerDown(player);

        return score;
    }


    /**
     * Runs a replay given a game, level and file with the actions to execute.
     * @param game_file game description file.
     * @param level_file file with the level to be played.
     * @param visuals true to show the graphics, false otherwise.
     * @param actionFile name of the file where the actions of this player, for this game, must be read from.
     */
    public static double replayGame(String game_file, String level_file, boolean visuals, String actionFile)
    {
        String agentName = "controllers.replayer.Agent";
        VGDLFactory.GetInstance().init();  //This always first thing to do.
        VGDLRegistry.GetInstance().init();

        // First, we create the game to be played..
        Game toPlay = new VGDLParser().parseGame(game_file);
        toPlay.buildLevel(level_file);

        //Second, create the player. Note: null as action_file and -1 as sampleRandom seed
        // (we don't want to record anything from this execution).
        AbstractPlayer player = ArcadeMachine.createPlayer(agentName, null, toPlay.getObservation(), -1);

        if(player == null)
        {
            //Something went wrong in the constructor, controller disqualified
            toPlay.disqualify();

            //Get the score for the result.
            return toPlay.handleResult();
        }

        int seed = 0;
        ArrayList<Types.ACTIONS> actions = new ArrayList<Types.ACTIONS> ();

        try
        {
            BufferedReader br = new BufferedReader(new FileReader(actionFile));

            //First line should be the sampleRandom seed.
            seed = Integer.parseInt(br.readLine());
            System.out.println("Replaying game in " + game_file + ", " + level_file + " with seed " + seed);

            //The rest are the actions:
            String line = br.readLine();
            while(line != null)
            {
                Types.ACTIONS nextAction = Types.ACTIONS.fromString(line);
                actions.add(nextAction);

                //next!
                line = br.readLine();
            }

        }catch(Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }

        //Assign the actions to the player:
        ((controllers.replayer.Agent)player).setActions(actions);

        //Then, (re-)play the game.
        double score = 0.0;
        if(visuals)
            score = toPlay.playGame(player, seed);
        else
            score = toPlay.runGame(player, seed);

        //Finally, when the game is over, we need to tear the player down. Actually in this case this might never do anything.
        ArcadeMachine.tearPlayerDown(player);

        return score;
    }


    /**
     * Reads and launches a game for a bot to be played. It specifies which levels to play and how many times.
     * Filenames for saving actions can be specified. Graphics always off.
     * @param game_file game description file.
     * @param level_files array of level file names to play.
     * @param level_times how many times each level has to be played.
     * @param actionFiles names of the files where the actions of this player, for this game, should be recorded. Accepts
     *                    null if no recording is desired. If not null, this array must contain as much String objects as
     *                    level_files.length*level_times.
     */
    public static void runGames(String game_file, String[] level_files, int level_times,
                                String agentName, String[] actionFiles)
    {
        VGDLFactory.GetInstance().init(); //This always first thing to do.
        VGDLRegistry.GetInstance().init();

        boolean recordActions = false;
        if(actionFiles != null)
        {
            recordActions = true;
            assert actionFiles.length >= level_files.length*level_times :
                    "runGames (actionFiles.length<level_files.length*level_times): " +
                    "you must supply an action file for each game instance to be played, or null.";
        }

        StatSummary scores = new StatSummary();

        Game toPlay = new VGDLParser().parseGame(game_file);
        int levelIdx = 0;
        for(String level_file : level_files)
        {

            for(int i = 0; i < level_times; ++i)
            {
                System.out.println(" ** Playing game " + game_file + ", level " + level_file + " ("+(i+1)+"/"+level_times+") **");

                //build the level in the game.
                toPlay.buildLevel(level_file);

                String filename = recordActions ? actionFiles[levelIdx*level_times + i] : null;

                //Warm the game up.
                ArcadeMachine.warmUp(toPlay, CompetitionParameters.WARMUP_TIME);

                //Determine the random seed, different for each game to be played.
                int randomSeed = new Random().nextInt();

                //Create the player.
                AbstractPlayer player = ArcadeMachine.createPlayer(agentName, filename, toPlay.getObservation(), randomSeed);

                double score = -1;
                if(player == null)
                {
                    //Something went wrong in the constructor, controller disqualified
                    toPlay.disqualify();

                    //Get the score for the result.
                    score = toPlay.handleResult();

                }else{

                    //Then, play the game.
                    score = toPlay.runGame(player, randomSeed);
                }

                scores.add(score);

                //Finally, when the game is over, we need to tear the player down.
                if(player != null) ArcadeMachine.tearPlayerDown(player);

                //reset the game.
                toPlay.reset();
            }

            levelIdx++;
        }

        System.out.println(" *** Results in game " + game_file + " *** ");
        System.out.println(scores);
        System.out.println(" *********");
    }

    /**
     * Creates a player given its name with package. This class calls the constructor of the agent
     * and initializes the action recording procedure.
     * @param playerName name of the agent to create. It must be of the type "<agentPackage>.Agent".
     * @param actionFile filename of the file where the actions of this player, for this game, should be recorded.
     * @param so Initial state of the game to be played by the agent.
     * @param randomSeed Seed for the sampleRandom generator of the game to be played.
     * @return the player, created and initialized, ready to start playing the game.
     */
    private static AbstractPlayer createPlayer(String playerName, String actionFile, StateObservation so, int randomSeed)
    {
        AbstractPlayer player = null;

        try{
            //create the controller.
            player = createController(playerName, so);
            if(player != null)
                player.setup(actionFile, randomSeed);

        }catch (Exception e)
        {
            //This probably happens because controller took too much time to be created.
            e.printStackTrace();
            System.exit(1);
        }

        return player;
    }

    /**
     * Creates and initializes a new controller with the given name. Takes into account the initialization time,
     * calling the appropriate constructor with the state observation and time due parameters.
     * @param playerName Name of the controller to instantiate.
     * @param so Initial state of the game to be played by the agent.
     * @return the player if it could be created, null otherwise.
     */
    protected static AbstractPlayer createController(String playerName, StateObservation so) throws RuntimeException
    {
        AbstractPlayer player = null;
        try
        {
            //Get the class and the constructor with arguments (StateObservation, long).
            Class<? extends AbstractPlayer> controllerClass = Class.forName(playerName).asSubclass(AbstractPlayer.class);
            Class[] gameArgClass = new Class[]{StateObservation.class, ElapsedCpuTimer.class};
            Constructor controllerArgsConstructor = controllerClass.getConstructor(gameArgClass);

            //Determine the time due for the controller creation.
            ElapsedCpuTimer ect = new ElapsedCpuTimer(CompetitionParameters.TIMER_TYPE);
            ect.setMaxTimeMillis(CompetitionParameters.INITIALIZATION_TIME);

            //Call the constructor with the appropriate parameters.
            Object[] constructorArgs = new Object[] {so, ect.copy()};
            player = (AbstractPlayer) controllerArgsConstructor.newInstance(constructorArgs);

            //Check if we returned on time, and act in consequence.
            long timeTaken = ect.elapsedMillis();
            if(ect.exceededMaxTime())
            {
                long exceeded =  - ect.remainingTimeMillis();
                System.out.println("Controller initialization time out (" + exceeded + ").");

                return null;
            }
            else
            {
                System.out.println("Controller initialization time: " + timeTaken + " ms.");
            }

        //This code can throw many exceptions (no time related):

        }catch(NoSuchMethodException e)
        {
            e.printStackTrace();
            System.err.println("Constructor " + playerName + "(StateObservation,long) not found in controller class:");
            System.exit(1);

        }catch(ClassNotFoundException e)
        {
            System.err.println("Class " + playerName + " not found for the controller:");
            e.printStackTrace();
            System.exit(1);

        }catch(InstantiationException e)
        {
            System.err.println("Exception instantiating " + playerName + ":");
            e.printStackTrace();
            System.exit(1);

        }catch(IllegalAccessException e)
        {
            System.err.println("Illegal access exception when instantiating " + playerName + ":");
            e.printStackTrace();
            System.exit(1);
        }catch(InvocationTargetException e)
        {
            System.err.println("Exception calling the constructor " + playerName + "(StateObservation,long):");
            e.printStackTrace();
            System.exit(1);
        }

        return player;
    }

    /**
     * This methods takes the game and warms it up. This allows Java to finish the runtime compilation
     * process and optimize the code before the proper game starts.
     * @param toPlay game to be warmed up.
     * @param howLong for how long the warming up process must last (in milliseconds).
     */
    private static void warmUp(Game toPlay, long howLong)
    {
        StateObservation stateObs = toPlay.getObservation();
        ElapsedCpuTimer ect = new ElapsedCpuTimer(CompetitionParameters.TIMER_TYPE);
        ect.setMaxTimeMillis(howLong);

        int playoutLength = 10;
        ArrayList<Types.ACTIONS> actions = stateObs.getAvailableActions();
        int copyStats = 0;
        int advStats = 0;

        StatSummary ss1 = new StatSummary();
        StatSummary ss2 = new StatSummary();


        boolean finish = ect.exceededMaxTime() || (copyStats>CompetitionParameters.WARMUP_CP && advStats>CompetitionParameters.WARMUP_ADV);

        //while(!ect.exceededMaxTime())
        while(!finish)
        {
            for (Types.ACTIONS action : actions)
            {
                StateObservation stCopy = stateObs.copy();
                ElapsedCpuTimer ectAdv = new ElapsedCpuTimer();
                stCopy.advance(action);
                copyStats++;
                advStats++;

                if( ect.remainingTimeMillis() < CompetitionParameters.WARMUP_TIME*0.5)
                {
                    ss1.add(ectAdv.elapsedNanos());
                }

                for (int i = 0; i < playoutLength; i++) {

                    int index = new Random().nextInt(actions.size());
                    Types.ACTIONS actionPO = actions.get(index);

                    ectAdv = new ElapsedCpuTimer();
                    stCopy.advance(actionPO);
                    advStats++;

                    if( ect.remainingTimeMillis() < CompetitionParameters.WARMUP_TIME*0.5)
                    {
                        ss2.add(ectAdv.elapsedNanos());
                    }
                }
            }

            finish = ect.exceededMaxTime() || (copyStats>CompetitionParameters.WARMUP_CP && advStats>CompetitionParameters.WARMUP_ADV);

            //if(VERBOSE)
            //System.out.println("[WARM-UP] Remaining time: " + ect.remainingTimeMillis() +
            //        " ms, copy() calls: " + copyStats + ", advance() calls: " + advStats);
        }

        if(VERBOSE)
        {
            System.out.println("[WARM-UP] Finished, copy() calls: " + copyStats + ", advance() calls: " + advStats + ", time (s): " + ect.elapsedSeconds());
            //System.out.println(ss1);
            //System.out.println(ss2);
        }


        //Reset input to delete warm-up effects.
        Game.ki.reset();
    }


    /**
     * Tears the player down. This initiates the saving of actions to file.
     * It should be called when the game played is over.
     * @param player player to be closed.
     */
    private static void tearPlayerDown(AbstractPlayer player)
    {
        player.teardown();
    }


}
