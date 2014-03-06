package controllers.Heuristics;

import core.game.StateObservation;
import ontology.Types;
import tools.Vector2d;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: ssamot
 * Date: 11/02/14
 * Time: 15:44
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class SimpleStateHeuristic extends StateHeuristic {

    double initialNpcCounter = 0;

    public SimpleStateHeuristic(StateObservation stateObs) {

    }

    public double evaluateState(StateObservation stateObs) {
        ArrayList<Vector2d>[] npcPositions = stateObs.getNPCPositions();
        Vector2d avatarPosition = stateObs.getAvatarPosition();
        ArrayList<Vector2d>[] portalPositions = stateObs.getPortalsPositions();

        double won = 0;
        if (stateObs.getGameWinner() == Types.WINNER.PLAYER_WINS) {
            won = 1000000000;
        } else if (stateObs.getGameWinner() == Types.WINNER.PLAYER_LOSES) {
            return -999999999;
        }


        double minDistance = Double.POSITIVE_INFINITY;
        Vector2d minObject = null;

        int npcCounter = 0;
        if (npcPositions != null) {
            for (ArrayList<Vector2d> npcs : npcPositions) {

                //System.out.println("----------------------------");

                for (Vector2d npc : npcs) {
                    //System.out.println(npc);
                    double cDistance = npc.dist(avatarPosition);
                    npcCounter++;
                    if (cDistance < minDistance) {
                        minDistance = cDistance;
                        minObject = npc;

                    }

                }
            }
        }

        if (portalPositions == null) {

            double score = 0;
            if (npcCounter == 0) {
                score = stateObs.getGameScore() + won*100000000;
            } else {
                score = -minDistance / 100.0 + (-npcCounter) * 100.0 + stateObs.getGameScore() + won*100000000;
            }

            return score;
        }

        double minDistancePortal = Double.POSITIVE_INFINITY;
        Vector2d minObjectPortal = null;


        for (ArrayList<Vector2d> portals : portalPositions) {

            //System.out.println("----------------------------");

            for (Vector2d portal : portals) {
                //System.out.println(npc);
                double cDistance = portal.dist(avatarPosition);

                if (cDistance < minDistancePortal) {
                    minDistancePortal = cDistance;
                    minObjectPortal = portal;

                }

            }
        }


        //System.out.println(minObject + " " + minDistance);

        double score = 0;
        if (minObjectPortal == null) {
            score = stateObs.getGameScore() + won*100000000;
            }
        else {
            score = stateObs.getGameScore() + won*1000000 - minDistancePortal * 10.0;
        }

        return score;
    }


}


