package controllers.hillClimber;

import java.util.ArrayList;
import java.util.Random;

import controllers.Heuristics.WinScoreHeuristic;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

public class Agent extends AbstractPlayer{
	private int maxLength = 10;
	private ArrayList<Types.ACTIONS> actions;
	private Random random;
	
	public Agent(StateObservation stateObs, ElapsedCpuTimer elpasedTimer){
		actions = stateObs.getAvailableActions();
		random = new Random();
	}
	
	private ArrayList<Types.ACTIONS> getRandomNeighbour(ArrayList<Types.ACTIONS> listActions){
		ArrayList<Types.ACTIONS> newList = (ArrayList<Types.ACTIONS>)listActions.clone();
		int randomIndex = random.nextInt(maxLength);
		newList.set(randomIndex, actions.get(random.nextInt(actions.size())));
		
		return newList;
	}
	
	private double calculateFitness(StateObservation stateObs, ArrayList<Types.ACTIONS> listActions){
		WinScoreHeuristic h = new WinScoreHeuristic(stateObs);
		StateObservation newState = stateObs.copy();
		
		for(Types.ACTIONS a:listActions){
			newState.advance(a);
		}
		
		return h.evaluateState(newState);
	}
	
	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		ArrayList<Types.ACTIONS> currentActions = new ArrayList<Types.ACTIONS>();
		for(int i=0; i<maxLength; i++){
			currentActions.add(actions.get(random.nextInt(actions.size())));
		}
		
		double worstTime = 10;
		double avgTime = 10;
		double totalTime = 0;
		double numberOfTime = 0;
		
		while(elapsedTimer.remainingTimeMillis() > avgTime && 
				elapsedTimer.remainingTimeMillis() > worstTime){
			ArrayList<Types.ACTIONS> newList = getRandomNeighbour(currentActions);
			if(calculateFitness(stateObs, currentActions) < calculateFitness(stateObs, newList)){
				currentActions = newList;
			}
		}
		
		return currentActions.get(0);
	}

}
