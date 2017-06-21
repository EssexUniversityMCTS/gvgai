package core.optimization.ucbOptimization;

/**
 * Contains information about the current used UCB Equation
 * @author AhmedKhalifa
 */
public interface UCBEquation {
	/**
	 * get the number of parameters used in this equation
	 * @return number of ucb parameters
	 */
	int lengthParameters();
	/**
	 * evaluate the ucb equation using the current parameters at the current state
	 * @param values		state values
	 * @param parameters	ucb parameters
	 * @return				ucb equation result
	 */
	double evaluate(double[] values, double[] parameters);
	/**
	 * print the ucb equation using the current parameters
	 * @param parameters	ucb parameters
	 * @return				the ucb equation
	 */
	String toString(double[] parameters);
}
