package controllers.Return42.knowledgebase.observation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import controllers.Return42.util.StateObservationUtils;
import tools.Vector2d;
import core.game.Observation;
import core.game.StateObservation;

public class EffectObserver implements GameObserver {

	private final Set<Integer> effectTypes;
	
	public EffectObserver() {
		this.effectTypes = new HashSet<Integer>();
	}
	
	@Override
	public void preStepObserve(StateObservation stateObs) {
	}

	@Override
	public void postStepObserve(StateObservation stateObs) {
		List<Observation> movables = StateObservationUtils.flatten( stateObs.getMovablePositions() );
		Vector2d avatarPos = stateObs.getAvatarPosition();
		
		for( Observation movable: movables ) {
			if (avatarPos.equals( movable.position ) ) {
				effectTypes.add( movable.itype );
			}
		}
	}

	public boolean isEffectType( int itype ) {
		return effectTypes.contains( itype );
	}
	
}
