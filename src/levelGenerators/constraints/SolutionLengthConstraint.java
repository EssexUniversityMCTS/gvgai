package levelGenerators.constraints;


public class SolutionLengthConstraint extends AbstractConstraint{

	public double solutionLength;
	public double maxSolutionLength;
	
	@Override
	public double checkConstraint() {
		if(solutionLength >= maxSolutionLength){
			return 1;
		}
		return solutionLength / maxSolutionLength;
	}
}
