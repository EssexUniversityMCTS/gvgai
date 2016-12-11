package core.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import core.VGDLParser;
import core.VGDLRegistry;
import core.game.GameDescription.InteractionData;
import core.game.GameDescription.SpriteData;

public class SLDescription {
    /**
     * the keyword to encode the sprites in the game
     */
    private String KEYWORD = "sprite";
    
    /**
     * the current game object
     */
    private Game currentGame;
    /**
     * the current level
     */
    private String[] level;
    /**
     * the current game sprites
     */
    private SpriteData[] gameSprites;
    /**
     * the current encoded level
     */
    private String[][] currentLevel;
    
    /**
     * the seed value to encode the names
     */
    private int shift;
    
    /**
     * constructor for the SLDescription contains information about game sprites and the current level
     * @param currentGame	the current game
     * @param level		the current level
     * @param shift		random seed to encode the sprites
     * @throws Exception	if the level is empty
     */
    public SLDescription(Game currentGame, String[] level, int shift) throws Exception{
	this.currentGame = currentGame;
	this.level = level;
	this.gameSprites = null;
	this.currentLevel = null;
	
	this.shift = shift;
	
	this.reset(currentGame, level);
    }
    
    /**
     * reset the current variables to a new current game and level
     * @param currentGame	the new game
     * @param level		the new level
     * @throws Exception	if the level is empty
     */
    public void reset(Game currentGame, String[] level) throws Exception{
	this.currentGame = currentGame;
	this.level = level;
	if(this.currentGame == null){
	    return;
	}
	/**
	 * get all the game sprites in the game
	 */
	ArrayList<SpriteData> list = this.currentGame.getSpriteData();
	this.gameSprites = new SpriteData[list.size()];
	for(int i=0; i<this.gameSprites.length; i++){
	    this.gameSprites[i] = list.get(i);
	}
	
	if(this.level == null){
	    throw new Exception("level can't be null while game is not");
	}
	/**
	 * encode the level map
	 */
	HashMap<Character, ArrayList<String>> levelMapping = this.currentGame.getCharMapping();
	this.currentLevel = new String[level.length][getWidth(level)];
	for(int i=0; i<this.currentLevel.length; i++){
	    for(int j=0; j<this.currentLevel[i].length; j++){
		if(j >= this.currentLevel[i].length){
		    //if the length of the line shorter than the maximum width
		    this.currentLevel[i][j] = "";
		}
		else{
		    ArrayList<String> tempSprites = levelMapping.get(level[i].charAt(j));
		    if(tempSprites == null || tempSprites.size() == 0){
			// empty location
			this.currentLevel[i][j] = "";
		    }
		    else{
			//encode the different sprites
			this.currentLevel[i][j] = KEYWORD + "_" + this.encodeName(tempSprites.get(0), this.shift);
			for(int k=1; k<tempSprites.size(); k++){
			    this.currentLevel[i][j] += ", " + KEYWORD + "_" + this.encodeName(tempSprites.get(k), this.shift);
			}
		    }
		}
	    }
	}
    }
    
    /**
     * get the width of the level
     * @param level	current level
     * @return		width of the level
     */
    private int getWidth(String[] level){
	int width = 0;
	for(int i=0; i<level.length; i++){
	    if(level[i].length() > width){
		width = level[i].length();
	    }
	}
	
	return width;
    }
    
    /**
     * encode the current sprite index to a new index
     * @param index	current sprite index
     * @return		encoded sprite index
     */
    private int encodeIndex(int index, int seed){
	return index ^ seed;
    }
    
    /**
     * encode sprite name to an encoded index
     * @param name	the current sprite name to be encoded
     * @return		encoded index for a sprite name
     */
    private int encodeName(String name, int seed){
	for(int i=0; i<this.gameSprites.length; i++){
	    if(this.gameSprites[i].name.toLowerCase().trim().equals(name.toLowerCase().trim())){
		int result = encodeIndex(i, seed);
		return result;
	    }
	}
	return -1;
    }
    
    /**
     * decode the sprite name
     * @param name	current encoded sprite name
     * @return		correct sprite name
     */
    private String decodeName(String name, int seed){
	return this.gameSprites[Integer.parseInt(name.split("_")[1]) ^ seed].name;
    }
    
    /**
     * get an array of game sprites
     * @return	array contain all the game sprites
     */
    public SpriteData[] getGameSprites(){
	SpriteData[] result = new SpriteData[this.gameSprites.length];
	for(int i=0; i<result.length; i++){
	    try {
		result[i] = (SpriteData) this.gameSprites[i].clone();
	    } catch (CloneNotSupportedException e) {
		e.printStackTrace();
	    }
	    result[i].name = KEYWORD + "_" + this.encodeIndex(i, this.shift);
	    for(int j=0; j<result[i].sprites.size(); j++){
		result[i].sprites.set(j, KEYWORD + "_" + this.encodeName(result[i].sprites.get(j), this.shift));
	    }
	}
	
	return result;
    }
    
    /**
     * Return the current level as a comma separated 2D Array
     * @return a comma separated 2D Array of Sprites
     */
    public String[][] getCurrentLevel(){
	return this.currentLevel;
    }
    
    /**
     * Decode the rules and strings based on the seed
     * @param rules	current interaction rules to decode
     * @param wins	current termination rules to decode
     * @param seed	current encoding seed
     * @return		return decoded interaction and termination rules
     */
    public String[][] modifyRules(String[] rules, String[] wins, int seed){
	String[] modifiedRules = new String[rules.length + 1];
	String[] modifiedWins = new String[wins.length + 1];
	
	modifiedRules[0] = "InteractionSet";
	for(int i=1; i<modifiedRules.length; i++){
	    String[] parts = rules[i-1].split(" ");
	    modifiedRules[i] = "   ";
	    for(int j=0; j<parts.length; j++){
		if(parts[j].toLowerCase().contains(KEYWORD + "_")){
		    String[] temp = parts[j].split(KEYWORD + "_");
		    modifiedRules[i] += temp[0] + this.decodeName(parts[j].trim().toLowerCase(), seed) + " ";
		}
		else{
		    modifiedRules[i] += parts[j] + " ";
		}
	    } 
	}
	
	modifiedWins[0] = "TerminationSet";
	for(int i=1; i<modifiedWins.length; i++){
	    String[] parts = wins[i-1].split(" ");
	    modifiedWins[i] = "   ";
	    for(int j=0; j<parts.length; j++){
		if(parts[j].toLowerCase().contains(KEYWORD + "_")){
		    String[] temp = parts[j].split(KEYWORD + "_");
		    modifiedWins[i] += temp[0] + this.decodeName(parts[j].trim().toLowerCase(), seed) + " ";
		}
		else{
		    modifiedWins[i] += parts[j] + " ";
		}
	    } 
	}
	
	return new String[][]{modifiedRules, modifiedWins};
    }
    
    /**
     * get state observation based on the interaction rules and termination conditions
     * @param rules	current interaction rules
     * @param wins	current termination conditions
     * @return		state observation of the current game using 
     * 			the new interaction rules and termination conditions
     */
    public StateObservation testRules(String[] rules, String[] wins){
//	String[][] rw = this.modifyRules(rules, wins, this.shift);
//
//	this.currentGame.clearInteractionTerminationData();
//	new VGDLParser().parseInteractionTermination(this.currentGame, rw[0], rw[1]);
//
//	this.currentGame.reset();
//	this.currentGame.buildStringLevel(this.level, new Random().nextInt());
//	return this.currentGame.getObservation();
		return null;
    }
}
