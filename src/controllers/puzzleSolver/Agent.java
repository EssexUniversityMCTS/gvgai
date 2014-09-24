package controllers.puzzleSolver;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Vector2d;
import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;

public class Agent extends AbstractPlayer{

	
	ACTIONS[] actions;
	Random r;
    int blockSize = -1;
    int heightOfLevel = -1;
    int widthOfLevel = -1;
    
    boolean foundSolution = false;
    LinkedList<Integer> solution = new LinkedList<Integer>();
    int solutionIndex = 0;
    
	
    final boolean VERBOSE = false;
    final boolean LOOP_VERBOSE = false;
    
    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer)
    {
        ArrayList<Types.ACTIONS> act = so.getAvailableActions();
        actions = new Types.ACTIONS[act.size()];
        for(int i = 0; i < actions.length; ++i)
        {
            actions[i] = act.get(i);
        }
    	r = new Random();
    	
    	blockSize = so.getBlockSize();
    	heightOfLevel = (int) (so.getWorldDimension().height / so.getBlockSize());
    	widthOfLevel = (int) (so.getWorldDimension().width / so.getBlockSize());
    }
	
	public ACTIONS act(StateObservation so, ElapsedCpuTimer ect) {
		if (foundSolution && solutionIndex< solution.size()-1){
			solutionIndex++;
			return actions[solution.get(solutionIndex)];
		}
				
        ArrayDeque<Node> q = new ArrayDeque<Node>();
        
		
        HashSet<Node> visitedNodes = new HashSet<Node>();
        
        
        Node currentNode = new Node(so, new LinkedList<Integer>());
        q.add(currentNode);

        int numIters = 0;
        while (true){
        	if (q.isEmpty()){
        		System.out.println("QUEUE EMPTY!!!");
        	}
        	
        	Node n = q.pollFirst();
        	int d = n.list.size();
            int lastAct = d>0 ? n.list.peekLast() : -1;
            int firstAct = d>0 ? n.list.peekFirst() : -1;
                        
            if (d > 0) n.state.advance(actions[lastAct]);

            n.moveables = getMoveables(n.state);
        	n.avatarPos = n.state.getAvatarPosition();
        	double val = value(n.state);
        	//Node has now finished initializing
        	
        	
        	if (LOOP_VERBOSE){
        		System.out.println("---New node initialized!--- (iteration: " + numIters + " - q length: + " + q.size() +")");
        	
        		System.out.println("Node action list: " + getActionList(n.list));
        		System.out.println("Node value: " + val);
	            System.out.println("Avatar pos: " + n.avatarPos);
	            System.out.println("Moveables:");
	            for (Moveable moveable: n.moveables) {
						System.out.println(moveable);
				}
        	}
        	
        	if (visitedNodes.contains(n)){
        		if (LOOP_VERBOSE){
	        		Node existingNode = null;
	        		for (Node node : visitedNodes) if (node.equals(n)) existingNode = node;
	        		System.out.println("VISITED ALREADY NODES CONTAIN NODE!!");
	        		System.out.println("orig actions: "+getActionList(existingNode.list));
	        		System.out.println("new actions: "+getActionList(n.list));
        		}
        		continue;
        	}


        	
        	

        	if (n.state.isGameOver() ){
        		if (val > 0){
	        		System.out.println("FOUND SOLUTION!");
	        		solution = n.list;
	        		foundSolution = true;
	        		System.out.println("Solution: " + getActionList(solution));
//	        		System.out.println("Visted nodes:  " + visitedNodes);
	        		return actions[solution.get(solutionIndex)];
        		}else{
        			continue;
        		}
        	}

    		for (int i = 0; i < actions.length; i++) {
				StateObservation soCopy = n.state.copy();
        		Node n_new = new Node(soCopy, (LinkedList<Integer>)n.list.clone());
        		n_new.addAction(i);
        		q.add(n_new);
    		}


        	visitedNodes.add(n);
        	numIters++;
        }
  	}

	HashSet<Moveable> getMoveables(StateObservation so){
		HashSet<Moveable> result = new HashSet<Moveable>();
		if (so.getMovablePositions() == null) return result;
		
		for (ArrayList<Observation> arrayList : so.getMovablePositions()) {
			for (Observation observation : arrayList) {
				result.add(new Moveable(observation.position, observation.itype));
			}
		}
		
//		for (ArrayList<Observation> arrayList : so.getImmovablePositions()) {
//			for (Observation observation : arrayList) {
//				result.add(new Moveable(observation.position, observation.itype));
//			}
//		}
		return result;
	}
	
	
    ArrayList<ACTIONS> getActionList(LinkedList<Integer> list){
    	ArrayList<ACTIONS> result = new ArrayList<ACTIONS>();
    	
    	for (Integer integer : list){
    		result.add(actions[integer]);
		}
    	return result;
    }
    
    private double value(StateObservation a_gameState) {

        boolean gameOver = a_gameState.isGameOver();
        Types.WINNER win = a_gameState.getGameWinner();
        double rawScore = a_gameState.getGameScore();

        if(gameOver && win == Types.WINNER.PLAYER_LOSES)
            return -100000;

        if(gameOver && win == Types.WINNER.PLAYER_WINS){
            return 1000 + rawScore; //rawScore + a_gameState.getGameTick() > 1000 ? 100000 : 0;  //WINNING IS ONLY GOOD LATE IN GAME
        }
            
        return rawScore;
    }
}
