package controllers.YOLOBOT.Util.Planner;

import ontology.Types.ACTIONS;

public class Zustand implements Comparable<Zustand>{

	public int lastX, lastY, lastPlayerX, lastPlayerY;
	public byte lastDirection;
	public int malus;
	public Zustand nachfolger;
	
	public Zustand(int lastX, int lastY, int lastPlayerX, int lastPlayerY,
			byte lastDirection, int malus, Zustand nachfolger) {
		super();
		this.lastX = lastX;
		this.lastY = lastY;
		this.lastPlayerX = lastPlayerX;
		this.lastPlayerY = lastPlayerY;
		this.lastDirection = lastDirection;
		this.malus = malus;
		this.nachfolger = nachfolger;
	}

	@Override
	public int compareTo(Zustand o) {
		return malus-o.malus;
	}
	
	@Override
	public String toString() {
		return lastX + "|" + lastY;
	}
	
	public int key(){
		return (((((((lastX<<7)|lastY)<<7)|lastPlayerX)<<7)|lastPlayerY)<<2)|lastDirection;
	}

	public ACTIONS getAction() {
		switch (lastDirection) {
		case KnowledgeBasedPushSlidePlanner.BOTTOM:
			return ACTIONS.ACTION_UP;
		case KnowledgeBasedPushSlidePlanner.TOP:
			return ACTIONS.ACTION_DOWN;
		case KnowledgeBasedPushSlidePlanner.LEFT:
			return ACTIONS.ACTION_RIGHT;
		default: // PushSlidePlanner.RIGHT
			return ACTIONS.ACTION_LEFT;
		}
	}
	
}
