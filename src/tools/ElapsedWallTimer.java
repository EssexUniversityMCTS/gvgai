package tools;

/**
 * Created by diego on 26/02/14.
 */

import core.competition.CompetitionParameters;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class ElapsedWallTimer {

    long oldTime;
    long maxTime;

    public ElapsedWallTimer() {
        oldTime = getTime();
    }


    public ElapsedWallTimer copy()
    {
        ElapsedWallTimer newWallTimer = new ElapsedWallTimer();
        newWallTimer.maxTime = this.maxTime;
        newWallTimer.oldTime = this.oldTime;
        return newWallTimer;
    }

    public long elapsed() {
        return getTime() - oldTime;
    }


    public long elapsedNanos() {
        return elapsed();
    }

    public long elapsedMillis() {
        return (long) (elapsed() / 1000000.0);
    }

    public double elapsedSeconds() {
        return elapsedMillis()/1000.0;
    }

    public double elapsedMinutes() {
        return elapsedMillis()/1000.0/60.0;
    }


    public double elapsedHours() {
        return elapsedMinutes()/60.0;
    }


    @Override
	public String toString() {
        // now resets the timer...
        String ret = elapsed() / 1000000.0 + " ms elapsed";
        //reset();
        return ret;
    }

    private long getTime() {
        return getWallTime();
    }

    private long getWallTime() {
        return System.nanoTime();
    }

    public void setMaxTimeMillis(long time) {
        maxTime = time * 1000000;

    }

    public long remainingTimeMillis()
    {
        long diff = maxTime - elapsed();
        return (long) (diff / 1000000.0);
    }

    public boolean exceededMaxTime() {
        if (elapsed() > maxTime) {
            return true;
        }
        return false;
    }

}
