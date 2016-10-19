package tools;

import java.util.ArrayList;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;

public class StepController{
	
	/**
	 * the selected agent to play the game
	 */
	private AbstractPlayer agent;
	/**
	 * the final state reached after playing
	 */
	private StateObservation finalState;
	/**
	 * list of all actions taken by the agent after playing the game
	 */
	private ArrayList<Types.ACTIONS> solution;
	/**
	 * the length of the time step used by the agent
	 */
	private long stepTime;
	
	/**
	 * Initialize the Step Agent
	 * @param agent		agent used to play the game
	 * @param stepTime	amount of time spend for each step
	 */
	public StepController(AbstractPlayer agent, long stepTime){
		this.stepTime = stepTime;
		this.agent = agent;
	}
	
	/**
	 * play the current game for a specific amount of time using the initialized player
	 * @param stateObs		starting observation object
	 * @param elapsedTimer	amount of time that can be spent in this function
	 */
	public void playGame(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		solution = new ArrayList<Types.ACTIONS>();
		finalState = stateObs;
		
		while(elapsedTimer.remainingTimeMillis() > stepTime && !finalState.isGameOver()){
			ElapsedCpuTimer timer = new ElapsedCpuTimer();
			timer.setMaxTimeMillis(stepTime);
			Types.ACTIONS action = agent.act(finalState.copy(), timer);
			finalState.advance(action);
			solution.add(action);
		}
	}

	/**
	 * get list of action used during playing the game
	 * @return	list of actions used during playing
	 */
	public ArrayList<ACTIONS> getSolution() {
		return solution;
	}

	/**
	 * called after playing a game and return the final game state reached
	 * @return	game state after playing the game
	 */
	public StateObservation getFinalState() {
		return finalState;
	}
}
