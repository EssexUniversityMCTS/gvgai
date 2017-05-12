package core.competition;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 17/11/13
 * Time: 13:48
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class CompetitionParameters
{
    /**
     * Milliseconds allowed per controller action.
     */
    public static int ACTION_TIME = 40;

    /**
     * Milliseconds for controller disqualification, if it returns an action after this time.
     */
    public static int ACTION_TIME_DISQ = 50;

    /**
     * Milliseconds allowed for controller initialization.
     */
    public static int INITIALIZATION_TIME = 1000;

    /**
     * Milliseconds allowed for controller tear down.
     */
    public static int TEAR_DOWN_TIME = 100;

    /**
     * Milliseconds allowed for the level generator to generate a level
     */
    //public static int LEVEL_ACTION_TIME = 1800000*10;
    //public static int LEVEL_ACTION_TIME = 28800000;
    public static int LEVEL_ACTION_TIME = 3600000;
    /**
     * Milliseconds allowed for the level generator disqualification, if it returns a level after this time.
     */
    public static int LEVEL_ACTION_TIME_DISQ = 21600000;
    
    /**
     * Milliseconds allowed for level generator to initialize
     */
    public static int LEVEL_INITIALIZATION_TIME = 60000;
    
    /**
     * Milliseconds allowed for the rule generator to generate rules
     */
    public static int RULE_ACTION_TIME = 1800000*10;
    
    /**
     * Milliseconds allowed for the rule generator disqualification, if it returns rules after this time.
     */
    public static int RULE_ACTION_TIME_DISQ = 21600000;
    
    /**
     * Milliseconds allowed for rule generator to initialize
     */
    public static int RULE_INITIALIZATION_TIME = 60000;
    
    /**
     * Number of repetition during the optimization operation
     */
    public static int OPTIMIZATION_REPEATITION = 1;

    /**
     * Indicates if the OS is Windows.
     */
    public static boolean OS_WIN = System.getProperty("os.name").contains("Windows");

    /**
     * Indicates if the overspend should be taken into account or not.
     *  Time limits are WALL TIME on Windows, because CPU TIME is not accurate enough
     *  at the level of milliseconds on this OS.
     */
    public static boolean TIME_CONSTRAINED = true;

    /**
     * Max number of evaluations that can be done
     */
    public static int OPTIMIZATION_EVALUATION = 5;
    
    /**
     * Path to sprite images.
     */
    public static String IMG_PATH = "sprites/";


    /**
     * Delay for human play.
     */
    public static int DELAY = 15;

    /**
     * Longer delay for human play.
     */
    public static int LONG_DELAY = 25;
    
    /**
     * Number of warnings that the game will be considered over after it.
     */
    public static int MAX_ALLOWED_WARNINGS = 20;


    /**
     * Max time a game can run
     */
    public static int MAX_TIMESTEPS = 2000;

    /**
     * Terminates the program when the playing window is closed
     */
    public static boolean closeAppOnClosingWindow = false;
    
    /**
     * Pause the game at the beginning and at the end
     */
    public static boolean dialogBoxOnStartAndEnd = true;
    
    /**
     * Close the open window when you die or win
     */
    public static boolean killWindowOnEnd = true;
    
    /**
     * Java Warm-up time before starting the game.
     */
    public static final long WARMUP_TIME = 5000;
    public static final long WARMUP_CP = 100;
    public static final long WARMUP_ADV = 1000;

    /**
     * Key input type. We set the default here, but this will be set by the game in VGDL.
     */
    public static final int KEY_INPUT = 0;
    public static final int KEY_PULSE = 1;
    public static int KEY_HANDLER = KEY_INPUT;
}
