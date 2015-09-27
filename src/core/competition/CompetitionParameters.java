package core.competition;

import tools.ElapsedCpuTimer;

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
    public static int ACTION_TIME = 50;

    /**
     * Milliseconds for controller disqualification, if it returns an action after this time.
     */
    public static int ACTION_TIME_DISQ = 60;

    /**
     * Milliseconds allowed for controller initialization.
     */
    public static int INITIALIZATION_TIME = 1000;

    /**
     * Milliseconds allowed for the level generator to generate a level
     */
    public static int LEVEL_ACTION_TIME = 1000;
    
    /**
     * Milliseconds allowed for the level generator disqualification, if it returns a level after this time.
     */
    public static int LEVEL_ACTION_TIME_DISQ = 1100;
    
    /**
     * Milliseconds allowed for level generator to initialize
     */
    public static int LEVEL_INITIALIZATION_TIME = 1000;
    
    /**
     * Path to sprite images.
     */
    public static String IMG_PATH = "sprites/";


    /**
     * Delay for human play.
     */
    public static int DELAY = 20;

    /**
     * Longer delay for human play.
     */
    public static int LONG_DELAY = 30;

    /**
     * Max time a game can run
     */
    public static int MAX_TIMESTEPS = 2000;


    /**
     * Java Warm-up time before starting the game.
     */
    public static final long WARMUP_TIME = 5000;
    public static final long WARMUP_CP = 100;
    public static final long WARMUP_ADV = 1000;

    /**
     * Indicates the type of timer the framework should use.
     */
    public static ElapsedCpuTimer.TimerType TIMER_TYPE = ElapsedCpuTimer.TimerType.CPU_TIME;


}
