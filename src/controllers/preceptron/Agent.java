package controllers.preceptron;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

public class Agent extends AbstractPlayer{
	
	private String fileName;
	private double[] maxValues;
	private Precept[] precepts;
	
	public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer){
		fileName = "maxValues.txt";
		maxValues = null;
		
		tools.IO input = new tools.IO();
		String[] parts = input.readFile(fileName)[0].split(",");
		maxValues = new double[parts.length];
		for(int i=0; i<parts.length; i++){
			maxValues[i] = Double.parseDouble(parts[i]);
		}
		
		fileName = "preceptWeights.txt";
		precepts = null;
		
		String[] lines = input.readFile(fileName);
		precepts = new Precept[lines.length];
		for(int i=0; i<lines.length; i++){
			precepts[i] = new Precept(lines[i]);
		}
	}
	
	private void analyzeData(ArrayList<Observation>[] observations, Vector2d avatarPosition, Tuple data){
		HashMap<Types.ACTIONS, Integer> numbers = new HashMap<Types.ACTIONS, Integer>();
        double shortestDistance = -1;
        Types.ACTIONS shortestDirection = Types.ACTIONS.ACTION_NIL;
        
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
        	data.values.add(num.getValue() * 1.0);
        }
        data.values.add(shortestDistance);
        data.values.add(getDirection(shortestDirection) * 1.0);
	}
	
	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		Tuple current = new Tuple();
		
		if(stateObs.getAvailableActions().contains(ACTIONS.ACTION_USE)){
			current.values.add(1.0);
		}
		else
		{
			current.values.add(0.0);
		}
		if(stateObs.getAvailableActions().contains(ACTIONS.ACTION_UP) || 
				stateObs.getAvailableActions().contains(ACTIONS.ACTION_DOWN)){
			current.values.add(1.0);
		}
		else{
			current.values.add(0.0);
		}
		
		Vector2d avatarPosition = stateObs.getAvatarPosition();
        
        analyzeData(stateObs.getResourcesPositions(), avatarPosition, current);
        analyzeData(stateObs.getNPCPositions(), avatarPosition, current);
        analyzeData(stateObs.getImmovablePositions(), avatarPosition, current);
        analyzeData(stateObs.getMovablePositions(), avatarPosition, current);
        analyzeData(stateObs.getPortalsPositions(), avatarPosition, current);
        
        current.normalize(maxValues);
		
        double maxValue = Integer.MIN_VALUE;
        int index = 0;
        for(int i=0; i<precepts.length; i++){
        	double output = (precepts[i].getOutput(current.values));
        	if(output > maxValue){
        		maxValue = output;
        		index = i;
        	}
        }
        
        //System.out.println(resultAction);
		return getDirection(index);
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
	
	private Types.ACTIONS getDirection(int action){
		if(action == 0){
			return Types.ACTIONS.ACTION_LEFT;
		}
		if(action == 1){
			return Types.ACTIONS.ACTION_RIGHT;
		}
		if(action == 2){
			return Types.ACTIONS.ACTION_UP;
		}
		if(action == 3){
			return Types.ACTIONS.ACTION_DOWN;
		}
		if(action == 4){
			return Types.ACTIONS.ACTION_USE;
		}
		return Types.ACTIONS.ACTION_NIL;
	}
}
