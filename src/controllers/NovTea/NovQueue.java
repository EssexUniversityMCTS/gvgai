package controllers.NovTea;

import java.util.LinkedList;
import java.util.Queue;

public class NovQueue {

	public Queue<GraphNode> qNov1;
	public Queue<GraphNode> qNov32;
	
	public NovQueue(){
		qNov1 = new LinkedList<GraphNode>();
		qNov32 = new LinkedList<GraphNode>();
	}

	public boolean isEmpty() {
		if (qNov1.isEmpty() && qNov32.isEmpty()) return true;
		return false;
	}

	public void add(GraphNode graphNode, int nov) {
		if (nov != 1 && Math.random() > 0.9){
			qNov1.add(graphNode);
			return;
		}
		if (nov == 1) qNov1.add(graphNode);
		else if (nov == 32) qNov32.add(graphNode);
		else{
			if (nov != 0) System.out.println("Error nov! It is not 0, 1 or 32...");
		}
	}

	public GraphNode poll() {
		if (!qNov1.isEmpty()) return qNov1.poll();
		if (!qNov32.isEmpty()) return qNov32.poll();
		return null;
	}
	
	
	
}
