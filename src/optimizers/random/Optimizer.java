package optimizers.random;

import java.util.Random;

import core.optimization.AbstractOptimizer;
import core.optimization.OptimizationObjective;

/**
 * random optimizer, it just return any random values
 * @author AhmedKhalifa
 */
public class Optimizer extends AbstractOptimizer{
	/**
	 * random object to get random values
	 */
	private Random random;
	
	public Optimizer(OptimizationObjective obj) {
		super(obj);
		
		this.random = new Random();
	}
	
	/**
	 * return random parameters
	 */
	@Override
	public double[][] optimize(OptimizationObjective obj) {
		/**
		 * initialize random array with number of optimized
		 */
		double[][] parameters = new double[1][obj.getNumberOfParameters()];
		/**
		 * put random values for the parameters
		 */
		for(int i=0; i<parameters[0].length; i++){
			parameters[0][i] = 2 * random.nextDouble() - 1;
		}
		/**
		 * return the parameter array
		 */
		return parameters;
	}

}
