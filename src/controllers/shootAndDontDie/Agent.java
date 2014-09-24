package controllers.shootAndDontDie;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.ElapsedCpuTimer.TimerType;
import tools.Vector2d;
import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;

public class Agent extends AbstractPlayer{
    public static int NUM_ACTIONS;
    public static Types.ACTIONS[] actions;
	Random r;
    
    float lastGeneralDeadliness = 0;
        
    float[] boringPlaces;
    
    int blockSize = -1;
    int heightOfLevel = -1;
    int widthOfLevel = -1;
    
    
    final boolean VERBOSE = false;
    final boolean LOOP_VERBOSE = false;
    
    
    //Constants
    final float deadwayThreshold = 4f;
    final float deadGeneralThreshold = 20f;
    
    final float K = 1.414213562373095f;
    
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
    	
    	boringPlaces = new float[((heightOfLevel+2) * (widthOfLevel+2)) + 10];
    	for (int i = 0; i < boringPlaces.length; i++) {
    		boringPlaces[i] = 0.0001f;
		}
    	
    }
	
	public ACTIONS act(StateObservation so, ElapsedCpuTimer et) {		
		Vector2d currentPos = so.getAvatarPosition();
		double currentScore = so.getGameScore();
		if (VERBOSE) System.out.println("ACT BEGUN - score: " + currentScore + " avatar pos: " + currentPos);
		Node currentNode = new Node(so, currentPos, new LinkedList<Integer>());
		
		boringPlaces[getPositionKey(currentPos)] = (float)Math.pow(boringPlaces[getPositionKey(currentPos)], (9f/10f));
		float positionBoringness = boringPlaces[getPositionKey(currentPos)];
		
        int action = -1;
        
        ArrayDeque<Node> q = new ArrayDeque<Node>();
        q.add(currentNode);

        double[] bestVals = new double[actions.length];
        Node[] bestNodes = new Node[actions.length];
        for (int i = 0; i < actions.length; i++) {
        	bestVals[i] = 0;
		}
        float[] leastBoringActions = new float[actions.length];
        for (int i = 0; i < leastBoringActions.length; i++) {
        	leastBoringActions[i] = 0f;
		}
        
        float[] deathActions = new float[actions.length];
        for (int i = 0; i < actions.length; i++) {
        	deathActions[i] = 0f;
		}
        
        boolean[] wallActions = new boolean[actions.length];
        for (int i = 0; i < actions.length; i++) {
        	wallActions[i] = false;
		}
        
        boolean[] directDeathActions = new boolean[actions.length];
        for (int i = 0; i < actions.length; i++) {
        	directDeathActions[i] = false;
		}

        
        Vector2d pos = currentPos.copy();
        
        int lastDepth = 0;
        
        double avgTimeTaken = 0;
        double acumTimeTaken = 0;
        long remaining = et.remainingTimeMillis();
        int numIters = 0;
        int remainingLimit = 5;
        ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();
        while(remaining > 2*avgTimeTaken && remaining > remainingLimit)
        {
            if (LOOP_VERBOSE) System.out.println("START LOOP--" + elapsedTimerIteration.elapsedMillis() + " --> " + acumTimeTaken + " (" + remaining + "),  avgTimeTaken: " + avgTimeTaken);

            Node n = q.pollFirst();
            if (n == null){
            	if (VERBOSE) System.out.println("QUEUE IS EMPTY! - Adding new currentNode (empty)");
            	q.add(currentNode);
            	continue;
            }

            int d = n.list.size();
            int lastAct = d>0 ? n.list.peekLast() : -1;
            int firstAct = d>0 ? n.list.peekFirst() : -1;
            
            
            if (d > 0)  n.state.advance(actions[lastAct]);
            pos = n.state.getAvatarPosition();
            
            if (d > 0) leastBoringActions[firstAct] = boringPlaces[getPositionKey(pos)] + leastBoringActions[firstAct];
            
            
            double val = value(n.state); // + (r.nextDouble() - 0.5f); //+- 0.5 (less than a single point)
            
            boolean expandFromNode = !n.state.isGameOver();

            
            if (d == 1 && pos.equals(n.lastAvatarPos) && actions[lastAct] != ACTIONS.ACTION_USE){ //Avatar moved into wall (OR BECAUSE OF COOLDOWN) with last move. Mark wall-position as super boring
            	
            	wallActions[lastAct] = true;
//            	Vector2d badPos = changePosByAction(pos, n.list.peekLast());
//        		boringPlaces[getPositionKey(badPos)] = 1f;
//        		leastBoringActions[lastAct] = 1 + leastBoringActions[lastAct];
//        		if (LOOP_VERBOSE) System.out.println("--Found super boring tile at: " + badPos + " - actions to get there" + n.list + " (from: ) " + so.getAvatarPosition() + " -  " + n.lastAvatarPos);
            	if (LOOP_VERBOSE) System.out.println("FOUND POSSIBLE WALL FOR ACTION: " + actions[lastAct]);
            }
            

            
            if (d > 0){
            	float diff = (float) (val - currentScore);
            	float scoreDiff = (float) (diff/(float)Math.pow(K, d));
            	bestVals[firstAct] += scoreDiff > 0 ? scoreDiff : 0; // > 0 ? scoreDiff : 0;
            	bestNodes[firstAct] = n;
            	
            }
            
            if (val < -10000 && d>0){
            	float newCount = deathActions[firstAct] + (1 / (float)Math.pow(10, d));
            	deathActions[firstAct] +=  newCount;
            	expandFromNode = false;
            	if (d==1) directDeathActions[firstAct] = true;
            }
            
            
            
            if (d > (lastGeneralDeadliness > 0.1 ? 1 : 1)  && d != lastDepth){
            	q.add(currentNode);
            }
            
            int newAction = -1;
            if (d < 1){
            	//Advance with all possible actions
	            for (int i = 0; i < actions.length; i++) {	
//	            	if (wallActions[i]) continue;
	            	StateObservation stCopy = n.state.copy();            
	            	Vector2d newPos = pos.copy();
	            	Node newn = new Node(stCopy, newPos, (LinkedList<Integer>)n.list.clone());
	            	newn.addAction(i);
	            	q.add(newn);
				}
            }else if (expandFromNode){
            	//Advance with a random action
//            	if (positionBoringness < 0.0003 || lastGeneralDeadliness > 1){ 		
//            		newAction = r.nextInt(actions.length);
//            	}
//            	else
            	{
	            	//Advandce with least boring action
	            	int leastBoringAct = action;
	            	float leastBoringness = Float.MAX_VALUE;
		            for (int i = 0; i < actions.length; i++) {		            	
		            	Vector2d expectPos = changePosByAction(pos, i);
		            	float boringness = boringPlaces[getPositionKey(expectPos)] + (r.nextFloat() - 0.5f) * 0.1f;
		            	
		            	if (i == lastAct) {
		            		boringness -= 0.125f;
		            		boringness = boringness < 0 ? 0 : boringness;
		            	}
		           
		            	if (boringness < leastBoringness){
		            		leastBoringness = boringness;
		            		leastBoringAct = i;
		            	}
		            }
		            newAction = leastBoringAct;
            	}
            	
            	StateObservation stCopy = n.state;
            	Vector2d newPos = pos.copy();
            	Node newn = new Node(stCopy, newPos, (LinkedList<Integer>)n.list.clone());
            	newn.addAction(newAction);
            	q.add(newn);
            }
            
            lastDepth = d;
            
            numIters++;
            acumTimeTaken = (elapsedTimerIteration.elapsedMillis()) ;
            if (LOOP_VERBOSE) System.out.println("Node action list: " + getActionList(n.list));
            if (LOOP_VERBOSE) System.out.println("Node value: " + val);
//            if (LOOP_VERBOSE) System.out.println("Boring places: " + convertBoringList(boringPlaces));
            avgTimeTaken  = acumTimeTaken/numIters;
            remaining = et.remainingTimeMillis();
            if (LOOP_VERBOSE) System.out.println(elapsedTimerIteration.elapsedMillis() + " --> " + acumTimeTaken + " (" + remaining + "),  avgTimeTaken: " + avgTimeTaken);

        }
        
        boolean foundMove = false;
        double bestVal = -Double.MAX_VALUE;
        double worstVal = Double.MAX_VALUE;
        Node bestNode = currentNode;
        boolean noActualBest = true;
        for (int i = 0; i < actions.length; i++) {
//        	if (wallActions[i]) continue;
        	Node n = bestNodes[i];
        	double val = bestVals[i];
        	if (n != null && n.list.size() > 0){
        		foundMove = true;
        		if (val > bestVal){
	        		bestVal = val;
	        		bestNode = n;
        		}
        		if (val < worstVal){
        			worstVal = val;
        		}
        	}
		}
        
        float ratioForActualBest = 0;
//        f(b) = r
//        f(0) = 0.001
//        f(0.5) = 0.01
//        f(1) = 0.1
//        
//        0>1			
//        0.5>10
//        1>100
//        0.001f*10^(b*2)
        
        ratioForActualBest = (float) (0.001*Math.pow(10, positionBoringness*2));
        if (Math.abs(worstVal-bestVal)/Math.abs(bestVal) > ratioForActualBest) noActualBest = false; 
        

        if (foundMove){
        	action = bestNode.list.peekFirst();
        }
     
        //Check for deadly moves:
        float generalDeadliness = 0;
        float leastDeadly = Float.MAX_VALUE;
        int leastDeadlyAct = -1;
        for (int i = 0; i < actions.length; i++) {
        	generalDeadliness += deathActions[i];
        	if (deathActions[i] < leastDeadly){
				leastDeadlyAct = i;
				leastDeadly = deathActions[i];
        	}
        }
        

        
        if (noActualBest){
        	//Pick least boring move        	
        	float leastBoringness = Float.MAX_VALUE;
        	int leastBoringAct = -1;
        	
        	for (int i=0;i<leastBoringActions.length;i++) {
        		if (positionBoringness < 0.5 && wallActions[i]) continue;
        		if (directDeathActions[i]) continue;
//        		if (actions[i] == ACTIONS.ACTION_USE) continue;
				float val = leastBoringActions[i];
								
				if (val + (r.nextFloat()-0.5f)< leastBoringness){
					leastBoringness = val;
					leastBoringAct = i;
				}
			}
        	action = leastBoringAct;
        }
        
        boolean spooked = false;
        
        
        //f(b) = dt
        //f(0) = 1		dt = 10*b + 1
        //f(0.5) = 6
        //f(1) = 11
        
//        float dt = deadwayThreshold - (deadwayThreshold * (1- positionBoringness));
//        float dgt = deadGeneralThreshold - (deadGeneralThreshold * (1- positionBoringness));
        float dt = 1000*positionBoringness*positionBoringness + 1;
        float dgt = 5000*positionBoringness*positionBoringness + 5;
        dt *= numIters/300f;
        dgt *= numIters/300f;
        
        if (action == -1){
       	  action = r.nextInt(actions.length);
     	}
        
        if (generalDeadliness > dgt || deathActions[action] > dt){
        	
        	action = leastDeadlyAct;
        	spooked = true;
        }
        

     
       if (VERBOSE){
    	   System.out.println("deadwayThreshold: " + dt + " deadGeneralThreshold: " + dgt + " - pos boring: " + positionBoringness);
    	   if (noActualBest) System.out.println("NO ACTUAL BEST MOVE"); else System.out.println();
    	   if (spooked) System.out.println("SPOOOOOOOOOOOOOOOKED"); else System.out.println();
	        System.out.println("---ACTUAL ACTION CHOSEN: " + actions[action] + " , iterations: " +  numIters);
	        System.out.println("BEST ACTION: " + (bestNode.list.peekFirst() != null ? actions[bestNode.list.peekFirst()] : "ERR"));
	        System.out.println("\t\t\t" + Arrays.toString(actions));
	        System.out.println("BEST NODES: \t\t" + Arrays.toString(bestVals));

	        System.out.println("Death actions:\t\t "+Arrays.toString(deathActions));
	        System.out.println("Wall actions:\t\t "+Arrays.toString(wallActions));
	        System.out.println("Least boring actions:\t" + Arrays.toString(leastBoringActions));
	        System.out.println("Current avatar pos: " + so.getAvatarPosition());
//	        System.out.println("Boring list: " + convertBoringList(boringPlaces));
	        System.out.println("Current pos boringness: " + boringPlaces[getPositionKey(currentPos)]);        
	        System.out.println("Planned move boringness: " + boringPlaces[getPositionKey(changePosByAction(currentPos, action))]);
	        System.out.println("Expected next pos: " + changePosByAction(currentPos, action));
	        System.out.println("General deadliness: " +generalDeadliness);
       }
       lastGeneralDeadliness = generalDeadliness;
       
       if (action == -1){
     	  action = r.nextInt(actions.length);
       	}
       return actions[action];
        
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

    private int getPositionKey(Vector2d vec){
    	if (vec.x < 0 || vec.y < 0 || vec.x > blockSize*widthOfLevel || vec.y > blockSize*heightOfLevel) return widthOfLevel *heightOfLevel + 5;
		return (int)((vec.x/blockSize) + (vec.y/blockSize) * widthOfLevel);
    	
    }
    
    private Vector2d getPositionFromKey(int key){
    	Vector2d result = new Vector2d();
    	result.x = (key % widthOfLevel) * blockSize;
    	result.y = (key / widthOfLevel) * blockSize;
		return result;
    	
    }
    
    private Vector2d changePosByAction(Vector2d pos, int action){
    	Vector2d newPos = pos.copy();
    	
    	switch (actions[action]) {
		case ACTION_DOWN:
			newPos.y += blockSize;
			break;
		case ACTION_LEFT:
			newPos.x -= blockSize;
			break;
		case ACTION_RIGHT:
			newPos.x += blockSize;
			break;
		case ACTION_UP:
			newPos.y -= blockSize;
			break;
		case ACTION_USE:
			
			break;

		default:
			break;
		}
    	return newPos;
    }
    
    private HashMap<Vector2d, Float> convertBoringList(HashMap<Integer, Float> boringPlaces){
    	HashMap<Vector2d, Float> result = new HashMap<Vector2d, Float>();
    	
    	
    	for (Integer key : boringPlaces.keySet()) {
    		Float val = boringPlaces.get(key);
    		
    		
    		
    		Vector2d pos = getPositionFromKey(key);
    		
    		result.put(pos, val);
    		
		}
    	
    	
		return result;
    	
    }
    
    
    ArrayList<ACTIONS> getActionList(LinkedList<Integer> list){
    	ArrayList<ACTIONS> result = new ArrayList<ACTIONS>();
    	
    	for (Integer integer : list){
    		result.add(actions[integer]);
		}
    	return result;
    }
}
