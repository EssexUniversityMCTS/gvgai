package controllers.YOLOBOT.Util.Wissensdatenbank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

import controllers.YOLOBOT.Agent;
import controllers.YOLOBOT.YoloState;
import controllers.YOLOBOT.Util.SimpleState;
import ontology.Types;
import ontology.Types.ACTIONS;
import ontology.Types.WINNER;
import tools.Vector2d;
import core.game.Event;
import core.game.Observation;

public class YoloKnowledge {

	public static final int RESSOURCE_MAX = 100;
	public static final int ITYPE_MAX_COUNT = 100;
	public static final int INDEX_MAX = 32;
	public static final int AXIS_X = 0;
	public static final int AXIS_Y = 1;
	public static final int AXIS_VALUE_NOT_CHANGE_INDEX = 2;
	public static final int  FULL_INT_MASK = 0b1111_1111__1111_1111__1111_1111__1111_1111;
	private static final boolean DEBUG = false;
	public static final Vector2d ORIENTATION_NULL = new Vector2d(0, 0);
	public static final Vector2d ORIENTATION_UP = new Vector2d(0, -1);
	public static final Vector2d ORIENTATION_DOWN = new Vector2d(0, 1);
	public static final Vector2d ORIENTATION_LEFT = new Vector2d(-1, 0);
	public static final Vector2d ORIENTATION_RIGHT = new Vector2d(1, 0);

	private LinkedList<Integer> playerITypes;
	private LinkedList<Integer> pushableITypes;

	public static YoloKnowledge instance;
	private byte[] agentMoveControlCounter;
	private byte[] agentItypeCounter;
	private byte[] ressourceIndexMap;
	private int[] ressourceIndexReverseMap;
	private byte[] itypeIndexMap;
	private int[] itypeIndexReverseMap;
	private int[] extraPlayerItypeIndexMap;
	private int[] extraPlayerItypeIndexReverseMap;
	private byte firstFreeRessourceIndex;
	private byte firstFreeItypeIndex;
	private boolean[] isPlayerIndex;
	private boolean[] isPushableIndex;
	private byte[] inventoryMax;
	private boolean[] inventoryIsMax;
	private byte[] pushTargetIndex;
	private byte[] portalExitToEntryItypeMap;
	private byte[] objectCategory;
	private boolean[] isStochasticEnemy;
	private boolean[] isDynamic;
	private int dynamicMask;
	private int playerIndexMask;
	private boolean[][] hasBeenAliveAt;
	private byte[] spawnerOf;
	private boolean[] spawnerInfoSure;
	private byte[] spawnedBy;
	private boolean[] useEffektIsSingleton;
	private byte[] useEffektToSpawnIndex;

	private byte[][] maxMovePerNPC_PerAxis;
	private byte[] npcMoveModuloTicks;
	private boolean haveEverGotScoreWithoutWinning;
	
	private int fromAvatarMask;
	
	private YoloState initialState;

	public boolean learnDeactivated;

	/**
	 * Die Events der Objekte, die schon da waren (sich nicht bewegt haben um
	 * die kollision aufzurufen)
	 */
	private YoloEventController[][] passiveObjectEffects;
	/**
	 * Die Events der Objekte, die die Kollision durch ihre Bewegung ausgeloest
	 * haben
	 */
	private YoloEventController[][] activeObjectEffects;
	private PlayerUseEvent[][] useEffects;
	
	/**
	 * 0er Bit bedeutet: Hier wird sicher nicht geblockt!<br>
	 * 	Das heisst insbesondere: Hier ist kein rekusiver Push-Stein!
	 */
	private int[] blockingMaskTheorie;
	/**
	 * 1er Bit bedeutet: Hier kann sicher gepushed werden! 
	 */
	private int[] pushingMaskTheorie;
	private final int MAX_X, MAX_Y;
	/**
	 * Bestimmt, ob ein push von einen Objekt (gelernt werden kann, dass) mehr als zwei Objekte gepushed werden koennen.<br>
	 * Achtung: Erhoeht Lernaufwand (enorm)
	 */
	private boolean searchMultiplePushes = false;
	private int stochasticNpcCount;
	private boolean minusScoreIsBad;

	public YoloKnowledge(YoloState startState) {
		instance = this;
		learnDeactivated = false;
		initialState = startState;
		stochasticNpcCount = 0;
		MAX_X = startState.getWorldDimension().width / startState.getBlockSize();
		MAX_Y = startState.getWorldDimension().height / startState.getBlockSize();
		minusScoreIsBad = true;
		
		// Init mapping-maps
		ressourceIndexMap = new byte[RESSOURCE_MAX];
		itypeIndexMap = new byte[ITYPE_MAX_COUNT];
		extraPlayerItypeIndexMap = new int[ITYPE_MAX_COUNT];
		
		ressourceIndexReverseMap = new int[INDEX_MAX];
		itypeIndexReverseMap = new int[INDEX_MAX];
		extraPlayerItypeIndexReverseMap = new int[INDEX_MAX];
		isPlayerIndex = new boolean[INDEX_MAX];
		isPushableIndex = new boolean[INDEX_MAX];
		pushTargetIndex = new byte[INDEX_MAX];
		portalExitToEntryItypeMap = new byte[INDEX_MAX];
		objectCategory = new byte[INDEX_MAX];
		inventoryMax = new byte[INDEX_MAX];
		inventoryIsMax = new boolean[INDEX_MAX];
		isDynamic = new boolean[INDEX_MAX];
		npcMoveModuloTicks = new byte[INDEX_MAX];
		spawnerOf = new byte[INDEX_MAX];
		spawnedBy = new byte[INDEX_MAX];
		spawnerInfoSure = new boolean[INDEX_MAX];
		hasBeenAliveAt = new boolean[INDEX_MAX][INDEX_MAX];
		useEffektIsSingleton = new boolean[INDEX_MAX];
		useEffektToSpawnIndex = new byte[INDEX_MAX];
		agentMoveControlCounter = new byte[INDEX_MAX];
		agentItypeCounter = new byte[INDEX_MAX];
		
		firstFreeRessourceIndex = 0;
		firstFreeItypeIndex = 0;
		
		dynamicMask = 0;
		
		for (int i = 0; i < RESSOURCE_MAX; i++) {
			ressourceIndexMap[i] = -1;
		}
		for (int i = 0; i < ITYPE_MAX_COUNT; i++) {
			itypeIndexMap[i] = -1;
			extraPlayerItypeIndexMap[i] = -1;
		}
		for (int i = 0; i < INDEX_MAX; i++) {
			ressourceIndexReverseMap[i] = -1;
			itypeIndexReverseMap[i] = -1;
			extraPlayerItypeIndexReverseMap[i] = -1;
			pushTargetIndex[i] = -1;
			portalExitToEntryItypeMap[i] = -1;
			objectCategory[i] = -1;
			inventoryMax[i] = -1;
			spawnerOf[i] = -1;
			spawnedBy[i] = -1;
			useEffektToSpawnIndex[i] = -1;
			useEffektIsSingleton[i] = true;
			npcMoveModuloTicks[i] = (byte) 0b1000_0000;
		}
		
		passiveObjectEffects = new YoloEventController[INDEX_MAX][INDEX_MAX];
		activeObjectEffects = new YoloEventController[INDEX_MAX][INDEX_MAX];
		useEffects = new PlayerUseEvent[INDEX_MAX][INDEX_MAX];
		blockingMaskTheorie = new int[INDEX_MAX];
		pushingMaskTheorie = new int[INDEX_MAX];
		
		for (int i = 0; i < blockingMaskTheorie.length; i++) {
			blockingMaskTheorie[i] = FULL_INT_MASK;
		}
		
		playerITypes = new LinkedList<Integer>();
		pushableITypes = new LinkedList<Integer>();
		
		learnObjectCategories(startState);
		
		maxMovePerNPC_PerAxis = new byte[INDEX_MAX][3];
		isStochasticEnemy = new boolean[INDEX_MAX];
		
		learnStochasticEffekts(initialState);
	}

	public void learnStochasticEffekts(YoloState state) {
		//Learn NPC Movement:
		//Save old pos:
		HashMap<Integer, Vector2d> map = new HashMap<Integer, Vector2d>();
		if(state.getNpcPositions() == null || state.getNpcPositions().length <= stochasticNpcCount)
			return;
		for (int iteration = 0; iteration < 10; iteration++) {
			boolean haveNonStochasticEnemy = false;
			state.setNewSeed((int)(Math.random()*10000));
			YoloState folgezustand = state.copyAdvanceLearn(ACTIONS.ACTION_NIL);
			ArrayList<Observation>[] nowNpcs = folgezustand.getNpcPositions();
			if(nowNpcs != null){
				for (int npcNr = 0; npcNr < nowNpcs.length; npcNr++) {
					if(!nowNpcs[npcNr].isEmpty() && !nowNpcs[npcNr].isEmpty()){
						//Gibt npcs dieses Typs!
						//Check map:
						int itypeIndex = itypeToIndex(nowNpcs[npcNr].get(0).itype);
						if(!isStochasticEnemy[itypeIndex]){
							//Bisher nicht als stochastisch erkannt:
							for (int i = 0; i < nowNpcs[npcNr].size(); i++) {
								Observation obs = nowNpcs[npcNr].get(i);
								if(iteration == 0){
									map.put(obs.obsID, obs.position);
								}else{
									Vector2d referenceVector = map.get(obs.obsID);
									if(referenceVector != null){	//NPC koennte durch andere stochastische Effekte, die nicht er ausgeloest hat sterben!
										if(!referenceVector.equals(obs.position)){
											//NPC stochastic movement detected!
											isStochasticEnemy[itypeIndex] = true;
											stochasticNpcCount++;
											break;
										}
									}								
								}
							}
						}
						//Iteration fuer diesen Itype durchgelaufen
						if(isStochasticEnemy[itypeIndex])
							haveNonStochasticEnemy = true;		//Merke, dass es einen nicht stochastischen Gegner gab!
					}
				}
			}
			if(!haveNonStochasticEnemy && iteration != 0)
				break;
		}
		if(!Agent.UPLOAD_VERSION)
			System.out.println("Stochastische NPCs: " + stochasticNpcCount);
		
	}

