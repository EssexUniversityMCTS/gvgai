package controllers.Return42.algorithms.deterministic.randomSearch.rollout.strategy;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import controllers.Return42.knowledgebase.KnowledgeBase;
import controllers.Return42.knowledgebase.observation.WalkableSpace;
import controllers.Return42.util.Util;
import ontology.Types.ACTIONS;
import tools.Vector2d;
import core.game.StateObservation;

public class GuidedRollout implements RollOutStrategy {

	private final Random random;
	private final KnowledgeBase knowledge;
	private WalkableSpace[][] walkable;
	private ACTIONS lastAction;
	
	public GuidedRollout(KnowledgeBase knowledge) {
		this.knowledge = knowledge;
		this.random = new Random();
	}

	@Override
	public void beginnRollOutPhase( StateObservation startState ) {
		int avatarType = knowledge.getGameInformation().getTypeInformation().getCurrentAvatarTypeId( startState );
		knowledge.getGameInformation().getTypeInformation().updateAll( startState );
		walkable = knowledge.getWalkableSpaceGenerator().getWalkableSpace( startState, avatarType );
	}

	@Override
	public ACTIONS getNextAction(StateObservation state) {
		
		List<ACTIONS> applicable = getApplicableActions( state );
		if (applicable.isEmpty()) {
			return state.getAvailableActions().get( random.nextInt( state.getAvailableActions().size() ) );
		}
		
		boolean shouldRepeatLastAction = applicable.contains(lastAction)
			&& lastAction != ACTIONS.ACTION_USE
			&& lastAction != ACTIONS.ACTION_NIL
			&& random.nextDouble() < 0.7;
		if ( shouldRepeatLastAction ){
			return lastAction;
		}
		
		lastAction = applicable.get( random.nextInt( applicable.size() ) );
		return lastAction;
	}

	private List<ACTIONS> getApplicableActions( StateObservation state ) {
		List<ACTIONS> applicable = new LinkedList<ACTIONS>();
		
		for( ACTIONS action: state.getAvailableActions()) {
			Vector2d nextPos = Util.translateAvatar( state, action, knowledge.getGameInformation().isAvatarOriented() );
			Vector2d gridPos = knowledge.getGameInformation().gamePositionToGridPosition(nextPos);
			
			if (walkable[(int)gridPos.x][(int)gridPos.y] != WalkableSpace.BLOCKED) {
				applicable.add( action );
			}
		}
		
		return applicable;
	}

}
