package controllers.Return42.algorithms.deterministic.randomSearch.planning.update;

import java.util.Observable;
import java.util.Observer;

import controllers.Return42.algorithms.deterministic.randomSearch.planning.Plan;
import controllers.Return42.knowledgebase.ScoreHeightMapGenerator;
import core.game.StateObservation;

public class HeightMapChangedUpdatePolicy implements PlanUpdatePolicy, Observer {
	boolean changeSinceLastCall = false;
	boolean cleanupThisRound = false;
	
	public HeightMapChangedUpdatePolicy(ScoreHeightMapGenerator heightMapGenerator) {
		heightMapGenerator.addObserver(this);
	}

	@Override
	public boolean doesPlanMatchToGameState(Plan plan, StateObservation state) {
		if(plan.isWinningPlan()){
			// We the plan wins we don't care that its heuristic value is invalid
			return true;
		}
		
		if (plan.getNumberOfSteps() < 2)
			return false;
		
		if (cleanupThisRound) {
			return false;
		}
		return true;
	}

	@Override
	public void update(Observable o, Object arg) {
		changeSinceLastCall = true;		
	}

	@Override
	public void startCleanup() {
		if(cleanupThisRound){
			cleanupThisRound = false;
		}
		
		if(changeSinceLastCall){
			changeSinceLastCall = false;
			cleanupThisRound = true;
		}
		
	}

}
