package controllers.Return42.heuristics.features;

import core.game.Observation;
import tools.Vector2d;

import java.util.List;

import controllers.Return42.GameStateCache;

public class ResourceCollection extends Feature {

    private final double resDistanceFactor;
    private final double resCounterFactor;

    public ResourceCollection(double resDistanceFactor, double resCounterFactor) {
        this.resDistanceFactor = resDistanceFactor;
        this.resCounterFactor = resCounterFactor;
    }

    @Override
    public boolean isUseful(GameStateCache state) {
        return false;
    }

    @Override
    public double evaluate(GameStateCache state) {
        List<Observation>[] resourcePositions = state.getResourcesPositions();
        if(resourcePositions != null) {
            int resourceCounter = 0;
            double minDistanceRes = 0;
            Vector2d minObjectRes = null;
            for(List<Observation> res : resourcePositions) {
                int size = res.size();
                if(size > 0) {
                    Observation o = res.get(0);
                    minObjectRes = o.position;
                    minDistanceRes += Math.sqrt(o.sqDist) / state.getBlockSize();
                    resourceCounter += size;
                }
            }
            if(minObjectRes != null) {
                return -minDistanceRes * resDistanceFactor - resourceCounter * resCounterFactor;
            }
        }
        return 0;
    }

    @Override
    public double getWeight() {
        return (resDistanceFactor + resCounterFactor) * weight;
    }
}
