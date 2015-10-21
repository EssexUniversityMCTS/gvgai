package controllers.Return42.algorithms.deterministic.randomSearch.planning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

import controllers.Return42.algorithms.deterministic.randomSearch.planning.update.PlanUpdatePolicy;
import core.game.StateObservation;

public class PlanKeeper {

	private final PriorityQueue<Plan> plans;
	private final PlanUpdatePolicy updatePolicy;
	
	public PlanKeeper( PlanUpdatePolicy updatePolicy ) {
		this.plans = new PriorityQueue<Plan>( 100, Collections.reverseOrder() );
		this.updatePolicy = updatePolicy;
	}
	
	public void addPlan( Plan plan ) {
		plans.add( plan );
	}

	public void addPlans( Collection<Plan> newPlans ) {
		plans.addAll( newPlans );
	}
	
	public boolean hasWinningPlan() {
		if (plans.isEmpty())
			return false;
		
		return plans.peek().isWinningPlan();
	}
	
	public boolean hasAnyPlan() {
		return !plans.isEmpty();
	}

	public void deletePlansThatDoNotMatch( StateObservation state ) {
		List<Plan> copy = new ArrayList<>( plans );
		updatePolicy.startCleanup();

		for( Plan plan: copy ) {
			if (updatePolicy.doesPlanMatchToGameState(plan, state)) {
				plan.pollFirstStep();
			} else {
				plans.remove( plan );
			}
		}
	}
	
	public Plan getBestPlan() {
		if (plans.isEmpty())
			throw new IllegalStateException( "Cannot get best plan, because we have no plans." );
		
		return plans.peek();
	}

	public void clear() {
		plans.clear();
	}
}
