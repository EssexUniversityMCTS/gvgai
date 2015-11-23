package controllers.humanCollectingData;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import core.game.Game;
import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Utils;
import tools.Vector2d;

public class Agent extends AbstractPlayer{

	private int canUse;
	private int moveVertically;
	
	public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer){
		canUse = 0;
		moveVertically = 0;
		if(stateObs.getAvailableActions().contains(ACTIONS.ACTION_USE)){
			canUse = 1;
		}
		if(stateObs.getAvailableActions().contains(ACTIONS.ACTION_UP) || 
				stateObs.getAvailableActions().contains(ACTIONS.ACTION_DOWN)){
			moveVertically = 1;
		}
		
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("dataCollected.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private ACTIONS fromAngle(double angle){
    	if(angle >= Math.PI / 4 && angle < 3 * Math.PI / 4) return ACTIONS.ACTION_DOWN;
    	if(angle >= -3 * Math.PI / 4 && angle < -Math.PI / 4) return ACTIONS.ACTION_UP;
    	if(angle >= 3 * Math.PI / 4 || angle < -3 * Math.PI / 4) return ACTIONS.ACTION_LEFT;
    	return ACTIONS.ACTION_RIGHT;
    }
	
	private int getDirection(Types.ACTIONS action){
		if(action == Types.ACTIONS.ACTION_LEFT){
			return 0;
		}
		if(action == Types.ACTIONS.ACTION_RIGHT){
			return 1;
		}
		if(action == Types.ACTIONS.ACTION_UP){
			return 2;
		}
		if(action == Types.ACTIONS.ACTION_DOWN){
			return 3;
		}
		if(action == Types.ACTIONS.ACTION_USE){
			return 4;
		}
		return -1;
	}
	
	private String analyzeData(ArrayList<Observation>[] observations, Vector2d avatarPosition){
		HashMap<Types.ACTIONS, Integer> numbers = new HashMap<Types.ACTIONS, Integer>();
        double shortestDistance = -1;
        Types.ACTIONS shortestDirection = Types.ACTIONS.ACTION_NIL;
        String dataRow = "";
        
        numbers.put(ACTIONS.ACTION_UP, 0);
        numbers.put(ACTIONS.ACTION_DOWN, 0);
        numbers.put(ACTIONS.ACTION_LEFT, 0);
        numbers.put(ACTIONS.ACTION_RIGHT, 0);
        
        if(observations != null){
	        for(int t=0; t<observations.length; t++){
	        	for(int o=0; o<observations[t].size(); o++){
	        		Types.ACTIONS direction = fromAngle(observations[t].get(o).position.subtract(avatarPosition).theta());
	        		double distance = observations[t].get(o).position.mag();
	        		if(shortestDistance == -1 || distance < shortestDistance){
	        			shortestDistance = distance;
	        			shortestDirection = direction;
	        		}
	        		numbers.put(direction, numbers.get(direction) + 1);
	        	}
	        }
        }
        
        for(Entry<ACTIONS, Integer> num:numbers.entrySet()){
        	dataRow += num.getValue() + ", ";
        }
        
        dataRow += shortestDistance + ", " + getDirection(shortestDirection);
        
        return dataRow;
	}
	
	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		Vector2d move = Utils.processMovementActionKeys(Game.ki.getMask());
        boolean useOn = Utils.processUseKey(Game.ki.getMask());

        //In the keycontroller, move has preference.
        Types.ACTIONS action = Types.ACTIONS.fromVector(move);

        if(action == Types.ACTIONS.ACTION_NIL && useOn)
            action = Types.ACTIONS.ACTION_USE;
        BufferedWriter writer = null;
        try{
        	writer = new BufferedWriter(new FileWriter("dataCollected.txt", true));
        	
	        String dataRow = "";
	        dataRow += canUse + ", " + moveVertically + ", ";
	        
	        Vector2d avatarPosition = stateObs.getAvatarPosition();
	        
	        dataRow += analyzeData(stateObs.getResourcesPositions(), avatarPosition) + ",";
	        dataRow += analyzeData(stateObs.getNPCPositions(), avatarPosition) + ", ";
	        dataRow += analyzeData(stateObs.getImmovablePositions(), avatarPosition) + ", ";
	        dataRow += analyzeData(stateObs.getMovablePositions(), avatarPosition) + ", ";
	        dataRow += analyzeData(stateObs.getPortalsPositions(), avatarPosition) + ", ";
	        
	        dataRow += getDirection(action);
	        if(action != Types.ACTIONS.ACTION_NIL){
	        	writer.write(dataRow);
	        	writer.newLine();
	        	writer.close();
	        }
        }
        catch(IOException e){
    		e.printStackTrace();
    	}
        
        return action;
	}

}
