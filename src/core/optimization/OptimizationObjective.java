package core.optimization;

public interface OptimizationObjective {
	int getNumberOfParameters();
	
	int getNumberOfObjectives();
	
	double[] evaluate(double[] parameters);
}
