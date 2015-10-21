package controllers.Return42.heuristics.features;

import core.game.Observation;
import tools.Vector2d;

import java.util.List;

import controllers.Return42.GameStateCache;
import controllers.Return42.heuristics.features.controller.PortalController;

public class PortalDistance extends Feature {

    private final double portalDistanceFactor;
    private final PortalController controller;

    public PortalDistance(double portalDistanceFactor, PortalController controller) {
        this.portalDistanceFactor = portalDistanceFactor;
        this.controller = controller;
    }

    @Override
    public boolean isUseful(GameStateCache state) {
        return state.getPortalsPositions() != null;
    }

    @Override
    public double evaluate(GameStateCache state) {
        if(controller.isActive()) {
            List<Observation>[] portalPositions = state.getPortalsPositions();
            if(portalPositions != null) {
                double minDistancePortal = 0;
                Vector2d minObjectPortal = null;
                for(List<Observation> portals : portalPositions) {
                    if(portals.size() > 0) {
                        Observation o = portals.get(0);
                        minObjectPortal = o.position;
                        minDistancePortal += Math.sqrt(o.sqDist) / state.getBlockSize();
                    }
                }
                if(minObjectPortal != null) {
                    return -minDistancePortal * getWeight();
                }
            }
            return 0;
        } else {
            return -1;
        }
    }

    @Override
    public double getWeight() {
        return portalDistanceFactor * weight;
    }
}