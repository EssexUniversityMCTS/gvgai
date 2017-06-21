package tracks.levelGeneration.constraints;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;

public class CombinedConstraints extends AbstractConstraint{

	
	/**
	 * array of all constraints need to be checked
	 */
	private ArrayList<AbstractConstraint> constraints;
	
	/**
	 * 
	 */
	public CombinedConstraints(){
		constraints = new ArrayList<AbstractConstraint>();
	}
	

	/**
	 * Add multiple constraints to the combined constraints class
	 * @param conStrings	array of name of the constraint classes needed
	 */
	@SuppressWarnings("unchecked")
	public void addConstraints(String[] conStrings){
		for(String c:conStrings){
			try{
				Class constrainClass = Class.forName("tracks.levelGeneration.constraints." + c);
				Constructor constrainConstructor = constrainClass.getConstructor();
				AbstractConstraint constraint = (AbstractConstraint) constrainConstructor.newInstance();
				constraints.add(constraint);
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	

	/**
	 * Set the parameters of all the constraints added
	 * @param parameters	a hashmap contains all the objects needed for all constraints
	 */
	@Override
	public void setParameters(HashMap<String, Object> parameters) {
		for(AbstractConstraint c:constraints){
			c.setParameters(parameters);
		}
	}
	

	/**
	 * Check if all constraints are satisfied
	 * @return	return a percentage of how many constraints are satisfied
	 */
	@Override
	public double checkConstraint() {
		double score = 0;
		for(AbstractConstraint c:constraints){
			score += c.checkConstraint();
		}
		return score / constraints.size();
	}

}
