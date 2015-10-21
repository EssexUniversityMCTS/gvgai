package controllers.Return42.knowledgebase;

import java.util.Arrays;
import java.util.List;

import controllers.Return42.util.Util;
import ontology.Types.ACTIONS;
import tools.Vector2d;
import core.game.StateObservation;

/**
 * Provides access to observed information about the game
 * 
 * You should call {@link #init(StateObservation)} after initialization or if
 * major game parts have changed.
 * 
 * @author Frederik Buss-Joraschek
 *
 */
public class GameInformation {

	private TypeInformation typeInformation;
	private double blockSize;
	private int width;
	private int height;
	private boolean isAvatarOriented;

	public GameInformation() {
		this.typeInformation = new TypeInformation(this);
	}

	/**
	 * Triggers a (Re)-Build of the knowledgebase
	 * 
	 * @param stateObs
	 */
	public void init(StateObservation stateObs) {
		width = stateObs.getObservationGrid().length;
		height = stateObs.getObservationGrid()[0].length;
		blockSize = stateObs.getBlockSize();
		typeInformation.updateAll(stateObs);
		
		isAvatarOriented = checkIsAvatarOriented(stateObs);
		System.out.println("Avatar oriented: " + isAvatarOriented);
	}
	
	private boolean checkIsAvatarOriented(StateObservation stateObs) {
		Vector2d originalOrientation = stateObs.getAvatarOrientation();
		Vector2d originalPosition = stateObs.getAvatarPosition();
		List<ACTIONS> actions = Arrays.asList(ACTIONS.ACTION_LEFT, ACTIONS.ACTION_UP, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_DOWN);
		for(ACTIONS testAction : actions){
			StateObservation stateCopy = stateObs.copy();
			stateCopy.advance(testAction);
			Vector2d walkDirection = Util.convertActionToMovement(testAction);
			Vector2d newPosition = stateCopy.getAvatarPosition();
			
			// Can we can walk to an field without having the right orientation?
			if (!walkDirection.equals(originalOrientation)) {
				if (!originalPosition.equals(newPosition)) {
					return false;
				}
			}
		}

		return true;
	}

	public TypeInformation getTypeInformation() {
		return typeInformation;
	}

	public Vector2d gamePositionToGridPosition(Vector2d gamePosition) {
		int x = (int) Util.clamp(gamePosition.x / blockSize, 0, width-1);
		int y = (int) Util.clamp(gamePosition.y / blockSize, 0, height-1);
		
		return new Vector2d(x,y);
	}

	public Vector2d gridPositionToGamePosition(Vector2d gamePosition) {
		return new Vector2d(gamePosition.x * blockSize, gamePosition.y * blockSize);
	}

	public double getBlockSize() {
		return blockSize;
	}

	public boolean isAvatarOriented() {
		return isAvatarOriented;
	}
}