	public void learnObjectCategories(YoloState state) {
		learnObjectCategories(state.getImmovablePositions());
		learnObjectCategories(state.getFromAvatarSpritesPositions());
		learnObjectCategories(state.getMovablePositions());
		learnObjectCategories(state.getNpcPositions());
		learnObjectCategories(state.getPortalsPositions());
		learnObjectCategories(state.getResourcesPositions());
	}
	
	private void learnObjectCategories(ArrayList<Observation>[] list){
		if(list == null)
			return;
		for (ArrayList<Observation> observationList : list) {
			if(observationList != null && !observationList.isEmpty()){
				Observation obs = observationList.get(0);
				int index = itypeToIndex(obs.itype);
				objectCategory[index] = (byte) obs.category;
				if(obs.category == Types.TYPE_NPC){
					isDynamic[index] = true;
					dynamicMask = dynamicMask | 1 << index;
				}
				
			}
		}
	}

	public int ressourceToIndex(int ressource) {
		if (ressourceIndexMap[ressource] == -1)
			reserveRessourceIndex(ressource);
		return ressourceIndexMap[ressource];
	}

	public int indexToRessource(int index) {
		return ressourceIndexReverseMap[index];
	}

	public byte itypeToIndex(int itype) {
		if (itypeIndexMap[itype] == -1)
			reserveItypeIndex(itype);
		return itypeIndexMap[itype];
	}
	
	public int indexToItype(int index) {
		return itypeIndexReverseMap[index];
	}

	private void reserveRessourceIndex(int ressource) {
		ressourceIndexMap[ressource] = firstFreeRessourceIndex;
		ressourceIndexReverseMap[firstFreeRessourceIndex] = ressource;
		firstFreeRessourceIndex++;
	}

	private void reserveItypeIndex(int itype) {
		itypeIndexMap[itype] = firstFreeItypeIndex;
		itypeIndexReverseMap[firstFreeItypeIndex] = itype;
		firstFreeItypeIndex++;
	}

	public byte[] getInventoryArray(HashMap<Integer, Integer> inventory) {
		byte[] array = new byte[INDEX_MAX];

		for (Iterator<Integer> iterator = inventory.keySet().iterator(); iterator
				.hasNext();) {
			int itemNr = (int) iterator.next();
			int itemIndex = YoloKnowledge.instance.ressourceToIndex(itemNr);
			byte inventoryCount = (byte) (int) inventory.get(itemNr);
			array[itemIndex] = inventoryCount;
		}
		
		return array;
	}

	public void learnFrom(YoloState currentState, YoloState lastState, ACTIONS actionDone) {
		if(learnDeactivated)
			return;

		
		if(currentState.getGameTick() != lastState.getGameTick()+1){
			if(!Agent.UPLOAD_VERSION)
				System.out.println("Falsche uebergabe von States!");
			return;
		}
		
		if(lastState == null || lastState.getAvatar() == null){
			if(!Agent.UPLOAD_VERSION && DEBUG)
				System.out.println("Didnt find State or Avatar");
			return;
		}else if(currentState.getAvatar() == null || currentState.isGameOver()){
			learnGameEnd(currentState, lastState, actionDone);
			return;
		}
		
		learnNpcMovement(currentState, lastState);
		learnAlivePosition(currentState);
		learnSpawner(currentState, lastState);
		learnDynamicObjects(currentState, lastState);
		learnAgentMovement(currentState, lastState, actionDone);
		
		if(actionDone == ACTIONS.ACTION_USE)
			learnUseActionResult(currentState, lastState);
			
		
		int lastAgentItype = lastState.getAvatar().itype;
		byte[] inventory = getInventoryArray(lastState.getAvatarResources());
		int lastGameTick = lastState.getGameTick();
		TreeSet<Event> history = currentState.getEventsHistory();
    	while (history.size() > 0) {
			Event newEvent = history.pollLast();
			if(newEvent.gameStep != lastGameTick){
				break;
			}
//			if(DEBUG)
//				System.out.println("Event!");
			int passiveItype = newEvent.passiveTypeId;
			byte passiveIndex = itypeToIndex(passiveItype);
			int activeItype = newEvent.activeTypeId;
			byte activeIndex = itypeToIndex(activeItype);
			
			//Lerne PlayerIndex
			if(!isPlayerIndex[activeIndex]){
				//Dieser Index wurde bisher nicht mit Player in verbindung gebracht:
				isPlayerIndex[activeIndex] = true;
				playerIndexMask = playerIndexMask | 1 << activeIndex;
				playerITypes.add(activeItype);
			}
			
			if(!newEvent.fromAvatar){
				//Was the Avatar itself
				learnAgentEvent(currentState, lastState, passiveIndex, newEvent.passiveSpriteId, actionDone);
			}else{
				learnEvent(currentState, lastState, newEvent);
			}
		}
	}
	
	private void learnAgentMovement(YoloState currentState,
			YoloState lastState, ACTIONS actionDone) {
		
		if(!agentHasControlOfMovement(lastState))
			return;
		
		int lastX = lastState.getAvatarX();
		int lastY = lastState.getAvatarY();

		int currentX = currentState.getAvatarX();
		int currentY = currentState.getAvatarY();
		
		boolean fullControl = false;
		
		if(lastX == currentX && lastY == currentY){
			//Agent didnt move. Player has full control! (Imagine walls etc)
			fullControl = true;
		}else{
			//Agent should have been moved according to the action used
			
			int simpleLookaheadX = lastX + (actionDone==ACTIONS.ACTION_RIGHT?1:(actionDone==ACTIONS.ACTION_LEFT?-1:0));
			int simpleLookaheadY = lastY + (actionDone==ACTIONS.ACTION_DOWN?1:(actionDone==ACTIONS.ACTION_UP?-1:0));
			
			if(simpleLookaheadX == currentX && simpleLookaheadY == currentY){
				//Agent didnt move. Player has full control! (Imagine walls etc)
				fullControl = true;
			}
		}

		int index = itypeToIndex(lastState.getAvatar().itype);
		
		if(agentItypeCounter[index] < Byte.MAX_VALUE)
			agentItypeCounter[index]++;
		
		if(fullControl){
			//Player has full control over the agent
			if(agentMoveControlCounter[index] < Byte.MAX_VALUE)
				agentMoveControlCounter[index]++;
		}else{
			//Agent moves by itself (OR: gets teleported!)
			if(agentMoveControlCounter[index] > Byte.MIN_VALUE)
				agentMoveControlCounter[index]--;
		}
		
	}

	private void learnUseActionResult(YoloState currentState,
			YoloState lastState) {
		if(currentState.isGameOver())
			return;
		SimpleState simpleBefore = lastState.getSimpleState();
		int avatarItype = currentState.getAvatar().itype;
		ArrayList<Observation>[] fromAvatars = currentState.getFromAvatarSpritesPositions();
		if(fromAvatars != null){
			for (ArrayList<Observation> fromAvatarList : fromAvatars) {
				for (Observation fromAvatar : fromAvatarList) {
					Observation oldFromAvatar = simpleBefore.getObservationWithIdentifier(fromAvatar.obsID);
					if(oldFromAvatar == null){
						//Spawned this Object!
						byte index = itypeToIndex(fromAvatar.itype);
						useEffektToSpawnIndex[itypeToIndex(avatarItype)] = index;
						fromAvatarMask |= 1 << index;
						if(fromAvatarList.size()>1)
							useEffektIsSingleton[avatarItype] = false;
					}
				}
			}
		}else{
			useEffektToSpawnIndex[itypeToIndex(avatarItype)] = -1;
		}
	}

	private void learnDynamicObjects(YoloState currentState, YoloState lastState) {
		SimpleState simpleBefore = lastState.getSimpleState();
		
		for (int category = 0; category < 7; category++) {	//For all types
			if(category == Types.TYPE_AVATAR || category == Types.TYPE_FROMAVATAR)
				continue;	//Avatar things are not interpreted as dynamic!
			
			ArrayList<Observation>[] obsByCategory = currentState.getObservationList(category);
			if(obsByCategory != null){
				for (int i = 0; i < obsByCategory.length; i++) {
					ArrayList<Observation> list = obsByCategory[i];
					if(list != null && !list.isEmpty()){
						int index = itypeToIndex(list.get(0).itype);
						if(!isDynamic[index]){
							simpleBefore.fullInit();
							int beforeCountGuess = 0;
							for (Observation obs : list) {
								beforeCountGuess++;
								Observation before = simpleBefore.getObservationWithIdentifier(obs.obsID);
								if(before == null){
									beforeCountGuess--;
									isDynamic[index] = true;
									dynamicMask = dynamicMask | 1 << index;
								}else if(!before.position.equals(obs.position)){
									isDynamic[index] = true;
									dynamicMask = dynamicMask | 1 << index;
								}
							}
							
							if(beforeCountGuess != simpleBefore.getItypeOccurenceCount(index)){
								//Some Objects have vanished!
								isDynamic[index] = true;
								dynamicMask = dynamicMask | 1 << index;
							}
						}
					}
				}
			}
		}
		
	}

