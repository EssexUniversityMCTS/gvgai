package controllers.preceptron;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class Train {
	public static void main(String[] args){
		String fileName = "bigFile.txt";
		double[] maxValues = null;
		ArrayList<Tuple> tuples = new ArrayList<Tuple>();
		
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
		
		Precept[] p = new Precept[5];
		Random random = new Random();
		
		for(int i=0; i<p.length; i++){
			p[i]=new Precept(random, tuples.get(0).values.size());
		}
		
		int error = 0;
		do
		{
			error = 0;
			for(int i=0; i<tuples.size(); i++){
				for(int j=0; j<p.length; j++){
					error += p[j].updateWeights(tuples.get(i).values, tuples.get(i).output[j])? 1: 0;
				}
			}
			System.out.println(error);
		}while(error > 1500);
		
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("maxValues.txt"));
			
			for(int i=0; i<maxValues.length - 1; i++){
				writer.write(Double.toString(maxValues[i]));
				writer.write(",");
			}
			writer.write(Double.toString(maxValues[maxValues.length - 1]));
			
			writer.close();
			
			writer = new BufferedWriter(new FileWriter("preceptWeights.txt"));
			
			for(int i=0; i<p.length; i++){
				writer.write(p[i].toString());
				writer.newLine();
			}
			
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
