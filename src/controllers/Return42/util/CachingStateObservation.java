package controllers.Return42.util;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import ontology.Types;
import tools.Vector2d;
import core.game.Event;
import core.game.Observation;
import core.game.StateObservation;

public class CachingStateObservation extends StateObservation {

	private StateObservation actualState;
	
	private HashMap<Integer, Integer> avatarResources;
	private ArrayList<Observation>[] npcPositions;
	private ArrayList<Observation>[] immovablePositions;
	private ArrayList<Observation>[] movablePositions;
	private ArrayList<Observation>[] resourcePositions;
	private ArrayList<Observation>[] portalPositions;
	private ArrayList<Observation>[] fromAvatarSpritesPositions;

	public CachingStateObservation(StateObservation state) {
		super(null);
		
		actualState = state;
	}

	public StateObservation copy() {
		return new CachingStateObservation(actualState.copy());
	}

	public void advance(Types.ACTIONS action) {
		actualState.advance(action);

		avatarResources = null;
		npcPositions = null;
		immovablePositions = null;
		movablePositions = null;
		resourcePositions = null;
		portalPositions = null;
		fromAvatarSpritesPositions = null;
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
		if(avatarResources == null)
			avatarResources = actualState.getAvatarResources();
		
		return avatarResources;
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
		if(npcPositions == null)
			npcPositions = actualState.getNPCPositions();
		
		return npcPositions;
	}

	public ArrayList<Observation>[] getNPCPositions(Vector2d reference) {
		return actualState.getNPCPositions(reference);
	}

	public ArrayList<Observation>[] getImmovablePositions() {
		if(immovablePositions == null)
			immovablePositions = actualState.getImmovablePositions();
		
		return immovablePositions;
	}

	public ArrayList<Observation>[] getImmovablePositions(Vector2d reference) {
		return actualState.getImmovablePositions(reference);
	}

	public ArrayList<Observation>[] getMovablePositions() {
		if(movablePositions == null)
			movablePositions = actualState.getMovablePositions();
		
		return movablePositions;
	}

	public ArrayList<Observation>[] getMovablePositions(Vector2d reference) {
		return actualState.getMovablePositions(reference);
	}

	public ArrayList<Observation>[] getResourcesPositions() {
		if(resourcePositions == null)
			resourcePositions = actualState.getResourcesPositions();
		
		return resourcePositions;
	}

	public ArrayList<Observation>[] getResourcesPositions(Vector2d reference) {
		return actualState.getResourcesPositions(reference);
	}

	public ArrayList<Observation>[] getPortalsPositions() {
		if(portalPositions == null)
			portalPositions = actualState.getPortalsPositions();
		
		return portalPositions;
	}

	public ArrayList<Observation>[] getPortalsPositions(Vector2d reference) {
		return actualState.getPortalsPositions(reference);
	}

	public ArrayList<Observation>[] getFromAvatarSpritesPositions() {
		if(fromAvatarSpritesPositions == null)
			fromAvatarSpritesPositions = actualState.getFromAvatarSpritesPositions();
		
		return fromAvatarSpritesPositions;
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
