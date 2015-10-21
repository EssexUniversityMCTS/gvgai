package controllers.Return42.util;

import tools.ElapsedCpuTimer;

/**
 * Created by Oliver on 06.05.2015.
 */
public class TimerUtils {

    /**
     * Creates a new timer with smaller amount of remaining time.
     * This is useful, if you want to set a safety margin of a few millisecs in your operations.
     */
    public static ElapsedCpuTimer copyWithLessTime( ElapsedCpuTimer timer, long msToSubtract ) {
        ElapsedCpuTimer shorterTimer = timer.copy();
        shorterTimer.setMaxTimeMillis(shorterTimer.remainingTimeMillis() - msToSubtract );

        return shorterTimer;
    }
}
