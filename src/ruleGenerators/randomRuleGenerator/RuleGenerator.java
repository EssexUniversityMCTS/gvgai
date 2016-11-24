package ruleGenerators.randomRuleGenerator;

import java.util.ArrayList;
import java.util.Random;

import core.game.SLDescription;
import core.generator.AbstractRuleGenerator;
import tools.ElapsedCpuTimer;

public class RuleGenerator extends AbstractRuleGenerator {
    /**
     * Array contains all the simple interactions
     */
    private String[] interactions = new String[]{
	    "killSprite", "killIfFromAbove", "stepBack", "undoAll", "flipDirection" , 
	    "reverseDirection", "attractGaze", "align", "turnAround", "wrapAround", 
	    "pullWithIt", "bounceForward", "collectResource"};
    /**
     * A list of all the useful sprites in the game
     */
    private ArrayList<String> usefulSprites;
    /**
     * Random object to help in generation
     */
    private Random random;

    /**
     * This is a random rule generator
     * @param sl	contains information about sprites and current level
     * @param time	amount of time allowed
     */
    public RuleGenerator(SLDescription sl, ElapsedCpuTimer time) {
	this.usefulSprites = new ArrayList<String>();
	this.random = new Random();
	String[][] currentLevel = sl.getCurrentLevel();
	
	//Just get the useful sprites from the current level
	for (int y = 0; y < currentLevel.length; y++) {
	    for (int x = 0; x < currentLevel[y].length; x++) {
		String[] parts = currentLevel[y][x].split(",");
		for (int i = 0; i < parts.length; i++) {
		    if (parts[i].trim().length() > 0) {
			//Add the sprite if it doesn't exisit
			if (!usefulSprites.contains(parts[i].trim())) {
			    usefulSprites.add(parts[i].trim());
			}
		    }
		}
	    }
	}
	//Add End Of Screen as one of useful sprites
//	this.usefulSprites.add("EOS");
    }
    
    /**
     * convert the arraylist of string to a normal array of string
     * @param list	input arraylist
     * @return		string array
     */
    private String[] getArray(ArrayList<String> list){
	String[] array = new String[list.size()];
	for(int i=0; i<list.size(); i++){
	    array[i] = list.get(i);
	}
	
	return array;
    }
    
    /**
     * Generate random interaction rules and termination conditions
     * @param sl	contains information about sprites and current level
     * @param time	amount of time allowed
     */
    @Override
    public String[][] generateRules(SLDescription sl, ElapsedCpuTimer time) {
	ArrayList<String> interaction = new ArrayList<String>();
	ArrayList<String> termination = new ArrayList<String>();
	
	//number of interactions in the game based on the number of sprites
	int numberOfInteractions = (int) (this.usefulSprites.size() * (0.5 + 0.5 * this.random.nextDouble()));
	for (int i = 0; i < numberOfInteractions; i++) {
	    //get two random indeces for the two sprites in the interaction
	    int i1 = this.random.nextInt(this.usefulSprites.size());
	    int i2 = (i1 + 1 + this.random.nextInt(this.usefulSprites.size() - 1)) % this.usefulSprites.size();
	    //add the new random interaction
	    interaction.add(this.usefulSprites.get(i1) + " " + 
		    this.usefulSprites.get(i2) + " > " + 
		    this.interactions[this.random.nextInt(this.interactions.length)] + 
		    " scoreChange=" + (this.random.nextInt(2) + 1));
	}
	//Add a single Timeout termination condition
	termination.add("Timeout limit=" + (800 + this.random.nextInt(500)) + " win=True");
	
	return new String[][] { getArray(interaction), getArray(termination) };
    }

}
