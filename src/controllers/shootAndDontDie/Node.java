package controllers.shootAndDontDie;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.TreeSet;

import core.game.ForwardModel;
import core.game.StateObservation;
import ontology.Types.ACTIONS;
import tools.Vector2d;

public class Node {
	public StateObservation state;
	public Vector2d lastAvatarPos;
	public LinkedList<Integer> list = new LinkedList<Integer>();
	
	public Node(StateObservation state, Vector2d lastAvatarPos, LinkedList<Integer> list){
		this.state = state;
		this.lastAvatarPos = lastAvatarPos;
		this.list = list;
	}
	
	
	public void addAction(int act){
		list.add(act);
	}

}
