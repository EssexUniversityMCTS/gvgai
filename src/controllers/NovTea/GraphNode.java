package controllers.NovTea;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import core.game.StateObservation;

public class GraphNode {

    public StateObservation state;
    public GraphNode parent;
    public double game_score;
    public double value;
    public int depth;
    public ACTIONS firstAction;
    public ACTIONS actionApplied;
    public boolean win = false;
    public boolean lose = false;
	
	public GraphNode(StateObservation stateObs, GraphNode parent, ACTIONS actionApplied2) {
		this.actionApplied = actionApplied2;
		double gamma = 0.995;
		this.state = stateObs;
		this.parent = parent;
		this.game_score = stateObs.getGameScore();
		if (parent == null){
			this.depth = 0;
			this.value = 0;
			this.firstAction = null;
		}
		else{
			this.depth = parent.depth + 1;
			this.value = parent.value + Math.pow(gamma, this.depth) * (this.game_score - parent.game_score);
			if (parent.firstAction == null) this.firstAction = actionApplied;
			else this.firstAction = parent.firstAction;
		}
		
	    boolean gameOver = stateObs.isGameOver();
	    if (gameOver){
	       	Types.WINNER winState = stateObs.getGameWinner();
	        if(winState == Types.WINNER.PLAYER_LOSES){
	        	this.value -= 9000;
	        	lose = true;
	        }
	        if(winState == Types.WINNER.PLAYER_WINS){
	        	this.value += 9000;
	        	win = true;
	        }
	    }
	}
	
