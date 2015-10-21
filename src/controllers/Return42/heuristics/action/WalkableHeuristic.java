package controllers.Return42.heuristics.action;

import java.awt.Dimension;

import controllers.Return42.knowledgebase.GameInformation;
import controllers.Return42.knowledgebase.WalkableSpaceGenerator;
import controllers.Return42.knowledgebase.observation.WalkableSpace;
import controllers.Return42.util.Util;
import ontology.Types.ACTIONS;
import tools.Vector2d;
import core.game.StateObservation;

/**
 * A heuristic that respects the walkable space. 

 * @author Frederik Buss-Joraschek
 *
 */
public class WalkableHeuristic implements ActionHeuristic {

	private GameInformation gameInformation;
	private WalkableSpaceGenerator walkableSpaceGenerator;

	public WalkableHeuristic(GameInformation gameInformation, WalkableSpaceGenerator walkableSpaceGenerator) {
		this.gameInformation = gameInformation;
		this.walkableSpaceGenerator = walkableSpaceGenerator;
	}

	/**
	 * Returns -1 if the target field is definitely not walkable. 0 in all other cases.
	 */
	@Override
	public double evaluate(StateObservation state, ACTIONS action) {	
		Vector2d movement = Util.convertActionToMovement(action);
		
		double speed = state.getAvatarSpeed();
		if (speed == 0) {
			speed = 1;
		}

		Vector2d oldPosition = state.getAvatarPosition();
		Vector2d futurePosition = new Vector2d(movement.x * speed + oldPosition.x, movement.y * speed + oldPosition.y);

		Dimension dimension = state.getWorldDimension();
		boolean isValid = futurePosition.x >= 0 && futurePosition.y >= 0 && futurePosition.x <= dimension.width
				&& futurePosition.y <= dimension.height;
		if (!isValid) {
			return -1;
		}
		
		Vector2d gridCoordinates = gameInformation.gamePositionToGridPosition(futurePosition);
		//TODO: Get real avatar id
		WalkableSpace walkable = walkableSpaceGenerator.checkWalkable(state, 1, (int) gridCoordinates.x,
				(int) gridCoordinates.y);

		Vector2d orientation = state.getAvatarOrientation();

		
		switch (walkable) {
		case BLOCKED:
			if (orientation.equals(movement)) {
				return -1;
			} else {
				return 0;
			}
		case MOVABLE:
			return 0;
		case WALKABLE:
			return 0;
		default:
			return 0;
		}
	}

}
