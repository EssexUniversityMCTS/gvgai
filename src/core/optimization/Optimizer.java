package core.optimization;

import tools.ElapsedCpuTimer;

public abstract class Optimizer {
	public Optimizer(ElapsedCpuTimer time, OptimizationObjective obj){
		
	}
	
	public abstract double[] optimize(ElapsedCpuTimer time, OptimizationObjective obj);
}
