package controllers.YOLOBOT.SubAgents.HandleMCTS;

import controllers.YOLOBOT.YoloState;
import ontology.Types.ACTIONS;

public class OLNode extends MCTNode{
	
	public static YoloState currentState;

	public OLNode(ACTIONS action) {
		super(action, currentState);
		
	}

	public OLNode(ACTIONS action, OLNode parent) {
		super(action, parent);
	}

	@Override
	protected void postSelectChild(MCTNode bestNode) {
		if(!bestNode.isLeaf)
			currentState.advance(bestNode.action);		
	}

	@Override
	protected YoloState getState() {
		return currentState;
	}


	@Override
	protected void setState(YoloState state) {
		currentState = state;
	}

	@Override
	protected void createPseudoChildren() {
		for (int i = 0; i < children.length; i++) {
			children[i] = new OLNode(validActions.get(i), this);
		}
	}
}
