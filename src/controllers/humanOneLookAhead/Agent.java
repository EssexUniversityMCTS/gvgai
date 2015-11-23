package controllers.humanOneLookAhead;

import java.util.Random;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

public class Agent extends AbstractPlayer{
	
	private ACTIONS previousMove;
	private double moves;
	private AbstractPlayer automatedPlayer;
	private Random random;
	
	public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer){
		previousMove = null;
		moves = 0;
		automatedPlayer = new controllers.sampleonesteplookahead.Agent(stateObs, elapsedTimer);
		random = new Random();
	}
	
	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		if(previousMove == null || moves < 1){
			previousMove = automatedPlayer.act(stateObs, elapsedTimer);
			moves += Math.abs(random.nextGaussian()) * 0.4 + 1;
		}
		
		moves -= 1;
		return previousMove;
	}

}
