package controllers.NovTea;

import java.util.ArrayList;
import ontology.Types.ACTIONS;

public class Sequence {

	public ArrayList<ACTIONS> seq;

	
	public Sequence(GraphNode bestNode) {
		seq = new ArrayList<ACTIONS>(400);
		GraphNode node = bestNode;
		while (node != null && node.parent != null){
			//System.out.println(node.actionApplied.toString());
			seq.add(node.actionApplied);
			node = node.parent;
		}
		
	}

	public ArrayList<ACTIONS> getActionsFollow() {
		int quant = seq.size();
		//System.out.println(quant);
		ArrayList<ACTIONS> actionsFollow = new ArrayList<ACTIONS>(quant);
		if (quant <= 1) return actionsFollow;
		
		//System.out.println(quant);
		
		for (int i = quant - 1; i >= 0; i--){
			//System.out.println(seq.get(i).toString());
			actionsFollow.add(seq.get(i));
		}
		
		seq.remove(quant - 1); // I remove the first action of the sequence		
		return actionsFollow;
	}

	public void print() {
		for (int i = seq.size() - 1; i >= 0; i--){
			System.out.println(seq.get(i).toString());
		}
	}

	
	
}
