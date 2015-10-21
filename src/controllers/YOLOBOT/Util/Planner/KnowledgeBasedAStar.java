package controllers.YOLOBOT.Util.Planner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import controllers.YOLOBOT.YoloState;
import controllers.YOLOBOT.Util.Wissensdatenbank.PlayerEvent;
import controllers.YOLOBOT.Util.Wissensdatenbank.YoloEvent;
import controllers.YOLOBOT.Util.Wissensdatenbank.YoloKnowledge;
import ontology.Types;
import ontology.Types.ACTIONS;
import core.game.Observation;

public class KnowledgeBasedAStar {

	public static final int MALUS = 40;
	
	private List<Observation>[][] grid;
	public int[][] distance;
	public int[][] fromAvatarDistance;
	public byte[][] originDirectionArray;
	public int[][] ausnahmeID;
	public boolean[][] interpretedAsWall;
	public int[] fieldsReachedCount;
	private boolean stopEarly;
	private int stopEarlyX, stopEarlyY;
	private boolean extraIllegalMove;
	private int extraIllegalMoveX, extraIllegalMoveY, extraIllegalMoveItype;
	private int oneMoveableIgnoreIType;
	private byte[] zycleMet;
	private Collection<Integer> blacklistedObjects;
	
	public int[] iTypesFoundCount;
	public Observation[] nearestITypeObservationFound;
	public int[] nearestITypeObservationFoundFirstTargetX;
	public int[] nearestITypeObservationFoundFirstTargetY;
	private boolean countFoundItypes;
	private HashSet<Integer> countedObsIds;
	
	private int[] possibleMovesX, possibleMovesY;
	
	/***
	 * Direction from where it got reached.<br/>
	 * 0 = Left<br/>
	 * 1 = Right<br/>
	 * 2 = Top<br/>
	 * 3 = Bottom<br/>
	 * 5 = By Portal
	 */
	public byte[][] from;
	public final static byte LEFT = 0;
	public final static byte RIGHT = 1;
	public final static byte TOP = 2;
	public final static byte BOTTOM = 3;
	private static boolean[] markedItypes;
	
	private byte[] inventoryItems;
	
	private YoloState state;
	private boolean moveDirectionInverse;
	

	public KnowledgeBasedAStar(YoloState yoloState) {
		this.state = yoloState;
		this.grid = yoloState.getObservationGrid();
		inventoryItems = yoloState.getInventoryArray();
		oneMoveableIgnoreIType = -1;
		countFoundItypes = false;
		moveDirectionInverse = false;

		ArrayList<ACTIONS> actions = yoloState.getAvailableActions();
		int actionsSize = actions.size() - (actions.contains(Types.ACTIONS.ACTION_USE)?1:0);
		possibleMovesX = new int[actionsSize];
		possibleMovesY = new int[actionsSize];
		int ignoredActions = 0;
		for (int i = 0; i < actions.size(); i++) {
			switch (actions.get(i)) {
			case ACTION_DOWN:
				possibleMovesY[i-ignoredActions] = 1;
				break;
			case ACTION_UP:
				possibleMovesY[i-ignoredActions] = -1;
				break;
			case ACTION_RIGHT:
				possibleMovesX[i-ignoredActions] = 1;				
				break;
			case ACTION_LEFT:
				possibleMovesX[i-ignoredActions] = -1;
				break;
			default:
				ignoredActions++;
				break;
			}
		}
	}
	
	public void setIllegalMove(int x, int y, int itype){
		extraIllegalMove = true;
		extraIllegalMoveX = x;
		extraIllegalMoveY = y;
		extraIllegalMoveItype = itype;
	}
	
	public void disableIllegalMove(){
		extraIllegalMove = false;
	}
	
	public void setStopEarly(int x, int y){
		stopEarly = true;
		stopEarlyX = x;
		stopEarlyY = y;
	}
	
	public void disableStopEarly(){
		stopEarly = false;
	}

	public void setGrid(List<Observation>[][] grid) {
		this.grid = grid;
	}
	

	public List<Observation> calculate(int startX, int startY, int agent_itype_start,
			int[] interestingItypes, boolean ignoreMoveables) {
		return calculate(startX, startY, agent_itype_start, interestingItypes, ignoreMoveables, false, true);
	}