	private void learnSpawner(YoloState currentState, YoloState lastState) {
		int maxBefore = lastState.getMaxObsId();
		if(maxBefore == -1){
			maxBefore = getMaxObsId(lastState);
			lastState.setMaxObsId(maxBefore);
		}

		SimpleState simpleBefore = lastState.getSimpleState();
		ArrayList<Observation> spawns = getObservationsWithIdBiggerThan(currentState, maxBefore);
		int blockSize = currentState.getBlockSize();
		for (Observation observation : spawns) {
			byte index = itypeToIndex(observation.itype);
			if(spawnedBy[index] != -1 && spawnerInfoSure[spawnedBy[index]])
				continue;
			int spawnX = (int) (observation.position.x/blockSize);
			int spawnY = (int) (observation.position.y/blockSize);
			int mask = simpleBefore.getMask(spawnX, spawnY);
			byte  spawnerItypeIndex = (byte) Integer.numberOfTrailingZeros(mask);
			boolean onlyOneSpawnerPossible = Integer.numberOfLeadingZeros(mask)+spawnerItypeIndex == 31;
			boolean isGoodGuess = false;
			if(!onlyOneSpawnerPossible && positionAufSpielfeld(spawnX, spawnY)){
				//Suche Portals 
				ArrayList<Observation> obsList = lastState.getObservationGrid()[spawnX][spawnY];
				int portalsCount = 0;
				Observation lastPortal = null;
				for (Observation possibleSpawnObs : obsList) {
					if(possibleSpawnObs.category == Types.TYPE_PORTAL){
						portalsCount++;
						lastPortal = possibleSpawnObs;
						byte possibleSpawnItypeIndex = itypeToIndex(possibleSpawnObs.itype);
						if(spawnedBy[possibleSpawnItypeIndex] == -1){
							spawnerItypeIndex = possibleSpawnItypeIndex;
							isGoodGuess = true;
							break;
						}
					}
				}
				if(!isGoodGuess && lastPortal != null && portalsCount == 1){
					//No 'free' portal found, but only one --> choose this and override info!
					spawnerItypeIndex = itypeToIndex(lastPortal.itype);
					isGoodGuess = true;
				}
			}
			if(onlyOneSpawnerPossible || isGoodGuess){
				//Only one bit is set (One itype only on this field)
				
				//Check if something disappered next to spawn:
				ArrayList<Observation> nearObservations = new ArrayList<Observation>();
				ArrayList<Observation>[][] grid = lastState.getObservationGrid();
				if(positionAufSpielfeld(spawnX-1, spawnY))
					nearObservations.addAll(grid[spawnX-1][spawnY]);
				if(positionAufSpielfeld(spawnX+1, spawnY))
					nearObservations.addAll(grid[spawnX+1][spawnY]);
				if(positionAufSpielfeld(spawnX, spawnY-1))
					nearObservations.addAll(grid[spawnX][spawnY-1]);
				if(positionAufSpielfeld(spawnX, spawnY+1))
					nearObservations.addAll(grid[spawnX][spawnY+1]);

				SimpleState simpleNow = currentState.getSimpleState();
				boolean nothingGone = true;
				for (Observation nearObs : nearObservations) {
					if(simpleNow.getObservationWithIdentifier(nearObs.obsID) == null)
						nothingGone = false;
//					else{
//						for (ArrayList<Observation> obsLists : currentState.getObservationList(nearObs.category)) {
//							if(obsLists != null && !obsLists.isEmpty() && obsLists.get(0).itype == nearObs.itype){
//								boolean found = false;
//								for (Observation obsToCheck: obsLists) {
//									if(obsToCheck.obsID == nearObs.obsID)
//										found = true;
//								}
//								if(!found)
//									nothingGone = false;
//							}
//						}
//					}
//						nothingGone = false;
				}
				
				//NothingGone doesnt work cause of bugs in the forwardModel, so:
//				nothingGone &= objectCategory[spawnerItypeIndex] == Types.TYPE_PORTAL;
				
				if(nothingGone){
					if(spawnerOf[spawnerItypeIndex] != index && spawnerInfoSure[spawnerItypeIndex]){
						//Wir wissen, dass der spawner etwas anderes spawnt!
						//TODO: interaktion mit normalerweise gespawntem lernen!? 
					}else{
						spawnerOf[spawnerItypeIndex] = index;
						spawnerInfoSure[spawnerItypeIndex] = onlyOneSpawnerPossible;
						spawnedBy[index] = spawnerItypeIndex;
						isDynamic[index] = true;
						isDynamic[spawnerItypeIndex] = true;
						dynamicMask = dynamicMask | 1 << index;
						dynamicMask = dynamicMask | 1 << spawnerItypeIndex;
					}
				}
			}
		}
		
	}

	private ArrayList<Observation> getObservationsWithIdBiggerThan(
			YoloState currentState, int max) {

		int myMax = max;
		ArrayList<Observation> set = new ArrayList<Observation>();
		for (int category = 1; category < 7; category++) {
			if(category == Types.TYPE_FROMAVATAR)
				continue;
			if(category == Types.TYPE_STATIC)
				continue;
			if(category == Types.TYPE_AVATAR)
				continue;
			
			ArrayList<Observation>[] lists = currentState.getObservationList(category);
			if(lists == null)
				continue;
			
			for (ArrayList<Observation> list : lists) {
				if(list == null)
					continue;
				for (Observation observation : list) {
					if(observation.obsID > max){
						set.add(observation);
						if(myMax < observation.obsID)
							myMax = observation.obsID;
					}
				}
			}
			
		}
		
		currentState.setMaxObsId(myMax);
		
		return set;
	}

	private int getMaxObsId(YoloState lastState) {
		int max = -1;
		
		for (int category = 1; category < 7; category++) {
			if(category == Types.TYPE_FROMAVATAR)
				continue;
			if(category == Types.TYPE_STATIC)
				continue;
			if(category == Types.TYPE_AVATAR)
				continue;
			
			ArrayList<Observation>[] lists = lastState.getObservationList(category);
			if(lists == null)
				continue;
			
			for (ArrayList<Observation> list : lists) {
				if(list == null)
					continue;
				for (Observation observation : list) {
					if(observation.obsID > max)
						max = observation.obsID;
				}
			}
			
		}
		
		return max;
	}

	private void learnAlivePosition(YoloState currentState) {
		int x = currentState.getAvatarX();
		int y = currentState.getAvatarY();
		int avatarIndex = itypeToIndex(currentState.getAvatar().itype);
		byte[] inventory = currentState.getInventoryArray();
		
		for (Observation obs : currentState.getObservationGrid()[x][y]) {
			int passiveIndex = itypeToIndex(obs.itype);
			hasBeenAliveAt[avatarIndex][passiveIndex] = true;
			if(passiveIndex != avatarIndex){
				//This Observationn is not the player!
				if(activeObjectEffects[avatarIndex][passiveIndex] == null)
					activeObjectEffects[avatarIndex][passiveIndex] = new PlayerEvent();
	
				PlayerEvent pEvent = (PlayerEvent) activeObjectEffects[avatarIndex][passiveIndex];
				//if(!(isStochasticEnemy[passiveIndex] && pEvent.getEvent(inventory).getKill()))
					pEvent.update(inventory, false);
			}
		}
	}

	private void learnNpcMovement(YoloState currentState, YoloState lastState) {
		ArrayList<Observation>[] lastNpcs = lastState.getNpcPositions();
		ArrayList<Observation>[] nowNpcs = currentState.getNpcPositions();
		if(nowNpcs == null || lastNpcs == null)
			return;
		HashMap<Integer, Observation> map;
		int size = Math.min(nowNpcs.length, lastNpcs.length);
		for (int npcNr = 0; npcNr < size; npcNr++) {
			if(maxMovePerNPC_PerAxis[npcNr][AXIS_VALUE_NOT_CHANGE_INDEX] < 30000 && !lastNpcs[npcNr].isEmpty() && !nowNpcs[npcNr].isEmpty()){
				//Gibt npcs dieses Typs!
				map = new HashMap<Integer, Observation>(lastNpcs[npcNr].size());
				int itypeIndex = itypeToIndex(lastNpcs[npcNr].get(0).itype);
				//Fill map:
				for (int i = 0; i < lastNpcs[npcNr].size(); i++) {
					Observation obs = lastNpcs[npcNr].get(i);
					map.put(obs.obsID, obs);
				}
				
				//Search Pairs:
				for (int i = 0; i < nowNpcs[npcNr].size(); i++) {
					Observation now = nowNpcs[npcNr].get(i);
					Observation old = map.get(now.obsID);
					if(old == null)
						continue;
					
					//Paerchen gefunden!

					byte xMove = (byte) Math.abs(now.position.x - old.position.x);
					byte yMove = (byte) Math.abs(now.position.y - old.position.y);
					
					if((xMove != 0 || yMove != 0) && xMove <= currentState.getBlockSize() && yMove <= currentState.getBlockSize()){
	
						if(xMove > maxMovePerNPC_PerAxis[itypeIndex][AXIS_X]){
							maxMovePerNPC_PerAxis[itypeIndex][AXIS_VALUE_NOT_CHANGE_INDEX] = 0;
							maxMovePerNPC_PerAxis[itypeIndex][AXIS_X] = xMove;
						}else{
							maxMovePerNPC_PerAxis[itypeIndex][AXIS_VALUE_NOT_CHANGE_INDEX]++;
						}
	
						if(yMove > maxMovePerNPC_PerAxis[itypeIndex][AXIS_Y]){
							maxMovePerNPC_PerAxis[itypeIndex][AXIS_VALUE_NOT_CHANGE_INDEX] = 0;
							maxMovePerNPC_PerAxis[itypeIndex][AXIS_Y] = yMove;
						}else{
							maxMovePerNPC_PerAxis[itypeIndex][AXIS_VALUE_NOT_CHANGE_INDEX]++;
						}
						
						//Learn possible Enemy-Positions:
						int npcX = (int) (now.position.x/currentState.getBlockSize());
						int npcY = (int) (now.position.y/currentState.getBlockSize());
						if(positionAufSpielfeld(npcX, npcY))
							blockingMaskTheorie[itypeIndex] &= ~lastState.getSimpleState().getMask(npcX, npcY);
						
						
						//Learn moveTicks:
						byte currentMoveRule = (byte) (Integer.numberOfTrailingZeros(npcMoveModuloTicks[itypeIndex])+1);
						int remainer = (lastState.getGameTick()+1) % currentMoveRule;
						if(remainer > 0){
							//Enemy moved in a gameTick where it wasnt expected to move!
							npcMoveModuloTicks[itypeIndex] = (byte) (npcMoveModuloTicks[itypeIndex] >> (Integer.numberOfTrailingZeros(remainer)+1));
//							System.out.println("Enemy " + now.itype + " moves all " + (Integer.numberOfTrailingZeros(npcMoveModuloTicks[itypeIndex])+1) + " ticks!");
						}
					}
				}
			}
		}
	}

