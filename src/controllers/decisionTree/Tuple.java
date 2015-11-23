package controllers.decisionTree;

import java.util.ArrayList;

public class Tuple implements Comparable<Tuple>{
	public ArrayList<Double> values;
	public int output;
	public double distance;
	
	public Tuple(){
		values = new ArrayList<Double>();
		output = 0;
	}
	
	public Tuple(String line){
		this();
		String[] parts = line.split(",");
		
		for(int i=0; i<parts.length - 1; i++){
			values.add(Double.parseDouble(parts[i]));
		}
		
		output = (int)Double.parseDouble(parts[parts.length - 1]);
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
