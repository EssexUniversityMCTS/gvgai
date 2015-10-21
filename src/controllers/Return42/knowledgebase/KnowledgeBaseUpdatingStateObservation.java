package controllers.Return42.knowledgebase;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import controllers.Return42.knowledgebase.observation.GameObserver;
import ontology.Types;
import tools.Vector2d;
import core.game.Event;
import core.game.Observation;
import core.game.StateObservation;

public class KnowledgeBaseUpdatingStateObservation extends StateObservation {

	private final StateObservation actualState;
	private final GameObserver observer;
	
	public KnowledgeBaseUpdatingStateObservation( StateObservation state, GameObserver observer ) {
		super(null);
		
		this.actualState = state;
		this.observer = observer;
	}

	public StateObservation copy() {
		return new KnowledgeBaseUpdatingStateObservation( actualState.copy(), observer );
	}

	public void advance(Types.ACTIONS action) {
		observer.preStepObserve(actualState);
		actualState.advance(action);
		observer.postStepObserve(actualState);
	}

	public void setNewSeed(int seed) {
		actualState.setNewSeed(seed);
	}

	public ArrayList<Types.ACTIONS> getAvailableActions() {
		return actualState.getAvailableActions(false);
	}

	public ArrayList<Types.ACTIONS> getAvailableActions(boolean includeNIL) {
		return actualState.getAvailableActions(includeNIL);
	}

	public double getGameScore() {
		return actualState.getGameScore();
	}

	public int getGameTick() {
		return actualState.getGameTick();
	}

	public Types.WINNER getGameWinner() {
		return actualState.getGameWinner();
	}

	public boolean isGameOver() {
		return actualState.isGameOver();
	}

	public Dimension getWorldDimension() {
		return actualState.getWorldDimension();
	}

	public int getBlockSize() {
		return actualState.getBlockSize();
	}

	// Methods to retrieve the state of the avatar, in the game...

	public Vector2d getAvatarPosition() {
		return actualState.getAvatarPosition();
	}

	public double getAvatarSpeed() {
		return actualState.getAvatarSpeed();
	}

	public Vector2d getAvatarOrientation() {
		return actualState.getAvatarOrientation();
	}

	public HashMap<Integer, Integer> getAvatarResources() {
		return actualState.getAvatarResources();
	}

	public Types.ACTIONS getAvatarLastAction() {
		return actualState.getAvatarLastAction();
	}

	// Methods to retrieve the state external to the avatar, in the game...

	public ArrayList<Observation>[][] getObservationGrid() {
		return actualState.getObservationGrid();
	}

	public TreeSet<Event> getEventsHistory() {
		return actualState.getEventsHistory();
	}

	public ArrayList<Observation>[] getNPCPositions() {
		return actualState.getNPCPositions();
	}

	public ArrayList<Observation>[] getNPCPositions(Vector2d reference) {
		return actualState.getNPCPositions(reference);
	}

	public ArrayList<Observation>[] getImmovablePositions() {
		return actualState.getImmovablePositions();
	}

	public ArrayList<Observation>[] getImmovablePositions(Vector2d reference) {
		return actualState.getImmovablePositions(reference);
	}

	public ArrayList<Observation>[] getMovablePositions() {
		return actualState.getMovablePositions();
	}

	public ArrayList<Observation>[] getMovablePositions(Vector2d reference) {
		return actualState.getMovablePositions(reference);
	}

	public ArrayList<Observation>[] getResourcesPositions() {
		return actualState.getResourcesPositions();
	}

	public ArrayList<Observation>[] getResourcesPositions(Vector2d reference) {
		return actualState.getResourcesPositions(reference);
	}

	public ArrayList<Observation>[] getPortalsPositions() {
		return actualState.getPortalsPositions();
	}

	public ArrayList<Observation>[] getPortalsPositions(Vector2d reference) {
		return actualState.getPortalsPositions(reference);
	}

	public ArrayList<Observation>[] getFromAvatarSpritesPositions() {
		return actualState.getFromAvatarSpritesPositions();
	}

	public ArrayList<Observation>[] getFromAvatarSpritesPositions(Vector2d reference) {
		return actualState.getFromAvatarSpritesPositions(reference);
	}

	public boolean equiv(Object o)
    {
		if (!(o instanceof StateObservation))
			return false;
		
    	return ((StateObservation)o).equiv( this.actualState );
    }
}
