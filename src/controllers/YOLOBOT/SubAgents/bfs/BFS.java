package controllers.YOLOBOT.SubAgents.bfs;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Random;

import controllers.YOLOBOT.Agent;
import controllers.YOLOBOT.YoloState;
import controllers.YOLOBOT.SubAgents.SubAgent;
import controllers.YOLOBOT.SubAgents.SubAgentStatus;
import controllers.YOLOBOT.Util.Planner.KnowledgeBasedAStar;
import controllers.YOLOBOT.Util.Wissensdatenbank.YoloKnowledge;
import ontology.Types;
import ontology.Types.ACTIONS;
import ontology.Types.WINNER;
import tools.ElapsedCpuTimer;
import core.game.Observation;

public class BFS extends SubAgent {
	private static final long MAX_MEMORY = 1600000000;

	private final boolean FORCE_RUN = false;
	
	private boolean sawNPC;
	
    /**
     * Random generator for the agent.
     */
    protected Random randomGenerator;

    /**
     * Observation grid.
     */
    protected ArrayList<Observation> grid[][];

    /**
     * block size
     */
    protected int block_size;

    private HashSet<Long> visited;
    
    private PriorityQueue<OwnHistoryLight> queue;
    
    private OwnHistoryLight targetSolution;
    private OwnHistoryLight winSolution;
    
    private int targetSolutionStep;
    private double currentBranchingFactor;
    private int expandedCount;
    private int branchedCount;
    private int lastDepthStep;
    private int lastDepth;
    private boolean fastForward;
    private int expandSteps, cancelMoves;
    private OwnHistoryLight bestScore;
    private long lastRealStateHash;
    
    private boolean foundSolutionInPrerun;

	private YoloState currentState_ForDrawing;
	private YoloState deepestState;
	
