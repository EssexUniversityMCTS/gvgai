package core.generator;

import java.util.ArrayList;
import java.util.HashMap;

import core.game.SLDescription;
import tools.ElapsedCpuTimer;

public abstract class AbstractRuleGenerator {
    /**
     * This function is called by the framework to generate rules for the specific level
     * @param sl	description object contains information about the level and sprites
     * @param time	the amount of time allowed to generate the rules
     * @return		two arrays: 1- interaction rules 2- termination conditions
     */
    public abstract String[][] generateRules(SLDescription sl, ElapsedCpuTimer time);
    
    /**
     * Return a list of all supersets 
     * @return
     */
    public HashMap<String, ArrayList<String>> getSpriteSetStructure(){
	return null;
    }
}
