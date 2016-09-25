package optimizers.random;

import java.util.Random;

import core.optimization.AbstractOptimizer;
import core.optimization.OptimizationObjective;
import tools.ElapsedCpuTimer;

public class Optimizer extends AbstractOptimizer{
	private Random random;
	
	public Optimizer(ElapsedCpuTimer time, OptimizationObjective obj) {
		super(time, obj);
		
		this.random = new Random();
	}
	
	@Override
	public double[] optimize(ElapsedCpuTimer time, OptimizationObjective obj) {
		double[] parameters = new double[obj.getNumberOfParameters()];
		for(int i=0; i<parameters.length; i++){
			parameters[i] = 2 * random.nextDouble() - 1;
		}
		
		return parameters;
	}

}
