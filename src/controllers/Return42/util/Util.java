package controllers.Return42.util;

import java.awt.Dimension;
import java.util.ArrayList;

import ontology.Types;
import ontology.Types.ACTIONS;
import tools.Vector2d;
import core.game.Observation;
import core.game.StateObservation;

public class Util {

	public static Vector2d translateAvatar(StateObservation state, ACTIONS action, boolean respectOrientation) {
		Vector2d movement = convertActionToMovement(action);
		Vector2d oldPosition = state.getAvatarPosition();

		boolean avatarLooksInMovementDirection = state.getAvatarOrientation().equals(movement);

		if (respectOrientation && !avatarLooksInMovementDirection) {
			 return oldPosition;
		}
		
		double speed = state.getAvatarSpeed();
		if (speed == 0) {
			speed = 1;
		}
		speed *= state.getBlockSize();

		Dimension dimension = state.getWorldDimension();
		Vector2d futurePosition = new Vector2d(
				clamp(movement.x * speed + oldPosition.x, 0, dimension.width), 
				clamp(movement.y * speed + oldPosition.y, 0, dimension.height));

		return futurePosition;
	}
	
	public static boolean isGridPositionValid(StateObservation state, Vector2d position) {
		ArrayList<Observation>[][] grid = state.getObservationGrid();
		return position.x >= 0 && position.y >= 0 && position.x < grid.length && position.y < grid[0].length ;
	}
	
	public static Vector2d convertActionToMovement(ACTIONS action) {

		switch (action) {
		case ACTION_DOWN:
			return Types.DOWN;
		case ACTION_UP:
			return Types.UP;
		case ACTION_RIGHT:
			return Types.RIGHT;
		case ACTION_LEFT:
			return Types.LEFT;
		default:
			return Types.NONE;
		}
	}
	
	public static double clamp(double value, double min, double max){
		return Math.min(Math.max(value, min), max);
	}

	/**
	 * Calculates the softmax
	 * 
	 * @param inputs
	 * @param temperature
	 *            For high temperatures (t -> +Infinity), all actions have
	 *            nearly the same probability and the lower the temperature, the
	 *            more expected rewards affect the probability. For a low
	 *            temperature (t -> +0), the probability of the action with
	 *            the highest expected reward tends to 1.
	 * @return
	 */
	public static double[] softMax(double[] inputs, double temperature) {
		double[] outputs = new double[inputs.length];
		double expSum = 0.0;
		for (int i = 0; i < inputs.length; i++) {
			expSum += Math.exp(inputs[i] / temperature);
		}
		for (int i = 0; i < outputs.length; i++) {
			outputs[i] = Math.exp(inputs[i] / temperature) / expSum;
		}
		return outputs;
	}
	
	public static Vector2d gamePositionToGridPosition(StateObservation stateObs, Vector2d gamePosition) {
		int blockSize = stateObs.getBlockSize();
		return new Vector2d((int) (gamePosition.x / blockSize), (int) (gamePosition.y / blockSize));
	}

	public static Vector2d gridPositionToGamePosition(StateObservation stateObs, Vector2d gamePosition) {
		int blockSize = stateObs.getBlockSize();
		return new Vector2d(gamePosition.x * blockSize, gamePosition.y * blockSize);
	}
}
