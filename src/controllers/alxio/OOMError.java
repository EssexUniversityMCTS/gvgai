/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers.alxio;

import java.text.NumberFormat;

/**
 *
 * @author ALX
 */
public class OOMError extends Error{
    public static OOMError create(int size, int ticks, Throwable e){
        //Agent.dummy = null;
        //System.gc();
        Runtime runtime = Runtime.getRuntime();
        NumberFormat format = NumberFormat.getInstance();
        StringBuilder sb = new StringBuilder();
        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        sb.append("tree size" + size);
        sb.append("\n");
        sb.append("ticks count" + ticks);
        sb.append("\n");
        sb.append("free memory: " + format.format(freeMemory / 1024));
        sb.append("\n");
        sb.append("allocated memory: " + format.format(allocatedMemory / 1024));
        sb.append("\n");
        sb.append("max memory: " + format.format(maxMemory / 1024));
        sb.append("\n");
        sb.append("total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024));
        sb.append("\n");
        return new OOMError(sb.toString(), e);
    }
    private OOMError(String s, Throwable e){
        super(s, e);
    }
}
