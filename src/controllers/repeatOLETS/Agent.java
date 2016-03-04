package controllers.repeatOLETS;

import java.util.ArrayList;
import java.util.Random;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types.ACTIONS;
import ontology.Types.WINNER;
import tools.ElapsedCpuTimer;
import tools.IO;

public class Agent extends AbstractPlayer{
	
	/**
	 * probability it will react to surprise
	 */
	public static double surpriseProb = 1;
	/**
	 * probability it will react to not hit walls
	 */
	public static double nonMoveProb = 1;
	
	/**
	 * different states the player can be at
	 */
	private final int DECIDE_ACTION = 0;
	private final int REPEAT_MOVE = 1;
	private final int REPEAT_NIL = 2;
	
	/**
	 * the previous action taken by the agent
	 */
	private ACTIONS pastAction;
	/**
	 * the amount of moves it has to repeat
	 */
	private double moves;
	/**
	 * the amount of nil moves it has to repeat
	 */
	private double nilMoves;
	/**
	 * the current state of the agent
	 */
	private int currentState;
	/**
	 * the automated player used for playing
	 */
	private AbstractPlayer automatedPlayer;
	/**
	 * random object for deciding the distribution
	 */
	private Random random;
	/**
	 * the action repetition distribution
	 */
	private ArrayList<Double> actDist;
	/**
	 * the nil repetition distribution
	 */
	private ArrayList<Double> nilDist;
	
	/**
	 * Initialize the parameters and construct the automated player
	 * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
	 */
	public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer){
		moves = 0;
		nilMoves = 0;
		pastAction = ACTIONS.ACTION_NIL;
		currentState = DECIDE_ACTION;
		automatedPlayer = new controllers.olets.Agent(stateObs, elapsedTimer);
		random = new Random();
		
		actDist = new ArrayList<Double>();
        nilDist = new ArrayList<Double>();
        
        IO reader = new IO();
        String[] values = reader.readFile("action.txt")[0].split(",");
        for (String v:values){
        	actDist.add(Double.parseDouble(v.trim()));
        }
        
        values = reader.readFile("nil.txt")[0].split(",");
        for (String v:values){
        	nilDist.add(Double.parseDouble(v.trim()));
        }
	}
	
	/**
	 * get CDF distribution of the distribution sent
	 * @param dist	an array of probabilities
	 * @return		return CDF array
	 */
	private ArrayList<Double> getCDF(ArrayList<Double> dist){
		ArrayList<Double> array = new ArrayList<Double>();
		
		array.add(dist.get(0));
		for(int i=1; i<dist.size(); i++){
			array.add(array.get(i - 1) + dist.get(i));
		}
		return array;
	}
	
	/**
	 * get a random number for the input distribution
	 * @param dist	an array of probabilities
	 * @return		return a number that is sampled from this dist
	 */
	private int getNextEmpericalDist(ArrayList<Double> dist){
		ArrayList<Double> cdf = getCDF(dist);
		double value = random.nextDouble();
		for(int i=0; i<cdf.size(); i++){
			if(value < cdf.get(i)){
				return i;
			}
		}
		return dist.size();
	}
	
	/**
	 * decide the next action to be done (either repeating same action or nil or deciding new action)
	 * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
	 * @return the most suitable action
	 */
	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		ACTIONS currentAction = ACTIONS.ACTION_NIL;
		
		//respond to surprises by giving adrienctx the control again
		if(random.nextDouble() < surpriseProb){
			StateObservation tempState = stateObs.copy();
			tempState.advance(pastAction);
			if(tempState.getGameWinner() == WINNER.PLAYER_LOSES){
				moves = 0;
				nilMoves = 0;
				currentState = DECIDE_ACTION;
			}
		}
		
		//respond to walking into walls by giving adrienctx the control again
		if(random.nextDouble() < nonMoveProb && 
				pastAction != ACTIONS.ACTION_USE && pastAction != ACTIONS.ACTION_NIL){
			StateObservation tempState = stateObs.copy();
			tempState.advance(pastAction);
			if(tempState.getAvatarPosition().equals(stateObs.getAvatarPosition()) &&
				tempState.getAvatarOrientation().equals(stateObs.getAvatarOrientation())){
				moves = 0;
				nilMoves = 0;
				currentState = DECIDE_ACTION;
			}
		}
		
		//handling different states of the controller
		switch(currentState){
		//give the control to adrienctx to decide what i gonna do
		case DECIDE_ACTION:
			int temp = getNextEmpericalDist(nilDist);
			
			if(pastAction == ACTIONS.ACTION_NIL || 
					(pastAction != ACTIONS.ACTION_NIL && temp == 0)){
				currentAction = automatedPlayer.act(stateObs, elapsedTimer);
				moves = getNextEmpericalDist(actDist);
				if(moves > 1){
					currentState = REPEAT_MOVE;
				}
			}
			else{
				currentAction = ACTIONS.ACTION_NIL;
				nilMoves = temp;
				if(temp > 1){
					currentState = REPEAT_NIL;
				}
			}
			break;
		//repeat the previous move multiple time
		case REPEAT_MOVE:
			currentAction = pastAction;
			if(moves >= 1){
				moves -= 1;
			}
			else{
				currentState = DECIDE_ACTION;
			}
			break;
		//repeat the nil move between two different actions
		case REPEAT_NIL:
			currentAction = ACTIONS.ACTION_NIL;
			if(nilMoves >= 1){
				nilMoves -= 1;
			}
			else{
				currentState = DECIDE_ACTION;
			}
			break;
		}
		
		pastAction = currentAction;
		
		return currentAction;
	}

}
