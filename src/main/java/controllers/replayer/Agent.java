package controllers.replayer;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA. User: ssamot Date: 14/11/13 Time: 21:45 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Agent extends AbstractPlayer {

	/**
	 * List of actions to execute. They must be loaded using loadActions().
	 */
	private List<Types.ACTIONS> actions;

	/**
	 * Current index of the action to be executed.
	 */
	private int actionIdx;

	/**
	 * Public constructor with state observation and time due.
	 * 
	 * @param so
	 *            state observation of the current game.
	 * @param elapsedTimer
	 *            Timer for the controller creation.
	 */
	public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer) {
		actions = new ArrayList<>();
	}

	/**
	 * Loads the action from the contents of the object received as parameter.
	 * 
	 * @param actionsToLoad
	 *            ArrayList of actions to execute.
	 */
	public void setActions(ArrayList<Types.ACTIONS> actionsToLoad) {
		actionIdx = 0;
		actions = actionsToLoad;
	}

	/**
	 * Picks an action. This function is called every game step to request an
	 * action from the player.
	 * 
	 * @param stateObs
	 *            Observation of the current state.
	 * @param elapsedTimer
	 *            Timer when the action returned is due.
	 * @return An action for the current state
	 */
	@Override
	public Types.ACTIONS act(StateObservation stateObs,
			ElapsedCpuTimer elapsedTimer) {
		Types.ACTIONS action = actions.get(actionIdx);
		actionIdx++;

		long remaining = elapsedTimer.remainingTimeMillis();
		while (1 < remaining) {
			// This allows visualization of the replay.
			remaining = elapsedTimer.remainingTimeMillis();
		}

		return action;
	}
}
