/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers.alxio;

import core.game.Observation;
import java.util.ArrayList;

/**
 *
 * @author ALX
 */
public class Debug {

    static int level = 10;

    public static void println(String line) {
        log(0, line);
    }

    static void log(int lvl, String line) {
        if (lvl >= level) {
            System.out.println(line);
        }
    }

    public static void printGrid(ArrayList<Observation>[][] grid) {
        if (level < 1) {
            for (int i = 0; i < grid[0].length; ++i) {
                for (int j = 0; j < grid.length; ++j) {
                    ArrayList<Observation> obs = grid[j][i];
                    if (obs.size() == 0) {
                        System.out.print("|    ");
                    } else if (obs.size() == 1) {
                        String s = obs.get(0).itype + "";
                        s = ("|    " + s).substring(s.length());
                        System.out.print(s);
                    } else {
                        StringBuilder ss = new StringBuilder();
                        for(Observation o : obs) {
                            ss.append(' ');
                            ss.append(o.itype);
                        }
                        String sss = ("    " + ss).substring(ss.length());
                        System.out.print(sss);
                    }
                    //System.out.print(grid[i][j].size() + " ");
                }
                System.out.println();
            }
            System.out.println();
        }
    }
}