	public ACTIONS expandWrapper(ElapsedCpuTimer elapsedTimer, StateGraph stateGraph) {
		if (stateGraph.getTypeExpansion() == 0){
			return expandFromScratch(elapsedTimer, stateGraph);
		}
		if (stateGraph.getTypeExpansion() == 1){
			return expandSearch(elapsedTimer, stateGraph);
		}
		if (stateGraph.getTypeExpansion() == 2){
			return followSequence(elapsedTimer, stateGraph);
		}
		System.out.println("UT?");
		return null;
	}
	
	
	public ACTIONS expandFromScratch(ElapsedCpuTimer elapsedTimer, StateGraph stateGraph) {
		//System.out.println("Expanding from scratch..............");
		
		int i;
		double maxValue = -1;
		ACTIONS bestFirstAction = null;
		
		int maxD = 0;
		
		NoveltyChecker32 noveltyChecker = new NoveltyChecker32(this.state);

		NovQueue qFront = new NovQueue();
		qFront.add(this, 1);
		
		FASelector faSel = new FASelector(this.state);
		boolean firstIter;
		if (faSel.shouldCheck(2)) firstIter = true;
		else firstIter = true;
		
		double tIni, tElap, tMax = 3;
		
		do{
			tIni = System.nanoTime();
			
			GraphNode nodeExpand = qFront.poll();
			StateObservation actualState = nodeExpand.state;
			nodeExpand.state = null;
			ArrayList<ACTIONS> actions;
			
			if (firstIter){
				firstIter = false;
				ArrayList<Integer> firstActions = faSel.getFirstPossibleActions(elapsedTimer); // returns a list with the best first actions
				actions = this.getFirstActions(firstActions, actualState);
			}
			else{
				actions = actualState.getAvailableActions();
			}
			
			int num_actions = actions.size();
			ArrayList<Integer> actInd = this.obtainRandomOrder(num_actions);
			for (i = 0; i < num_actions; i++){
				ACTIONS act = actions.get(actInd.get(i));
				StateObservation stateNext = actualState.copy();
				stateNext.advance(act);
				
				int nov = noveltyChecker.getNovelty(stateNext);
				
				if (nov != 0){
					GraphNode nodeSon = new GraphNode(stateNext, nodeExpand, act);
					if (!nodeSon.lose && !nodeSon.win) qFront.add(nodeSon, nov);
					if (nodeSon.depth > maxD) maxD = nodeSon.depth;
					if (nodeSon.value >= maxValue){
						maxValue = nodeSon.value;
						bestFirstAction = nodeSon.firstAction;
					}
				}
			}
			if (maxValue > 500)	break;
			
			tElap = (System.nanoTime() - tIni) / 1e6;
			if (tElap > tMax) tMax = tElap;
			
		}while (elapsedTimer.remainingTimeMillis() > tMax && !qFront.isEmpty());
		//System.out.println(maxD);
		//System.out.println(Agent.addDepth(maxD));
		
		if (maxValue == 0){
			stateGraph.setTypeExpansion(1);
		}
		return bestFirstAction;
	}

	
	private ACTIONS expandSearch(ElapsedCpuTimer elapsedTimer, StateGraph stateGraph) {
		//System.out.println("Expanding continuously...");
		
		FASelector faSel = new FASelector(this.state);
		
		//System.out.println("Pienso:\t" + this.state.getAvatarPosition().x + "\t" + this.state.getAvatarPosition().y);
		
		NovQueue qFront = stateGraph.getQueue();
		boolean firstSearch;
		
		if (qFront == null){
			firstSearch = true;
			qFront = new NovQueue();
		}
		if (qFront.isEmpty()){
			firstSearch = true;
		}
		else firstSearch = false;
		
		boolean stayIsSafe = faSel.isNilSafe(elapsedTimer);
		if (!stayIsSafe){
			//System.out.println("Nil not safe");
			stateGraph.setSafe(false);
			stateGraph.setTypeExpansion(0);
			return expandFromScratch(elapsedTimer, stateGraph);
		}
		
		if (firstSearch){
			if (faSel.shouldCheck(3)){ // Means that NPC are near
				//System.out.println("NPC near");
				stateGraph.setSafe(false);
				stateGraph.setTypeExpansion(0);
				return expandFromScratch(elapsedTimer, stateGraph);
			}
		}
		
		int i;
		double maxValue = -1;
		
		int maxD = 0;
		
		//NoveltyCheckerAv noveltyChecker;
		NoveltyChecker32 noveltyChecker;
		if (firstSearch){
			//noveltyChecker = new NoveltyCheckerAv(this.state);
			noveltyChecker = new NoveltyChecker32(this.state);
			qFront.add(this, 1);
		}
		else{
			//noveltyChecker = new NoveltyCheckerAv();
			noveltyChecker = new NoveltyChecker32();
		}
		
		boolean firstIter = false;
		if (firstSearch){
			if (faSel.shouldCheck(2)) firstIter = true;
			else firstIter = true;
		}
		
		double tIni, tElap, tMax = 3;
		
		GraphNode bestNode = null;
		
		do{
			tIni = System.nanoTime();
			
			GraphNode nodeExpand = qFront.poll();
			StateObservation actualState = nodeExpand.state;
			nodeExpand.state = null;
			ArrayList<ACTIONS> actions;
			
			if (firstIter && firstSearch){
				firstIter = false;
				ArrayList<Integer> firstActions = faSel.getFirstPossibleActions(elapsedTimer); // returns a list with the best first actions
				actions = this.getFirstActions(firstActions, actualState);
			}
			else{
				actions = actualState.getAvailableActions();
			}
			
			int num_actions = actions.size();
			ArrayList<Integer> actInd = this.obtainRandomOrder(num_actions);
			for (i = 0; i < num_actions; i++){
				ACTIONS act = actions.get(actInd.get(i));
				StateObservation stateNext = actualState.copy();
				stateNext.advance(act);
				int nov = noveltyChecker.getNovelty(stateNext);
				
				if (nov != 0){
					GraphNode nodeSon = new GraphNode(stateNext, nodeExpand, act);
					if (!nodeSon.lose && !nodeSon.win) qFront.add(nodeSon, nov);
					if (nodeSon.depth > maxD) maxD = nodeSon.depth;
					if (nodeSon.value >= maxValue){
						bestNode = nodeSon;
						maxValue = nodeSon.value;
					}
				}
			}
			
			tElap = (System.nanoTime() - tIni) / 1e6;
			if (tElap > tMax) tMax = tElap;
			
		}while (elapsedTimer.remainingTimeMillis() > tMax && !qFront.isEmpty());
		
		//System.out.println(maxValue);
		
		if (maxValue == 0 && qFront.isEmpty()){
			stateGraph.setTypeExpansion(0);
			stateGraph.setSafe(false);
			return this.getActionNil(this.state);
		}		
		if (maxValue == 0 && !qFront.isEmpty()){
			stateGraph.setTypeExpansion(1);
			stateGraph.setQueue(qFront);
			return this.getActionNil(this.state);
		}
		else{
			stateGraph.setTypeExpansion(2);
			stateGraph.resetSequence();
			stateGraph.setNewSequence(bestNode);
			return this.getActionNil(this.state);
		}
	}
	
	
	private ACTIONS followSequence(ElapsedCpuTimer elapsedTimer, StateGraph stateGraph) {
		//System.out.println("Following...");
		
		ArrayList<ACTIONS> sequence = stateGraph.getSequenceFollow(); // This already removes the first action in the var in StateGraph
		if (sequence.size() <= 1){
			//System.out.println("Seq <= 1");
			stateGraph.incrementGoodFollow();
			stateGraph.setTypeExpansion(0);
			stateGraph.setSafe(false);
			stateGraph.resetSequence();
			//System.out.println("Salida 1");
			return expandFromScratch(elapsedTimer, stateGraph);
		}
		
		ACTIONS firsActSeq = sequence.get(0);
		
		FASelector faSel = new FASelector(this.state);
		boolean firstIsSafe = faSel.isActionSafe(sequence.get(0), elapsedTimer);
		if (!firstIsSafe){
			stateGraph.setTypeExpansion(0);
			stateGraph.setSafe(false);
			stateGraph.incrementErrorFollow();
			//System.out.println("Incr error");
			//System.out.println("Salida 2");
			return expandFromScratch(elapsedTimer, stateGraph);
		}
		
		NoveltyChecker32 noveltyChecker = new NoveltyChecker32(this.state);
		noveltyChecker.reset();
		
		double valAcum = 0;
		
		int quant = sequence.size();
		StateObservation stateSeq = this.state.copy();
		//System.out.println(quant);
		for (int i = 0; i < quant - 1; i++){
			if (elapsedTimer.remainingTimeMillis() < 3){
				stateGraph.setTypeExpansion(2);
				//System.out.println("Salida 3");
				return firsActSeq;
			}
			double pScore = stateSeq.getGameScore();
			stateSeq.advance(sequence.get(i));
			//System.out.println(sequence.get(i).toString());
			valAcum += (stateSeq.getGameScore() - pScore);
			if (stateSeq.getGameWinner() == Types.WINNER.PLAYER_LOSES){
				stateGraph.incrementErrorFollow();
				stateGraph.setTypeExpansion(0);
				stateGraph.setSafe(false);
				//System.out.println("Salida 4");
				return expandFromScratch(elapsedTimer, stateGraph);
			}
			if (stateSeq.getGameWinner() == Types.WINNER.PLAYER_WINS){
				stateGraph.setTypeExpansion(2);
				//System.out.println("Salida 5");
				return firsActSeq;
			}
			if (!noveltyChecker.shouldExpand(stateSeq)){
				//StateGraph.typeExpansion = 0;
				//System.out.println("Not expand follow **********MMMMM****MMMMM*************** \t" + i + "\t" + quant);
				stateGraph.setTypeExpansion(0);
				stateGraph.setSafe(false);
				//StateGraph.printInitialState();
				//printAvatarPos();
				//StateGraph.printSequence();
				//System.out.println("--------------");
				//this.printSequence(sequence);
				//ArrayList<Integer> list = null; list.get(0);
				return expandFromScratch(elapsedTimer, stateGraph);
			}
		}
		
		GraphNode nodeEndSeq = new GraphNode(stateSeq, null, null);
		nodeEndSeq.value = valAcum;
		
		double maxValue = valAcum;

		Queue<GraphNode> qFront = new LinkedList<GraphNode>();
		qFront.add(nodeEndSeq);
		
		double tIni, tElap, tMax = 3;
		
		do{
			tIni = System.nanoTime();
			
			GraphNode nodeExpand = qFront.poll();
			StateObservation actualState = nodeExpand.state;
			nodeExpand.state = null;
			ArrayList<ACTIONS> actions;
			
			actions = actualState.getAvailableActions();
			
			int num_actions = actions.size();
			ArrayList<Integer> actInd = this.obtainRandomOrder(num_actions);
			for (int i = 0; i < num_actions; i++){
				ACTIONS act = actions.get(actInd.get(i));
				StateObservation stateNext = actualState.copy();
				stateNext.advance(act);
				if (noveltyChecker.shouldExpand(stateNext)){
					GraphNode nodeSon = new GraphNode(stateNext, nodeExpand, act);
					//System.out.println("-------\t" + nodeSon.value);
					//if (nodeSon.win) System.out.println("WIN");
					if (!nodeSon.lose && !nodeSon.win) qFront.add(nodeSon);
					if (nodeSon.value >= maxValue){
						maxValue = nodeSon.value;
					}
				}
			}
			
			tElap = (System.nanoTime() - tIni) / 1e6;
			if (tElap > tMax) tMax = tElap;
			
		}while (elapsedTimer.remainingTimeMillis() > tMax && !qFront.isEmpty());
		
		if (maxValue <= 0){
			//System.out.println("Estoy donde no debo! " + maxValue);
			stateGraph.setSafe(false);
			stateGraph.setTypeExpansion(0);
		}
		
		//System.out.println("Salida 6 " + maxValue);
		stateGraph.setTypeExpansion(2);
		return firsActSeq;
	}
	
	
	private ACTIONS getActionNil(StateObservation state) {
		return Types.ACTIONS.ACTION_NIL;
	}


	/*private void printFirstActions(ArrayList<ACTIONS> actions) {
		for (int i = 0; i < actions.size(); i++){
			System.out.print(actions.get(i) + "\t");
		}
		System.out.println(" ");
	}*/

	private ArrayList<ACTIONS> getFirstActions(ArrayList<Integer> firstActions, StateObservation state) {
		ArrayList<ACTIONS> availableActions = state.getAvailableActions();
		ArrayList<ACTIONS> firstRecActions = new ArrayList<ACTIONS>();
		int numRecActions = firstActions.size();
		
		for (int i = 0; i < numRecActions; i++){
			firstRecActions.add(availableActions.get(firstActions.get(i)));
		}
		
		return firstRecActions;
	}

	private ArrayList<Integer> obtainRandomOrder(int num) {
		ArrayList<Integer> sequence = new ArrayList<Integer>();
		
		for (int i = 0; i < num; i++) sequence.add(i);
		Collections.shuffle(sequence);
		return sequence;
	}

}
