package controllers.decisionTree;

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
	
	private Node root;
	private int numberOfSlots;
	
	public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer){
		String fileName = "bigFile.txt";
		double[] maxValues = null;
		ArrayList<Double>[] differentValues = null;
		ArrayList<Tuple> tuples = new ArrayList<Tuple>();
		
		tools.IO input = new tools.IO();
		String[] lines = input.readFile(fileName);
		for(int i=0; i<lines.length; i++){
			Tuple t = new Tuple(lines[i]);
			if(maxValues == null){
				maxValues = new double[t.values.size()];
				differentValues = new ArrayList[t.values.size()];
				for(int j=0; j<differentValues.length; j++){
					differentValues[j] = new ArrayList<Double>();
				}
			}
			tuples.add(t);
			for(int j=0; j<t.values.size(); j++){
				if(maxValues[j] < t.values.get(j)){
					maxValues[j] = t.values.get(j);
				}
				if(!differentValues[j].contains(t.values.get(j))){
					differentValues[j].add(t.values.get(j));
				}
			}
		}
		
		ArrayList<Integer> properties = new ArrayList<Integer>();
		for(int i=0; i<maxValues.length; i++){
			properties.add(i);
		}
		
		numberOfSlots = 10;
		root = constructTree(tuples, maxValues, differentValues, properties, null);
	}
	
	private int checkSame(ArrayList<Tuple> examples){
		int output = examples.get(0).output;
		for(Tuple t:examples){
			if(output != t.output){
				return -1;
			}
		}
		
		return output;
	}
	
	private int getMajority(ArrayList<Tuple> examples){
		int[] values = new int[5];
		for(Tuple t:examples){
			values[t.output]++;
		}
		int maxIndex = 0;
		for(int i=0; i<values.length; i++){
			if(values[maxIndex] < values[i]){
				maxIndex = i;
			}
		}
		return maxIndex;
	}
	
	private Node constructTree(ArrayList<Tuple> examples, double[] maxValues, ArrayList<Double>[] differentValues, ArrayList<Integer> properties, ArrayList<Tuple> parentExamples){
		if(examples.size() <= 0){
			return new Node(getMajority(parentExamples));
		}
		int output = checkSame(examples);
		if(output >= 0){
			return new Node(output);
		}
		if(properties.size() <= 0){
			return new Node(getMajority(examples));
		}
		
		ArrayList<Double> entropies = new ArrayList<Double>();
		int minIndex = 0;
		for(int i=0; i<properties.size(); i++){
			entropies.add(calculateEntropy(examples, maxValues, Math.min(numberOfSlots, differentValues[properties.get(i)].size()), properties.get(i)));
			if(entropies.get(i) < entropies.get(minIndex)){
				minIndex = i;
			}
		}
		
		int slots = Math.min(numberOfSlots, differentValues[minIndex].size());
		
		Node node = new Node(properties.get(minIndex), -1, slots);
		ArrayList<Tuple>[] splitList = splitTuples(examples, maxValues, slots, properties.get(minIndex));
		properties.remove(minIndex);
		for(int i=0; i<splitList.length; i++){
			node.children[i] = constructTree(splitList[i], maxValues, differentValues, properties, examples);
			node.children[i].upperBound = (i + 1) * maxValues[node.property] / slots;
		}
		
		return node;
	}
	
	private int getSlotNumber(double current, double maxValue, int slots){
		for(int slot=1; slot<slots; slot++){
			if(current <= slot * maxValue / slots){
				return slot;
			}
		}
		
		return slots - 1;
	}
	
	private ArrayList<Tuple>[] splitTuples(ArrayList<Tuple> examples, double[] maxValues, int slots, int property){
		ArrayList<Tuple>[] result = new ArrayList[slots];
		for(int i=0; i<slots; i++){
			result[i] = new ArrayList<Tuple>();
		}
		for(Tuple t:examples){
			int slotNumber = getSlotNumber(t.values.get(property), maxValues[property], slots);
			result[slotNumber].add(t);
		}
		
		return result;
	}
	
	private double calculateEntropy(ArrayList<Tuple> examples){
		double[] classes = new double[5];
		for(Tuple t : examples){
			classes[t.output]++;
		}
		
		double result = 0;
		for(int j=0; j<5; j++){
			if(classes[j] != 0){
				result += (classes[j] / examples.size()) * Math.log(examples.size() / classes[j]);
			}
		}
		
		return result;
	}
	
	private double calculateEntropy(ArrayList<Tuple> examples, double[] maxValues, int slots, int property){
		ArrayList<Tuple>[] splitList = splitTuples(examples, maxValues, slots, property);
		
		double result = 0;
		for(int i=0; i<slots; i++){
			result += (splitList[i].size() / examples.size()) * calculateEntropy(splitList[i]);
		}
		
		return result;
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
        
		return getDirection(root.decide(current));
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