	private void learnGameEnd(YoloState currentState, YoloState lastState, ACTIONS action) {
//		System.out.println("Tod oder Sieg?");
		int x = lastState.getAvatarX();
		int y = lastState.getAvatarY();
		int avatarIndex = itypeToIndex(lastState.getAvatar().itype);
		Vector2d orientation = lastState.getAvatarOrientation();
		byte[] inventory = lastState.getInventoryArray();
		switch (action) {
		case ACTION_DOWN:
			if(orientation.equals(ORIENTATION_NULL) || orientation.equals(ORIENTATION_DOWN))
				y++;
			break;
		case ACTION_UP:
			if(orientation.equals(ORIENTATION_NULL) || orientation.equals(ORIENTATION_UP))
				y--;
			break;
		case ACTION_RIGHT:
			if(orientation.equals(ORIENTATION_NULL) || orientation.equals(ORIENTATION_RIGHT))
				x++;
			break;
		case ACTION_LEFT:
			if(orientation.equals(ORIENTATION_NULL) || orientation.equals(ORIENTATION_LEFT))
				x--;
			break;
		default:
			return;
		}

		if(!positionAufSpielfeld(x, y))
			return;	//Ziel ist nicht im Spielfeld!
		
		//Wurde der Spieler geblockt?
		int maskAtTargetPosition = currentState.getSimpleState().getMask(x, y);
		boolean surelyWillNotBlock = (blockingMaskTheorie[avatarIndex] & maskAtTargetPosition) == 0;
		
		boolean willMove;
		if(surelyWillNotBlock){
			willMove = true;
		}else{
			willMove = true;
			//Might Block, check PlayerEvents:
			for (Observation obs : currentState.getObservationGrid()[x][y]) {
				int index = itypeToIndex(obs.itype);
				if(activeObjectEffects[avatarIndex][index] != null){
					PlayerEvent pEvent = (PlayerEvent) activeObjectEffects[avatarIndex][index];
					if(pEvent.willCancel(inventory) && pEvent.getObserveCount() > 0)
						willMove = false;
				}
			}
		}
		if(!willMove){
			//Reset Position to old Player Position:
			x = lastState.getAvatarX();
			y = lastState.getAvatarY();			
		}
//		int blockSize = lastState.getBlockSize();
//		ArrayList<Observation>[] gridFields = lastState.getNpcPositions(new Vector2d(x*blockSize, y*blockSize));
//		Observation killOption = null;
//		int maxDistance = blockSize*blockSize*3;
//		int nearNpcs = 0;
//		if(gridFields != null){
//			for (ArrayList<Observation> gridField : gridFields) {
//				for (Observation observation : gridField) {
//					if(observation.sqDist <= maxDistance){
//						nearNpcs++;
//						killOption = observation;
//					}
//				}
//			}
//			if(nearNpcs > 1)
//				return;
//		}
		
		
		
		SimpleState lastSimpleState = lastState.getSimpleState();
		//Ermittle ob gelernt werden kann:
		int mask = currentState.getSimpleState().getMask(x, y);
		int possibleKillingMask = mask & ~(1 <<avatarIndex);	//Avatar does not kill!
		if(possibleKillingMask == 0){
			mask = lastState.getSimpleState().getMask(x, y);
			possibleKillingMask = mask & ~(1 <<avatarIndex);	//Avatar does not kill!
		}
		
		if(Integer.numberOfLeadingZeros(possibleKillingMask) + Integer.numberOfTrailingZeros(possibleKillingMask) < 31){
			//Es waren mehrere Objekte auf dem Todesfeld

			ArrayList<Observation> endObs = currentState.getObservationGrid()[x][y];
			for (Iterator<Observation> iterator = endObs.iterator(); iterator.hasNext();) {
				Observation observation = (Observation) iterator.next();
				if(lastSimpleState.getObservationWithIdentifier(observation.obsID) == null){
					//Object didnt exist lastTick!
					possibleKillingMask &= ~(1 <<itypeToIndex(observation.itype));
				}
			}
			
			if(Integer.numberOfLeadingZeros(possibleKillingMask) + Integer.numberOfTrailingZeros(possibleKillingMask) < 31)
				return;
			
			//TODO: frueherer ansatz: possibleKillingMask = possibleKillingMask & blockingMaskTheorie[avatarIndex];
		}
//		System.out.println("\t Mask = " +  Integer.toBinaryString(mask));
		int possibleEndCauseIndex = Integer.numberOfTrailingZeros(possibleKillingMask);
		if(possibleKillingMask != 0 && possibleEndCauseIndex != avatarIndex && Integer.numberOfLeadingZeros(possibleKillingMask) + possibleEndCauseIndex == 31){
			//Es ist genau ein Bit in der possibleKillingMask gesetzt.
//			System.out.println("Objekt: " + possibleKillerIndex + " mit Itype: " + indexToItype(possibleKillerIndex));
			if(activeObjectEffects[avatarIndex][possibleEndCauseIndex] == null)
				activeObjectEffects[avatarIndex][possibleEndCauseIndex] = new PlayerEvent();
	
			boolean win = currentState.getGameWinner() == WINNER.PLAYER_WINS;
			
//			int categoryMissing = getFirstCategoryWhereAnObjectIsMissingInNewState(currentState, lastState);
//			if(categoryMissing != -1){
//				//Es ist neben dem Avatar ein weiteres Objekt verschwunden!
//				Observation killer = getFirstObservationMissingOfCategory(currentState, lastState, categoryMissing);
//				if(killer != null && !getPlayerEvent(lastState.getAvatar().itype, killer.itype, false).getEvent(inventory).getKill()){
//					//Player konnte das objekt nicht toeten
//					if(Math.abs(killer.position.x/currentState.getBlockSize() - x) + Math.abs(killer.position.y/currentState.getBlockSize() - y) <= 1){
//						//Objekt war in meiner Naehe. Es wird mich gekillt haben!
//						
//						possibleEndCauseIndex = itypeToIndex(killer.itype);					
//					}
//				}
//			}
//			if(!Agent.UPLOAD_VERSION)
//				System.out.println("Ende von " + indexToItype(possibleEndCauseIndex));
			if(activeObjectEffects[avatarIndex][possibleEndCauseIndex] == null)
				activeObjectEffects[avatarIndex][possibleEndCauseIndex] = new PlayerEvent();
			PlayerEvent pEvent = (PlayerEvent) activeObjectEffects[avatarIndex][possibleEndCauseIndex];
			pEvent.learnCancelEvent(lastState.getInventoryArray(), false);
			pEvent.learnEventHappened(inventory, (byte) -1, true, (byte)0, !win, (byte)-1, (byte)-1, win, (byte)-1, (byte)-1);
		}
		
	}
	
	private Observation getFirstObservationMissingOfCategory(YoloState newState,
			YoloState lastState, int category){
		ArrayList<Observation>[] currentObs = newState.getObservationList(category);
		ArrayList<Observation>[] lastObs = lastState.getObservationList(category);
		HashSet<Integer> map = new HashSet<Integer>();
		if(currentObs != null){
			for (int index = 0; index < currentObs.length; index++) {
				if(currentObs[index] != null){
					for (Observation obs : currentObs[index]) {
						map.add(obs.obsID);
					}
				}
			}
		}

		if(lastObs == null)
			return null;
		for (int index = 0; index < lastObs.length; index++) {
			if(lastObs[index] != null){
				for (Observation obs : lastObs[index]) {
					if(!map.contains(obs.obsID))
						return obs;
				}
			}
		}
		return null;
	}

	private int getFirstCategoryWhereAnObjectIsMissingInNewState(YoloState newState,
			YoloState lastState) {
		for (int categorie = 1; categorie <= 6; categorie++) {
			ArrayList<Observation>[] currentObs = newState.getObservationList(categorie);
			ArrayList<Observation>[] lastObs = lastState.getObservationList(categorie);
			if(lastObs == null)
				continue;
			if(currentObs == null || currentObs.length < lastObs.length)
				return categorie;
			for (int index = 0; index < lastObs.length; index++) {
				if(currentObs[index].size() != lastObs[index].size())
					return categorie;
			}
		}
		return -1;
	}

