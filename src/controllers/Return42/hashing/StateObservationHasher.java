package controllers.Return42.hashing;

import java.util.List;

import controllers.Return42.knowledgebase.KnowledgeBase;
import controllers.Return42.knowledgebase.observation.EffectObserver;
import controllers.Return42.util.StateObservationUtils;
import ontology.Types;
import core.game.Observation;
import core.game.StateObservation;

public class StateObservationHasher {
	
	public static int hashState( StateObservation state, KnowledgeBase knowledge ) {
        // warning, this is really really ugly!
        // we write our state into a string and use the String's hashcode function afterwards.
        // Feel free to improve this code, but be aware of possible collisions.

        StringBuilder sb = new StringBuilder();

        hashScore( state, sb );
        hashAvatar( state, sb );
        hashImmovables(state, sb);
        hashFromAvator( state, sb );
        hashRessources( state, sb );
        hashPortals( state, sb );
    	hashMovables(state, sb, knowledge);

        return sb.toString().hashCode();
    }

	private static void hashScore(StateObservation state, StringBuilder sb) {
		sb.append( state.getGameScore() );
	}

	private static void hashAvatar(StateObservation state, StringBuilder sb) {
        // in games where we can use items, we have to consider the orientation of the avatar
        if (state.getAvailableActions().contains(Types.ACTIONS.ACTION_USE )) {
            sb.append( state.getAvatarOrientation().x );
            sb.append( state.getAvatarOrientation().y );
        }

        sb.append( state.getAvatarPosition().x );
        sb.append( state.getAvatarPosition().y );
        sb.append( StateObservationUtils.getAvatarType( state ) );
    }

    private static void hashMovables(StateObservation state, StringBuilder sb, KnowledgeBase knowledge) {
        List<Observation>[] observations = state.getMovablePositions();

        if (observations == null)
            return;

        EffectObserver effects = knowledge.getEffectObserver();
        for( List<Observation> observationsOfSameType: observations ) {
            for( Observation anObservation: observationsOfSameType ) {
                if (!effects.isEffectType(anObservation.itype)) {
                	hashObservation( anObservation, sb );
                }
            }
        }
    }

    private static void hashObservation(Observation anObservation, StringBuilder sb) {
        sb.append( anObservation.itype );
        //sb.append( anObservation.obsID );
        sb.append( anObservation.position.x );
        sb.append( anObservation.position.y );
    }

    private static void hashImmovables(StateObservation state, StringBuilder sb) {
        List<Observation>[] observations = state.getImmovablePositions();

        if  (observations == null)
            return;

        for( List<Observation> immovablesOfSameType: observations ) {
            hashImmovablesOfSameType(immovablesOfSameType, sb);
        }
    }

    private static void hashImmovablesOfSameType(List<Observation> immovablesOfSameType, StringBuilder sb) {
        sb.append( immovablesOfSameType.size() );
        int sumOfIds = 0;

        for( Observation observation: immovablesOfSameType ) {
            sumOfIds += observation.obsID;
        }

        sb.append( sumOfIds );
    }

    private static void hashFromAvator(StateObservation state, StringBuilder sb) {
        List<Observation>[] observations = state.getFromAvatarSpritesPositions();

        if  (observations == null)
            return;

        for( List<Observation> sameType: observations ) {
            for( Observation anObservation: sameType ) {
                hashObservation( anObservation, sb );
            }
        }
	}
    
	private static void hashRessources(StateObservation state, StringBuilder sb) {
        List<Observation>[] observations = state.getResourcesPositions();

        if  (observations == null)
            return;

        for( List<Observation> immovablesOfSameType: observations ) {
            hashResourcesOfSameType(immovablesOfSameType, sb);
        }
    }

    private static void hashResourcesOfSameType(List<Observation> resourcesOfSameType, StringBuilder sb) {
        sb.append( resourcesOfSameType.size() );
        int sumOfIds = 0;

        for( Observation observation: resourcesOfSameType ) {
            sumOfIds += observation.obsID;
        }

        sb.append( sumOfIds );
    }

	private static void hashPortals(StateObservation state, StringBuilder sb) {
        List<Observation>[] observations = state.getResourcesPositions();

        if  (observations == null)
            return;

        for( List<Observation> portalssOfSameType: observations ) {
            hashPortalsOfSameType(portalssOfSameType, sb);
        }
    }

    private static void hashPortalsOfSameType(List<Observation> portalsOfSameType, StringBuilder sb) {
        sb.append( portalsOfSameType.size() );
        int sumOfIds = 0;

        for( Observation observation: portalsOfSameType ) {
            sumOfIds += observation.obsID;
        }

        sb.append( sumOfIds );
    }
}
