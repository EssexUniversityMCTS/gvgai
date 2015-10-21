package controllers.YOLOBOT.Util;

import java.util.HashSet;

import controllers.YOLOBOT.YoloState;
import controllers.YOLOBOT.Util.Wissensdatenbank.PlayerEvent;
import controllers.YOLOBOT.Util.Wissensdatenbank.YoloEvent;
import controllers.YOLOBOT.Util.Wissensdatenbank.YoloKnowledge;
import core.game.Observation;

public class StochasticKillmap {

	private int[][] killmap;
	private YoloState state;
	private int blockSize;
	private HashSet<Integer> hash;
	private int maxSteps = 10;
	private int stepX,stepY;
	private int npcMaske;
	private int npcMaskeDyn;
	private boolean stopNextRec;
	
	public StochasticKillmap(YoloState state) {
		this.state = state;
		blockSize = state.getBlockSize();
		killmap = new int[state.getObservationGrid().length][state.getObservationGrid()[0].length];
		if(state.getNpcPositions() == null){
			return;
		}
		for (int i = 0; i < state.getNpcPositions().length; i++) {
			if(state.getNpcPositions()[i] != null){
				for (int j = 0; j < state.getNpcPositions()[i].size(); j++) {
					Observation npc = state.getNpcPositions()[i].get(j);
					if(YoloKnowledge.instance.isStochasticEnemy(YoloKnowledge.instance.itypeToIndex(npc.itype))){
						PlayerEvent enemyEvent = YoloKnowledge.instance.getPlayerEvent(state.getAvatar().itype, npc.itype, true);
						YoloEvent event = enemyEvent.getEvent(state.getInventoryArray());
						if(event.getKill() /*&& !YoloKnowledge.instance.canInteractWithUse(state.getAvatar().itype, enemyItype)*/){
							fillMapForNPC(npc);
						}
					}	
					
					
				}
			}
		}
		
		//YoloKnowledge.instance.canBeKilledByStochasticEnemyAt(state, xPos, yPos);
	}
	
	private void fillMapForNPC(Observation npc) {
		hash = new HashSet<Integer>();

		int x = (int) npc.position.x;
		int y = (int) npc.position.y;
		stepX = YoloKnowledge.instance.getNpcMaxMovementX(npc.itype);
		stepY = YoloKnowledge.instance.getNpcMaxMovementY(npc.itype);
		
		npcMaske = YoloKnowledge.instance.getBlockingMask(YoloKnowledge.instance.itypeToIndex(npc.itype)) & ~YoloKnowledge.instance.getPlayerIndexMask();
		
		npcMaskeDyn = npcMaske & ~YoloKnowledge.instance.getDynamicMask();
		
		fillMapForNPCRec(npc,x,y, 0, false);
		
//		int x = (int) ((npc.position.x - YoloKnowledge.instance.getNpcMaxMovementX(npc.itype))/blockSize);
//		int y = (int) ((npc.position.y - YoloKnowledge.instance.getNpcMaxMovementY(npc.itype))/blockSize);
		
	}

	

	private void fillMapForNPCRec(Observation npc, int x, int y, int steps, boolean lastStep) {
		
		//1. Gegener kann an dieser Stelle stehen
		setCanReach(x/blockSize, y/blockSize, steps);
		setCanReach((x+blockSize-1)/blockSize, y/blockSize, steps);
		setCanReach(x/blockSize, (y+blockSize-1)/blockSize, steps);
		setCanReach((x+blockSize-1)/blockSize, (y+blockSize-1)/blockSize, steps);
		
		//Position Hashen
		addToHash(x, y);
		
		if(steps == maxSteps || lastStep){
			return;
		}
		
		//2. kann er an seine Nachbarfelder
		checkAndDoRecursion(npc, steps, x+stepX, y);

		checkAndDoRecursion(npc, steps, x-stepX, y);

		checkAndDoRecursion(npc, steps, x, y+stepY);

		checkAndDoRecursion(npc, steps, x, y-stepY);
		
	
	//wenn ja:
		//gehe zu 1.
	
	//wenn nein:
		//ende (terminieren)
	}

	private void checkAndDoRecursion(Observation npc, int steps,
			int newX, int newY) {
		if(isHashed(newX, newY)){
			return;
		}
		int x = newX/blockSize;
		int y = newY/blockSize;
		boolean isFixX = newX % blockSize == 0;
		boolean isFixY = newY % blockSize == 0;
		boolean canStandHere = true;
		stopNextRec = false;
		
		canStandHere &= canStandOn(newX, newY);
		if(!isFixX){
			canStandHere &= canStandOn(newX+blockSize, newY);
		}
		if(!isFixY){
			canStandHere &= canStandOn(newX, newY+blockSize);
		}
		if(!isFixX && !isFixY){
			canStandHere &= canStandOn(newX+blockSize, newY+blockSize);
		}
		
		
		if(canStandHere)
			fillMapForNPCRec(npc, newX, newY, steps+1, stopNextRec);
		
	}

	private boolean canStandOn(int xPixel, int yPixel) {
		int x = xPixel / blockSize;
		int y = yPixel / blockSize;
		
		if(YoloKnowledge.instance.positionAufSpielfeld(x, y)){
			//is Collision
			int fieldMask = state.getSimpleState().getMask(x, y);
			if((npcMaske & fieldMask)==0){
				if((npcMaskeDyn & fieldMask) != 0 )
					stopNextRec = true;
				return true;
			}
		}
		return false;
	}

	private void addToHash(int x, int y) {
		hash.add((x<<16)+y);
		
	}

	private boolean isHashed(int x, int y) {
		
		return hash.contains((x<<16)+y);
	}


	private void setCanReach(int x, int y, int steps){
		killmap[x][y] = killmap[x][y] | 1<<steps;
	}

	public int getMinDistanceToEnemy(int x, int y) {
		int distance = Integer.numberOfTrailingZeros(killmap[x][y]);
		if(distance > maxSteps){
			distance = Integer.MAX_VALUE;
		}
		return distance;
	}
	
}