	private void learnEvent(YoloState currentState, YoloState lastState,
			Event newEvent) {
		//TODO: lernen
		if(!Agent.UPLOAD_VERSION && DEBUG)
			System.out.println("Learn Event: " + newEvent.activeSpriteId + " -> " + newEvent.passiveSpriteId);

		int index = itypeToIndex(newEvent.passiveTypeId);
		int useIndex = itypeToIndex(newEvent.activeTypeId);
		boolean wall = currentState.getSimpleState().getObservationWithIdentifier(newEvent.passiveSpriteId) != null;
		if(useEffects[useIndex][index] == null){
			useEffects[useIndex][index] = new PlayerUseEvent();
		}
		PlayerUseEvent uEvent = useEffects[useIndex][index];
		byte deltaScore = (byte) (currentState.getGameScore()-lastState.getGameScore());
		if(!haveEverGotScoreWithoutWinning && deltaScore>0 && !currentState.isGameOver())
			haveEverGotScoreWithoutWinning = true;
		
		uEvent.learnTriggerEvent(deltaScore, wall);


	}

	private void learnAgentEvent(YoloState stateNow, YoloState stateBefore, byte passiveIndex, int passiveIdentifier, ACTIONS actionDone) {

		Observation avatarBefore = stateBefore.getAvatar();
		Observation avatarNow = stateNow.getAvatar();
		byte[] inventoryItemsBefore = stateBefore.getInventoryArray();
		byte[] inventoryItemsNow = stateNow.getInventoryArray();
		byte avatarIndex = itypeToIndex(avatarBefore.itype);
		
		if(!Agent.UPLOAD_VERSION && DEBUG)
			System.out.println("Learn AgentEvent: " + indexToItype(avatarIndex) + " -> " + indexToItype(passiveIndex));
//		if(indexToItype(avatarIndex) == 1 && indexToItype(passiveIndex) == 2)
//			System.out.println("Aha!");

		if(activeObjectEffects[avatarIndex][passiveIndex] == null)
			activeObjectEffects[avatarIndex][passiveIndex] = new PlayerEvent();
		if(passiveObjectEffects[avatarIndex][passiveIndex] == null)
			passiveObjectEffects[avatarIndex][passiveIndex] = new PlayerEvent();

		PlayerEvent pEvent = (PlayerEvent) activeObjectEffects[avatarIndex][passiveIndex];		
		PlayerEvent oEvent = (PlayerEvent) passiveObjectEffects[avatarIndex][passiveIndex];
		

//		if(DEBUG){
//			System.out.println("Erwartetes Event:");
//			System.out.println("\tCancel:" + pEvent.willCancel(inventoryItems));
//			System.out.println("\tSpecialEvent:" + pEvent.willTriggerSpecialEvent(inventoryItems));
//			System.out.println(pEvent.toString());
//		}
		

		
		byte itypeAvatar, teleportToItypeAvatar = -1;
		itypeAvatar = stateNow.getAvatar()!=null?(byte) stateNow.getAvatar().itype:-1;
		

		int agentBeforeX = stateBefore.getAvatarX();
		int agentBeforeY = stateBefore.getAvatarY();
		int agentNowX = stateNow.getAvatarX();
		int agentNowY = stateNow.getAvatarY();
		int agentWalkTargetX = agentBeforeX;
		int agentWalkTargetY = agentBeforeY;

		
		SimpleState simpleBefore = stateBefore.getSimpleState();
		simpleBefore.fullInit();
		SimpleState simpleNow = stateNow.getSimpleState();
		simpleNow.fullInit();
		
		
		//boolean pushExpected = (pEvent.willTriggerSpecialEvent(inventoryItems) && pEvent.getSpecialEvent().doesMove()) || (!pEvent.willTriggerSpecialEvent(inventoryItems) && pEvent.getDefaultEvent().doesMove());
		
		//	1. Hat sich der Spieler bewegt / Bewegen wollen?
		boolean wasMoveAction = actionDone == ACTIONS.ACTION_DOWN || actionDone == ACTIONS.ACTION_LEFT || actionDone == ACTIONS.ACTION_RIGHT || actionDone == ACTIONS.ACTION_UP;

		if(!stateBefore.getAvatarOrientation().equals(stateNow.getAvatarOrientation())){
			//Spieler hat 'nur' die richtung geaendert
			wasMoveAction = false;
		}
		
		if(wasMoveAction){
			//Calculate where move should have brought us to
			switch (actionDone) {
			case ACTION_DOWN:
				agentWalkTargetY++;
				break;
			case ACTION_UP:
				agentWalkTargetY--;
				break;
			case ACTION_RIGHT:
				agentWalkTargetX++;
				break;
			case ACTION_LEFT:
				agentWalkTargetX--;
				break;
			default:
				break;
			}
		}
		
		//2. Gab es score?
		byte scoreDelta = 0;
		if(stateNow.getGameScore() - stateBefore.getGameScore() > Byte.MAX_VALUE){
			scoreDelta = Byte.MAX_VALUE;
		}else{
			scoreDelta = (byte)(stateNow.getGameScore() - stateBefore.getGameScore());
		}
		
		//	3. Was ist mit dem passiv Object passiert?
			//Kill, wenn es nicht mehr da ist
		Observation passiveBefore = simpleBefore.getObservationWithIdentifier(passiveIdentifier);
		Observation passiveNow = simpleNow.getObservationWithIdentifier(passiveIdentifier); 
		boolean killPassive = (passiveNow == null);
			//Push, wenn sich die position veraendert hat
		boolean movePassive;
		boolean itypePassiveChanged;
		byte itypePassive;
		if(killPassive || passiveBefore == null){
			movePassive = false;
			itypePassiveChanged = false;
			itypePassive = (byte) -1;
		}else{
			movePassive = !passiveNow.position.equals(passiveBefore.position);
			itypePassiveChanged = passiveNow.itype != passiveBefore.itype;
			itypePassive = (byte) (itypePassiveChanged?passiveNow.itype:-1);
		}
		
		//	4. Was ist mit dem Spieler passiert?
		
		// passe WasMove an, wenn nicht auf passive gemoved wurde
		if(wasMoveAction){
			wasMoveAction &= !moveWillCancel(stateBefore, actionDone, false, true);	//Move war gegen eine Wand!
			wasMoveAction &= !(passiveBefore != null && Math.abs(agentWalkTargetX - passiveBefore.position.x / stateBefore.getBlockSize()) +  Math.abs(agentWalkTargetY - passiveBefore.position.y / stateBefore.getBlockSize()) > 1);	//Gegner konnte target nicht erreichen
			wasMoveAction &= passiveBefore == null || avatarBefore == null || !passiveBefore.position.equals(avatarBefore.position); 
		}
		
		
		Observation activeBefore = avatarBefore;//Original before Workaround: simpleBefore.getObservationWithIdentifier(avatarIdentifier); 
		Observation activeNow = avatarNow; //Original before Workaround: simpleNow.getObservationWithIdentifier(avatarIdentifier); 
		boolean killActive = (activeNow == null);
			//Move, wenn sich die position veraendert hat
		boolean moveActive, itypeActiveChanged;
		byte itypeActive;
		if(killActive || activeBefore == null){
			moveActive = false;
			itypeActiveChanged = false;
//			itypeActive = (byte) -1;
		}else{
			moveActive = wasMoveAction?!activeNow.position.equals(activeBefore.position):false;
			itypeActiveChanged = activeNow.itype != activeBefore.itype;
//			itypeActive = (byte) (itypeActiveChanged?itypeToIndex(activeNow.itype):-1);
		}
		//IType speichern, wenn Aenderung vorliegt, 
		if(itypeActiveChanged){
			itypeActive = (byte) itypeToIndex(activeNow.itype);			
		}else{
			//Keine Aenderung des Itypes. Uebernehme alten Wert:
			itypeActive = (byte) pEvent.getEvent(inventoryItemsBefore).getIType();
		}
		//System.out.println("Itype: " + itypeActive);
		if(Math.abs(stateNow.getAvatarX()-stateBefore.getAvatarX()) + Math.abs(stateNow.getAvatarY()-stateBefore.getAvatarY()) > 2){
			//Teleport!
			
			teleportToItypeAvatar = (byte) Integer.numberOfTrailingZeros(simpleBefore.getMask(agentNowX, agentNowY));
			if(teleportToItypeAvatar == 32)
				teleportToItypeAvatar = -1;
			else{
				//Passe portalExitToEntryIndexMap an:
				if(passiveBefore != null)
					portalExitToEntryItypeMap[teleportToItypeAvatar] = (byte) passiveBefore.itype;
			}
		}
		

		//	5.Ermittle unterschiede auf dem Kaestchen, wo der Agent nun steht

		int beforeMask = simpleBefore.getMask(agentNowX, agentNowY);
		int nowMask = simpleNow.getMask(agentNowX, agentNowY);
		byte spawnedType = -1;
		
		int diffMask = (beforeMask ^ nowMask); //diffMask ist eine Maske mit unterschieden als 1er-Bit kodiert.
					
		if((diffMask & nowMask) != 0){
			//Etwas ist neu da -> Spawn
			spawnedType = (byte) Integer.numberOfTrailingZeros(diffMask);
		}
		
		//  6. Beachte inventarunterschiede:
		
		byte inventoryAdd = -1;
		byte inventoryRemove = -1;

		int inventorySizeBefore = stateBefore.getInventoryArrayUsageSize();
		int inventorySizeNow = stateBefore.getInventoryArrayUsageSize();

		int inventorySlotSeenUsedBefore = 0;
		int inventorySlotSeenUsedNow = 0;
		byte i = 0;
		while(i < 32 && inventorySlotSeenUsedBefore != inventorySizeBefore && inventorySlotSeenUsedNow != inventorySizeNow){

			if(inventoryItemsBefore[i] > 0)
				inventorySlotSeenUsedBefore++;

			if(inventoryItemsNow[i] > 0)
				inventorySlotSeenUsedNow++;
			
			if(inventoryItemsBefore[i] > inventoryItemsNow[i] && inventoryRemove == -1){
				//Item removed!
				inventoryRemove = i;
				if(inventoryAdd != -1)		//Can we terminate loop early?
					break;	
			}
			
			if(inventoryItemsNow[i] > inventoryItemsBefore[i] && inventoryAdd == -1){
				//Item added!
				inventoryAdd = i;
				if(inventoryRemove != -1)	//Can we terminate loop early?
					break;	
			}
			
			i++;
		}
		
		//InventoryMax test:
		if(inventoryAdd != -1){
			//We got an item!
			if(inventoryItemsNow[inventoryAdd] > inventoryMax[inventoryAdd]){
				//Got a new max inventory number!
				inventoryMax[inventoryAdd] = inventoryItemsNow[inventoryAdd];
				inventoryIsMax[inventoryAdd] = false;	//Das wird nicht als maximum angesehen, koennte noch mehr geben!
			}
		}else{
			//Kein add:
			int inventoryShouldAdd = pEvent.getEvent(inventoryItemsBefore).getAddInventorySlotItem();
			if(inventoryShouldAdd != -1){
				//There should be an increase!
				if(inventoryMax[inventoryShouldAdd] == inventoryItemsBefore[inventoryShouldAdd]){
					//We are at the current maximum!
					inventoryIsMax[inventoryShouldAdd] = true;
				}
			}
		}
		
		
		//	7. Lerne Events

		if(!haveEverGotScoreWithoutWinning && scoreDelta>0 && !stateNow.isGameOver())
			haveEverGotScoreWithoutWinning = true;
		
		if(stateNow.getGameWinner() == WINNER.PLAYER_LOSES){
			//Das Spiel wurde verloren!
			pEvent.learnCancelEvent(inventoryItemsBefore, false);
			oEvent.learnCancelEvent(inventoryItemsBefore, false);
			pEvent.learnEventHappened(inventoryItemsBefore, (byte)-1, false, (byte)0, true, (byte)-1, (byte)-1, false, inventoryAdd, inventoryRemove);
			oEvent.learnEventHappened(inventoryItemsBefore, (byte)-1, false, (byte)0, false, (byte)-1, (byte)-1, false, inventoryAdd, inventoryRemove);
			return;
		}else{
			//Ermitteln, ob Aktion nicht durchgefuehrt wurde (z.B. move gegen wand)
			boolean wasCanceled = !movePassive && spawnedType == -1 && !itypeActiveChanged && !itypePassiveChanged && !killActive && !killPassive && scoreDelta == 0;
			if(wasMoveAction)
				wasCanceled &= !moveActive;
//			else
//				wasCanceled = false;
			pEvent.learnCancelEvent(inventoryItemsBefore, wasCanceled);
			oEvent.learnCancelEvent(inventoryItemsBefore, wasCanceled);
			if(!wasCanceled){
				pEvent.learnEventHappened(inventoryItemsBefore, itypeActive, moveActive || !wasMoveAction, scoreDelta, killActive, spawnedType, teleportToItypeAvatar, false, inventoryAdd, inventoryRemove);
				oEvent.learnEventHappened(inventoryItemsBefore, itypePassive, movePassive, scoreDelta, killPassive, spawnedType, (byte)-1, false, inventoryAdd, inventoryRemove);
				
				if(movePassive){
					//War ein push!
					if(!isPushableIndex[passiveIndex]){
						isPushableIndex[passiveIndex] = true;
						pushableITypes.add(passiveBefore.itype);
					}
				}
				
			}
		}

		if(stateNow.getGameWinner() == WINNER.PLAYER_WINS){
			//TODO: Sieg merken!
		}
		
		if(movePassive){
			//Passive bewegt sich weil agent dagegen laeuft.
			//learnPushPossible(stateBefore, stateNow, agentBeforeX, agentBeforeY, avatarIndex,passiveIndex, scoreDelta);
		}
		
		//Lerne blocking:
		if(moveActive)
			blockingMaskTheorie[itypeToIndex(itypeAvatar)] = blockingMaskTheorie[itypeToIndex(itypeAvatar)] & ~beforeMask;
		
	}
	
