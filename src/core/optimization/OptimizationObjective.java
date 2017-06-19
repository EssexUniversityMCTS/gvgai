package core.optimization;

/**
 * Contains information about the current optimization problem
 * @author AhmedKhalifa
 */
public interface OptimizationObjective {
	/**
	 * get the number of parameter required to be optimized
	 * @return number of optimization parameters
	 */
	int getNumberOfParameters();
	/**
	 * get the number of objectives the parameters will be test against
	 * @return number of target objectives
	 */
	int getNumberOfObjectives();
	/**
	 * evaluate the current parameters against the target objectives
	 * @param parameters	the current set of parameters to test
	 * @return		array of fitness against all objectives (the higher the better)
	 */
	double[] evaluate(double[] parameters);
}
