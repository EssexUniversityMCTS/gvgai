package core.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


import core.logging.Logger;
import core.logging.Message;

import core.vgdl.VGDLRegistry;
import ontology.Types;
import ontology.avatar.MovingAvatar;

/**
 * This is an abstract class encapsulating all the data required for generating
 * and testing game levels.
 *
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
	private ArrayList<SpriteData> avatar;

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
	 * list of sprite data for all portal labeled objects
	 */
	private ArrayList<SpriteData> portalList;

	/**
	 * list of sprite data for all resource labeled objects
	 */
	private ArrayList<SpriteData> resourceList;

	/**
	 * list of sprite data for all static labeled objects
	 */
	private ArrayList<SpriteData> staticList;

	/**
	 * list of sprite data that is not labeled any of the previous
	 */
	private ArrayList<SpriteData> movingList;

	/**
	 * list of termination data objects that supplies game termination
	 * conditions
	 */
	private ArrayList<TerminationData> terminationData;

	/**
	 *
	 */
	private HashMap<Character, ArrayList<String>> charMapping;

	/**
	 * Constructor to the Game Description. It initialize all the data using the
	 * passed game object.
	 *
	 * @param currentGame
	 *            The current running game object.
	 */
	public GameDescription(Game currentGame) {
		this.currentGame = currentGame;
		this.avatar = new ArrayList<SpriteData>();
		this.npcList = new ArrayList<SpriteData>();
		this.portalList = new ArrayList<SpriteData>();
		this.resourceList = new ArrayList<SpriteData>();
		this.staticList = new ArrayList<SpriteData>();
		this.movingList = new ArrayList<SpriteData>();
		this.charMapping = currentGame.getCharMapping();

		reset(currentGame);
	}

	private boolean checkHaveInteraction(String stype) {
		ArrayList<SpriteData> allSprites = currentGame.getSpriteData();
		for (SpriteData sprite : allSprites) {
			if (getInteraction(stype, sprite.name).size() > 0) {
				return true;
			}
			if (getInteraction(sprite.name, stype).size() > 0) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Reset the game description object and assign new game object
	 *
	 * @param currentGame
	 *            new game object assigned
	 */
	public void reset(Game currentGame) {
		this.currentGame = currentGame;
		this.avatar.clear();
		this.npcList.clear();
		this.portalList.clear();
		this.resourceList.clear();
		this.staticList.clear();
		this.movingList.clear();
		this.charMapping = currentGame.getCharMapping();

		ArrayList<SpriteData> allSprites = this.currentGame.getSpriteData();
		for (SpriteData sd : allSprites) {
			if (sd.isAvatar) {
				if (checkHaveInteraction(sd.name)) {
					avatar.add(sd);
				}
			} else if (sd.isNPC) {
				npcList.add(sd);
			} else if (sd.isPortal) {
				portalList.add(sd);
			} else if (sd.isResource) {
				resourceList.add(sd);
			} else if (sd.isStatic) {
				staticList.add(sd);
			} else {
				movingList.add(sd);
			}
		}

		for (int i = 0; i < avatar.size(); i++) {
			MovingAvatar temp = (MovingAvatar) currentGame.getTempAvatar(avatar.get(i));
			if (actions == null || actions.size() < temp.actions.size()) {
				actionsNIL = temp.actionsNIL;
				actions = temp.actions;
			}
		}

		terminationData = currentGame.getTerminationData();
	}

	/**
	 * Build the generated level to be tested using an agent using the original
	 * Level Mapping.
	 *
	 * @param level
	 *            a string of characters that are supplied in the character
	 *            mapping
	 * @return StateObservation object that can be used to simulate the game.
	 * 				return null when there is errors
	 */
	public StateObservation testLevel(String level) {
		return testLevel(level, null);
	}

	/**
	 * Build the generated level to be tested using an agent. You should call
	 * this version if you are using your own character mapping
	 *
	 * @param level
	 *            a string of characters that are supplied in the character
	 *            mapping
	 * @return StateObservation object that can be used to simulate the game.
	 * 				return null when there is errors
	 */
	public StateObservation testLevel(String level, HashMap<Character, ArrayList<String>> charMapping) {
		Logger.getInstance().flushMessages();

		if (charMapping != null) {
			currentGame.setCharMapping(charMapping);
		}
		String[] lines = level.split("\n");
		currentGame.reset();
		currentGame.buildStringLevel(lines, new Random().nextInt());
		currentGame.setCharMapping(this.charMapping);

		if(Logger.getInstance().getMessageCount(1) > 0){
			return null;
		}
		return currentGame.getObservation();
	}

	/**
	 * get list of errors and warnings from the system
	 *
	 * @return a list of errors and warnings
	 * 		type 0: warnings
	 * 		type 1: errors
	 */
	public ArrayList<Message> getErrors(){
		return Logger.getInstance().getMessages();
	}

	/**
	 * Get player supported actions
	 *
	 * @param includeNIL
	 *            boolean to identify if the NIL action should exists in the
	 *            supported actions
	 * @return list of all player supported actions
	 */
	public ArrayList<Types.ACTIONS> getAvailableActions(boolean includeNIL) {
		if (includeNIL) {
			return actionsNIL;
		}

		return actions;
	}

	/**
	 * Get avatar sprite data information
	 *
	 * @return avatar's sprite data
	 */
	public ArrayList<SpriteData> getAvatar() {
		return avatar;
	}

	/**
	 * Get NPCs sprite data information
	 *
	 * @return array of sprite data
	 */
	public ArrayList<SpriteData> getNPC() {
		return npcList;
	}

	/**
	 * Get Statics sprite data information
	 *
	 * @return array of sprite data
	 */
	public ArrayList<SpriteData> getStatic() {
		return staticList;
	}

	/**
	 * Get Resources sprite data information
	 *
	 * @return array of sprite data
	 */
	public ArrayList<SpriteData> getResource() {
		return resourceList;
	}

	/**
	 * Get Portals sprite data information
	 *
	 * @return array of sprite data
	 */
	public ArrayList<SpriteData> getPortal() {
		return portalList;
	}

	/**
	 * Get all movable sprites
	 *
	 * @return arary of sprite data
	 */
	public ArrayList<SpriteData> getMoving() {
		return movingList;
	}

	/**
	 * Get all defined game sprites
	 *
	 * @return an array of sprite data
	 */
	public ArrayList<SpriteData> getAllSpriteData() {
		ArrayList<SpriteData> result = new ArrayList<SpriteData>();
		result.addAll(avatar);
		result.addAll(npcList);
		result.addAll(resourceList);
		result.addAll(staticList);
		result.addAll(portalList);
		result.addAll(movingList);

		return result;
	}

	/**
	 * Get a list of all effects happening to the first sprite
	 *
	 * @param stype1
	 *            the sprite name of the first sprite in the collision
	 * @param stype2
	 *            the sprite name of the second sprite in the collision
	 * @return an array of all possible effects. If there is no effects, an
	 *         empty array is returned
	 */
	public ArrayList<GameDescription.InteractionData> getInteraction(String stype1, String stype2) {
		int itype1 = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stype1);
		int itype2 = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stype2);

		return currentGame.getInteractionData(itype1, itype2);
	}

	/**
	 * Get a list of all termination conditions for the current game
	 *
	 * @return an array of termination data objects
	 */
	public ArrayList<GameDescription.TerminationData> getTerminationConditions() {
		return terminationData;
	}

	/**
	 * Get default character mapping
	 *
	 * @return hashmap of level characters and their corresponding sprites
	 */
	public HashMap<Character, ArrayList<String>> getLevelMapping() {
		return charMapping;
	}

	/**
	 * Simple data class represents all game sprites
	 */
	public static class SpriteData implements Cloneable {
		private HashMap<String, String> parameters;

		/**
		 * VGDL class type for the current sprite
		 */
		public String type;

		/**
		 * Sprite name
		 */
		public String name;

		/**
		 * List of all dependent sprite names
		 */
		public ArrayList<String> sprites;

		public boolean isSingleton;

		public boolean isAvatar;
		public boolean isNPC;
		public boolean isPortal;
		public boolean isResource;
		public boolean isStatic;

		public SpriteData(HashMap<String, String> parameters) {
			this.sprites = new ArrayList<String>();
			this.parameters = parameters;
		}

		@Override
		public String toString() {
			String reset = "";
			for(String key:parameters.keySet()){
				reset += " " + key + "=" + parameters.get(key);
			}
			return name + " > " + type + " " + reset;
		}

		/**
		 * Used in encode the names sprites
		 * @param oldName	the current name for the sprite
		 * @param newName	the new name for the sprite
		 */
		public void changeSpriteName(String oldName, String newName){
			if(name.equalsIgnoreCase(oldName)){
				name = newName;
			}
			for(int i=0; i<sprites.size(); i++){
				if(sprites.get(i).equalsIgnoreCase(oldName)){
					sprites.set(i, newName);
				}
			}
			for(String key:parameters.keySet()){
				if(parameters.get(key).equalsIgnoreCase(oldName)){
					parameters.put(key, newName);
				}
			}
		}

		@Override
		protected Object clone() throws CloneNotSupportedException {
			SpriteData s = new SpriteData(this.parameters);
			s.type = type;
			s.name = name;
			for (int i = 0; i < sprites.size(); i++) {
				s.sprites.add(sprites.get(i));
			}
			s.isSingleton = isSingleton;
			s.isAvatar = isAvatar;
			s.isNPC = isNPC;
			s.isPortal = isPortal;
			s.isResource = isResource;
			s.isStatic = isStatic;

			return s;
		}
	}

	/**
	 * Simple data class represents all game termination conditions
	 */
	public static class TerminationData {
		/**
		 * Termination Condition type
		 */
		public String type;

		/**
		 * Array of all dependent sprite names
		 */
		public ArrayList<String> sprites;

		/**
		 * Condition Limit
		 */
		public int limit;

		/**
		 * String containing booleans to differentiate between Winning or Losing
		 * Condition
		 */
		public String win;

		public TerminationData() {
			sprites = new ArrayList<String>();
		}

		/**
		 * Player ID for win state used is 0, default for single player games.
		 *
		 * @return
		 */
		@Override
		public String toString() {
			String[] winners = win.split(",");
			String result = Boolean.parseBoolean(winners[0]) ? "Win" : "Lose";
			result += ":" + type + " " + sprites.toString();
			return result;
		}
	}

	/**
	 * Simple data class represents the interaction between game sprites
	 */
	public static class InteractionData {
		/**
		 * Interaction class type
		 */
		public String type;

		/**
		 * The amount of score this interaction changes
		 */
		public String scoreChange;

		/**
		 * All the depending sprites on that
		 */
		public ArrayList<String> sprites;

		public InteractionData() {
			sprites = new ArrayList<String>();
		}

		@Override
		public String toString() {
			return type + " (" + scoreChange + ") " + sprites.toString();
		}
	}
}