	public boolean positionAufSpielfeld(int x, int y) {
		return x >= 0 && y >= 0 && x < MAX_X && y < MAX_Y;
	}

	public void lernActionResult(YoloState currentState, YoloState lastState){
		if(!Agent.UPLOAD_VERSION && DEBUG)
			System.out.println("Learn ActionResult!");
		
	}

	public boolean canIndexMoveTo(int itypeIndex, int x, int y, Vector2d moveDirection){
		return false;
	}
	
	public boolean moveWillCancel(YoloState currentState, ACTIONS action, boolean killIsCancel, boolean ignoreStochasticEnemyKilling){
		if(currentState.getAvatar() == null)
			return true;
		int avatarIndex = itypeToIndex(currentState.getAvatar().itype);
		byte[] inventory = getInventoryArray(currentState.getAvatarResources());
		int x = currentState.getAvatarX();
		int y = currentState.getAvatarY();
		boolean noMove = false;
		Vector2d orientation = currentState.getAvatarOrientation();
		switch (action) {
		case ACTION_DOWN:
			if(!orientation.equals(ORIENTATION_NULL) && !orientation.equals(ORIENTATION_DOWN))
				noMove = true;
			y++;
			break;
		case ACTION_UP:
			if(!orientation.equals(ORIENTATION_NULL) && !orientation.equals(ORIENTATION_UP))
				noMove = true;
			y--;
			break;
		case ACTION_RIGHT:
			if(!orientation.equals(ORIENTATION_NULL) && !orientation.equals(ORIENTATION_RIGHT))
				noMove = true;
			x++;
			break;
		case ACTION_LEFT:
			if(!orientation.equals(ORIENTATION_NULL) && !orientation.equals(ORIENTATION_LEFT))
				noMove = true;
			x--;
			break;
		default:
			//TODO: Action use auf singleton checken! Wenn schon geschossen, dann true!
			noMove = true;
		}
		
		if(noMove){
			x = currentState.getAvatarX();
			y = currentState.getAvatarY();
		}

		if(!positionAufSpielfeld(x, y))
			return true;	//Ziel ist nicht im Spielfeld!
		
		//Check enemy:
		if(!ignoreStochasticEnemyKilling && killIsCancel && canBeKilledByStochasticEnemyAt(currentState, x, y))
			return true;
		int mask = currentState.getSimpleState().getMask(x, y);
		
		boolean surelyWillNotBlock = (blockingMaskTheorie[avatarIndex] & mask) == 0;
//		if(surelyWillNotBlock)
//			return false;
		
		//Might Block, check PlayerEvents:
		int playerIndex = itypeToIndex(currentState.getAvatar().itype);
		for (Observation obs : currentState.getObservationGrid()[x][y]) {
			int index = itypeToIndex(obs.itype);
			
			//Bad-SpawnerCheck:
			if (isSpawner(obs.itype)) {
				int iTypeIndexOfSpawner = getSpawnIndexOfSpawner(obs.itype);
				PlayerEvent spawnedPEvent = getPlayerEvent(	currentState.getAvatar().itype,
						indexToItype(iTypeIndexOfSpawner), true);
				YoloEvent spawnedEvent = spawnedPEvent.getEvent(currentState.getInventoryArray());
				boolean isBadSpawner = spawnedEvent.getKill() || spawnedPEvent.getObserveCount() == 0;
				if(isBadSpawner){
					return true;
				}
			}
			
			
			if(activeObjectEffects[playerIndex][index] != null){
				PlayerEvent pEvent = (PlayerEvent) activeObjectEffects[playerIndex][index];
				if(pEvent.getObserveCount() > 20 && (pEvent.willCancel(inventory) && !canInteractWithUse(avatarIndex,index)) || (killIsCancel && !canInteractWithUse(avatarIndex,index) && pEvent.getEvent(inventory).getKill()))
					return true;
			}
		}
		//Nothing found that will block for sure, so guess action will work!
		return false;
	}
	/**
	 * Returnt den wahrscheinlichen Hash, den der advancte state haben wird
	 * @param currentState Der aktuelle state
	 * @param action	Die aktion, die vom currentState ausgefuehrt werden soll
	 * @return	Den ermittelten Hash oder null, wenn der Hash nicht ermittelt werden konnte.
	 */
	public Long getPropablyHash(YoloState currentState, ACTIONS action, boolean ignoreNPCs){
		if(currentState.getAvatar() == null)
			return (long) -1;
		int avatarItype = itypeToIndex(currentState.getAvatar().itype);
		byte[] inventory = getInventoryArray(currentState.getAvatarResources());
		int x = currentState.getAvatarX();
		int y = currentState.getAvatarY();
		int itype = (currentState.getAvatar() != null)?currentState.getAvatar().itype:-1;
		//System.out.println("Was at " + x + "|" + y);
		Vector2d orientation = currentState.getAvatarOrientation();
		long oldHash = currentState.getHash(ignoreNPCs);
		switch (action) {
		case ACTION_DOWN:
			if(!orientation.equals(ORIENTATION_NULL) && !orientation.equals(ORIENTATION_DOWN))
				return currentState.getModifiedHash(ignoreNPCs,x,y,itype,ORIENTATION_DOWN.x, ORIENTATION_DOWN.y);
			y++;
			break;
		case ACTION_UP:
			if(!orientation.equals(ORIENTATION_NULL) && !orientation.equals(ORIENTATION_UP))
				return currentState.getModifiedHash(ignoreNPCs,x,y,itype,ORIENTATION_UP.x, ORIENTATION_UP.y);
			y--;
			break;
		case ACTION_RIGHT:
			if(!orientation.equals(ORIENTATION_NULL) && !orientation.equals(ORIENTATION_RIGHT))
				return currentState.getModifiedHash(ignoreNPCs,x,y,itype,ORIENTATION_RIGHT.x, ORIENTATION_RIGHT.y);
			x++;
			break;
		case ACTION_LEFT:
			if(!orientation.equals(ORIENTATION_NULL) && !orientation.equals(ORIENTATION_LEFT))
				return currentState.getModifiedHash(ignoreNPCs,x,y,itype,ORIENTATION_LEFT.x, ORIENTATION_LEFT.y);
			x--;
			break;
		default:
			//TODO: Action use auf singleton checken! Wenn schon geschossen, dann true!
			return null;
		}

		if(!positionAufSpielfeld(x, y))
			return null;	//Ziel ist nicht im Spielfeld!
		
		int mask = currentState.getSimpleState().getMask(x, y);
		
		boolean surelyWillNotBlock = (blockingMaskTheorie[avatarItype] & mask) == 0;

		
		if(surelyWillNotBlock){
			//System.out.println("Guess will be at " + x + "|" + y);
			return currentState.getModifiedHash(ignoreNPCs,x,y,itype,orientation.x, orientation.y);
		}
		
		//TODO: ueberlegen, ob weiter wissen verwendet werden kann um hash zu generieren
		return null;
		/*
		
		//Might Block, check PlayerEvents:
		int playerIndex = itypeToIndex(currentState.getAvatar().itype);
		for (Observation obs : currentState.getObservationGrid()[x][y]) {
			int index = itypeToIndex(obs.itype);
			if(activeObjectEffects[playerIndex][index] != null){
				PlayerEvent pEvent = (PlayerEvent) activeObjectEffects[playerIndex][index];
				if(pEvent.getObserveCount() > 20 && pEvent.willCancel(inventory))
					return oldHash;
			}
		}
		
		//Nothing found that will block for sure, so guess action will work!
		//TODO: ueberpruefen ob hier nicht besser null returnt werden sollte
		return null;//currentState.getModifiedHash(ignoreNPCs,x,y,orientation.x, orientation.y);*/
	}

