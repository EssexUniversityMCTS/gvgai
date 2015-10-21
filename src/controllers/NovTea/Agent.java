package controllers.NovTea;

import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import core.game.StateObservation;
import core.player.AbstractPlayer;

public class Agent extends AbstractPlayer{

	private StateGraph stateGraph;

	
    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer){
    	stateGraph = new StateGraph();
    }
	
	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		stateGraph.setNewRoot(stateObs);
        ACTIONS action = stateGraph.simulate(elapsedTimer);
        return action;
	}

}
