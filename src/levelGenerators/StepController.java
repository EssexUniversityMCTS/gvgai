package levelGenerators;

import java.util.ArrayList;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

public class StepController{
	
	private AbstractPlayer agent;
	private StateObservation finalState;
	private ArrayList<Types.ACTIONS> solution;
	private long stepTime;
	
	public StepController(AbstractPlayer agent){
		stepTime = 40;
		this.agent = agent;
	}
	
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

	public ArrayList<ACTIONS> getSolution() {
		return solution;
	}

	public StateObservation getFinalState() {
		return finalState;
	}
}
