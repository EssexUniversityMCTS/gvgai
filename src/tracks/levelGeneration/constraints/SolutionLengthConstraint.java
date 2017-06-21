package tracks.levelGeneration.constraints;


public class SolutionLengthConstraint extends AbstractConstraint{


	/**
	 * the number of steps that the agent did when plays the game
	 */
	public double solutionLength;
	/**
	 * the minimum number of steps the agent should do
	 */
	public double minSolutionLength;
	
	/**
	 * check if the solution length is at least equal to minSolutionLength
	 * @return 	1 if the solution length is larger than or equal to min solution length
	 * 			and percentage of how near the solution length to min solution length otherwise
	 */
	@Override
	public double checkConstraint() {
		if(solutionLength >= minSolutionLength){
			return 1;
		}
		return solutionLength / minSolutionLength;
	}
}
