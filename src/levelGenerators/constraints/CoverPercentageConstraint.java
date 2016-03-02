package levelGenerators.constraints;

public class CoverPercentageConstraint extends AbstractConstraint{

	/**
	 * the current cover percentage
	 */
	public double coverPercentage;
	/**
	 * the min acceptable cover percentage
	 */
	public double minCoverPercentage;
	/**
	 * the max acceptable cover percentage
	 */
	public double maxCoverPercentage;
	
	/**
	 * Check if the percentage of objects in the play field is between minCoverPercentage
	 * and maxCoverPercentage
	 * @return	return 1 if the the covered area in the level between minCoverPercentage 
	 * 			and maxCoverPercentage or value that indicates how near it is to the correct region
	 */
	@Override
	public double checkConstraint() {
		if(coverPercentage >= minCoverPercentage){
			if(coverPercentage <= maxCoverPercentage){
				return 1;
			}
			
			return (coverPercentage - 1) / (maxCoverPercentage - 1);
		}
		
		return coverPercentage / minCoverPercentage;
	}
}
