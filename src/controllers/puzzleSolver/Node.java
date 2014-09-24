package controllers.puzzleSolver;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.TreeSet;

import core.game.ForwardModel;
import core.game.StateObservation;
import ontology.Types.ACTIONS;
import tools.Vector2d;

public class Node {
	public StateObservation state;
	public Vector2d avatarPos;
	public LinkedList<Integer> list = new LinkedList<Integer>();
	public HashSet<Moveable> moveables = new HashSet<Moveable>();
	
	public Node(StateObservation state, LinkedList<Integer> list){
		this.state = state;
		this.list = list;
	}
	
	
	public void addAction(int act){
		list.add(act);
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((avatarPos == null) ? 0 : avatarPos.hashCode());
		result = prime * result
				+ ((moveables == null) ? 0 : moveables.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (avatarPos == null) {
			if (other.avatarPos != null)
				return false;
		} else if (!avatarPos.equals(other.avatarPos))
			return false;
		if (moveables == null) {
			if (other.moveables != null)
				return false;
		} else if (!moveables.equals(other.moveables))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		String result = "Node: avatar pos: ";
		result += avatarPos.toString();
		result += ", moveables: ";
		int moveablenr = 0;
		for (Moveable m: moveables) {
			result += "" + moveablenr + ": ";
			result += m.toString();
			moveablenr++;
		}
		
		return result;
	}
	
	
//	@Override
//	public int hashCode() {
////		int hash = 17;
////	    hash = hash * 31 + field1Hash;
////	    hash = hash * 31 + field2Hash;
////	    hash = hash * 31 + field3Hash;
////	    hash = hash * 31 + field4Hash;
////	    ...
////	    return hash;
//		
////		int hash = 17;
////		hash = hash * 31 + avatarPos.x;
////		
////		int hash = (int) (avatarPos.x * 713 + avatarPos.y * 491);
////		for (Moveable m: moveables) {
////			hash += m.type*411;
////			hash += m.pos.x*21
////		}
////		return (int) (avatarPos.x * 713 + avatarPos.y * 491);
//		int hash = 17;
//		for (Moveable m: moveables) {
//			hash = hash * 31 + m.type;
//			hash = hash * 31 +  (int)m.pos.x;
//			hash = hash * 31 +  (int)m.pos.y;
//		}
//		
////		System.out.println("HASH: " + hash );
//		return hash;
//	}
//	
//	@Override
//	public boolean equals(Object obj) {
////		System.out.println("EQUALS");
//		if ((Node)obj != null){
//			return equals((Node) obj);
//		}
//		
//		return false;
//	}
//	
//	public boolean equals(Node other) {
//		System.out.println(moveables + " - " + other.moveables);
//		System.out.println("moveables.containsAll(other.moveables): " + moveables.containsAll(other.moveables));
//		if (moveables.containsAll(other.moveables)) return true;
//		return false;
//	}

	
	
}
