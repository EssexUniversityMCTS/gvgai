package levelGenerators.constraints;

public class CoverPercentageConstraint extends AbstractConstraint{
	public double coverPercentage;
	public double minCoverPercentage;
	public double maxCoverPercentage;
	
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