	public List<Observation> calculate(int startX, int startY, int agent_itype_start,
			int[] interestingItypes, boolean ignoreMoveables, boolean ignoreNPC, boolean ignorePortals) {
		LinkedList<Observation> retVal = new LinkedList<Observation>();
		if(!YoloKnowledge.instance.positionAufSpielfeld(startX, startY))
			return null;
		if(countFoundItypes){
			iTypesFoundCount = new int[YoloKnowledge.ITYPE_MAX_COUNT];
			nearestITypeObservationFound = new Observation[YoloKnowledge.ITYPE_MAX_COUNT];
			nearestITypeObservationFoundFirstTargetX = new int[YoloKnowledge.ITYPE_MAX_COUNT];
			nearestITypeObservationFoundFirstTargetY = new int[YoloKnowledge.ITYPE_MAX_COUNT];
			countedObsIds = new HashSet<Integer>();
		}

		fieldsReachedCount = new int[4];
		zycleMet = new byte[]{0b0001,0b0010,0b0100,0b1000};
		markedItypes = new boolean[YoloKnowledge.ITYPE_MAX_COUNT];
		for (int i = 0; i < interestingItypes.length; i++) {
			if(interestingItypes[i] != -1)
				markedItypes[interestingItypes[i]] = true;
		}
		originDirectionArray = new byte[grid.length][grid[0].length];
		distance = new int[grid.length][grid[0].length];
		ausnahmeID = new int[grid.length][grid[0].length];
		distance[startX][startY] = 1;
		from = new byte[grid.length][grid[0].length];
		interpretedAsWall = new boolean[grid.length][grid[0].length];

		PriorityQueue<AStarEntry> queue = new PriorityQueue<AStarEntry>();
		queue.add(new AStarEntry(startX, startY, agent_itype_start, oneMoveableIgnoreIType, (byte)-1, 1, -1, -1));
		
		if(countFoundItypes){
			for (Observation obs : grid[startX][startY]) {
				if(countedObsIds.contains(obs.obsID))
					continue;
				countedObsIds.add(obs.obsID);
				int index = YoloKnowledge.instance.itypeToIndex(obs.itype);
				if(blacklistedObjects != null && blacklistedObjects.contains(obs.obsID))
					continue;
				int oldVal = iTypesFoundCount[index]++;
				if(oldVal == 0){
					//Nearest Observation of this itype:
					nearestITypeObservationFound[index] = obs;
					nearestITypeObservationFoundFirstTargetX[index] = startX;
					nearestITypeObservationFoundFirstTargetY[index] = startY;
				}
			}
		}
		

		while (queue.size() > 0) {
			AStarEntry oldEntry = queue.poll();
			int x = oldEntry.getX();
			int y = oldEntry.getY();
			int agent_itype = oldEntry.getItype();
			int itypeAusnahme = oldEntry.getItypeAusnahme();
			int oldDistance = oldEntry.getDistance();
			byte fromDirectionOld = oldEntry.getOriginAusrichtung();
			int changedItypeX = oldEntry.getxFirstItypeChange();
			int changedItypeY = oldEntry.getyFirstItypeChange();
			
			for (int i = 0; i < possibleMovesX.length; i++) {
				int xNew = x + possibleMovesX[i];
				int yNew = y + possibleMovesY[i];
				int newDistance = oldDistance + 1;
				byte fromDirection = fromDirectionOld;
				if (xNew >= 0 && xNew < grid.length && yNew >= 0 && yNew < grid[xNew].length && (xNew == x || yNew == y)){
					
					//First-step-check:
					if(fromDirection == -1){
						//Is first step:
						if(possibleMovesX[i]>0)
							fromDirection = RIGHT;
						else if(possibleMovesX[i] < 0)
							fromDirection = LEFT;
						if(possibleMovesY[i]>0)
							fromDirection = BOTTOM;
						else if(possibleMovesY[i] < 0)
							fromDirection = TOP;
					}
					
					//Gueltiges Feld
					//System.out.println("Gueltig:"+xNew+ "-"+yNew);
					if(distance[xNew][yNew] == 0 || interpretedAsWall[xNew][yNew]){
						//System.out.println("Neu gefunden:"+xNew+ "-"+yNew);
						//Neu gefunden!
						

						boolean moveBlocked = false;
						boolean isPortalEntry = false;
						int deadlyField = canBeKilledAtByStochasticEnemy(xNew, yNew);
						int new_itype = agent_itype;
						int portalIType = -1;
						boolean[] blockedBy = new boolean[grid[xNew][yNew].size()];
						boolean[] useActionEffective = new boolean[grid[xNew][yNew].size()];
						int nr = 0;
						for (Observation obs : grid[xNew][yNew]) {
							if(ignoreNPC && obs.category == Types.TYPE_NPC){// && (fromAvatarDistance == null || fromAvatarDistance[xNew][yNew] > 5) && (fromAvatarDistance != null || distance[xNew][yNew] > 5)){
								continue;
							}
							
							if(obs.category == Types.TYPE_PORTAL){
								if(!moveDirectionInverse)
									portalIType = obs.itype;
								PlayerEvent pEvent = YoloKnowledge.instance.getPlayerEvent(agent_itype, obs.itype, true);
								YoloEvent event = pEvent.getEvent(inventoryItems);
								if(!pEvent.willCancel(inventoryItems) && event.getTeleportTo() != -1)
									isPortalEntry = true;
									
							}
							if(moveDirectionInverse){
								int fromTeleport = YoloKnowledge.instance.getPortalExitEntryIType(YoloKnowledge.instance.itypeToIndex(obs.itype));
								portalIType = fromTeleport;
							}
							
							if(obs.category != Types.TYPE_AVATAR){
								int passiveIndex = YoloKnowledge.instance.itypeToIndex(obs.itype);
								PlayerEvent event = YoloKnowledge.instance.getPlayerEvent(agent_itype, obs.itype, true);
								int spawnIndex = YoloKnowledge.instance.getSpawnIndexOfSpawner(obs.itype);
								boolean isBadSpawner = false;
								if(spawnIndex != -1){
									//Etwas wird gespawnt!
									PlayerEvent spawnCollisionEvent = YoloKnowledge.instance.getPlayerEvent(agent_itype, YoloKnowledge.instance.indexToItype(spawnIndex), true);
									if(spawnCollisionEvent.getObserveCount() > 0){
										YoloEvent yEvent = spawnCollisionEvent.getEvent(inventoryItems);
										isBadSpawner = yEvent.getKill() || yEvent.getScoreDelta() < 0 || yEvent.getRemoveInventorySlotItem() != -1;
									}
								}
								boolean interactable = YoloKnowledge.instance.canInteractWithUse(agent_itype, obs.itype);
								useActionEffective[nr] = interactable;
								boolean deadly = event.getEvent(inventoryItems).getKill() && !YoloKnowledge.instance.hasEverBeenAliveAtFieldWithItypeIndex(YoloKnowledge.instance.itypeToIndex(agent_itype),passiveIndex) && obs.category != Types.TYPE_MOVABLE;
								if(deadly)
										deadlyField = 0;
								blockedBy[nr] = isBadSpawner || (event.getObserveCount() > 0 && (event.willCancel(inventoryItems) || !event.getEvent(inventoryItems).getMove() || (deadly ))) && !interactable;
								if(!(moveBlocked||blockedBy[nr]) && !ignoreMoveables && event.getObserveCount() > 0){
									if(obs.category == Types.TYPE_MOVABLE && !event.getEvent(inventoryItems).getMove()){
										//Hier wird wegen eines MOVEABLES geblockt! Teste oneMoveableIgnoreIType
										if(itypeAusnahme == obs.itype){
											//Ausnahme trifft ein, dass ein bestimmter IType ein mal ignoriert werden darf!
											ausnahmeID[xNew][yNew] = obs.obsID;
											itypeAusnahme = -1;
										}else{
											//Keine Ausnahme!
											blockedBy[nr] = true;
										}
									}
								}
								moveBlocked |= blockedBy[nr];
								if(markedItypes[obs.itype])
									retVal.add(obs);
								if(!moveBlocked){
									int modType = event.getEvent(inventoryItems).getIType();
									if(modType != -1){
										new_itype = YoloKnowledge.instance.indexToItype(modType);
										if(changedItypeX == -1 && changedItypeY == -1){
											// not yet changed Itype:
											changedItypeX = xNew;
											changedItypeY = yNew;
										}
									}
								}
							}

							nr++;
						}
						if(deadlyField != Integer.MAX_VALUE)
							newDistance += 2*MALUS/(deadlyField+1);
						moveBlocked |= !ignorePortals && isPortalEntry && moveDirectionInverse;
						
						if(!moveBlocked && extraIllegalMove && extraIllegalMoveX == xNew && extraIllegalMoveY == yNew){
							//Stepping on illegal field
							PlayerEvent event = YoloKnowledge.instance.getPlayerEvent(agent_itype, extraIllegalMoveItype, false);	//Was passiert mit dem passive?
							YoloEvent triggeredEvent = event.getEvent(inventoryItems);
							if(triggeredEvent.getMove() || event.getObserveCount() == 0)	//Passive bewegt sich?
								moveBlocked = true;
						}
						if(distance[xNew][yNew] == 0 || (interpretedAsWall[xNew][yNew] && !moveBlocked)){
							distance[xNew][yNew] = newDistance;
							from[xNew][yNew] = (byte)((xNew < x?0:(xNew > x?1:(yNew < y?2:3))));
						}
						
						interpretedAsWall[xNew][yNew] = moveBlocked; 
						
						//Count appearances:
						if(countFoundItypes && deadlyField > 1){
							nr = -1;
							for (Observation obs : grid[xNew][yNew]) {
								nr++;
								if(countedObsIds.contains(obs.obsID) || (moveBlocked && !blockedBy[nr] && !useActionEffective[nr]))
									continue;
								countedObsIds.add(obs.obsID);
								int index = YoloKnowledge.instance.itypeToIndex(obs.itype);
								if(blacklistedObjects != null && blacklistedObjects.contains(obs.obsID))
									continue;
								int oldVal = iTypesFoundCount[index]++;
								if(oldVal == 0){
									//Nearest Observation of this itype:
									nearestITypeObservationFound[index] = obs;
									if(changedItypeX != -1 && changedItypeY != -1){
										//Itype has been changed, so move frist to Itypechange!
										nearestITypeObservationFoundFirstTargetX[index] = changedItypeX;
										nearestITypeObservationFoundFirstTargetY[index] = changedItypeY;
									}else{
										//Itype has not been changed, move to object
										nearestITypeObservationFoundFirstTargetX[index] = xNew;
										nearestITypeObservationFoundFirstTargetY[index] = yNew;
										
									}
								}
								
							}
						}
						
						if(!moveBlocked){
							fieldsReachedCount[fromDirection]++;
							originDirectionArray[xNew][yNew] = fromDirection;
							//Early Stop
							if(stopEarly && xNew == stopEarlyX && yNew == stopEarlyY)
								return retVal;
							
							
							boolean teleported = false;
							if(portalIType != -1 && !ignorePortals){
								int portalExitIType = -1, portalExitIndex;
								if(!moveDirectionInverse){
									PlayerEvent pEvent = YoloKnowledge.instance.getPlayerEvent(agent_itype, portalIType, true);
									YoloEvent event = pEvent.getEvent(inventoryItems);
									portalExitIndex = event.getTeleportTo();
									if(!pEvent.willCancel(inventoryItems) && portalExitIndex != -1)
										portalExitIType = YoloKnowledge.instance.indexToItype(portalExitIndex);
								}else{
									portalExitIType = portalIType;
									portalExitIndex = YoloKnowledge.instance.itypeToIndex(portalExitIType);
								}
								if(portalExitIType != -1){
									int portalExitCategory = YoloKnowledge.instance.getObjectCategory(portalExitIndex, state);
									if(portalExitCategory != -1){
										ArrayList<Observation>[] obs = state.getObservationList(portalExitCategory);
										if(obs != null){
											ArrayList<Observation> portalExits = null;
											for (ArrayList<Observation> list : obs) {
												if(!list.isEmpty() && list.get(0).itype == portalExitIType)
													portalExits = list;
											}
											if(portalExits != null){
												//Habe Portalausgaenge gefunden!
												teleported = true;
												
												int blockSize = state.getBlockSize();
												for (Observation exit : portalExits) {
													xNew = (int)exit.position.x/blockSize;
													yNew = (int)exit.position.y/blockSize;
													if(distance[xNew][yNew] == 0 || interpretedAsWall[xNew][yNew]){
														//Hier war die SUche noch nicht!
														from[xNew][yNew] = 5;
														originDirectionArray[xNew][yNew] = fromDirection;
														distance[xNew][yNew] = newDistance;
														queue.add(new AStarEntry(xNew, yNew, new_itype, itypeAusnahme, fromDirection, newDistance, changedItypeX, changedItypeY));
													}else if(distance[xNew][yNew]>1 && !interpretedAsWall[xNew][yNew]){
														//Hier waren wir schon, untersuche zyklus
														handleZyklus(fromDirection, originDirectionArray[xNew][yNew]);
													}											
												}	
											}
										}
									}
								}
							}
							
							if(!teleported){
								//Standard appending for later loop
								queue.add(new AStarEntry(xNew, yNew, new_itype, itypeAusnahme, fromDirection, newDistance, changedItypeX, changedItypeY));
							}
						}
							
					}else if(distance[xNew][yNew]>1 && !interpretedAsWall[xNew][yNew]){
						//Hier waren wir schon, untersuche zyklus
						handleZyklus(fromDirection, originDirectionArray[xNew][yNew]);
					}
				}
			}
		}

		//Postwork - zycles:
		for (int iteration = 0; iteration < 2; iteration++) {
			for (int from = 0; from < zycleMet.length; from++) {
				for (int to = 0; to < 4; to++) {
					if(to == from)
						continue;					
					if((zycleMet[from]>>to & 1) == 1){
						//from hat zykel zu to						
						byte newMask = (byte) (zycleMet[from] | zycleMet[to]);						
						zycleMet[from] = newMask;
						zycleMet[to] = newMask;						
					}					
				}
			}
		}
		int[] oldFieldReached = new int[]{fieldsReachedCount[0],fieldsReachedCount[1],fieldsReachedCount[2],fieldsReachedCount[3]};
		for (int from = 0; from < zycleMet.length; from++) {
			fieldsReachedCount[from] = 0;
			for (int to = 0; to < 4; to++) {
				if((zycleMet[from]>>to & 1) == 1){
					//from hat zykel zu to						
					fieldsReachedCount[from] += oldFieldReached[to];
				}					
			}
		}
		
		return retVal;
	}
	
