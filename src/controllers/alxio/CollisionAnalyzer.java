/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers.alxio;

import core.game.Event;
import core.game.Observation;
import core.game.StateObservation;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import ontology.Types;
import tools.Vector2d;

/**
 *
 * @author ALX
 */
public class CollisionAnalyzer {

    int x, y;

    public CollisionAnalyzer(StateObservation so) {
        ArrayList<Observation>[][] grid = so.getObservationGrid();
        x = grid[0].length;
        y = grid.length;
        for (int i = 0; i < y; ++i) {
            for (int j = 0; j < x; ++j) {
                ArrayList<Observation> obs = grid[i][j];
                for (Observation o : obs) {
                    int t = o.itype;
                    if (count[t] == 0) {
                        count[t] = 1;
//                        switch (o.category) {
//                            case Types.TYPE_RESOURCE:
//                                values[t] = 2;
//                                break;
//                            case Types.TYPE_PORTAL:
//                                values[t] = 2;
//                                break;
//                            case Types.TYPE_NPC:
//                                values[t] = -1;
//                                //shootCount[t] = 1;
//                                //shootValues[t] = 1;
//                                count[t] = 0.1;
//                                break;
//                        }
                    }
                }
            }
        }
    }

    public void analyze(StateObservation so, double lastScore) {
        double delta = so.getGameScore() - lastScore;
        boolean gameOver = so.isGameOver();
        Types.WINNER win = so.getGameWinner();

        if (gameOver && win == Types.WINNER.PLAYER_LOSES) {
            delta -= 100;
        }
        if (gameOver && win == Types.WINNER.PLAYER_WINS) {
            delta += 100;
        }

        if(delta == 0) return;
        
        Iterator<Event> iter = so.getEventsHistory().descendingIterator();
        int lastStep = so.getGameTick() - 1;
        Event event = null;

        ArrayDeque<Integer> Q = new ArrayDeque<>();
        // ArrayDeque<Integer> Q1 = new ArrayDeque<>();
        while (iter.hasNext()) {
            event = iter.next();
            if (event.gameStep < lastStep) {
                break;
            }
            Vector2d pos = so.getAvatarPosition();
            if(event.position.dist(pos) < so.getBlockSize() || pos == Types.NIL ){
                Q.add(event.activeTypeId);
                Q.add(event.passiveTypeId);
            }
        }
        double cnt = Q.isEmpty() ? 0 : 1.0 / Q.size();
        for (Integer i : Q) {
            values[i] += cnt * delta;
            count[i] += cnt;
        }
//        cnt = Q1.isEmpty() ? 0 : 1.0 / Q1.size();
//        for (Integer i : Q1) {
//            shootValues[i] += cnt * delta;
//            shootCount[i] += cnt;
//        }
    }

    public double getValue(int id) {
        return values[id] == 0 ? 0
                : values[id] / count[id] + K * Math.sqrt(Math.log(count[id] + 1) / (count[id] + K));
    }

    public void printValues(){
        for(int i=0;i<1000;++i){
            if(values[i] != 0){
                System.out.println(i + ": " + getValue(i));
            }
        }
    }
    
//    public double getShootValue(int id) {
//        return shootValues[id] / (shootCount[id] + 1) + K * Math.sqrt(Math.log(count[id] + 1) / (count[id] + K));
//    }
    public double eval(StateObservation so) {
        double value = 0;
        ArrayList<Observation>[] array;
        array = so.getResourcesPositions();
        if (array != null) {
            for (ArrayList<Observation> obs : array) {
                for (int i = 0; i < 4 && i < obs.size(); ++i) {
                    value += getValue(obs.get(i).itype) / obs.get(i).sqDist;
                }
            }
        }
        array = so.getNPCPositions();
        if (array != null) {
            for (ArrayList<Observation> obs : array) {
                for (int i = 0; i < 4 && i < obs.size(); ++i) {
                    value += getValue(obs.get(i).itype) / Math.sqrt(obs.get(i).sqDist);
                }
            }
        }
        array = so.getPortalsPositions();
        if (array != null) {
            for (ArrayList<Observation> obs : array) {
                for (int i = 0; i < 4 && i < obs.size(); ++i) {
                    value += getValue(obs.get(i).itype) / Math.sqrt(obs.get(i).sqDist);
                }
            }
        }
        return value;
    }

    double values[] = new double[1001];
    double count[] = new double[1001];

//    double shootValues[] = new double[1001];
//    double shootCount[] = new double[1001];
    private static final double K = 0.001;
}
