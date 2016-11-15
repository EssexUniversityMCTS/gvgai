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
     * 
     */
    private String KEYWORD = "sprite";
    
    /**
     * 
     */
    private Game currentGame;
    /**
     * 
     */
    private String[] level;
    /**
     * 
     */
    private SpriteData[] gameSprites;
    /**
     * 
     */
    private String[][] currentLevel;
    /**
     * 
     */
    private boolean[] background;
    
    /**
     * 
     */
    private int shift;
    /**
     * 
     */
    private Random random;
    
    /**
     * 
     * @param currentGame
     * @param level
     * @throws Exception
     */
    public SLDescription(Game currentGame, String[] level, int shift) throws Exception{
	this.currentGame = currentGame;
	this.level = level;
	this.gameSprites = null;
	this.currentLevel = null;
	this.background = null;
	
	this.shift = shift;
	this.random = new Random();
	
	this.reset(currentGame, level);
    }
    
    /**
     * 
     * @param currentGame
     * @param level
     * @throws Exception
     */
    public void reset(Game currentGame, String[] level) throws Exception{
	this.currentGame = currentGame;
	this.level = level;
	if(this.currentGame == null){
	    return;
	}
	ArrayList<SpriteData> list = this.currentGame.getSpriteData();
	this.gameSprites = new SpriteData[list.size()];
	this.background = new boolean[list.size()];
	for(int i=0; i<this.gameSprites.length; i++){
	    this.gameSprites[i] = list.get(i);
	}
	
	for(int i=0; i<this.gameSprites.length; i++){
	    int itype1 = VGDLRegistry.GetInstance().getRegisteredSpriteValue(this.gameSprites[i].name);
	    this.background[i] = true;
	    for(int j=0; j<this.gameSprites.length; j++){
		int itype2 = VGDLRegistry.GetInstance().getRegisteredSpriteValue(this.gameSprites[j].name);
		ArrayList<InteractionData> i1 = this.currentGame.getInteractionData(itype1, itype2);
		ArrayList<InteractionData> i2 = this.currentGame.getInteractionData(itype2, itype1);
		if(i1.size() + i2.size() > 0){
		    this.background[i] = false;
		    break;
		}
		//TODO: if its a side object or a bullet object or in the win conditions
	    }
	}
	
	if(this.level == null){
	    throw new Exception("level can't be null while game is not");
	}
	HashMap<Character, ArrayList<String>> levelMapping = this.currentGame.getCharMapping();
	this.currentLevel = new String[level.length][getWidth(level)];
	for(int i=0; i<this.currentLevel.length; i++){
	    for(int j=0; j<this.currentLevel[i].length; j++){
		if(j >= this.currentLevel[i].length){
		    this.currentLevel[i][j] = "";
		}
		else{
		    ArrayList<String> tempSprites = levelMapping.get(level[i].charAt(j));
		    if(tempSprites.size() == 0){
			this.currentLevel[i][j] = "";
		    }
		    else{
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
     * 
     * @param level
     * @return
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
     * 
     * @param index
     * @return
     */
    private int encodeIndex(int index, int seed){
	return (index + seed) % this.gameSprites.length;
    }
    
    /**
     * 
     * @param name
     * @return
     */
    private int encodeName(String name, int seed){
	for(int i=0; i<this.gameSprites.length; i++){
	    if(this.gameSprites[i].name.toLowerCase().trim().equals(name.toLowerCase().trim())){
		return encodeIndex(i, seed);
	    }
	}
	return -1;
    }
    
    /**
     * 
     * @param name
     * @return
     */
    private String decodeName(String name, int seed){
	int index = Integer.parseInt(name.split("_")[1]);
	int result = index - seed;
	if(result < 0){
	    result += this.gameSprites.length;
	}
	return this.gameSprites[result].name;
    }
    
    /**
     * 
     * @return
     */
    public SpriteData[] getGameSprites(){
	SpriteData[] result = this.gameSprites.clone();
	for(int i=0; i<result.length; i++){
	    this.gameSprites[i].name = KEYWORD + "_" + this.encodeIndex(i, this.shift);
	}
	
	return result;
    }
    
//    /**
//     * return which sprites are background sprite to either 
//     * ignore in the rules or use them for something more interesting
//     * @return an array of sprite data that are everywhere
//     */
//    public SpriteData[] getBackgroundSprites(){
//	int size=0;
//	for(int i=0; i<background.length; i++){
//	    if(background[i]){
//		size += 1;
//	    }
//	}
//	SpriteData[] result = new SpriteData[size];
//	size = 0;
//	for(int i=0; i<background.length; i++){
//	    if(background[i]){
//		try {
//		    result[size] = (SpriteData) gameSprites[i].clone();
//		} catch (CloneNotSupportedException e) {
//		    e.printStackTrace();
//		}
//		result[size].name = KEYWORD + "_" + this.encodeIndex(i, this.shift);
//		size += 1;
//	    }
//	}
//	
//	return result;
//    }
    
    /**
     * Return the current level as a comma separated 2D Array
     * @return a comma separated 2D Array of Sprites
     */
    public String[][] getCurrentLevel(){
	return this.currentLevel;
    }
    
    /**
     * 
     * @param rules
     * @param wins
     * @param seed
     * @return
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
     * 
     * @param rules
     * @param wins
     * @return
     */
    public StateObservation testRules(String[] rules, String[] wins){
	String[][] rw = this.modifyRules(rules, wins, this.shift);
	
	this.currentGame.clearInteractionTerminationData();
	new VGDLParser().parseInteractionTermination(this.currentGame, rw[0], rw[1]);
	
	this.currentGame.reset();
	this.currentGame.buildStringLevel(this.level, new Random().nextInt());
	return this.currentGame.getObservation();
    }
}
