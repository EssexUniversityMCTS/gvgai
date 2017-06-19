package core.optimization;

import tools.ElapsedCpuTimer;

/**
 * Base class for optimization
 * participants have to extends this class and 
 * provide a constructor and optimize function
 * @author AhmedKhalifa
 */
public abstract class AbstractOptimizer {
	/**
	 * Initialize your class variables
	 * @param obj	optimization objective object, contain information about the current optimization problem
	 */
	public AbstractOptimizer(OptimizationObjective obj){}
	/**
	 * Optimize the objective object and return the best parameters after fixed amount of time
	 * @param obj	optimization objective object, can be used to test the current parameters
	 * @return 		the best parameters for the optimization problem,
	 */
	public abstract double[][] optimize(OptimizationObjective obj);
}
