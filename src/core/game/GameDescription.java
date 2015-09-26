package core.game;

import java.util.ArrayList;
import java.util.HashMap;

import core.VGDLRegistry;
import core.termination.Termination;
import ontology.Types;
import ontology.avatar.MovingAvatar;
import ontology.effects.Effect;

/**
 * This is an abstract class encapsulating all the data required for generating
 * and testing game levels.
 * @author Ahmed A Khalifa
 */
public class GameDescription {

	/**
	 * object from the current loaded game. This object is used to initialize
	 * other fields and get interaction data.
	 */
	private Game currentGame;
	
	/**
	 * abstract data about main avatar
	 */
	private SpriteData avatar;
	
	/**
	 * list of all allowed actions by the avatar including the NIL action
	 */
	private ArrayList<Types.ACTIONS> actionsNIL;
	
	/**
	 * list of all allowed actions by the avatar
	 */
	private ArrayList<Types.ACTIONS> actions;
	
	/**
	 * list of sprite data for all npc labeled objects
	 */
	private ArrayList<SpriteData> npcList;
	
	/**
	 * 
	 */
	private ArrayList<SpriteData> portalList;
	
	/**
	 * 
	 */
	private ArrayList<SpriteData> resourceList;
	
	/**
	 * 
	 */
	private ArrayList<SpriteData> staticList;
	
	/**
	 * 
	 */
	private ArrayList<SpriteData> movingList;
	
	/**
	 * 
	 */
	private ArrayList<TerminationData> terminationData;
	
	/**
	 * Constructor to the Game Description. It initialize all the data using
	 * the passed game object.
	 * @param currentGame	The current running game object.
	 */
	public GameDescription(Game currentGame){
		this.currentGame = currentGame;
		this.npcList = new ArrayList<SpriteData>();
		this.portalList = new ArrayList<SpriteData>();
		this.resourceList = new ArrayList<SpriteData>();
		this.staticList = new ArrayList<SpriteData>();
		this.movingList = new ArrayList<SpriteData>();
		
		ArrayList<SpriteData> allSprites = this.currentGame.getSpriteData();
		for (SpriteData sd:allSprites){
			if(sd.isAvatar){
				avatar = sd;
			}
			else if(sd.isNPC){
				npcList.add(sd);
			}
			else if(sd.isPortal){
				portalList.add(sd);
			}
			else if(sd.isResource){
				resourceList.add(sd);
			}
			else if(sd.isStatic){
				staticList.add(sd);
			}
			else{
				movingList.add(sd);
			}
		}
		
		MovingAvatar temp = (MovingAvatar)currentGame.getTempAvatar();
		actionsNIL = temp.actionsNIL;
		actions = temp.actions;
		
		terminationData = currentGame.getTerminationData();
	}
	
	public StateObservation testLevel(String level){
		String[] lines = level.split("\n");
		currentGame.buildStringLevel(lines);
		
		return currentGame.getObservation();
	}
	
	public ArrayList<Types.ACTIONS> getAvailableActions(boolean includeNIL){
		if(includeNIL){
			return actionsNIL;
		}
		
		return actions;
	}
	
	public SpriteData getAvatar(){
		return avatar;
	}
	
	public ArrayList<SpriteData> getNPC(){
		return npcList;
	}
	
	public ArrayList<SpriteData> getStatic(){
		return staticList;
	}
	
	public ArrayList<SpriteData> getResource(){
		return resourceList;
	}
	
	public ArrayList<SpriteData> getPortal(){
		return portalList;
	}
	
	public ArrayList<SpriteData> getAllSpriteData(){
		ArrayList<SpriteData> result = new ArrayList<SpriteData>();
		result.add(avatar);
		
		return result;
	}
	
	public ArrayList<GameDescription.InteractionData> getInteraction(String stype1, String stype2){
		int itype1 = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stype1);
		int itype2 = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stype2);
		
		return currentGame.getInteractionData(itype1, itype2);
	}
	
	public ArrayList<GameDescription.TerminationData> getTerminationConditions(){
		return terminationData;
	}
	
	public HashMap<Character, ArrayList<String>> getLevelMapping(){
		return (HashMap<Character, ArrayList<String>>)currentGame.getCharMapping().clone();
	}
	
	public static class SpriteData{
		public String type;
		public String name;
		public ArrayList<String> sprites;
		
		public boolean isAvatar;
		public boolean isNPC;
		public boolean isPortal;
		public boolean isResource;
		public boolean isStatic;
		
		public SpriteData(){
			sprites = new ArrayList<String>();
		}
		
		@Override
		public String toString(){
			return name + ":" + type + " " + sprites.toString();
		}
	}
	
	public static class TerminationData{
		public String type;
		public ArrayList<String> sprites;
		public int limit;
		public boolean win;
		
		public TerminationData(){
			sprites = new ArrayList<String>();
		}
		
		@Override
		public String toString(){
			String result = win?"Win":"Lose";
			result += ":" + type + " " + sprites.toString();
			return result;
		}
	}
	
	public static class InteractionData{
		public String type;
		public int scoreChange;
	}
}
