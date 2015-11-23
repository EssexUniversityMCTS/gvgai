package controllers.kNearestNeighbour;

import java.util.ArrayList;
import java.util.Collections;
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
	
	private int k;
	private String fileName;
	private double[] maxValues;
	private ArrayList<Tuple> tuples;
	
	public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer){
		k = 25;
		fileName = "bigFile.txt";
		maxValues = null;
		tuples = new ArrayList<Tuple>();
		
		tools.IO input = new tools.IO();
		String[] lines = input.readFile(fileName);
		for(int i=0; i<lines.length; i++){
			Tuple t = new Tuple(lines[i]);
			if(maxValues == null){
				maxValues = new double[t.values.size()];
			}
			tuples.add(t);
			for(int j=0; j<t.values.size(); j++){
				if(maxValues[j] < t.values.get(j)){
					maxValues[j] = t.values.get(j);
				}
			}
		}
		
		for(int i=0; i<maxValues.length; i++){
			if(maxValues[i] <= 0){
				maxValues[i] = 1;
			}
		}
		
		for(int i=0; i<tuples.size(); i++){
			tuples.get(i).normalize(maxValues);
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
		
        for(int i=0; i<tuples.size(); i++){
        	tuples.get(i).getDistance(current);
        }
        Collections.sort(tuples);
        
        HashMap<Types.ACTIONS, Integer> output = new HashMap<Types.ACTIONS, Integer>();
        output.put(Types.ACTIONS.ACTION_LEFT, 0);
        output.put(Types.ACTIONS.ACTION_RIGHT, 0);
        output.put(Types.ACTIONS.ACTION_DOWN, 0);
        output.put(Types.ACTIONS.ACTION_UP, 0);
        output.put(Types.ACTIONS.ACTION_USE, 0);
        
        for(int i=0; i<k; i++){
        	output.put(tuples.get(i).output, output.get(tuples.get(i).output) + 1);
        }
        
        Types.ACTIONS resultAction = Types.ACTIONS.ACTION_NIL;
        int maxValue = -1;
        for(Entry<Types.ACTIONS, Integer> e:output.entrySet()){
        	if(maxValue < e.getValue()){
        		resultAction = e.getKey();
        		maxValue = e.getValue();
        	}
        }
        
        //System.out.println(resultAction);
		return resultAction;
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

	class Tuple implements Comparable<Tuple>{
		public ArrayList<Double> values;
		public Types.ACTIONS output;
		public double distance;
		
		public Tuple(){
			values = new ArrayList<Double>();
			output = Types.ACTIONS.ACTION_NIL;
		}
		
		public Tuple(String line){
			this();
			String[] parts = line.split(",");
			
			for(int i=0; i<parts.length - 1; i++){
				values.add(Double.parseDouble(parts[i]));
			}
			
			output = getDirection((int)Double.parseDouble(parts[parts.length - 1]));
		}
		
		public void normalize(double[] maxValues){
			for(int i=0; i<values.size(); i++){
				values.set(i, values.get(i) / maxValues[i]) ;
			}
		}
		
		public void getDistance(Tuple t){
			double distance = 0;
			for(int i=0; i<values.size(); i++){
				distance += Math.pow((this.values.get(i) - t.values.get(i)), 2);
			}
			
			this.distance = Math.sqrt(distance);
		}

		@Override
		public int compareTo(Tuple t) {
			if(this.distance < t.distance){
				return -1;
			}
			else if(this.distance > t.distance){
				return 1;
			}
			
			return 0;
		}
	}
	
}
