package controllers.Heuristics;

import core.game.Observation;
import core.game.StateObservation;
import ontology.Types;
import tools.Vector2d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA. User: ssamot Date: 11/02/14 Time: 15:44 This is a Java port from Tom Schaul's VGDL -
 * https://github.com/schaul/py-vgdl
 */
public class SimpleStateHeuristic implements StateHeuristic {

  double initialNpcCounter;

  public SimpleStateHeuristic(StateObservation stateObs) {

  }

  @Override
  public double evaluateState(StateObservation stateObs) {
    Vector2d avatarPosition = stateObs.getAvatarPosition();
    ArrayList<Observation>[] npcPositions = stateObs.getNPCPositions(avatarPosition);
    ArrayList<Observation>[] portalPositions = stateObs.getPortalsPositions(avatarPosition);
    HashMap<Integer, Integer> resources = stateObs.getAvatarResources();

    ArrayList<Observation>[] npcPositionsNotSorted = stateObs.getNPCPositions();

    double won = 0;
    if (Types.WINNER.PLAYER_WINS == stateObs.getGameWinner()) {
      won = 1000000000;
    } else if (Types.WINNER.PLAYER_LOSES == stateObs.getGameWinner()) {
      return -999999999;
    }

    double minDistance = Double.POSITIVE_INFINITY;

    int npcCounter = 0;
    if (null != npcPositions) {

      for (List<Observation> npcs : npcPositions)
        if (!npcs.isEmpty()) {
          Vector2d minObject = npcs.get(0).position;
          minDistance = npcs.get(0).sqDist; // This is the (square)
          // distance to the
          // closest NPC.
          int minNPC_ID = npcs.get(0).obsID;
          // closest NPC.
          int minNPCType = npcs.get(0).itype;
          // closest NPC.
          npcCounter += npcs.size();
        }
    }

    if (null == portalPositions)
      return 0 == npcCounter ? stateObs.getGameScore() + won * 100000000 : -minDistance / 100.0
          + -npcCounter * 100.0 + stateObs.getGameScore() + won * 100000000;

    double minDistancePortal = Double.POSITIVE_INFINITY;
    Vector2d minObjectPortal = null;
    for (List<Observation> portals : portalPositions)
      if (!portals.isEmpty()) {
        minObjectPortal = portals.get(0).position; // This is the
        // closest portal
        minDistancePortal = portals.get(0).sqDist; // This is the
        // (square) distance
        // to the closest
        // portal
      }

    return minObjectPortal == null ? stateObs.getGameScore() + won * 100000000 : stateObs
        .getGameScore()
        + won * 1000000 - minDistancePortal * 10.0;
  }
}
