package ruleGenerators.constructiveRuleGenerator;

import java.util.ArrayList;
import java.util.Random;

import core.game.GameDescription.SpriteData;
import core.game.SLDescription;
import core.generator.AbstractRuleGenerator;
import tools.ElapsedCpuTimer;
import tools.LevelAnalyzer;

public class RuleGenerator extends AbstractRuleGenerator{
    private LevelAnalyzer la;
    
    private String[] movableWallInteraction = new String[]{"stepBack", "flipDirection", "reverseDirection", 
	    "turnAround", "wrapAround"};
    
    private double wallPercentageProb = 0.5;
    private double spikeProb = 0.5;
    private double doubleNPCsProb = 0.5;
    private double usefulMovableProb = 0.5;
    private double firewallProb = 0.1;
    private double scoreSpikeProb = 0.1;
    private double killScoreProb = 0.2;
    
    private ArrayList<String> interactions;
    private ArrayList<String> terminations;
    
    private SpriteData wall;
    private ArrayList<SpriteData> exit;
    private SpriteData score;
    private SpriteData spike;
    
    private Random random;
    private ArrayList<String> harmfulObjects;
    private ArrayList<String> fleeingNPCs;
    
    public RuleGenerator(SLDescription sl, ElapsedCpuTimer time){
	la = new LevelAnalyzer(sl);
	
	interactions = new ArrayList<String>();
	terminations = new ArrayList<String>();
	
	random = new Random();
	harmfulObjects = new ArrayList<String>();
	fleeingNPCs = new ArrayList<String>();
	
	wall = null;
	SpriteData[] temp = la.getBorderObjects(
		(2.0 * (la.getWidth() + la.getLength())) / (la.getLength() * la.getWidth()), this.wallPercentageProb);
	if(temp.length > 0){
	    wall = temp[0];
	    for(int i=0; i<temp.length; i++){
		if(la.getNumberOfObjects(temp[i].name) < la.getNumberOfObjects(wall.name)){
		    wall = temp[i];
		}
	    }
	}
	
	exit = new ArrayList<SpriteData>();
	temp = la.getPortals(0, 1, true);
	for(int i=0; i<temp.length; i++){
	    if(!temp[i].type.equalsIgnoreCase("portal")){
		exit.add(temp[i]);
	    }
	}
	
	ArrayList<SpriteData> tempList = new ArrayList<SpriteData>();
	score = null;
	spike = null;
	temp = la.getImmovables(0, scoreSpikeProb, true);
	if (temp.length > 0) {
	    if (wall == null) {
		score = temp[random.nextInt(temp.length)];
		spike = temp[random.nextInt(temp.length)];
	    } 
	    else {
		tempList = new ArrayList<SpriteData>();
		SpriteData[] relatedSprites = la.getSpritesOnSameTile(wall.name);
		for (int i = 0; i < temp.length; i++) {
		    for (int j = 0; j < relatedSprites.length; j++) {
			if (!temp[i].name.equals(relatedSprites[j].name)) {
			    tempList.add(temp[i]);
			}
		    }
		    if(relatedSprites.length == 0){
			tempList.add(temp[i]);
		    }
		}

		score = tempList.get(random.nextInt(tempList.size()));
		spike = tempList.get(random.nextInt(tempList.size()));
	    }
	}
    }
    
    /**
     * convert the arraylist of string to a normal array of string
     * @param list	input arraylist
     * @return		string array
     */
    private String[] convertToStringArray(ArrayList<String> list){
	String[] array = new String[list.size()];
	for(int i=0; i<list.size(); i++){
	    array[i] = list.get(i);
	}
	
	return array;
    }
    
    private boolean isAvatar(String spriteName){
	SpriteData[] avatar = la.getAvatars(false);
	for(int i=0; i<avatar.length; i++){
	    if(avatar[i].equals(spriteName)){
		return true;
	    }
	}
	return false;
    }
    
    //DONE
    private void getWallInteractions(){
	String wallName = "EOS";
	if (wall != null) {
	    wallName = wall.name;
	}
	boolean isFireWall = this.random.nextDouble() < firewallProb;
	
	String action = "stepBack";
	if(isFireWall){
	    action = "killSprite";
	}
	SpriteData[] temp = la.getAvatars(false);
	for (int i = 0; i < temp.length; i++) {
	    interactions.add(temp[i].name + " " + wallName + " > " + action);
	}

	action = movableWallInteraction[random.nextInt(movableWallInteraction.length)];
	if(isFireWall){
	    action = "killSprite";
	}
	temp = la.getMovables(0, 1, false);
	for (int i = 0; i < temp.length; i++) {
	    interactions.add(temp[i].name + " " + wallName + " > " + action);
	}
	temp = la.getNPCs(0, 1, false);
	for (int i = 0; i < temp.length; i++) {
	    interactions.add(temp[i].name + " " + wallName + " > " + action);
	}
    }
    