	public PlayerEvent getPlayerEvent(int avatar_itype, int passive_itype, boolean activeEvent) {
		int avatarIndex = itypeToIndex(avatar_itype);
		int passiveIndex = itypeToIndex(passive_itype);
		YoloEventController[][] choosenEvents = activeEvent?activeObjectEffects:passiveObjectEffects;
		if(choosenEvents[avatarIndex][passiveIndex] == null){
			if(!Agent.UPLOAD_VERSION)
				System.out.println("Player Event null:" + avatar_itype + " -> " + passive_itype);
			choosenEvents[avatarIndex][passiveIndex] = new PlayerEvent();
		}
		return (PlayerEvent) choosenEvents[avatarIndex][passiveIndex];
	}

	public LinkedList<Integer> getPossiblePlayerItypes() {
		return playerITypes;
	}
	
	public LinkedList<Integer> getPushableITypes() {
		return pushableITypes;
	}
	
	public int getPushTargetIndex(int pushObjectIndex){
		return pushTargetIndex[pushObjectIndex];
	}
	
	public void setPushTargetIndex(int pushObjectIndex, int pushTargetIndex){
		this.pushTargetIndex[pushObjectIndex] = (byte)pushTargetIndex;
	}
	
	@Override
	public String toString() {
		String retVal = "#### YOLO-KNOWLEDGE ####";
		byte[] inventory = initialState.getInventoryArray();
		for (Integer avatarItype : playerITypes) {
			retVal += "\n\n----> Avatar IType:" + avatarItype;
			int avatarIndex = itypeToIndex(avatarItype);
			for (int i = 0; i < activeObjectEffects[avatarIndex].length; i++) {
				if(activeObjectEffects[avatarIndex][i] != null){
					PlayerEvent pEvent = (PlayerEvent) activeObjectEffects[avatarIndex][i];
					retVal += "\n  |-- " + indexToItype(i) + ((pEvent.willCancel(inventory) && pEvent.getObserveCount() > 0)?" blocks":(pEvent.getEvent(inventory).getKill()?" kills":" free"));
				}
			}
		}
		retVal += "\n######### END ##########";
		return retVal;
	}
	
	public int getKnowledgeHash(){
		int hash = 17;
		int prime = 31;

		for (Integer avatarItype : playerITypes) {
			hash = hash * prime + avatarItype;
			int avatarIndex = itypeToIndex(avatarItype);
			for (int i = 0; i < activeObjectEffects[avatarIndex].length; i++) {
				if(activeObjectEffects[avatarIndex][i] != null){
					PlayerEvent pEvent = (PlayerEvent) activeObjectEffects[avatarIndex][i];
					hash = hash * prime + i;
					hash = hash * prime + (pEvent.willCancel(initialState.getInventoryArray())?1:0);
				}
			}
		}
		
		return hash;
	}

	public int getPortalExitEntryIType(int portalExitIndex) {
		return portalExitToEntryItypeMap[portalExitIndex];
	}

	public int getObjectCategory(int objectIndex, YoloState state) {
		if(objectCategory[objectIndex] == -1){
			learnObjectCategories(state);
		}
		return objectCategory[objectIndex];
	}
	
	public int getNpcMaxMovementX(int itype){
		return maxMovePerNPC_PerAxis[itypeToIndex(itype)][AXIS_X];
	}
	
	public int getNpcMaxMovementY(int itype){
		return maxMovePerNPC_PerAxis[itypeToIndex(itype)][AXIS_Y];
	}

	public boolean canBeKilledByStochasticEnemyAt(YoloState state, int xPos, int yPos){
		return getPossibleStochasticKillerAt(state, xPos, yPos) != null;
	}

	public boolean canBeKilledByStochasticEnemyAt(YoloState state, int xPos, int yPos, boolean ignoreTicks){
		return getPossibleStochasticKillerAt(state, xPos, yPos, ignoreTicks) != null;
	}
	public Observation getPossibleStochasticKillerAt(YoloState state, int xPos, int yPos){
		return getPossibleStochasticKillerAt(state, xPos, yPos, false);
	}
	
	public Observation getPossibleStochasticKillerAt(YoloState state, int xPos, int yPos, boolean ignoreTicks){
		int blockSize = state.getBlockSize();
		if(state.getAvatar() == null)
			return null;
		int avatarItype = state.getAvatar().itype;
		ArrayList<Observation>[][] grid = state.getObservationGrid();
		byte[] inventory = state.getInventoryArray();
		int mask = state.getSimpleState().getMask(xPos, yPos) & ~dynamicMask & ~playerIndexMask;
		
		boolean checkDoubleMoveGlobal = !state.getAvatarOrientation().equals(ORIENTATION_NULL);
		
		//Auf Position (Gegner muss sich nicht bewegen):
		if(positionAufSpielfeld(xPos, yPos)){
			ArrayList<Observation> observations = grid[xPos][yPos];
			for (Observation observation : observations) {
				int obsIndex = itypeToIndex(observation.itype);
				if(isStochasticEnemy[obsIndex]){
					if(getPlayerEvent(avatarItype, observation.itype, true).getEvent(inventory).getKill()){
						return observation;
					}
				}
			}
		}
		
		//Rechts (Gegner muss nach Links gehen):
		if(positionAufSpielfeld(xPos+1, yPos)){
			ArrayList<Observation> observations = grid[xPos+1][yPos];
			for (Observation observation : observations) {
				int obsIndex = itypeToIndex(observation.itype);
				boolean checkDoubleMove = checkDoubleMoveGlobal && maxMovePerNPC_PerAxis[obsIndex][AXIS_X]<blockSize && maxMovePerNPC_PerAxis[obsIndex][AXIS_Y]<blockSize;
				if(isStochasticEnemy[obsIndex] && (ignoreTicks || movesAtTickOrDirectFollowing(obsIndex, state.getGameTick())) && (blockingMaskTheorie[obsIndex] & mask) == 0){
					//Check, ob der gegner nach Links gehen kann: 
					if((int)((observation.position.x - maxMovePerNPC_PerAxis[obsIndex][AXIS_X])/blockSize) == xPos || checkDoubleMove && (int)((observation.position.x - 2*maxMovePerNPC_PerAxis[obsIndex][AXIS_X])/blockSize) == xPos){
						//Kann sich auf xPos | yPos bewegen!
						if(getPlayerEvent(avatarItype, observation.itype, true).getEvent(inventory).getKill()){
							return observation;
						}
					}
				}
			}
		}
		
		//Links (Gegner muss nach rechts gehen):
		if(positionAufSpielfeld(xPos-1, yPos)){
			ArrayList<Observation> observations = grid[xPos-1][yPos];
			for (Observation observation : observations) {
				int obsIndex = itypeToIndex(observation.itype);
				boolean checkDoubleMove = checkDoubleMoveGlobal && maxMovePerNPC_PerAxis[obsIndex][AXIS_X]<blockSize && maxMovePerNPC_PerAxis[obsIndex][AXIS_Y]<blockSize;
				if(isStochasticEnemy[obsIndex] && (ignoreTicks || movesAtTickOrDirectFollowing(obsIndex, state.getGameTick())) && (blockingMaskTheorie[obsIndex] & mask) == 0){
					//Check, ob der gegner nach Rechts gehen kann:
					if((int)((observation.position.x + maxMovePerNPC_PerAxis[obsIndex][AXIS_X])/blockSize) + 1 >= xPos|| checkDoubleMove && (int)((observation.position.x + 2*maxMovePerNPC_PerAxis[obsIndex][AXIS_X])/blockSize) + 1 >= xPos){
						//Kann sich auf xPos | yPos bewegen!
						if(getPlayerEvent(avatarItype, observation.itype, true).getEvent(inventory).getKill()){
							return observation;
						}
					}
				}
			}
		}
		
		//Oben (Gegner muss nach unten gehen):
		if(positionAufSpielfeld(xPos, yPos-1)){
			ArrayList<Observation> observations = grid[xPos][yPos-1];
			for (Observation observation : observations) {
				int obsIndex = itypeToIndex(observation.itype);
				boolean checkDoubleMove = checkDoubleMoveGlobal && maxMovePerNPC_PerAxis[obsIndex][AXIS_X]<blockSize && maxMovePerNPC_PerAxis[obsIndex][AXIS_Y]<blockSize;
				if(isStochasticEnemy[obsIndex] && (ignoreTicks || movesAtTickOrDirectFollowing(obsIndex, state.getGameTick())) && (blockingMaskTheorie[obsIndex] & mask) == 0){
					//Check, ob der gegner nach Unten gehen kann:
					if((int)((observation.position.y + maxMovePerNPC_PerAxis[obsIndex][AXIS_Y])/blockSize) + 1 >= yPos|| checkDoubleMove && (int)((observation.position.y + 2*maxMovePerNPC_PerAxis[obsIndex][AXIS_Y])/blockSize) + 1 >= yPos){
						//Kann sich auf xPos | yPos bewegen!
						if(getPlayerEvent(avatarItype, observation.itype, true).getEvent(inventory).getKill()){
							return observation;
						}
					}
				}
			}
		}
		
		//Unten (Gegner muss nach Oben gehen):
		if(positionAufSpielfeld(xPos, yPos+1)){
			ArrayList<Observation> observations = grid[xPos][yPos+1];
			for (Observation observation : observations) {
				int obsIndex = itypeToIndex(observation.itype);
				boolean checkDoubleMove = checkDoubleMoveGlobal && maxMovePerNPC_PerAxis[obsIndex][AXIS_X]<blockSize && maxMovePerNPC_PerAxis[obsIndex][AXIS_Y]<blockSize;
				if(isStochasticEnemy[obsIndex] && (ignoreTicks || movesAtTickOrDirectFollowing(obsIndex, state.getGameTick())) && (blockingMaskTheorie[obsIndex] & mask) == 0){
					//Check, ob der gegner nach Oben gehen kann:
					if((int)((observation.position.y - maxMovePerNPC_PerAxis[obsIndex][AXIS_Y])/blockSize) == yPos || checkDoubleMove && (int)((observation.position.y - 2*maxMovePerNPC_PerAxis[obsIndex][AXIS_Y])/blockSize) == yPos ){
						//Kann sich auf xPos | yPos bewegen!
						if(getPlayerEvent(avatarItype, observation.itype, true).getEvent(inventory).getKill()){
							return observation;
						}
					}
				}
			}
		}
		
		return null;
	}
	
