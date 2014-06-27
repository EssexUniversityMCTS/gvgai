package controllers.sampleMCTS;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created with IntelliJ IDEA. User: ssamot Date: 14/11/13 Time: 21:45 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Agent extends AbstractPlayer {

	public static int NUM_ACTIONS;
	public static int ROLLOUT_DEPTH = 10;
	public static double K = Math.sqrt(2);
	public static Types.ACTIONS[] actions;

	/**
	 * Random generator for the agent.
	 */
	private SingleMCTSPlayer mctsPlayer;

	/**
	 * Public constructor with state observation and time due.
	 * 
	 * @param so
	 *            state observation of the current game.
	 * @param elapsedTimer
	 *            Timer for the controller creation.
	 */
	public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer) {
		// Get the actions in a static array.
		List<Types.ACTIONS> act = so.getAvailableActions();
		Agent.actions = new Types.ACTIONS[act.size()];
		for (int i = 0; i < Agent.actions.length; ++i) {
			Agent.actions[i] = act.get(i);
		}
		Agent.NUM_ACTIONS = Agent.actions.length;

		// Create the player.
		mctsPlayer = new SingleMCTSPlayer(new Random());
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

		ArrayList<Observation>[] obs = stateObs.getFromAvatarSpritesPositions();
		ArrayList<Observation>[][] grid = stateObs.getObservationGrid();

		// Set the state observation object as the new root of the tree.
		mctsPlayer.init(stateObs);

		// Determine the action using MCTS...
		int action = mctsPlayer.run(elapsedTimer);

		// ... and return it.
		return Agent.actions[action];
	}
}
