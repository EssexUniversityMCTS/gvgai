package controllers.Return42.heuristics.features;

import core.game.Observation;
import tools.Vector2d;

import java.util.List;

import controllers.Return42.GameStateCache;

// looks for the nearest npc and tries to avoid him directly
// looks at the current gamestate and checks, if the next player position form the next gamestate is too close to the worst possible npc move
public class NextStepEnemy extends SelfDetectingFeature {

    private double npcWeight;

    public NextStepEnemy(double npcWeight) {
        this.npcWeight = npcWeight;
    }

    @Override
    public boolean isUseful(GameStateCache state) {
        return state.getNPCPositions() != null && state.getAvailableActions().size() > 4; //only for games where we have a sword etc.
    }

    @Override
    protected double evaluateFeature(GameStateCache newState, GameStateCache oldState) {
        Vector2d myNewPosition = newState.getAvatarPosition();
        List<Observation>[] npcs = oldState.getState().getNPCPositions(myNewPosition); //distances from new position
        if(npcs != null) {
            double score = 0;
            for(List<Observation> nearest : npcs) {
                if(nearest.size() > 0) {
                    double dist = myNewPosition.dist(nearest.get(0).position) / oldState.getBlockSize();
                    if(dist == 0) {
                        return 0;//return -10 * getWeight();
                    }
                    score -= (1 / (dist * dist * dist)) * getWeight();
                }
            }
            return score;
        } else {
            return 0;
        }
    }

    @Override
    public double getWeight() {
        return npcWeight * weight;
    }
}
