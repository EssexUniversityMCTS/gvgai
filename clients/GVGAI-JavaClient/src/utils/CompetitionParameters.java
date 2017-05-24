package utils;

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
     * Milliseconds allowed for controller's constructor.
     */
    public static final int START_TIME = 1000;

    /**
     * Milliseconds allowed for controller initialization (per game played).
     */
    public static final int INITIALIZATION_TIME = 1000;


    /**
     * Milliseconds allowed per controller action.
     */
    public static final int ACTION_TIME = 1;

    /**
     * Milliseconds for controller disqualification, if it returns an action after this time.
     */
    public static int ACTION_TIME_DISQ = 2;

    /**
     * Maximum time allowed for a learning track game, equivalent of 10 minutes in milliseconds.
     */
    public static final int TOTAL_LEARNING_TIME = 1000; //2*60000; //10*60000

    /**
     * Extra second for learning time, used in case the last game finished after TOTAL_LEARNING_TIME.
     */
    public static final int EXTRA_LEARNING_TIME = 1000; //1 second.

}
