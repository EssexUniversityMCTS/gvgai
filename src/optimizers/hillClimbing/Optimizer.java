package optimizers.hillClimbing;

import java.util.Random;

import core.optimization.AbstractOptimizer;
import core.optimization.OptimizationObjective;

/**
 * use hill climbing algorithm to get the best set of parameters
 * @author AhmedKhalifa
 */
public class Optimizer extends AbstractOptimizer {
	/**
	 * random object to help getting random new values
	 */
	private Random random;
	
	/**
	 * initialize the random object
	 */
	public Optimizer(OptimizationObjective obj) {
		super(obj);
		
		this.random = new Random();
	}

	/**
	 * change the current parameters array using a certain probability
	 * @param parameters	the current parameters
	 * @param probability	the probability to change each parameter
	 * @return				the new parameters after changes
	 */
	public double[] randomChange(double[] parameters, double probability){
		double[] results = parameters.clone();
		
		for(int i=0; i<results.length; i++){
			/**
			 * check if probability happens then change the parameter value
			 */
			if(this.random.nextDouble() < probability){
				results[i] += 2 * this.random.nextDouble() - 1;
			}
		}
		
		/**
		 * the modified parameters
		 */
		return results;
	}
	
	/**
	 * get total average values
	 * @param 	values array of values
	 * @return 	average value of the whole array
	 */
	public double getAverage(double[] values){
		double result = 0;
		for(int i=0; i<values.length; i++){
			result += values[i];
		}
		
		return result/values.length;
	}
	
	/**
	 * use hill climbing to optimize the problem
	 */
	@Override
	public double[] optimize(OptimizationObjective obj) {
		/**
		 * initialize random parameter
		 */
		double[] parameters = new double[obj.getNumberOfParameters()];
		for(int i=0; i<parameters.length; i++){
			parameters[i] = 2 * random.nextDouble() - 1;
		}
		/**
		 * evaluate the random parameters
		 */
		double currentFitness = this.getAverage(obj.evaluate(parameters));
		
		/**
		 * as long as there is evaluations
		 */
		while(true){
			/**
			 * change the current parameters
			 */
			double[] newParameters = this.randomChange(parameters, 0.1);
			/**
			 * evaluate the new parameters
			 */
			double[] tempValues = obj.evaluate(parameters);
			if(tempValues == null){
				break;
			}
			double newFitness = this.getAverage(tempValues);
			/**
			 * if the new parameters are better then pick it
			 */
			if(newFitness > currentFitness){
				parameters = newParameters;
			}
		}
		
		/**
		 * return the best parameters so far
		 */
		return parameters;
	}

}
