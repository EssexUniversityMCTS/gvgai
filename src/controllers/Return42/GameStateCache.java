package controllers.Return42;

import core.game.Observation;
import core.game.StateObservation;
import ontology.Types;
import tools.Vector2d;

import java.util.HashMap;
import java.util.List;

public class GameStateCache {

    private Vector2d avatarPosition;
    private List<Observation>[] npcs;
    private List<Observation>[] movs;
    private List<Observation>[] immovs;
    private List<Observation>[] ports;
    private List<Observation>[] res;
    private StateObservation state;
    private List<Types.ACTIONS> actions;
    private HashMap<Integer, Integer> inv;
    private GameStateCache futureCache;

    public GameStateCache(StateObservation state) {
        this.state = state;
    }

    public StateObservation getState() {
        return state;
    }

    public Vector2d getAvatarPosition() {
        if(avatarPosition == null) {
            avatarPosition = state.getAvatarPosition();
        }
        return avatarPosition;
    }

    public double getBlockSize() {
        return state.getBlockSize();
    }

    public double getGameScore() {
        return state.getGameScore();
    }

    public boolean isGameOver() {
        return state.isGameOver();
    }

    public Types.WINNER getGameWinner() {
        return state.getGameWinner();
    }

    public List<Types.ACTIONS> getAvailableActions() {
        if(actions == null) {
            actions = state.getAvailableActions();
        }
        return actions;
    }

    public List<Observation>[] getNPCPositions() {
        if(npcs == null) {
            npcs = state.getNPCPositions(getAvatarPosition());
        }
        return npcs;
    }

    public List<Observation>[] getMovablePositions() {
        if(movs == null) {
            movs = state.getMovablePositions(getAvatarPosition());
        }
        return movs;
    }

    public List<Observation>[] getImmovablePositions() {
        if(immovs == null) {
            immovs = state.getImmovablePositions(getAvatarPosition());
        }
        return immovs;
    }

    public List<Observation>[] getPortalsPositions() {
        if(ports == null) {
            ports = state.getPortalsPositions(getAvatarPosition());
        }
        return ports;
    }

    public List<Observation>[] getResourcesPositions() {
        if(res == null) {
            res = state.getResourcesPositions(getAvatarPosition());
        }
        return res;
    }

    public HashMap<Integer, Integer> getAvatarResources() {
        if(inv == null) {
            inv = state.getAvatarResources();
        }
        return inv;
    }

    public GameStateCache getFutureCache(int steps) {
        if(futureCache == null) {
            StateObservation state = getState().copy();
            for(int i = 0; i < steps; i++) {
                state.advance(Types.ACTIONS.ACTION_NIL);
            }
            futureCache = new GameStateCache(state);
        }

        return futureCache;
    }
}