	private boolean firstSecond = true;
	public int tick;
	public int maybeEndOfTick = 2000;
	
    
    /**
     * Public constructor with state observation and time due.
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public BFS(YoloState so, ElapsedCpuTimer elapsedTimer)
    {
    	deepestState = so;
    	foundSolutionInPrerun = false;
    	lastRealStateHash = so.getHash(true);
    	expandedCount = 0;
    	branchedCount = 0;
    	lastDepthStep = 0;
    	expandSteps = 0;
    	cancelMoves = 0;
    	lastDepth = 0;
    	targetSolutionStep = 0;
    	targetSolution = null;
        randomGenerator = new Random();
        grid = so.getObservationGrid();
        block_size = so.getBlockSize();
        visited = new HashSet<Long>();
        
    	//LinkedList<OwnHistory> fifo = new LinkedList<OwnHistory>();

        Comparator<OwnHistoryLight> c = new Comparator<OwnHistoryLight>() {
        	public int compare(OwnHistoryLight o1, OwnHistoryLight o2) {return (int) Math.signum(o1.getPriority() - o2.getPriority());}
		};
		queue = new PriorityQueue<OwnHistoryLight>(2000,c);
		so.advance(ACTIONS.ACTION_NIL);
        OwnHistoryLight startState = new OwnHistoryLight(so);
        queue.add(startState);
        bestScore = startState;
        sawNPC = so.getNpcPositions() != null && so.getNpcPositions().length > 0;
        //fifo.add(new OwnHistory(so));
    }
    @Override
    public void preRun(YoloState yoloState, ElapsedCpuTimer elapsedTimer) {
        doBreitensuche(elapsedTimer);
        foundSolutionInPrerun = targetSolution != null;
        firstSecond = false;
        if(winSolution != null && !YoloKnowledge.instance.canIncreaseScoreWithoutWinning(winSolution.state)){ 
        	targetSolution = winSolution;
        }
        	
    }


    private void doBreitensuche(ElapsedCpuTimer elapsedTimer) {
        OwnHistoryLight h, h2;
        
        double avgTimeTaken = 0;
        double acumTimeTaken = 0;
        long remaining = elapsedTimer.remainingTimeMillis();
        int remainingLimit = 5;
        int numIters = 0;
        
        while (!queue.isEmpty()) {
        	ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();
            
        	//System.out.println(avgTime);
        	//Ist zeit uebrig?
        	if(!(remaining > 2*avgTimeTaken && remaining > remainingLimit))
        		break;
        	
        	//Queue
        	
        	h = queue.poll();
        	expandedCount++;
        	
        	if(h.tick>deepestState.getGameTick() && YoloKnowledge.instance.playerItypeIsWellKnown(h.state) && YoloKnowledge.instance.agentHasControlOfMovement(h.state))
        		deepestState = h.state;

        	for (ontology.Types.ACTIONS action : h.state.getAvailableActions(true)) {
        		boolean forceExpand = false;
				if(YoloKnowledge.instance.playerItypeIsWellKnown(h.state) && !YoloKnowledge.instance.agentHasControlOfMovement(h.state))
					if(!action.equals(ACTIONS.ACTION_NIL))
						continue;
					else
						forceExpand = true;
//        		boolean guessWillCancel = YoloKnowledge.instance.moveWillCancel(h.state, action); // Was tippt Knowledge?
        		Long probabilHash = YoloKnowledge.instance.getPropablyHash(h.state, action, true);
        		boolean guessWillCancel = probabilHash != null && visited.contains(probabilHash);
        		if(guessWillCancel && !forceExpand){
        			cancelMoves++;
        			continue;
        		}else{
            		//expandSteps++;
        		}
        		h2 = new OwnHistoryLight(h, action);
        		
        		if(!sawNPC && h2.state.getNpcPositions() != null && h2.state.getNpcPositions().length>0 ){
        	        sawNPC = true;
        		}
        		
        		long hash = h2.state.getHash(true);
        		
        		if(!Agent.UPLOAD_VERSION && guessWillCancel && !visited.contains(hash)){
        			//System.out.println("Is at" + h2.state.getAvatarX() + "|" + h2.state.getAvatarY());
        			System.out.println("\t\t\tKRITISCHER FEHLER DER WISSENSDATENBANK!");
        		}
        		
        		if (!forceExpand && visited.contains(hash) && h.tick != 0 && h2.timeSinceAvatarChange != 1 /* && (YoloKnowledge.instance.playerItypeIsWellKnown(h2.state) && YoloKnowledge.instance.agentHasControlOfMovement(h2.state)) */){
//        			System.out.println("Jump");
        			cancelMoves++;
        			continue;
        		}else{
            		expandSteps++;
//        			System.out.println("\t NoJump");
        		}
        		visited.add(hash);
        		branchedCount++;
        		        		
				if(!h2.state.isGameOver()){
	        		if((h2.score > bestScore.score || !YoloKnowledge.instance.agentHasControlOfMovement(bestScore.state)) && YoloKnowledge.instance.agentHasControlOfMovement(h2.state)){
						bestScore = h2;
//						System.out.println("bester Score"+ bestScore.score);
//						System.out.println("bester tick"+ bestScore.tick);
					}
					//fifo.add(h2);
					if(!h2.toPrune())
						queue.add(h2);
				}else if(h2.state.getGameWinner() == WINNER.PLAYER_WINS){	
					if (firstSecond || YoloKnowledge.instance.canIncreaseScoreWithoutWinning(h.state) && tick+h2.tick < maybeEndOfTick){
						if(winSolution == null || h2.score >= winSolution.score){
							winSolution = h2;
//							System.out.println("win Score"+ winSolution.score);
//							System.out.println("win tick"+ winSolution.tick);
						}						
					}else{
						targetSolution = h2;
						return;
					}					
				}
				if(fastForward)
					break;
				
			}

            numIters++;
            acumTimeTaken += (elapsedTimerIteration.elapsedMillis());

            avgTimeTaken  = acumTimeTaken/numIters;
            remaining = elapsedTimer.remainingTimeMillis();
		}

        currentBranchingFactor = (double)branchedCount/(double)expandedCount;
	}



	/**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(YoloState yoloState, ElapsedCpuTimer elapsedTimer) {
    	currentState_ForDrawing = yoloState;
    	
    	if(!Agent.UPLOAD_VERSION)
    		System.out.println("\t\t\t\t\t\t SIZE: " + queue.size());
    	
    	tick = yoloState.getGameTick();

//    	if(tick > 500){
//    		YoloKnowledge.instance.learnDeactivated = true;
//    	}
    	
    	//miri finde ende
    	if(maybeEndOfTick == 2000){
	    	deepestState.advance(ACTIONS.ACTION_NIL);
	    	if(deepestState.isGameOver()){
	    		maybeEndOfTick = deepestState.getGameTick()-1;
	    		//System.out.println("endTick1 :"+ maybeEndOfTick);
	    	}else{
	    		deepestState.advance(ACTIONS.ACTION_NIL);
		    	if(deepestState.isGameOver()){
		    		maybeEndOfTick = deepestState.getGameTick()-1;
		    		//System.out.println("endTick2 :"+ maybeEndOfTick);
		    	}
	    	}
    	}

    	long currentHash = yoloState.getHash(false);
    	if(tick > 2 && targetSolution == null && yoloState.getAvatarLastAction() == ACTIONS.ACTION_NIL && lastRealStateHash != currentHash){
    		//Der Zustand hat sich geaendert obwohl der agent nichts tat!!
    		if(!FORCE_RUN){
    			Status = SubAgentStatus.POSTPONED;
    			if(!Agent.UPLOAD_VERSION)
    				System.out.println("BFS will nich mehr!");
    		}
    	}
    	lastRealStateHash = currentHash;
    	
    	ACTIONS todo = ACTIONS.ACTION_NIL;    	
    	
    	
    	if(winSolution != null && tick+winSolution.tick == maybeEndOfTick  && targetSolution == null){//tick+winSolution.tick <= maybeEndOfTick && tick+winSolution.tick >= maybeEndOfTick-10
//    		System.out.println("tick"+ tick);
//    		System.out.println("winsoltick"+ winSolution.tick);
//    		System.out.println("maybeendoftick"+maybeEndOfTick);
    		if(!Agent.UPLOAD_VERSION)
    			System.out.println("BFS execute winSolution because end to close at tick " +tick );
    		targetSolution = winSolution;
    		queue.clear();
    		
    	}			
    	
    	if(targetSolution== null && tick+bestScore.tick > maybeEndOfTick*0.75){
    		if(!Agent.UPLOAD_VERSION)
    			System.out.println("BFS execute bestScore because end(" + (maybeEndOfTick*0.75)+ ") detected at tick " +tick );
    		targetSolution = bestScore;
    		queue.clear();
    	}
    	
    	
    	lastDepthStep++;
        
        //if(fastForward)
        //	System.out.println("FAAASTAA");
        
    	//weitersuchen
    	if(targetSolution == null){
    		doBreitensuche(elapsedTimer);
    		if(tick%20 == 0)
    			Runtime.getRuntime().gc();
    	}
    	if(targetSolution != null){
    		if(targetSolutionStep == 0){
				if(!Agent.UPLOAD_VERSION){
        			System.out.println("Ausfuehrung gestartet mit " + targetSolution.actions.size() + " Schritten!");
//        			YoloState testStart = yoloState.copy();
//        			for (int i = 0; i < targetSolution.actions.size(); i++) {
//        				testStart.advance(targetSolution.actions.get(i));
//					}
				}
				if(targetSolution.actions.isEmpty()){
					//New solution has no steps.
					if(!Agent.UPLOAD_VERSION)
	        			System.out.println("BFS abstellen, da nichts weiter gefunden wurde!");
        			Status = SubAgentStatus.POSTPONED;
					
				}else{
					//Remove non-valid elements from queue:

		    		queue.clear();
		    		visited.clear();
					Runtime.getRuntime().gc();
				}
    		}
    		
    		//System.out.println("Loesung gefunden! Schritt " + winStep +":" + todo.toString());
    		targetSolutionStep++;
    		
    		if(targetSolution.actions.isEmpty()){
    			queue.add(new OwnHistoryLight(targetSolution.state));
    			targetSolution = null;
    			targetSolutionStep = 0;
    		}else{
    			if(!surviveInFuture(yoloState, targetSolution)){

        			Status = SubAgentStatus.POSTPONED;
    				todo = ACTIONS.ACTION_NIL;
        		}else
        			todo = targetSolution.actions.removeFirst();
        		
        		if(targetSolution.actions.size() == 2 && mctsCouldSolve(yoloState)){
        			targetSolution = null;
        			targetSolutionStep = 0;
        			Status = SubAgentStatus.POSTPONED;
        		}
    		}
    		
    	}
    	/*else{
    		if(!foundSolutionInPrerun || winConditionIsStillValid(yoloState, elapsedTimer)){
    			
    		}else{
    			//Win condition is not valid any more
    			//	--->> No deterministic game!
    			targetSolution = null;
    			queue.clear();
        		if(!FORCE_RUN){
        			Status = SubAgentStatus.POSTPONED;
        			if(!Agent.UPLOAD_VERSION)
        				System.out.println("BFS will nich mehr!  Winstate wurde nicht erreicht!");
        		}
    		}
    		
    	}*/
    	
    	
    	long memoryUsed = (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory());
    	
    	if(!Agent.UPLOAD_VERSION)
    		System.out.println("Memory Remaining: " + (MAX_MEMORY-memoryUsed));
    	
    	//MemoryRemaining-Check:
    	if(targetSolution == null && (memoryUsed > MAX_MEMORY || queue.isEmpty())){
    		if(winSolution != null && !mctsCouldSolve(bestScore.state)){
    			
    			targetSolution = winSolution;
    			
    		}else {
    			targetSolution = bestScore;
    		}
    		
    		bestScore = targetSolution;
    		winSolution = null;
    		
    	}
    	
    	if(Status == SubAgentStatus.POSTPONED){
    		queue.clear();
    	}
		return todo;
    }


    private boolean surviveInFuture(YoloState yoloState,
			OwnHistoryLight executeSolution) {
    	
    	int advanceChecks = Math.min(2, executeSolution.actions.size());
    	YoloState checkState = yoloState.copy();
    	for (int i = 0; i < advanceChecks; i++) {
			ACTIONS action = executeSolution.actions.get(i);
			
			checkState.advance(action);
			if(checkState.isGameOver() && checkState.getGameWinner() != WINNER.PLAYER_WINS)
				return false;
			
		}
    	
    	return true;
	}
	private boolean mctsCouldSolve(YoloState state) {
    	//TODO ausprogrammieren
		return YoloKnowledge.instance.getPushableITypes().isEmpty();
	}
    
	private boolean winConditionIsStillValid(YoloState yoloState,
			ElapsedCpuTimer elapsedTimer) {
    	if(targetSolution == null || targetSolution.actions.isEmpty())
    		return false;

		if(true)
			return true;
		
        double avgTimeTaken = 0;
        double acumTimeTaken = 0;
        long remaining = elapsedTimer.remainingTimeMillis();
        int remainingLimit = 5;
        int numIters = 0;
        
        int startAction = 0;
        
        int successfullWins = 0;
        int successfullWinsMax = 20;
        
    	//check First-Game-Tick bug:
    	int x = yoloState.getAvatarX();
    	int y = yoloState.getAvatarY();
    	
    	yoloState.advance(targetSolution.actions.get(0));
    	
		boolean error = true;
		error &= yoloState.getGameTick() == 1;
		error &= yoloState.getAvatarOrientation().equals(YoloKnowledge.ORIENTATION_NULL);
		error &= yoloState.getAvatarX() == x;
		error &= yoloState.getAvatarY() == y;
		if(!error){
			startAction = 1;
		}else{
			if(!Agent.UPLOAD_VERSION)
				System.out.println("First-Game-Tick Error!");
		}
                
    	while(remaining > 2*avgTimeTaken && remaining > remainingLimit && successfullWins < successfullWinsMax){
        	ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();

        	
        	//Do win simulation:
        	YoloState currentState = yoloState.copy();
//        	currentState.setNewSeed((int)(Math.random()*10000));
        	
        	for (int i = startAction; i < targetSolution.actions.size(); i++) {
				ACTIONS todo = targetSolution.actions.get(i);
				//Do action:
				currentState.advance(todo);
				
				if(currentState.isGameOver()){
					if(currentState.getGameWinner() == WINNER.PLAYER_WINS){	
						//Erfolgreich durchsimuliert!
						break;
					}else{
						//Den Tod gefunden!
						return false;
					}
				}
			}
        	if(currentState.getGameWinner() != WINNER.PLAYER_WINS){
				//Durchsimuliert, nicht gestorben aber auch nicht gewonnen:
        		if(!Agent.UPLOAD_VERSION)
        			System.out.println("Simulation nicht beendet!");
				//return false;
			}

			successfullWins++;
			
	        numIters++;
	        acumTimeTaken += (elapsedTimerIteration.elapsedMillis());
	
	        avgTimeTaken  = acumTimeTaken/numIters;
	        remaining = elapsedTimer.remainingTimeMillis();
		}
    	
    	if(!Agent.UPLOAD_VERSION)
    		System.out.println("WinState has been recreated " + successfullWins + " times!");
    	
		return true;
	}
	/**
     * Gets the player the control to draw something on the screen.
     * It can be used for debug purposes.
     * @param g Graphics device to draw to.
     */
    public void draw(Graphics2D g)
    {
    	if(currentState_ForDrawing.getAvatar() == null)
    		return;
    	KnowledgeBasedAStar aStar;
    	try {
    		aStar = new KnowledgeBasedAStar(currentState_ForDrawing);
        	aStar.calculate(currentState_ForDrawing.getAvatarX(), currentState_ForDrawing.getAvatarY(), currentState_ForDrawing.getAvatar().itype, new int[0], false);
		} catch (java.util.ConcurrentModificationException e) {
			return;
		}
    	
    	
    	
    	if(queue.isEmpty())
    		return;
    	YoloState peek = queue.peek().state;
    	grid = peek.getObservationGrid();
//    	grid = currentState_ForDrawing.getObservationGrid();
        int half_block = (int) (block_size*0.5);
        for(int j = 0; j < grid[0].length; ++j)
        {
            for(int i = 0; i < grid.length; ++i)
            {
            	String printStr = "";
           	
           	
//            	g.setColor(Color.magenta);
//            	//Draw Current Walk-Knowledge:
//            	if(aStar.interpretedAsWall[i][j])
//            		printStr = "(" + aStar.distance[i][j] + ")";
//            	else
//            		printStr = "" + aStar.distance[i][j];
//            		
            	
            	
            	//Draw Current Idea (Queue Front):
                if(grid[i][j].size() > 0)
                {
                    Observation firstObs = grid[i][j].get(grid[i][j].size()-1); //grid[i][j].size()-1
                    //Three interesting options:
                    int print = firstObs.category; //firstObs.itype; //firstObs.obsID; firstObs.category; 
                    printStr = firstObs.category + " | " + firstObs.itype;
                }
                
                g.drawString(printStr + "", i*block_size+half_block,j*block_size+half_block);
            }
        }
    }
/*
	private void pruneHeuristical(int count) {
		for (int i = 0; i < count; i++) {
			queue.dequeue();
		}
	}*/




	@Override
	public double EvaluateWeight(YoloState yoloState) {

		if(FORCE_RUN || targetSolution != null || winSolution != null)
			return 10000;
//		if(FORCE_RUN)
//			return 10000;
		
		if(!sawNPC /*&& !YoloKnowledge.instance.getPushableITypes().isEmpty()*/)
			return 11;
		return -11;
	}
}
