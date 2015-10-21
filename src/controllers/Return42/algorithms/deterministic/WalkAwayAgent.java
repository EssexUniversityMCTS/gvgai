package controllers.Return42.algorithms.deterministic;

import java.awt.Graphics2D;
import java.lang.Thread.State;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import controllers.Return42.util.NilMoveChecker;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Vector2d;
import core.game.StateObservation;

public class WalkAwayAgent implements DeterministicAgent {

	final Vector2d startPosition;
	final Random random;
	int remainingTicks;
	
	public WalkAwayAgent( StateObservation state, int ticks ) {
		this.startPosition = state.getAvatarPosition().copy();
		this.remainingTicks = ticks;
		this.random = new Random();
	}
	
	@Override
	public ACTIONS act(StateObservation state, ElapsedCpuTimer timer) {
		remainingTicks--;
		Map<ACTIONS,Double> nilMoveDistances = calcDistancesFromStartpointForEachNilMove( state );
		return pickActionWithHighestDistance( nilMoveDistances );
	}

	private ACTIONS pickRandomNilMove( StateObservation state ) {
		List<ACTIONS> actions = new LinkedList<>(state.getAvailableActions());
		Collections.shuffle(actions);
		
		for( ACTIONS action: actions ) {
			StateObservation copy = state.copy();
			if (NilMoveChecker.advanceStateAndCheckIfIsNilMove(copy, action)) {
				return action;
			}
		}
		
		return ACTIONS.ACTION_NIL;
	}

	private Map<ACTIONS, Double> calcDistancesFromStartpointForEachNilMove(	StateObservation state) {
		Map<ACTIONS,Double> nilMoveDistances = new HashMap<>();
		
		for( ACTIONS action: state.getAvailableActions( false ) ) {
			StateObservation copy = state.copy();
			if (NilMoveChecker.advanceStateAndCheckIfIsNilMove(copy, action)) {
				double newDist = copy.getAvatarPosition().dist( startPosition );
				nilMoveDistances.put( action, newDist );
			}
		}
		
		return nilMoveDistances;
	}

	private ACTIONS pickActionWithHighestDistance( Map<ACTIONS, Double> nilMoveDistances ) {
		double highestDistance = Double.NEGATIVE_INFINITY;
		ACTIONS bestAction = ACTIONS.ACTION_NIL;
		
		for( Map.Entry<ACTIONS, Double> entry: nilMoveDistances.entrySet() ) {
			ACTIONS action = entry.getKey();
			double dist = entry.getValue();
			
			if (dist > highestDistance) {
				bestAction = action;
				highestDistance = dist;
			}
		}
		
		return bestAction;
	}
	
	@Override
	public void useConstructorExtraTime(StateObservation state,	ElapsedCpuTimer timer) {
	}

	@Override
	public boolean didFinish() {
		return remainingTicks <= 0;
	}

	@Override
	public void draw(Graphics2D g) {
	}

}
