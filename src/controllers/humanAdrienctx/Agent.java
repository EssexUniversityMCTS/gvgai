package controllers.humanAdrienctx;

import java.util.Random;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

public class Agent extends AbstractPlayer{
	
	private final int DECIDE_ACTION = 0;
	private final int REPEAT_MOVE = 1;
	private final int REPEAT_NIL = 2;
	
	private ACTIONS previousMove;
	private ACTIONS nextMove;
	private double moves;
	private double nilMoves;
	private int currentState;
	private AbstractPlayer automatedPlayer;
	private Random random;
	
	private double meanNIL;
	private double meanRepeat;
	private double sdNIL;
	private double sdRepeat;
	
	public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer){
		previousMove = null;
		nextMove = null;
		moves = 0;
		nilMoves = 0;
		currentState = DECIDE_ACTION;
		automatedPlayer = new controllers.adrienctx.Agent(stateObs, elapsedTimer);
		random = new Random();
		
		meanNIL = 1;
		meanRepeat = 1;
		sdNIL = 0.4;
		sdRepeat = 0.8;
	}
	
	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		ACTIONS currentAction = ACTIONS.ACTION_NIL;
		switch(currentState){
		case DECIDE_ACTION:
			currentAction = ACTIONS.ACTION_NIL;
			nextMove = automatedPlayer.act(stateObs, elapsedTimer);
			nilMoves += Math.abs(random.nextGaussian()) * sdNIL + meanNIL;
			if(nextMove != previousMove && nilMoves >= 1){
				currentState = REPEAT_NIL;
			}
			else{
				moves += Math.abs(random.nextGaussian()) * sdRepeat + meanRepeat;
				currentState = REPEAT_MOVE;
			}
			break;
		case REPEAT_MOVE:
			automatedPlayer.act(stateObs, elapsedTimer);
			currentAction = nextMove;
			if(moves >= 1){
				moves -= 1;
			}
			else{
				currentState = DECIDE_ACTION;
			}
			break;
		case REPEAT_NIL:
			automatedPlayer.act(stateObs, elapsedTimer);
			currentAction = ACTIONS.ACTION_NIL;
			if(nilMoves >= 1){
				nilMoves -= 1;
			}
			else{
				moves += Math.abs(random.nextGaussian()) * sdRepeat + meanRepeat;
				currentState = REPEAT_MOVE;
			}
			break;
		}
		
		previousMove = currentAction;
		
		return currentAction;
	}

}
