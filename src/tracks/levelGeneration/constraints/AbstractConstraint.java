package tracks.levelGeneration.constraints;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map.Entry;

public abstract class AbstractConstraint {
	
	/**
	 * Set the parameter of the constrains from a HashMap
	 * @param parameters	hashmap of constraints parameters
	 */
	public void setParameters(HashMap<String, Object> parameters){
		Field[] fields = this.getClass().getFields();
		for(Field f:fields){
			for(Entry<String, Object> p:parameters.entrySet()){
				if(f.getName().equalsIgnoreCase(p.getKey())){
					try{
						if(f.getType() == int.class || f.getType() == Integer.class){
							f.setInt(this, Integer.parseInt(p.getValue().toString()));
						}
						else if(f.getType() == double.class || f.getType() == Double.class){
							f.setDouble(this, Double.parseDouble(p.getValue().toString()));
						}
						else{
							f.set(this, f.getType().cast(p.getValue()));
						}
					}
					catch(Exception e){
						e.printStackTrace();	
					}
				}
			}
		}
	}
	
	/**
	 * Check if the current constrain is achieved or not
	 * @return	Reflect how much the constrain is achieved 1: achieved 0: very bad
	 */
	public abstract double checkConstraint();
}
