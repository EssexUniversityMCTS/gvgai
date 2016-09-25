package core.optimization;

import tools.ElapsedCpuTimer;

public abstract class AbstractOptimizer {
	public AbstractOptimizer(ElapsedCpuTimer time, OptimizationObjective obj){
		
	}
	
	public abstract double[] optimize(ElapsedCpuTimer time, OptimizationObjective obj);
}
