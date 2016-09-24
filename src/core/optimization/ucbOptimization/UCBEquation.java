package core.optimization.ucbOptimization;

public interface UCBEquation {
	int lengthParameters();
	double evaluate(double[] values, double[] parameters);
}
