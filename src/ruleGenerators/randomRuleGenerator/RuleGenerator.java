package ruleGenerators.randomRuleGenerator;

import java.util.ArrayList;
import java.util.Random;

import core.game.SLDescription;
import core.generator.AbstractRuleGenerator;
import tools.ElapsedCpuTimer;

public class RuleGenerator extends AbstractRuleGenerator {
    private String[] interactions = new String[]{
	    "killSprite", "killIfFromAbove", "stepBack", "undoAll", "flipDirection" , 
	    "reverseDirection", "attractGaze", "align", "turnAround", "wrapAround", 
	    "teleportToExit", "pullWithIt", "bounceForward", "collectResource"};
    private ArrayList<String> usefulSprites;
    private Random random;

    public RuleGenerator(SLDescription sl, ElapsedCpuTimer time) {
	this.usefulSprites = new ArrayList<String>();
	this.random = new Random();
	String[][] currentLevel = sl.getCurrentLevel();

	for (int y = 0; y < currentLevel.length; y++) {
	    for (int x = 0; x < currentLevel[y].length; x++) {
		String[] parts = currentLevel[y][x].split(",");
		for (int i = 0; i < parts.length; i++) {
		    if (parts[i].trim().length() > 0) {
			if (!usefulSprites.contains(parts[i].trim())) {
			    usefulSprites.add(parts[i].trim());
			}
		    }
		}
	    }
	}
	this.usefulSprites.add("EOS");
    }
    
    private String[] getArray(ArrayList<String> list){
	String[] array = new String[list.size()];
	for(int i=0; i<list.size(); i++){
	    array[i] = list.get(i);
	}
	
	return array;
    }
    
    @Override
    public String[][] generateRules(SLDescription sl, ElapsedCpuTimer time) {
	sl.getGameSprites();
	ArrayList<String> interaction = new ArrayList<String>();
	ArrayList<String> termination = new ArrayList<String>();

	int numberOfInteractions = (int) (this.usefulSprites.size() * (0.25 + 0.25 * this.random.nextDouble()));
	for (int i = 0; i < numberOfInteractions; i++) {
	    int i1 = this.random.nextInt(this.usefulSprites.size());
	    int i2 = (i1 + 1 + this.random.nextInt(this.usefulSprites.size() - 1)) % this.usefulSprites.size();
	    interaction.add(this.usefulSprites.get(i1) + " " + 
		    this.usefulSprites.get(i2) + " > " + 
		    this.interactions[this.random.nextInt(this.interactions.length)] + 
		    " scoreChange=" + (this.random.nextInt(2) + 1));
	}
	termination.add("Timeout limit=" + (800 + this.random.nextInt(500)) + " win=True");

	return new String[][] { getArray(interaction), getArray(termination) };
    }

}
