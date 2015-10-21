package controllers.Return42.algorithms.deterministic.randomSearch.planning;

import java.util.List;

import ontology.Types.ACTIONS;
import tools.Vector2d;

public class Step {

	private final ACTIONS action;
	private final List<Vector2d> npcs;
	
	public Step(ACTIONS action, List<Vector2d> npcs) {
		this.action = action;
		this.npcs = npcs;
	}

	public ACTIONS getAction() {
		return action;
	}
	
	public List<Vector2d> getNpcs() {
		return npcs;
	}
	
	
}