	private int canBeKilledAtByStochasticEnemy(int x, int y){
		return state.getStochasticKillMap().getMinDistanceToEnemy(x,y);
	}

	public LinkedList<ACTIONS> extractActions(int xTarget, int yTarget) {
		LinkedList<ACTIONS> actionsToDo = new LinkedList<Types.ACTIONS>();
		
		if (distance[xTarget][yTarget] > 1) {
			// Weg gefunden(!= 0) und bewegen notwendig(!= 1)
			int curX = xTarget;
			int curY = yTarget;

			while (distance[curX][curY] > 1) {
				switch (from[curX][curY]) {
				case KnowledgeBasedAStar.BOTTOM:
					actionsToDo.addFirst(ACTIONS.ACTION_DOWN);
					curY--;
					break;
				case KnowledgeBasedAStar.TOP:
					actionsToDo.addFirst(ACTIONS.ACTION_UP);
					curY++;
					break;
				case KnowledgeBasedAStar.LEFT:
					actionsToDo.addFirst(ACTIONS.ACTION_LEFT);
					curX++;
					break;
				case KnowledgeBasedAStar.RIGHT:
					actionsToDo.addFirst(ACTIONS.ACTION_RIGHT);
					curX--;
					break;

				default:
					//TODO: Portal-Case beachten!
					System.err.println("Portal gefunden, aber 'extractActions' wurde noch nicht passend implementiert!!");
					break;
				}
			}

		}
		
		return actionsToDo;
	}

	public void setIgnoreOneMoveableOfType(int itype) {
		oneMoveableIgnoreIType = itype;
	}
	
	public void setCountFoundItypes(boolean value) {
		this.countFoundItypes = value;
	}

	public void setMoveDirectionInverse(boolean value) {
		moveDirectionInverse = value;
		fromAvatarDistance = distance;
	}
	
	private void handleZyklus(byte direction1, byte direction2){
		zycleMet[direction1] |= 1<<direction2;
	}
	
	public void setBlacklistedObjects(Collection<Integer> blacklistedObjects) {
		this.blacklistedObjects = blacklistedObjects;
	}
}
