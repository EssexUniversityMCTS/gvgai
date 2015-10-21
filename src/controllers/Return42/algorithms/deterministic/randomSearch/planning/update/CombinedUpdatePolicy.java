package controllers.Return42.algorithms.deterministic.randomSearch.planning.update;

import java.util.Arrays;
import java.util.List;

import controllers.Return42.algorithms.deterministic.randomSearch.planning.Plan;
import core.game.StateObservation;

public class CombinedUpdatePolicy implements PlanUpdatePolicy {
	List<PlanUpdatePolicy> policies;
	
	public CombinedUpdatePolicy(PlanUpdatePolicy... policies) {
		this.policies = Arrays.asList(policies);
	}

	@Override
	public boolean doesPlanMatchToGameState(Plan plan, StateObservation state) {
		for(PlanUpdatePolicy policy : policies){
			if(policy.doesPlanMatchToGameState(plan, state) == false){
				return false;
			}
		}
		
		return true;
	}


	@Override
	public void startCleanup() {
		for(PlanUpdatePolicy policy : policies){
			policy.startCleanup();
		}
	}

}
