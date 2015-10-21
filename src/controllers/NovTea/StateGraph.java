package controllers.NovTea;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import core.game.StateObservation;

public class StateGraph {

	public GraphNode root;
	public int typeExpansion = 0;
	public NovQueue qFront;
	public Sequence sequence = null;
	private int limitDif = 3;
	public int errorsFollowing = 0;
	public int goodFollowing = 0;
	public boolean isSafe = true;
	private int quantNilNotSafe = 5;
	private int counterSafe = 0;
	
	private int counterThink = 0; /////////////
	private int quantThinkMax = 40; ////////////
	
	public StateGraph() { }

	public void setNewRoot(StateObservation stateObs) {
		root = new GraphNode(stateObs, null, null);
	}
	
	public ACTIONS simulate(ElapsedCpuTimer elapsedTimer) {
		if (!isSafe){
			counterSafe ++;
			if (counterSafe >= quantNilNotSafe){
				isSafe = true;
				counterSafe = 0;
			}
			typeExpansion = 0;
			qFront = null;
			return root.expandWrapper(elapsedTimer, this);
		}
		
		if (errorsFollowing - goodFollowing >= limitDif){
			typeExpansion = 0;
			qFront = null;
			System.out.println("Broken");
			return root.expandWrapper(elapsedTimer, this);
		}
		
		if (typeExpansion == 1) counterThink ++;/////////////
		else counterThink = 0;////////////////
		
		if (counterThink == quantThinkMax){//////////////////
			counterThink = 0;////////////////////
			typeExpansion = 0;//////////////////
			isSafe = false;////////////////////
		}
		
		if (typeExpansion == 0){
			qFront = null;
		}
		ACTIONS aux = root.expandWrapper(elapsedTimer, this);
		return aux;
	}

	public ArrayList<ACTIONS> getSequenceFollow() {
		ArrayList<ACTIONS> actionsFollow = sequence.getActionsFollow();
		return actionsFollow;
	}

	public void incrementErrorFollow() {
		errorsFollowing ++;
	}
	
	public void incrementGoodFollow() {
		goodFollowing ++;
	}

	public void printSequence() {
		sequence.print();
	}

	public void resetSequence() {
		sequence = null;
	}

	public int getTypeExpansion() {
		return typeExpansion;
	}

	public void setTypeExpansion(int i) {
		typeExpansion = i;
	}

	public void setSafe(boolean b) {
		isSafe = b;
	}

	public void setNewSequence(GraphNode bestNode) {
		sequence = new Sequence(bestNode);
	}

	public NovQueue getQueue() {
		return qFront;
	}

	public void setQueue(NovQueue qFront2) {
		qFront = qFront2;
	}

}