	private boolean movesAtTick(int obsIndex, int gameTick) {

		byte currentMoveRule = (byte) (Integer.numberOfTrailingZeros(npcMoveModuloTicks[obsIndex])+1);
		int remainer = (gameTick+1) % currentMoveRule;
		return remainer == 0;
		
	}
	
	private boolean movesAtTickOrDirectFollowing(int obsIndex, int gameTick) {

		return movesAtTick(obsIndex, gameTick) || movesAtTick(obsIndex, gameTick+1);
		
	}
	
	public byte getNpcMovesEveryXTicks(int npcIndex){
		return (byte) (Integer.numberOfTrailingZeros(npcMoveModuloTicks[npcIndex])+1);
	}

	public int getInventoryMax(int slot){
		if(inventoryIsMax[slot])
			return inventoryMax[slot];
		else
			return -1;
	}
	
	public boolean canInteractWithUse(int avatarItype, int objectItype){
		int useActionIndex = useEffektToSpawnIndex[itypeToIndex(avatarItype)];
		if(useActionIndex == -1)
			return false;
		PlayerUseEvent uEvent = useEffects[useActionIndex][itypeToIndex(objectItype)];
		
		if(uEvent == null || uEvent.getTriggerEvent().getWall())
			return false;
		else{
			if(minusScoreIsBad && uEvent.getTriggerEvent().getScoreDelta()<0)
				return false;
			else
				return true;
		}
	}
	
	public boolean getIncreaseScoreIfInteractWith(int avatarItype, int objectItype){
		int useActionIndex = useEffektToSpawnIndex[itypeToIndex(avatarItype)];
		if(useActionIndex == -1)
			return false;
		PlayerUseEvent uEvent = useEffects[useActionIndex][itypeToIndex(objectItype)];
		if(uEvent == null)
			return false;
		else
			return uEvent.getTriggerEvent().getScoreDelta()>0;
	}
	
	public boolean isSpawner(int itype){
		return spawnerOf[itypeToIndex(itype)] != -1;
	}
	
	public int getSpawnIndexOfSpawner(int itype){
		return spawnerOf[itypeToIndex(itype)];
	}
	
	public boolean isSpawnable(int itype){
		return spawnedBy[itypeToIndex(itype)] != -1;
	}
	
	public int getSpawnerIndexOfSpawned(int itype){
		return spawnedBy[itypeToIndex(itype)];
	}

	public boolean isDynamic(int index) {
		return isDynamic[index];
	}

	public boolean isStochasticEnemy(int index) {
		return isStochasticEnemy[index];
	}

	public boolean actionsLeadsOutOfBattlefield(YoloState state, ACTIONS action) {
		int x = state.getAvatarX();
		int y = state.getAvatarY();
		switch (action) {
		case ACTION_DOWN:
			y++;
			break;
		case ACTION_UP:
			y--;
			break;
		case ACTION_RIGHT:
			x++;
			break;
		case ACTION_LEFT:
			x--;
			break;
		default:
			return false;
		}

		if(!positionAufSpielfeld(x, y))
			return true;	//Ziel ist nicht im Spielfeld!
		return false;
	}
	
	public boolean hasEverBeenAliveAtFieldWithItypeIndex(int avatarIndex, int passiveIndex){
		return hasBeenAliveAt[avatarIndex][passiveIndex];
	}
	
	public boolean canIncreaseScoreWithoutWinning(YoloState state){
		if(state.isGameOver())
			return false;
		byte[] inventoryItems = state.getInventoryArray();
		
		for (int category = 0; category < 7; category++) {
			if(category == Types.TYPE_AVATAR || category == Types.TYPE_FROMAVATAR)
				continue;
			ArrayList<Observation>[] obsListArray = state.getObservationList(category);
			if(obsListArray != null){
				for (ArrayList<Observation> obsList : obsListArray) {
					if(obsList != null && !obsList.isEmpty()){
						for (Observation observation : obsList) {
							YoloEvent event = getPlayerEvent(state.getAvatar().itype, observation.itype, true).getEvent(inventoryItems);
							if(!event.getWinGame() &&event.getScoreDelta()>0){
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}

	public boolean canUseInteractWithSomethingAt(YoloState state) {
		
		int x = state.getAvatarX();
		int y = state.getAvatarY();
		if(!positionAufSpielfeld(x, y))
			return false;
		Vector2d orientation = state.getAvatarOrientation();
		if(orientation.equals(ORIENTATION_NULL))
			return false;
		else if(orientation.equals(ORIENTATION_DOWN))
			y++;
		else if(orientation.equals(ORIENTATION_UP))
			y--;
		else if(orientation.equals(ORIENTATION_RIGHT))
			x++;
		else if(orientation.equals(ORIENTATION_LEFT))
			x--;
		
		if(!positionAufSpielfeld(x, y))
			return false;
		
		
		for (Observation obs : state.getObservationGrid()[x][y]) {
			if(canInteractWithUse(state.getAvatar().itype, obs.itype))
				return true;
		}
		return false;
	}

	public int getFromAvatarMask() {
		return fromAvatarMask;
	}

	public boolean avatarLooksOutOfGame(YoloState state) {
		Vector2d orientation = state.getAvatarOrientation();
		if(orientation.equals(ORIENTATION_NULL))
			return false;
		else if(orientation.equals(ORIENTATION_DOWN))
			return state.getAvatarY() == MAX_Y;
		else if(orientation.equals(ORIENTATION_UP))
			return state.getAvatarY() == 0;
		else if(orientation.equals(ORIENTATION_RIGHT))
			return state.getAvatarX() == MAX_X;
		else if(orientation.equals(ORIENTATION_DOWN))
			return state.getAvatarX() == 0;
		
		return false;
	}

	public boolean haveEverGotScoreWithoutWinning() {
		return haveEverGotScoreWithoutWinning;
	}
	
	public boolean agentHasControlOfMovement(YoloState state){
		if(state.getAvatar() == null)
			return true;
		int index = itypeToIndex(state.getAvatar().itype);
		return agentMoveControlCounter[index]>-20;
	}

	public void setMinusScoreIsBad(boolean minusScoreIsBad) {
		this.minusScoreIsBad = minusScoreIsBad;
	}
	
	public boolean isMinusScoreBad(){
		return minusScoreIsBad;
	}

	public boolean playerItypeIsWellKnown(YoloState state){
		if(state.getAvatar() == null)
			return true;
		if(!agentHasControlOfMovement(state))
			return true;
		int index = itypeToIndex(state.getAvatar().itype);
		return agentItypeCounter[index] == Byte.MAX_VALUE; 
	}
	
	public int getBlockingMask(int index){
		return blockingMaskTheorie[index];
	}
	
	public int getPlayerIndexMask() {
		return playerIndexMask;
	}
	public int getDynamicMask() {
		return dynamicMask;
	}
}
