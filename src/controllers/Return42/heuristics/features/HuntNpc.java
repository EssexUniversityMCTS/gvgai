package controllers.Return42.heuristics.features;

import core.game.Observation;

import java.util.List;

import controllers.Return42.GameStateCache;

public class HuntNpc extends Feature {

    private final double npcDistanceFactor;
    private final double npcCounterFactor;

    public HuntNpc(double npcDistanceFactor, double npcCounterFactor) {
        this.npcDistanceFactor = npcDistanceFactor;
        this.npcCounterFactor = npcCounterFactor;
    }

    @Override
    public boolean isUseful(GameStateCache state) {
        return state.getNPCPositions() != null && state.getAvailableActions().size() < 5;
    }

    @Override
    public double evaluate(GameStateCache state) {
        List<Observation>[] npcPositions = state.getNPCPositions();
        if(npcPositions != null) {
            double npcMinDistance = 0;
            int npcCounter = 0;

            for(List<Observation> npcs : npcPositions) {
                if(npcs.size() > 0) {
                    npcMinDistance += Math.sqrt(npcs.get(0).sqDist) / state.getBlockSize();
                    npcCounter += npcs.size();
                }
            }

            if(npcCounter > 0) {
                //go for npc and try to kill
                return -npcMinDistance * npcDistanceFactor - npcCounter * npcCounterFactor;
            }
        }
        return 0;
    }

    @Override
    public double getWeight() {
        return (npcDistanceFactor + npcCounterFactor) * weight;
    }
}