    //DONE
    private void getResourceInteractions(){
	SpriteData[] avatar = la.getAvatars(false);
	SpriteData[] resources = la.getResources(0, 1, true);
	
	for(int i=0; i<avatar.length; i++){
	    for(int j=0; j<resources.length; j++){
		interactions.add(resources[j].name + " " + avatar[i].name + " > collectResource");
	    }
	}
    }
    
    //DONE
    private void getSpawnerInteractions(){
	SpriteData[] avatar = la.getAvatars(false);
	SpriteData[] spawners = la.getSpawners(0, 1, true);
	
	for(int i=0; i<avatar.length; i++){
	    for(int j=0; j<spawners.length; j++){
		for(int k=0; k<spawners[j].sprites.size(); k++){
		    harmfulObjects.add(spawners[j].sprites.get(k));
		    interactions.add(avatar[i].name + " " + spawners[j].sprites.get(k) + " > killSprite");
		}
	    }
	}
    }
    
    //DONE
    private void getImmovableInteractions(){	
	SpriteData[] avatar = la.getAvatars(false);
	
	if(score != null){
	    for(int i=0; i<avatar.length; i++){
		interactions.add(score.name + " " + avatar[i].name + " > killSprite scoreChange=1");
	    }
	}
	
	if (spike != null && !spike.name.equalsIgnoreCase(score.name)) {
	    if (random.nextDouble() < spikeProb) {
		harmfulObjects.add(spike.name);
		for (int i = 0; i < avatar.length; i++) {
		    interactions.add(avatar[i].name + " " + spike.name + " > killSprite");
		}
	    } 
	    else {
		for (int i = 0; i < avatar.length; i++) {
		    interactions.add(spike.name + " " + avatar[i].name + " > killSprite scoreChange=2");
		}
	    }
	}
    }
    
    //DONE
    private void getAvatarInteractions(){
	SpriteData[] avatar = la.getAvatars(false);
	
	for(int i=0; i<avatar.length; i++){
	    for (int j = 0; j < harmfulObjects.size(); j++) {
		for (int k = 0; k < avatar[i].sprites.size(); k++) {
		    interactions.add(harmfulObjects.get(j) + " " + avatar[i].sprites.get(k) + " > killSprite scoreChange=1");
		    interactions.add(avatar[i].sprites.get(k) + " " + harmfulObjects.get(j) + " > killSprite");
		}
	    }
	}
	
	if(score != null && random.nextDouble() < killScoreProb){
	    for (int i = 0; i < avatar.length; i++) {
		for (int k = 0; k < avatar[i].sprites.size(); k++) {
		    interactions.add(score.name + " " + avatar[i].sprites.get(k) + " > killSprite scoreChange=1");
		    interactions.add(avatar[i].sprites.get(k) + " " + score.name + " > killSprite");
		}
	    }
	}
    }
    
    //DONE
    private void getPortalInteractions() {
	SpriteData[] avatar = la.getAvatars(false);
	SpriteData[] portals = la.getPortals(0, 1, true);

	for (int i = 0; i < avatar.length; i++) {
	    for (int j = 0; j < exit.size(); j++) {
		interactions.add(exit.get(j).name + " " + avatar[i].name + " > killSprite");
	    }
	}
	for (int i = 0; i < portals.length; i++) {
	    for (int j = 0; j < avatar.length; j++) {
		if (portals[i].type.equalsIgnoreCase("Portal")) {
		    interactions.add(avatar[j].name + " " + portals[i].name + " > teleportToExit");
		}
	    }
	}
    }
    
