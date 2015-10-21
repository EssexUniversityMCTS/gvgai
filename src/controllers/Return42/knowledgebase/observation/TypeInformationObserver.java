package controllers.Return42.knowledgebase.observation;

import java.util.Observable;

import controllers.Return42.knowledgebase.GameInformation;
import controllers.Return42.knowledgebase.SpriteType;
import core.game.StateObservation;

/**
 * Observed the game updates the type information after every step
 *  
 * @author Frederik Buss-Joraschek
 *
 */
public class TypeInformationObserver extends Observable implements GameObserver {

	private GameInformation gameInformation;

	public TypeInformationObserver( GameInformation gameInformation ) {
		this.gameInformation = gameInformation;
	}

	public void preStepObserve(StateObservation stateObs) {
		gameInformation.getTypeInformation().update(SpriteType.TYPE_MOVABLE, stateObs.getMovablePositions());
		gameInformation.getTypeInformation().update(SpriteType.TYPE_NPC, stateObs.getNPCPositions());
		gameInformation.getTypeInformation().update(SpriteType.TYPE_STATIC, stateObs.getImmovablePositions());
		gameInformation.getTypeInformation().updateAvatarTypeId(stateObs);
	}

	@Override
	public void postStepObserve(StateObservation stateObs) {
	}


}


