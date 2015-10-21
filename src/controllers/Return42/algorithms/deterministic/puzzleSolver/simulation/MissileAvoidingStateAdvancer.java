package controllers.Return42.algorithms.deterministic.puzzleSolver.simulation;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import controllers.Return42.hashing.MovableHasher;
import controllers.Return42.knowledgebase.KnowledgeBase;
import controllers.Return42.knowledgebase.observation.EffectObserver;
import controllers.Return42.util.StateObservationUtils;
import ontology.Types;
import ontology.Types.ACTIONS;
import core.game.Event;
import core.game.Observation;
import core.game.StateObservation;

public class MissileAvoidingStateAdvancer implements AStarAdvanceFunction {

	private final Set<Integer> movableTypes;
	private final EffectObserver effectObserver;
	
	public MissileAvoidingStateAdvancer( StateObservation initialState, KnowledgeBase knowledge ) {
		this.movableTypes = extractMovableTypes( initialState );
		this.effectObserver = knowledge.getEffectObserver();
	}
	
	private Set<Integer> extractMovableTypes(StateObservation initialState) {
		List<Observation> allMovables = StateObservationUtils.flatten( initialState.getMovablePositions() );
		Set<Integer> types = new HashSet<Integer>();
		
		for( Observation movable: allMovables) {
			types.add( movable.itype );
		}
		
		return types;
	}

	@Override
	public AdvanceResult advance(StateObservation oldState,	StateObservation freeCopy, ACTIONS action) {
		freeCopy.advance(action);
		StateObservation newState = freeCopy;
		
		// user did not collide with movable --> no missile
		if (!didUserCollideWithMovable( oldState, newState ) )
			return new AdvanceResult( newState, new ACTIONS[] { action } );
		
		// no movable changed it's type --> no missile
		if (!didMovableChangeItsType( oldState, newState ) )
			return new AdvanceResult( newState, new ACTIONS[] { action } );
			
		return advanceUntilNoMoreChanges( oldState, newState, action );
	}

	private boolean didUserCollideWithMovable(StateObservation oldState, StateObservation newState ) {
		Collection<Event> newEvents = extractNewEvents( oldState.getEventsHistory(), newState.getEventsHistory() );
		
		for( Event event: newEvents ) {
			boolean isKnownMovable = movableTypes.contains( event.passiveTypeId );
			boolean isNoEffect = !effectObserver.isEffectType( event.passiveTypeId );
			if (isKnownMovable && isNoEffect) {
				return true;
			}
		}
		
		return false;
	}

	private Collection<Event> extractNewEvents( TreeSet<Event> oldEventsHistory, TreeSet<Event> newEventsHistory ) {
		if (oldEventsHistory.isEmpty())
			return newEventsHistory;

		return newEventsHistory.tailSet( oldEventsHistory.last(), false );
	}

	private boolean didMovableChangeItsType(StateObservation oldState, StateObservation newState) {
		List<Observation>[] oldMovables = oldState.getMovablePositions();
		List<Observation>[] newMovables = newState.getMovablePositions();
		
		boolean sizeChanged = StateObservationUtils.count( oldMovables ) != StateObservationUtils.count( newMovables );
		if (sizeChanged)
			return false;

		Map<Integer, Integer> oldCategoryCounts = StateObservationUtils.countSpritesPerCategories( oldMovables );
		Map<Integer, Integer> newCategoryCounts = StateObservationUtils.countSpritesPerCategories( newMovables );
		
		return !oldCategoryCounts.equals( newCategoryCounts );
	}

	private AdvanceResult advanceUntilNoMoreChanges(StateObservation initialState, StateObservation newState, ACTIONS firstAction) {
		MovableHasher hasher = new MovableHasher();
		List<ACTIONS> actions = new LinkedList<Types.ACTIONS>();
		actions.add( firstAction );

		// Advance one nil action. Don't ask why....
		// It looks like the framework needs one additional 
		// step before the missile starts to move.
		newState.advance( ACTIONS.ACTION_NIL );
		actions.add( ACTIONS.ACTION_NIL );
		
		int lastHash = hasher.hash( initialState );
		int newHash = hasher.hash( newState );

		
		while( lastHash != newHash ) {
			newState.advance( ACTIONS.ACTION_NIL );
			actions.add( ACTIONS.ACTION_NIL );
			lastHash = newHash;
			newHash = hasher.hash( newState );
		}
		
		return new AdvanceResult( newState, actions.toArray( new Types.ACTIONS[0] ) );
	}
}