    //DONE
    private void getNPCInteractions(){
	SpriteData[] avatar = la.getAvatars(false);
	SpriteData[] npc = la.getNPCs(0, 1, false);
	
	for(int i=0; i<npc.length; i++){
	    if (npc[i].type.equalsIgnoreCase("fleeing")) {
		for(int j=0; j<npc[i].sprites.size(); j++){
		    fleeingNPCs.add(npc[i].sprites.get(j));
		    interactions.add(npc[i].name + " " + npc[i].sprites.get(j) + " > killSprite scoreChange=1");
		}
	    } 
	    else if (npc[i].type.equalsIgnoreCase("bomber") || npc[i].type.equalsIgnoreCase("randombomber")) {
		for(int j=0; j<avatar.length; j++){
		    harmfulObjects.add(npc[i].name);
		    interactions.add(avatar[j].name + " " + npc[i].name + " > killSprite");
		}
		for(int j=0; j<npc[i].sprites.size(); j++){
		    harmfulObjects.add(npc[i].sprites.get(j));
		    interactions.add(avatar[j].name + " " + npc[i].sprites.get(j) + " > killSprite");
		}
	    } 
	    else if (npc[i].type.equalsIgnoreCase("chaser") || npc[i].type.equalsIgnoreCase("AlternateChaser")
		    || npc[i].type.equalsIgnoreCase("RandomAltChaser")) {
		for(int j=0; j<npc[i].sprites.size(); j++){
		    if(isAvatar(npc[i].sprites.get(j))){
			for(int k=0; k<avatar.length; k++){
			    harmfulObjects.add(npc[i].name);
			    interactions.add(avatar[k].name + " " + npc[i].name + " > killSprite");
			}
		    }
		    else{
			if(random.nextDouble() < doubleNPCsProb){
			    interactions.add(npc[i].sprites.get(j) + " " + npc[i].name + " > killSprite");
			}
			else{
			    interactions.add(npc[i].sprites.get(j) + " " + npc[i].name + " > transformTo stype=" + npc[i].name);
			}
			
		    }
		}
	    } 
	    else if (npc[i].type.equalsIgnoreCase("randomnpc")) {
		for(int j=0; j<avatar.length; j++){
		    harmfulObjects.add(npc[i].name);
		    interactions.add(avatar[j].name + " " + npc[i].name + " > killSprite");
		}
	    }
	}
    }
    
    //DONE
    private void getMovableInteractions(){
	SpriteData[] movables = la.getMovables(0, 1, false);
	SpriteData[] avatar = la.getAvatars(false);
	SpriteData[] spawners = la.getSpawners(0, 1, false);
	
	for(int j=0; j<movables.length; j++){
	    boolean found = false;
	    for(int i=0; i<avatar.length; i++){
		if(avatar[i].sprites.contains(movables[j].name)){
		    found = true;
		}
	    }
	    for(int i=0; i<spawners.length; i++){
		if(spawners[i].sprites.contains(movables[j].name)){
		    found = true;
		}
	    }
	    if(!found){
		if(random.nextDouble() < usefulMovableProb){
		    for(int i=0; i<avatar.length; i++){
			interactions.add(avatar[i].name + " " + movables[j].name + " > killSprite");
		    }
		}
		else{
		    for(int i=0; i<avatar.length; i++){
			interactions.add(movables[j].name + " " + avatar[i].name + " > killSprite scoreChange=1");
		    }
		}
	    }
	}
    }
    
    //DONE
    private void getTerminations(){
	SpriteData[] avatar = la.getAvatars(false);
	if(exit.size() > 0){
	    SpriteData door = null;
	    for(int i=0; i<exit.size(); i++){
		if(exit.get(i).type.equalsIgnoreCase("door")){
		    door = exit.get(i);
		    break;
		}
	    }
	    if(door != null){
		terminations.add("SpriteCounter stype=" + door.name + " limit=0 win=True");
	    }
	    else{
		terminations.add("SpriteCounter stype=" + exit.get(random.nextInt(exit.size())).name + " limit=0 win=True");
	    }
	}
	else {
	    if (fleeingNPCs.size() > 0) {
		terminations.add("SpriteCounter stype=" + fleeingNPCs.get(0) + " limit=0 win=True");
	    } 
    	    else {
    		terminations.add("Timeout limit=" + (500 + random.nextInt(5) * 100) + " win=True");
    	    }
	}
	
	if(harmfulObjects.size() > 0){
	    for(int i=0; i<avatar.length; i++){
		terminations.add("SpriteCounter stype=" + avatar[i].name + " limit=0 win=False");
	    }
	}
    }
    
    @Override
    public String[][] generateRules(SLDescription sl, ElapsedCpuTimer time) {
	this.getResourceInteractions();
	this.getImmovableInteractions();
	this.getNPCInteractions();
	this.getSpawnerInteractions();
	this.getPortalInteractions();
	this.getWallInteractions();
	this.getMovableInteractions();
	this.getAvatarInteractions();
	
	this.getTerminations();
	
	return new String[][]{convertToStringArray(interactions), convertToStringArray(terminations)};
    }

}
