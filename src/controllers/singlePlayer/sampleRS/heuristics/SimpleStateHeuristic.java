package controllers.singlePlayer.sampleRS.heuristics;

import core.game.Observation;
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

    public SimpleStateHeuristic(StateObservation stateObs) { }

    public double evaluateState(StateObservation stateObs) {
        Vector2d avatarPosition = stateObs.getAvatarPosition();
        ArrayList<Observation>[] npcPositions = stateObs.getNPCPositions(avatarPosition);
        ArrayList<Observation>[] portalPositions = stateObs.getPortalsPositions(avatarPosition);

        double won = 0;
        if (stateObs.getGameWinner() == Types.WINNER.PLAYER_WINS) {
            won = 1000000000;
        } else if (stateObs.getGameWinner() == Types.WINNER.PLAYER_LOSES) {
            return -999999999;
        }


        double minDistance = Double.POSITIVE_INFINITY;

        int npcCounter = 0;
        if (npcPositions != null) {
            for (ArrayList<Observation> npcs : npcPositions) {
                if(npcs.size() > 0)
                {
                    minDistance = npcs.get(0).sqDist;   //This is the (square) distance to the closest NPC.
                    npcCounter += npcs.size();
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
        for (ArrayList<Observation> portals : portalPositions) {
            if(portals.size() > 0)
            {
                minObjectPortal   =  portals.get(0).position; //This is the closest portal
                minDistancePortal =  portals.get(0).sqDist;   //This is the (square) distance to the closest portal
            }
        }

        double score;
        if (minObjectPortal == null) {
            score = stateObs.getGameScore() + won*100000000;
        }
        else {
            score = stateObs.getGameScore() + won*1000000 - minDistancePortal * 10.0;
        }

        return score;
    }


}


