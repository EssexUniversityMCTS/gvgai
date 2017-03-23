package tools;

/**
 * Created by diego on 26/02/14.
 */

import core.competition.CompetitionParameters;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class ElapsedCpuTimer {

    // allows for easy reporting of elapsed time
    ThreadMXBean bean = ManagementFactory.getThreadMXBean();
    long oldTime;
    long maxTime;

    public ElapsedCpuTimer() {
        oldTime = getTime();
    }


    public ElapsedCpuTimer copy()
    {
        ElapsedCpuTimer newCpuTimer = new ElapsedCpuTimer();
        newCpuTimer.maxTime = this.maxTime;
        newCpuTimer.oldTime = this.oldTime;
        newCpuTimer.bean = this.bean;
        return newCpuTimer;
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
        return getCpuTime();
    }

    private long getCpuTime() {

        if(CompetitionParameters.OS_WIN)
            return System.nanoTime();

        if (bean.isCurrentThreadCpuTimeSupported()) {
            return bean.getCurrentThreadCpuTime();
        } else {
            throw new RuntimeException("CpuTime NOT Supported");
        }

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
