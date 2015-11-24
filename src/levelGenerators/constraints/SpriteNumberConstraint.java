package levelGenerators.constraints;

import java.util.HashMap;
import java.util.Map.Entry;

import tools.GameAnalyzer;

public class SpriteNumberConstraint extends AbstractConstraint{
	
	public HashMap<String, Integer> numOfObjects;
	public GameAnalyzer gameAnalyzer;
	
	@Override
	public double checkConstraint() {
		double totalNum = 0;
		double acheivedNum = 0;
		
		for(Entry<String, Integer> n:numOfObjects.entrySet()){
			if(gameAnalyzer.checkIfSpawned(n.getKey()) > 0){
				totalNum += 1;
				if(n.getValue() > 0){
					acheivedNum += 1;
				}
			}
		}
		
		return acheivedNum / totalNum;
	}

}
