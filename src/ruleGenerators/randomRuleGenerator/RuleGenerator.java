package ruleGenerators.randomRuleGenerator;

import java.util.ArrayList;
import java.util.Random;

import core.game.GameDescription.SpriteData;
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
	    "pullWithIt", "bounceForward", "killAll", "spawnBehind", "transformTo",
	    "teleportToExit", "collectResource", "cloneSprite"};
    /**
     * A list of all the useful sprites in the game without the avatar
     */
    private ArrayList<String> usefulSprites;
    /**
     * the avatar sprite name
     */
    private String avatar;
    /**
     * Random object to help in generation
     */
    private Random random;
    /**
     * Parameter used to fix the number of interations in the game
     */
    private int FIXED = -1;

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
	//save the avatar separately
	this.avatar = this.getAvatar(sl);
	this.usefulSprites.remove(this.avatar);
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
     * Get the avatar sprite from SLDescription
     * @param sl	SLDescription object contains all the game info
     * @return		the avatar sprite name
     */
    private String getAvatar(SLDescription sl){
	SpriteData[] sprites = sl.getGameSprites();
	for(int i=0; i<this.usefulSprites.size(); i++){
	    SpriteData s = this.getSpriteData(sprites, this.usefulSprites.get(i));
	    if(s != null && s.isAvatar){
		return this.usefulSprites.get(i);
	    }
	}
	return "";
    }
    
    /**
     * Get SpriteData for certain sprite name
     * @param sprites	list of all game sprites
     * @param name	current sprite name
     * @return		current sprite data
     */
    private SpriteData getSpriteData(SpriteData[] sprites, String name){
	for(int i=0; i<sprites.length; i++){
	    if(sprites[i].name.equalsIgnoreCase(name)){
		return sprites[i];
	    }
	}
	
	return null;
    }

    /**
     * check if the supported interaction is valid
     * @param sl	SLDescription object contains all the information about the game
     * @param s1	the first sprite in the interaction
     * @param s2	the second sprite in the interaction
     * @param interact	the interaction itself
     * @return		True if its a valid interaction and False otherwise
     */
    private boolean isValidInteraction(SLDescription sl, SpriteData s1, SpriteData s2, String interact){
	if(interact.equalsIgnoreCase("collectResource")){
	    return s1.isResource;
	}
	if(interact.equalsIgnoreCase("teleportToExit")){
	    return s2.isPortal;
	}
	if(interact.equalsIgnoreCase("cloneSprite")){
	    return !s1.isAvatar;
	}
	
	return true;
    }
    
    /**
     * return a valid interaction for the current chosen sprites
     * @param sl	SLDescription object contains all the information about the game
     * @param s1	the first sprite name
     * @param s2	the second sprite name
     * @return		a valid interaction line for the current sprites
     */
    private String getValidInteraction(SLDescription sl, String s1, String s2){
	String interact = this.interactions[this.random.nextInt(this.interactions.length)];
	while(!this.isValidInteraction(sl, this.getSpriteData(sl.getGameSprites(), s1), 
		this.getSpriteData(sl.getGameSprites(), s2), interact)){
	    interact = this.interactions[this.random.nextInt(this.interactions.length)];
	}
	
	String properties = "";
	if(interact.equalsIgnoreCase("killAll") || interact.equalsIgnoreCase("spawnBehind") || 
		interact.equalsIgnoreCase("transformTo")){
	    properties += " stype=" + this.usefulSprites.get(this.random.nextInt(this.usefulSprites.size()));
	}
	properties += " scoreChange=" + (2 * this.random.nextInt(2) - 1);
	
	return s1 + " " + s2 + " > " + interact + " " + properties;
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
	if(this.FIXED > 0){
	    numberOfInteractions = this.FIXED;
	}
	for (int i = 0; i < numberOfInteractions; i++) {
	    //get two random indeces for the two sprites in the interaction
	    int i1 = this.random.nextInt(this.usefulSprites.size());
	    int i2 = (i1 + 1 + this.random.nextInt(this.usefulSprites.size() - 1)) % this.usefulSprites.size();
	    //add the new random interaction
	    switch(this.random.nextInt(5)){
	    case 0:
		interaction.add(this.getValidInteraction(sl, this.usefulSprites.get(i1), this.avatar));
		break;
	    case 1:
		interaction.add(this.getValidInteraction(sl, this.avatar, this.usefulSprites.get(i2)));
		break;
	    case 2:
		interaction.add(this.getValidInteraction(sl, this.usefulSprites.get(i1), "EOS"));
		break;
	    case 3:
		interaction.add(this.getValidInteraction(sl, this.avatar, "EOS"));
		break;
	    case 4:
		interaction.add(this.getValidInteraction(sl, this.usefulSprites.get(i1), this.usefulSprites.get(i2)));
		break;
	    }
	}
	//Add a winning termination condition
	if(this.random.nextBoolean()){
	    termination.add("Timeout limit=" + (800 + this.random.nextInt(500)) + " win=True");
	}
	else{
	    String chosen = this.usefulSprites.get(this.random.nextInt(this.usefulSprites.size()));
	    termination.add("SpriteCounter stype=" + chosen + " limit=0 win=True");
	}
	//Add a losing termination condition
	termination.add("SpriteCounter stype=" + this.avatar + " limit=0 win=False");
	
	return new String[][] { getArray(interaction), getArray(termination) };
    }

}
