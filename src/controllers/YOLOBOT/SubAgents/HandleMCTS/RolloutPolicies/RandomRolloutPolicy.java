package controllers.YOLOBOT.SubAgents.HandleMCTS.RolloutPolicies;

import java.util.ArrayList;
import java.util.Iterator;

import controllers.YOLOBOT.YoloState;
import controllers.YOLOBOT.Util.Wissensdatenbank.YoloKnowledge;
import ontology.Types.ACTIONS;

public class RandomRolloutPolicy extends RolloutPolicy {

	@Override
	public ArrayList<ACTIONS> possibleNextActions(YoloState state,
			ArrayList<ACTIONS> forbiddenAction, boolean forceNotEpsilon) {
		ArrayList<ACTIONS> validActions = new ArrayList<ACTIONS>(state.getAvailableActions(true));
		if(forbiddenAction != null && validActions.size() > forbiddenAction.size())
			validActions.removeAll(forbiddenAction);
		for (Iterator<ACTIONS> iterator = validActions.iterator(); iterator.hasNext();) {
			ACTIONS actions = (ACTIONS) iterator.next();

			if(YoloKnowledge.instance.actionsLeadsOutOfBattlefield(state, actions))
				iterator.remove();
		}
		
		if(validActions.isEmpty())
			return new ArrayList<ACTIONS>(state.getAvailableActions(true));
			
		return validActions;
	}

}
