package controllers.YOLOBOT.SubAgents;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Random;

import controllers.YOLOBOT.Agent;
import controllers.YOLOBOT.OwnHistory;
import controllers.YOLOBOT.YoloState;
import controllers.YOLOBOT.Util.Heatmap;
import ontology.Types;
import ontology.Types.ACTIONS;
import ontology.Types.WINNER;
import tools.ElapsedCpuTimer;
import core.game.Observation;

public class BreitenSucheAgent extends SubAgent {

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

    
    private int aktionenAnzahl;

    private LinkedList<ACTIONS> zielAktionen;
    private LinkedList<ACTIONS> durchgefuehrteAktionen;
    private int queueLengthSum;
    private double queueAvg;
    private double hashIgnorePropability;
    
    private HashSet<String> visited;
    
    private PriorityQueue<OwnHistory> pq;
    
    private OwnHistory winSolution;

    /**
     * Public constructor with state observation and time due.
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public BreitenSucheAgent(YoloState yoloState, ElapsedCpuTimer elapsedTimer)
    {
    	super();
    	queueLengthSum = 0;
    	durchgefuehrteAktionen = new LinkedList<Types.ACTIONS>();
    	winSolution = null;
    	aktionenAnzahl = yoloState.getAvailableActions().size();
        randomGenerator = new Random();
        grid = yoloState.getObservationGrid();
        block_size = yoloState.getBlockSize();
        visited = new HashSet<String>();
        Comparator<OwnHistory> c = new Comparator<OwnHistory>() {
        	public int compare(OwnHistory o1, OwnHistory o2) {return (int) Math.signum(o1.getPriority() - o2.getPriority());}
		};
        pq = new PriorityQueue<OwnHistory>(100,c);
        
    	//LinkedList<OwnHistory> fifo = new LinkedList<OwnHistory>();
    	pq.add(new OwnHistory(yoloState));
        //fifo.add(new OwnHistory(so));
        hashIgnorePropability = 1;
    }
    
    @Override
    public void preRun(YoloState yoloState, ElapsedCpuTimer elapsedTimer) {
        doBreitensuche(elapsedTimer);
    }


    private void doBreitensuche(ElapsedCpuTimer elapsedTimer) {
        OwnHistory h, h2;
    	long avgTime = 0;        
        int nr = 0;
        
        
        
        outer: while (!pq.isEmpty()) {
        	//System.out.println(avgTime);
        	//Ist zeit uebrig?
        	if(elapsedTimer.remainingTimeMillis() < 8*avgTime)
        		return;
        	
        	//Queue
        	
        	h = pq.poll();
        	
        	for (ontology.Types.ACTIONS action : h.state.getAvailableActions()) {
        		

        		h2 = new OwnHistory(h, action);
        			

        		if (visited.contains(getHash(h2.state)) && Math.random()<hashIgnorePropability){
        			//System.out.println("Jump");
        			continue;
        		}else
        			visited.add(getHash(h2.state));
				if(!h2.state.isGameOver()){
					//fifo.add(h2);
					if(!h2.toPrune())
						pq.add(h2);
				}else if(h2.state.getGameWinner() == WINNER.PLAYER_WINS){
					winSolution = h2;
					return;
				}
			}

    		nr++;
        	avgTime = elapsedTimer.elapsedMillis() / nr;
		}
	}


	private String getHash(YoloState state) {
    	String retVal = state.getAvatarOrientation().x + "" + state.getAvatarOrientation().y +" -> "; 
		for (int i = 0; i < state.getObservationGrid().length; i++) {
			for (int j = 0; j < state.getObservationGrid()[i].length; j++) {
				retVal += i+","+j+"_";
				for (Observation obs : state.getObservationGrid()[i][j]) {
					if(obs.category != Types.TYPE_NPC)
						retVal += obs.obsID +";";
				}
			}
		}
		retVal += "Res:" + state.getAvatarResources().hashCode();
		//retVal += "Score:" + state.getGameScore();
		return retVal;
	}

    public Types.ACTIONS act(YoloState yoloState, ElapsedCpuTimer elapsedTimer) {
    	ACTIONS todo = ACTIONS.ACTION_NIL;
		int tick = yoloState.getGameTick();
    	queueLengthSum += pq.size();
    	queueAvg = (double)queueLengthSum/(tick+1);
    	hashIgnorePropability = (double)pq.size()/queueAvg;
    	if(pq.isEmpty() && winSolution == null)
    		todo = ACTIONS.ACTION_NIL;
    	else{
    		//System.out.println("Tick:" + tick + " Queuesize:" + pq.size() + "\tAvg: " + queueAvg + "\t IgnoreP: " + hashIgnorePropability);
    		if(winSolution!=null){
    			if(winSolution.actions.size()<=tick)
    				winSolution = null;
    			else{
    				todo = winSolution.actions.get(tick);
    				//System.out.println("Hab lsg in tiefe " + winSolution.actions.size());
    				}
    		}else{
    			while(pq.peek().actions.size()<=tick)
    				pq.poll();
    			todo = pq.peek().actions.get(tick);
    		}
    		
    		for (Iterator<OwnHistory> iterator = pq.iterator(); iterator.hasNext();) {
				OwnHistory ownHistory = iterator.next();
				
				if(ownHistory.actions.get(tick) != todo)
					iterator.remove();				
			}
    		
    	}
    	
    	if(pq.isEmpty() && winSolution == null){
    		OwnHistory aktuell = new OwnHistory(yoloState, durchgefuehrteAktionen);
    		pq.add(new OwnHistory(aktuell, todo));
    		//System.out.println("RESTART! + " + visited.size());
    		visited.clear();
    	}
    	
    	//TODO: weitersuchen
    	if(winSolution==null)
    		doBreitensuche(elapsedTimer);
    	
    	durchgefuehrteAktionen.add(todo);
    	//System.out.println(todo.toString());
    	//System.out.println(elapsedTimer.remainingTimeMillis());
		return todo;
    }

    /**
     * Prints the number of different types of sprites available in the "positions" array.
     * Between brackets, the number of observations of each type.
     * @param positions array with observations.
     * @param str identifier to print
     */
    private void printDebug(ArrayList<Observation>[] positions, String str)
    {
        if(positions != null){
        	if(!Agent.UPLOAD_VERSION)
				System.out.print(str + ":" + positions.length + "(");
            for (int i = 0; i < positions.length; i++) {
                System.out.print(positions[i].size() + ",");
            }
            if(!Agent.UPLOAD_VERSION)
				System.out.print("); ");
        }else if(!Agent.UPLOAD_VERSION)
			System.out.print(str + ": 0; ");
    }

    /**
     * Gets the player the control to draw something on the screen.
     * It can be used for debug purposes.
     * @param g Graphics device to draw to.
     */
    public void draw(Graphics2D g)
    {
        int half_block = (int) (block_size*0.5);
        for(int j = 0; j < grid[0].length; ++j)
        {
            for(int i = 0; i < grid.length; ++i)
            {

                String printStr = "" + Heatmap.instance.getHeatValue(i, j) + "/" + Heatmap.instance.getMaxValueApproximation();
                g.drawString(printStr + "", i*block_size+half_block,j*block_size+half_block);
            }
        }
    }


	public double EvaluateWeight(YoloState yoloState) {
		//TODO: kleverer machen: z.b. den aktuellen suchbaum beruecksichtigen!s
		if(yoloState.getNpcPositions() != null)
			return -11;
		return -8;
	}
}
