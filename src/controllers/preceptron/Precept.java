package controllers.preceptron;

import java.util.ArrayList;
import java.util.Random;

public class Precept {
	public static double learningRate = 0.01;
	
	public ArrayList<Double> weights;
	public double bias;
	
	public Precept(Random random, int size){
		weights = new ArrayList<Double>();
		for(int i=0; i<size; i++){
			weights.add(random.nextDouble());
		}
		
		bias = random.nextDouble();
	}
	
	public Precept(String line){
		weights = new ArrayList<Double>();
		String[] parts = line.split(",");
		for(int i=0; i<parts.length - 1; i++){
			weights.add(Double.parseDouble(parts[i]));
		}
		
		bias = Double.parseDouble(parts[parts.length - 1]);
	}
	
	public boolean updateWeights(ArrayList<Double> input, double output){
		double result = getOutput(input);
		if(Math.signum(result) == Math.signum(output)){
			return false;
		}
		
		for(int i=0; i<input.size(); i++){
			weights.set(i, weights.get(i) + learningRate * output * input.get(i));
		}
		
		bias = bias + learningRate * output;
		
		return true;
	}
	
	public double getOutput(ArrayList<Double> input){
		double result = 0;
		for(int i=0; i<input.size(); i++){
			result += input.get(i) * weights.get(i);
		}
		result += bias;
		
		return result;
	}
	
	@Override
	public String toString(){
		String result = "";
		for(int i=0; i<weights.size(); i++){
			result += weights.get(i) + ", ";
		}
		result += bias;
		
		return result;
	}
}
