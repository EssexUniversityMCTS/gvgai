package levelGenerators.constraints;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;

public class CombinedConstraints extends AbstractConstraint{

	private ArrayList<AbstractConstraint> constraints;
	
	public CombinedConstraints(){
		constraints = new ArrayList<AbstractConstraint>();
	}
	
	public void addConstraints(String[] conStrings){
		for(String c:conStrings){
			try{
				Class constrainClass = Class.forName("levelGenerators.constraints." + c);
				Constructor constrainConstructor = constrainClass.getConstructor();
				AbstractConstraint constraint = (AbstractConstraint) constrainConstructor.newInstance();
				constraints.add(constraint);
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void setParameters(HashMap<String, Object> parameters) {
		for(AbstractConstraint c:constraints){
			c.setParameters(parameters);
		}
	}
	
	@Override
	public double checkConstraint() {
		double score = 0;
		for(AbstractConstraint c:constraints){
			score += c.checkConstraint();
		}
		return score / constraints.size();
	}

}
