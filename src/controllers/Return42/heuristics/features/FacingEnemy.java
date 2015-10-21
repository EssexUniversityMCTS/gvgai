package controllers.Return42.heuristics.features;

import core.game.Observation;
import tools.Vector2d;

import java.util.List;

import controllers.Return42.GameStateCache;

public class FacingEnemy extends SelfDetectingFeature {

    private double npcWeight;

    public FacingEnemy(double npcWeight) {
        this.npcWeight = npcWeight;
    }

    @Override
    public boolean isUseful(GameStateCache state) {
        return state.getNPCPositions() != null && state.getAvailableActions().size() > 4; //only for games where we have a sword etc.
    }

    @Override
    protected double evaluateFeature(GameStateCache newState, GameStateCache oldState) {
        //ACTION_UP: 0.0 : -1.0
        //ACTION_RIGHT: 1.0 : 0.0
        //ACTION_LEFT: -1.0 : 0.0
        //ACTION_DOWN: 0.0 : 1.0

        Vector2d orientation = newState.getState().getAvatarOrientation();
        Vector2d myNewPosition = newState.getState().getAvatarPosition();
        List<Observation>[] npcs = oldState.getState().getNPCPositions(myNewPosition); //distances from new position
        if(npcs != null) {
            double score = 1; //should not get deactivated while enemies are around
            for(List<Observation> nearest : npcs) {
                if(nearest.size() > 0) {
                    //Vector2d dir = (myNewPosition.subtract(nearest.get(0).position));
                    Vector2d dir = (nearest.get(0).position.subtract(myNewPosition));
                    double xDist = dir.x;
                    double yDist = dir.y;
                    if(xDist == 0) {
                        if(yDist > 0) {
                            score += orientation.y > 0 ? getWeight() : 0;
                        } else if(yDist < 0) {
                            score += orientation.y < 0 ? getWeight() : 0;
                        } else {
                            score += 0;
                        }
                    } else if(yDist == 0) {
                        if(xDist > 0) {
                            score += orientation.x > 0 ? getWeight() : 0;
                        } else if(xDist < 0) {
                            score += orientation.x < 0 ? getWeight() : 0;
                        } else {
                            score += 0;
                        }
                    }
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
