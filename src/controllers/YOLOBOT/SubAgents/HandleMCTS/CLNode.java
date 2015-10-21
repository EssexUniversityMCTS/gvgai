package controllers.YOLOBOT.SubAgents.HandleMCTS;

import controllers.YOLOBOT.YoloState;
import ontology.Types.ACTIONS;

public class CLNode extends MCTNode{
	
	private YoloState state;
	
	public CLNode(ACTIONS action, YoloState state) {
		super(action, state);
		

	}

	public CLNode(ACTIONS action, CLNode parent) {
		super(action, parent);
	}

	@Override
	protected void postSelectChild(MCTNode bestNode) {
		//Nothing to do
	}

	@Override
	protected YoloState getState() {
		return state;
	}


	@Override
	protected void setState(YoloState state) {
		this.state = state;
	}

	@Override
	protected void createPseudoChildren() {
		for (int i = 0; i < children.length; i++) {
			children[i] = new CLNode(validActions.get(i), this);
		}
	}
	
}
