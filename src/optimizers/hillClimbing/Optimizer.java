package optimizers.hillClimbing;

import java.util.Random;

import core.optimization.AbstractOptimizer;
import core.optimization.OptimizationObjective;
import tools.ElapsedCpuTimer;

public class Optimizer extends AbstractOptimizer {
	private Random random;
	
	public Optimizer(ElapsedCpuTimer time, OptimizationObjective obj) {
		super(time, obj);
		
		this.random = new Random();
	}

	public double[] randomChange(double[] parameters, double probability){
		double[] results = parameters.clone();
		
		for(int i=0; i<results.length; i++){
			if(this.random.nextDouble() < probability){
				results[i] += 2 * this.random.nextDouble() - 1;
			}
		}
		
		return results;
	}
	
	public double getAverage(double[] values){
		double result = 0;
		for(int i=0; i<values.length; i++){
			result += values[i];
		}
		
		return result/values.length;
	}
	
	@Override
	public double[] optimize(ElapsedCpuTimer time, OptimizationObjective obj) {
		double[] parameters = new double[obj.getNumberOfParameters()];
		for(int i=0; i<parameters.length; i++){
			parameters[i] = 2 * random.nextDouble() - 1;
		}
		double currentFitness = this.getAverage(obj.evaluate(parameters));
		
		double totalTime = 0;
		double iterations = 0;
		double averageTime = 0;
		while(time.remainingTimeMillis() > averageTime){
			ElapsedCpuTimer tempTimer = new ElapsedCpuTimer();
			
			double[] newParameters = this.randomChange(parameters, 0.1);
			double newFitness = this.getAverage(obj.evaluate(parameters));
			if(newFitness > currentFitness){
				parameters = newParameters;
			}
			
			totalTime += tempTimer.elapsedMillis();
			iterations += 1;
			averageTime = totalTime / iterations;
		}
		
		return parameters;
	}

}
