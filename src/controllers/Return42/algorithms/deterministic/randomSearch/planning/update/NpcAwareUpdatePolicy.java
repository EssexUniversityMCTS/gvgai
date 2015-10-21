package controllers.Return42.algorithms.deterministic.randomSearch.planning.update;

import java.util.List;

import controllers.Return42.algorithms.deterministic.randomSearch.planning.Plan;
import controllers.Return42.algorithms.deterministic.randomSearch.planning.Step;
import controllers.Return42.util.StateObservationUtils;
import tools.Vector2d;
import core.game.StateObservation;

public class NpcAwareUpdatePolicy implements PlanUpdatePolicy {

	@Override
	public boolean doesPlanMatchToGameState(Plan plan, StateObservation state) {
		if (plan.getNumberOfSteps() < 2)
			return false;
		
		Step nextPlannedStep = plan.getNextStep();
		if (nextPlannedStep.getAction() != state.getAvatarLastAction())
			return false;
		
		List<Vector2d> realNpcs = StateObservationUtils.getNpcPositions(state);
		List<Vector2d> plannedNpcs = nextPlannedStep.getNpcs();
		if (plannedNpcs.size() != realNpcs.size())
			return false;

		if (realNpcs.isEmpty())
			return true;
		
		return !isNpcWithWrongPositionCloseToPlayer( realNpcs, plannedNpcs, state.getAvatarPosition(), state.getBlockSize() );
	}

	private boolean isNpcWithWrongPositionCloseToPlayer( List<Vector2d> realNpcs, List<Vector2d> plannedNpcs, Vector2d avatar, int blockSize) {

		// note: plannedNpcs.size() == realNpcs.size()
		for( int i = 0; i < plannedNpcs.size(); i++ ){
			Vector2d real = realNpcs.get(i);
			Vector2d planned = plannedNpcs.get(i);
			
			if (!real.equals(planned)) {
				if (isClose(real, avatar, blockSize)) {
					return true;
				}
			}
		}
		
		return false;
	}

	private boolean isClose(Vector2d real, Vector2d avatar, int blockSize) {
		double distance = real.dist(avatar) / blockSize;
		return distance <= 2;
	}

	@Override
	public void startCleanup() {
	}

}
