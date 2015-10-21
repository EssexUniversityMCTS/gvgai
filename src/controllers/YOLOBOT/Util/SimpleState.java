package controllers.YOLOBOT.Util;

import java.util.ArrayList;
import java.util.HashMap;

import controllers.YOLOBOT.YoloState;
import controllers.YOLOBOT.Util.Wissensdatenbank.YoloKnowledge;
import core.game.Observation;


/**
 *	Instantiation complexity: O( X * Y * ObsPerField)<br><br>
 *
 *	Implements fast checks of:<br>
 *<ul>
 *<li>Is itype at (x|y)</li>
 *<li>Is at least one itype of i_1, ..., i_n at (x|y)</li>
 *<li>Are all itypes i_1, ..., i_n at (x|y)</li>
 *</ul>
 * @author Tobias
 */
public class SimpleState {

	protected int[] itypeOccurenceCount;
	protected int[][] state;
	protected boolean[][] init; 
	private YoloState referenceState;
	private HashMap<Integer, Observation> identifiersSeen;
	private boolean fullInited;
	
	public SimpleState(YoloState stateObservation, boolean initComplete){
		ArrayList<Observation>[][] grid = stateObservation.getObservationGrid();
		state = new int[grid.length][grid[0].length];
		identifiersSeen = new HashMap<Integer, Observation>();
		itypeOccurenceCount = new int[YoloKnowledge.INDEX_MAX];
		fullInited = false;
		
		if(initComplete){
			//Init all at once
			init = null;
			referenceState = null;
			fullInit();
		}else{
			//Save time and dont init all, but save State reference!
			referenceState = stateObservation;
			init = new boolean[grid.length][grid[0].length];
		}
	}
	
	private SimpleState() {
	}

	public void fullInit(){
		if(fullInited)
			return;	//fullInit wurde schon ausgefuehrt! 
		fullInited = true;
		for (int x = 0; x < state.length; x++) {
			for (int y = 0; y < state[0].length; y++) {
				if(init == null || !init[x][y]){ //<-- IMPORTATNT: "init" never gets initialized if "initComplete" was true in constructor!!!
					init(x,y);
				}
			}
		}
		init = null;
	}

	private void init(int x, int y) {
		if(init != null){
			if(init[x][y])
				return;	// Was already initialized!
			else
				init[x][y] = true;
		}else{
			//init was done in constructor
			return;
		}
		for (Observation obs : referenceState.getObservationGrid()[x][y]) {
			state[x][y] = state[x][y] | 1 << YoloKnowledge.instance.itypeToIndex(obs.itype);
			if(!identifiersSeen.containsKey(obs.obsID)){
				if(itypeOccurenceCount != null)
					itypeOccurenceCount[YoloKnowledge.instance.itypeToIndex(obs.itype)]++;
				identifiersSeen.put(obs.obsID, obs);
			}
		}
	}

	public boolean isItypeAt(int iType, int x, int y){
		init(x,y);
		return ((state[x][y] >> iType) & 1) == 1;
	}
	
	public boolean hasAtLeastOneOf(int mask, int x, int y){
		init(x,y);
		return (state[x][y] & mask) != 0;
	}
	
	public boolean hasAllOf(int mask, int x, int y){
		init(x,y);
		return (state[x][y] & mask) == mask;
	}
	
	public SimpleState copy(){
		SimpleState copy = new SimpleState();
		copy.state = new int[state.length][state[0].length];
		copy.referenceState = referenceState;
		if(init == null){
			copy.init = null;
		}else{
			copy.init = new boolean[state.length][state[0].length];
			
			for (int i = 0; i < init.length; i++) {
			    System.arraycopy(init[i], 0, copy.init[i], 0, init[0].length);
			}
		}
		
		for (int i = 0; i < state.length; i++) {
		    System.arraycopy(state[i], 0, copy.state[i], 0, state[0].length);
		}
		return copy;
	}

	public int getMask(int x, int y) {
		if(x >= 0 && x < state.length && y >= 0 && y < state[0].length){
			init(x,y);
			return state[x][y];
		}else{
			return Integer.MAX_VALUE;
		}
	}
	
	public Observation getObservationWithIdentifier(int id){
		if(init != null)
			fullInit();
		return identifiersSeen.get(id);
	}
	
	public int getItypeOccurenceCount(int index) {
		return itypeOccurenceCount[index];
	}
}
