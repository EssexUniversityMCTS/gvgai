package controllers.NovTea;

import java.util.ArrayList;
import java.util.HashMap;

import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Vector2d;
import core.game.Observation;
import core.game.StateObservation;

public class FASelector {

	public StateObservation initialState;
	private int tlim1 = 30;
	private int tlim2 = 20;
	private static boolean checkOther;
	
	public FASelector(StateObservation state) {
		initialState = state;
	}

	public ArrayList<Integer> getFirstPossibleActions(ElapsedCpuTimer elapsedTimer) {
		
		HashMap<Integer, Integer> mapActionDeath = this.getMapDeathActions(elapsedTimer);
		
		ArrayList<Integer> firstActions = getFirstPossibleActions(mapActionDeath);
		
		return firstActions;
	}
	
	private HashMap<Integer, Integer> getMapDeathActions(ElapsedCpuTimer elapsedTimer) {
		ArrayList<ACTIONS> actions = initialState.getAvailableActions();
		int numActions = actions.size();
		
		HashMap<Integer, Integer> mapActionDeath = new HashMap<Integer, Integer>(10);
		
		StateObservation nextState;
		int numTrials = 13;
		int i, max = 0;
		
		for (i = 0; i < numActions; i++){
			mapActionDeath.put(i, 0);
		}
		
		for (int j = 0; j < numTrials; j++){
			if (j > max) max = j;
			for (i = 0; i < numActions; i++){
				int tlim = tlim1;
				if (checkOther){
					tlim = tlim2;
					checkOther = false;
				}
				if (elapsedTimer.remainingTimeMillis() < tlim){
					//System.out.println("-----" + max);
					return mapActionDeath;
				}
				nextState = initialState.copy();
				nextState.advance(actions.get(i));
				if (nextState.isGameOver()){
					if (nextState.getGameWinner() == Types.WINNER.PLAYER_LOSES){
						mapActionDeath.put(i, mapActionDeath.get(i) + 1);
					}
				}
			}
		}
		
		//System.out.println("-----" + max);
		return mapActionDeath;
	}
	
	
	private ArrayList<Integer> getFirstPossibleActions(HashMap<Integer, Integer> mapActionDeath) {
		ArrayList<Integer> firstActions = new ArrayList<Integer>();
		int min = mapActionDeath.get(0);
		int aux;
		
		for (int key: mapActionDeath.keySet()){
			aux = mapActionDeath.get(key);
			if (aux < min) min = aux;
		}
		
		for (int key: mapActionDeath.keySet()){
			aux = mapActionDeath.get(key);
			if (aux == min){
				firstActions.add(key);
			}
		}
		
		return firstActions;
	}

	public boolean shouldCheck(int distance) {
		int factor = initialState.getBlockSize();
		Vector2d pos = initialState.getAvatarPosition();
		int pX = ((int)pos.x) / factor;
		int pY = ((int)pos.y) / factor;
		
		return this.areNPCNear(pX, pY, factor, distance);
	}	
	
	private boolean areNPCNear(int pX, int pY, int factor, int distance){
		ArrayList<Observation>[] listNPC = this.initialState.getNPCPositions();
		
		if (listNPC == null) return false;
		else{
			for (int i = 0; i < listNPC.length; i++){
				for (int j = 0; j < listNPC[i].size(); j++){
					int pXnpc = ((int)listNPC[i].get(j).position.x) / factor;
					int pYnpc = ((int)listNPC[i].get(j).position.y) / factor;
					
					if (Math.abs(pX - pXnpc) + Math.abs(pY - pYnpc) <= distance){
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean isNilSafe(ElapsedCpuTimer elapsedTimer){
		checkOther = true;
		int numTries = 10;
		if (!shouldCheck(2)) numTries = 1;
		ACTIONS actNil = this.getActionNil();
		
		StateObservation stateNext;
		
		for (int i = 0; i < numTries; i++){
			if (elapsedTimer.remainingTimeMillis() < tlim1){
				//System.out.println("-----" + i);
				break;
			}
			stateNext = initialState.copy();
			stateNext.advance(actNil);
			if (stateNext.getGameWinner() == Types.WINNER.PLAYER_LOSES) return false;
		}
		
		//System.out.println("-----" + 9);
		return true;
	}

	private ACTIONS getActionNil() {
		return Types.ACTIONS.ACTION_NIL;
	}

	public boolean isActionSafe(ACTIONS action, ElapsedCpuTimer elapsedTimer) {
		checkOther = true;
		int numTries = 10;
		if (!shouldCheck(2)) numTries = 1;
		
		StateObservation stateNext;
		
		for (int i = 0; i < numTries; i++){
			if (elapsedTimer.remainingTimeMillis() < tlim1) break;
			stateNext = initialState.copy();
			stateNext.advance(action);
			if (stateNext.getGameWinner() == Types.WINNER.PLAYER_LOSES) return false;
		}
		
		return true;
	}

}
