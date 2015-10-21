package controllers.YOLOBOT.Util.TargedChooser;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

import controllers.YOLOBOT.Agent;
import controllers.YOLOBOT.YoloState;
import controllers.YOLOBOT.SubAgents.HandleMCTS.MCTHandler;
import controllers.YOLOBOT.Util.Heuristics.AStarDistantsHeuristic;
import controllers.YOLOBOT.Util.Heuristics.DeadendHeuristic;
import controllers.YOLOBOT.Util.Heuristics.DistanceToNpcsHeuristic;
import controllers.YOLOBOT.Util.Planner.KnowledgeBasedAStar;
import controllers.YOLOBOT.Util.Wissensdatenbank.PlayerEvent;
import controllers.YOLOBOT.Util.Wissensdatenbank.YoloEvent;
import controllers.YOLOBOT.Util.Wissensdatenbank.YoloKnowledge;

import java.util.PriorityQueue;

import ontology.Types;
import core.game.Observation;

public class TargetChooser {

	private AStarDistantsHeuristic aStarHeuristic;
	private DistanceToNpcsHeuristic npcHeuristic;
	private DeadendHeuristic deadendHeuristic;
	private int[] cooldownList;
	private HashMap<Integer, Integer> singleObjectCooldownSet;
	private InterestingTarged lastTarget;
	private int fastChangeCounter;
	private int gameTickSinceLastTargetchanged;
	
	private int timeSinceNearAtTargetAndNothingHappens = 0;
	private int timeSinceDistancedidntdecrease = 0;
	private int timeSinceTargetIsInLock = 0;
	private double oldScore = -1;
	private int closestDistanceEver; 
	private int tickCounter;
	boolean exploit;
	private LinkedList<InterestingTarged> possibleTargets;
	private int foundScoreLastTimeCount;
	private int foundUnseenObjectTimeCount;
	private int[] iTypeFoundCount;
	private int[] lastTimeInTarget;
	private int targetInAimCounter;
	public boolean lockTarget;
	private int timeSinceNoScoreIncrease, timeSinceNothingInterestingInTarget;
	private int softlockTarget;
	

	public TargetChooser(AStarDistantsHeuristic aSDH, DistanceToNpcsHeuristic npcH, DeadendHeuristic deH) {
		aStarHeuristic = aSDH;
		npcHeuristic = npcH;
		deadendHeuristic = deH;
		cooldownList = new int[YoloKnowledge.INDEX_MAX];
		lastTimeInTarget = new int[YoloKnowledge.INDEX_MAX];
		lastTarget = null;
		singleObjectCooldownSet = new HashMap<Integer, Integer>();
		tickCounter = 0;
		closestDistanceEver = Integer.MAX_VALUE;
		targetInAimCounter = -1;
		timeSinceNothingInterestingInTarget = 0;
		timeSinceNoScoreIncrease = 0;
		fastChangeCounter = 0;
		gameTickSinceLastTargetchanged = 0;
		
	}

	public void handle(YoloState state) {
		exploit = exploit(state);
		if(exploit && !Agent.UPLOAD_VERSION)
			System.out.println("Exploit!");
		if (shouldChangeTarget(state)) {
			changeTarget(state);
		}
		
		if(lastTarget != null && lastTarget.isUnseen() && YoloKnowledge.instance.isStochasticEnemy(YoloKnowledge.instance.itypeToIndex(lastTarget.getObs().itype)) && lastTarget.getDistance() < 10){
			//Walking towards an unseen stochastic NPC --> Increase Rollouts!
			MCTHandler.largerRollouts = true;
		}else{
			MCTHandler.largerRollouts = false;
		}
	}

	private void changeTarget(YoloState state) {
		KnowledgeBasedAStar kBA = new KnowledgeBasedAStar(state);
		kBA.setCountFoundItypes(true);
		singleObjectCooldownSetCleanup(state);
		kBA.setBlacklistedObjects(singleObjectCooldownSet.keySet());
		kBA.calculate(state.getAvatarX(), state.getAvatarY(),
				state.getAvatar().itype, new int[0], false, true, false);
		kBA.setBlacklistedObjects(null);
		deadendHeuristic.setFieldsReached(kBA.fieldsReachedCount);
		
		Observation[] nearestITypesFound = kBA.nearestITypeObservationFound;
		iTypeFoundCount = kBA.iTypesFoundCount;
		possibleTargets = new LinkedList<InterestingTarged>();
		foundScoreLastTimeCount = 0;
		foundUnseenObjectTimeCount = 0;
		double prioSum = 0;
		double minPrio = Double.MAX_VALUE;
		for (int i = 0; i < nearestITypesFound.length; i++) {
			if (nearestITypesFound[i] == null
					|| nearestITypesFound[i].category == Types.TYPE_FROMAVATAR
					|| nearestITypesFound[i].category == Types.TYPE_AVATAR) {
				continue;
			}

			InterestingTarged target = new InterestingTarged(
					nearestITypesFound[i]);
			calculatePriorityValue(target, iTypeFoundCount[i], state, kBA);
			if(lockTarget && lastTarget != null && lastTarget.getObs().itype == nearestITypesFound[i].itype && target.getPriorityValue()+25 < lastTarget.getPriorityValue()){
				//Prio of locked lastTarget decreased!
				lockTarget = false;
			}
			if(target.getPriorityValue() < minPrio)
				minPrio = target.getPriorityValue();
			prioSum += target.getPriorityValue();
			possibleTargets.add(target);
		}
		double prioAdd = -minPrio;
		prioSum += possibleTargets.size() * prioAdd;
		InterestingTarged bestTarged = null;
		int bestX;
		int bestY;
		/*if(!lockTarget || lastTarget == null || timeSinceTargetIsInLock > 50 || (exploit && lastTarget.isWinCondition())){
			timeSinceTargetIsInLock = 0;
			lockTarget = false;
			double softMaxSum = 0;
			double maxSoftMaxValue = 0;
			for (InterestingTarged target : possibleTargets) {
				double softMaxValue = target.getSoftMaxValue(prioSum, prioAdd);
				softMaxSum += softMaxValue;
				if(softMaxValue > maxSoftMaxValue){
					maxSoftMaxValue = softMaxValue;
				}
			}
			double currentThreshold = 0;
			double rnd = Math.random();
			for (InterestingTarged target : possibleTargets) {
				double currentSoftMaxValue = target.getSoftMaxValue(prioSum, prioAdd);
				currentThreshold += (currentSoftMaxValue/softMaxSum);
				if(rnd < currentThreshold){
					bestTarged = target;
					lockTarget = target.getPriorityValue()>0;
					break;
				}
			}
			
			//bestTarged = possibleTargets.peek();
	
			if (bestTarged == null) {
				aStarHeuristic.disable();
				npcHeuristic.disable();
				return;
			}
		}else{
			bestTarged = lastTarget;
		}*/
		

		if (possibleTargets.isEmpty()) {
			aStarHeuristic.disable();
			npcHeuristic.disable();
			return;
		}
		
		//do best First:
		Collections.sort(possibleTargets);
		bestTarged = possibleTargets.getFirst();
		

		if(bestTarged.getPriorityValue() < 75){
			timeSinceNothingInterestingInTarget++;
			YoloKnowledge.instance.setMinusScoreIsBad(timeSinceNothingInterestingInTarget < 200);
		}else{
			timeSinceNothingInterestingInTarget = 0;
		}
		
		
		if(bestTarged.getPriorityValue() < -300) {
			//System.out.println("Disable (" + bestTarged.getPriorityValue() +")");
			aStarHeuristic.disable();
			npcHeuristic.disable();
			return;
		}
		
			
		timeSinceTargetIsInLock++;
		bestX = (int) (bestTarged.getObs().position.x / state.getBlockSize());
		bestY = (int) (bestTarged.getObs().position.y / state.getBlockSize());

		bestX = Math.max(0, Math.min(bestX, kBA.distance.length));
		bestY = Math.max(0, Math.min(bestY, kBA.distance[0].length));
		int distance = kBA.distance[bestX][bestY];
	
		// Unterscheiden zwischen NPC und anderem Observation.
		
		//Calculate from Target to avatar:
		int bestTargedIndex = YoloKnowledge.instance.itypeToIndex(bestTarged.getObs().itype);
		int targetFirstStopX = kBA.nearestITypeObservationFoundFirstTargetX[bestTargedIndex];
		int targetFirstStopY = kBA.nearestITypeObservationFoundFirstTargetY[bestTargedIndex];
		kBA.setMoveDirectionInverse(true);
		kBA.calculate(targetFirstStopX, targetFirstStopY, state.getAvatar().itype, new int[0], false, true, false);
		int avatarPosX = state.getAvatarX();
		int avatarPosY = state.getAvatarY(); 

		
		if (bestTarged.getObs().category == Types.TYPE_NPC) {
			aStarHeuristic.disable();
			npcHeuristic.setNpc(bestTarged.getObs().obsID, kBA.distance, kBA.interpretedAsWall);
			npcHeuristic.setTargetIsToUse(bestTarged.isUseable());

		} else {			
			aStarHeuristic.setDistance(kBA.distance, kBA.interpretedAsWall, bestX, bestY, bestTarged.getObs().itype);
			aStarHeuristic.setTargetIsToUse(bestTarged.isUseable());
			npcHeuristic.disable();
		}
		MCTHandler.scoreLookaheadHeuristic.refreshWalls(kBA.interpretedAsWall);
		
		checkTarget(
				bestTarged,
				state,
				distance);
	}

	private boolean queueEqualComparetor(PriorityQueue<InterestingTarged> oldQueue) {
		if(oldQueue == null || oldQueue.size() != possibleTargets.size()){
			return false;
		}else{
			return possibleTargets.containsAll(oldQueue);
		}
	}

	private void singleObjectCooldownSetCleanup(YoloState state) {
		int tick = state.getGameTick();
		for (Iterator<Entry<Integer, Integer>> iterator = singleObjectCooldownSet.entrySet().iterator(); iterator.hasNext();) {
			Entry<Integer, Integer> blackListEntry = (Entry<Integer, Integer>) iterator.next();
			if(blackListEntry.getValue()<tick){
				iterator.remove();
			}
		}
		
	}

	private void checkTarget(InterestingTarged target, YoloState state,
			int distance) {
		Observation observation = target.getObs();
		if(lastTarget == null || lastTarget.getObs().obsID != observation.obsID){
			if(lastTarget != null){
				lastTimeInTarget[YoloKnowledge.instance.itypeToIndex(lastTarget.getObs().itype)] = state.getGameTick();
				if((gameTickSinceLastTargetchanged - state.getGameTick()) < 10){
					fastChangeCounter++;
				}else{
					fastChangeCounter = 0;
				}
				gameTickSinceLastTargetchanged = state.getGameTick();

				if(fastChangeCounter >= 6 && (gameTickSinceLastTargetchanged-state.getGameTick())< 10){
					softlockTarget = target.getObs().obsID;
				}
			}
			lastTarget = target;
			timeSinceNearAtTargetAndNothingHappens = state.getGameTick();
			tickCounter = 0;
			timeSinceDistancedidntdecrease = state.getGameTick();
			closestDistanceEver = Integer.MAX_VALUE;
			targetInAimCounter = 0;
			return;
		}else
			targetInAimCounter++;
		if(targetInAimCounter < 2){
			return;
		}

//		System.out.println("\t\t Stehe daneben: " + tickCounter);
//		System.out.println("\t\t Stehe nahe: " + (state.getGameTick()-timeSinceNearAtTargetAndNothingHappens));
//		System.out.println("\t\t Komm nicht naeher seit: " + (state.getGameTick()-timeSinceDistancedidntdecrease));

		double timeMultiplier = Math.max(1, target.getPriorityValue()/50);
		int nearMaxTime = (int) (20*timeMultiplier);
		int mediumMaxTime = (int) (20*timeMultiplier);
		int comeCloserMaxTime = (int) (40*timeMultiplier);
		
		int distanceCheckRangeClose = 2;
		int distanceCheckRangeMedium = 3;
		if (observation.category == Types.TYPE_RESOURCE
				|| observation.category == Types.TYPE_STATIC)
			distanceCheckRangeMedium = 3;
		boolean uninteresting = false;
		
		// Check if target cant be reached
		if(distance == distanceCheckRangeClose){
			if(tickCounter >= nearMaxTime){
				uninteresting = true;
			}else{
				tickCounter++;
			}
		}else{
			tickCounter = 0;
		}
		if(distance <= distanceCheckRangeMedium){
			if(timeSinceNearAtTargetAndNothingHappens + mediumMaxTime <= state.getGameTick())
				uninteresting = true;			
		}else{
			timeSinceNearAtTargetAndNothingHappens = state.getGameTick();
		}
		if(closestDistanceEver > distance){
			closestDistanceEver = distance;
			timeSinceDistancedidntdecrease = state.getGameTick();
		}else if(closestDistanceEver <= distance && timeSinceDistancedidntdecrease + comeCloserMaxTime <= state
				.getGameTick()){
			singleObjectCooldownSet.put(observation.obsID, state.getGameTick() + 100);
			lockTarget = false;
			if(!Agent.UPLOAD_VERSION)
				System.out.println("in Tick: " + state.getGameTick() + "\n" + "Set iType: " + observation.itype + " with Category " + observation.category + " on single Blacklist for " + (state.getGameTick() + 100) + " with ID " + observation.obsID);
			timeSinceDistancedidntdecrease = state.getGameTick();
			closestDistanceEver = Integer.MAX_VALUE;
		
		}
		if(aStarHeuristic.isActive()){
			if(aStarHeuristic.EvaluateWithoutNormalisation(state) == 0){
				if(!Agent.UPLOAD_VERSION)
					System.out.println("AStar Heuristic   " + "in Tick: " + state.getGameTick() + "\n" + "Set iType: " + observation.itype + " with Category " + observation.category + " on single Blacklist for " + (state.getGameTick() + 100));
				singleObjectCooldownSet.put(observation.obsID, state.getGameTick() + 100);
				lockTarget = false;
			}
		}else if(npcHeuristic.isActive()){
			if(npcHeuristic.EvaluateWithoutNormalisation(state) == 0){
				if(!Agent.UPLOAD_VERSION)
					System.out.println("NPC Heuristic   " + "in Tick: " + state.getGameTick() + "\n" + "Set iType: " + observation.itype + " with Category " + observation.category + " on single Blacklist for " + (state.getGameTick() + 100));
				singleObjectCooldownSet.put(observation.obsID, state.getGameTick() + 100);
				lockTarget = false;
			}
		}
		
		if (oldScore < state.getGameScore()) {
			//Reset Counters if got some score
			timeSinceNearAtTargetAndNothingHappens = state.getGameTick();
			tickCounter = 0;
			timeSinceDistancedidntdecrease = state.getGameTick();
			closestDistanceEver = Integer.MAX_VALUE;
		}
		
		if (uninteresting) {
			int index = YoloKnowledge.instance.itypeToIndex(observation.itype);
			cooldownList[index] = state.getGameTick() + 100;
			if(!Agent.UPLOAD_VERSION)
				System.out.println("Ignore IType: " + observation.itype + " for "
					+ (state.getGameTick() + 100) + "Ticks");
			lockTarget = false;
		}

		// System.out.println("time Since Near At Target And Nothing Happens: "
		// + (state.getGameTick() - timeSinceNearAtTargetAndNothingHappens));
		lastTarget = target;
		oldScore = state.getGameScore();

	}

	private double calculatePriorityValue(InterestingTarged target,
			int iTypeFoundCount, YoloState state, KnowledgeBasedAStar kBA) {
		Observation observation = target.getObs();
		double prioValue = 0;
		int posX = (int) (observation.position.x / state.getBlockSize());
		int posY = (int) (observation.position.y / state.getBlockSize());
		PlayerEvent pEvent = YoloKnowledge.instance.getPlayerEvent(
				state.getAvatar().itype, observation.itype, true);
		YoloEvent event = pEvent.getEvent(state.getInventoryArray());
		int slot = event.getAddInventorySlotItem();
		boolean winState = event.getWinGame();
		boolean scoreIncrease = event.getScoreDelta() > 0;
		boolean notSeenYet = pEvent.getObserveCount() == 0;
		if(notSeenYet)
			foundUnseenObjectTimeCount++;
		boolean useActionEffective = YoloKnowledge.instance
				.canInteractWithUse(state.getAvatar().itype, observation.itype);
		boolean inventoryIncrease = event.getAddInventorySlotItem() != -1;
		boolean isBadSpawner = false;
		boolean decreaseScore = event.getScoreDelta() < 0;
		boolean willCancel = pEvent.willCancel(state.getInventoryArray());
		boolean isProbablyWall = (double)pEvent.getCancelCount()/(double)pEvent.getObserveCount() > 0.95;
		boolean killedOnColision = event.getKill();
		boolean isPortal = observation.category == Types.TYPE_PORTAL;
		boolean iTypeChange = event.getIType() != -1;
		boolean gotScoreThroughUseAction = YoloKnowledge.instance.getIncreaseScoreIfInteractWith(state.getAvatar().itype, observation.itype);
		
		//Is alone:
		int mask = state.getSimpleState().getMask(posX,posY);
		boolean standsOnWall = Integer.numberOfTrailingZeros(mask) + Integer.numberOfLeadingZeros(mask) != 31;

		if (YoloKnowledge.instance.isSpawner(observation.itype)) {
			isPortal = false;
			int iTypeIndexOfSpawner = YoloKnowledge.instance
					.getSpawnIndexOfSpawner(observation.itype);
			PlayerEvent spawnedPEvent = YoloKnowledge.instance.getPlayerEvent(
					state.getAvatar().itype,
					YoloKnowledge.instance.indexToItype(iTypeIndexOfSpawner),
					true);
			YoloEvent spawnedEvent = spawnedPEvent.getEvent(state
					.getInventoryArray());
			isBadSpawner = spawnedEvent.getKill() || spawnedPEvent.getObserveCount() == 0;
		}
		boolean isInventoryFull = false;
		if (inventoryIncrease) {

			isInventoryFull = YoloKnowledge.instance.getInventoryMax(slot) == state
					.getInventoryArray()[slot];
		}else{
			for (int i = 0; i < 5; i++) {
				if(YoloKnowledge.instance.getInventoryMax(i) == state.getInventoryArray()[i]){
					isInventoryFull = true;
					break;
				}
			}
			
		}
		double scoreIncreaseValue = event.getScoreDelta();
		int distanceToAvatar = kBA.distance[posX][posY];
		int index = YoloKnowledge.instance.itypeToIndex(observation.itype);
		if(cooldownList[index] > state.getGameTick()){
			prioValue -= 500;
		}
		
		if (isBadSpawner && !useActionEffective) {
			prioValue -= 1000;
		}
		if (decreaseScore) {
			prioValue -= 1000;
		}
		if (distanceToAvatar <= 6) {
			prioValue += 21;
		}

		if (winState) {
			prioValue += exploit ? 0:1000;
		} else if (notSeenYet && isPortal) {
			prioValue += 150;
		} else if (isPortal && isInventoryFull) {
			prioValue += 150;
		}else if (willCancel) {
			if(isProbablyWall){
				prioValue -= 250;
			}else
				prioValue -= 200;
		} else if (scoreIncrease && inventoryIncrease) {
			foundScoreLastTimeCount++;
			if(isInventoryFull)
				prioValue += 130;
			else
				prioValue += 140;
		} else if (scoreIncrease) {
			foundScoreLastTimeCount++;
			prioValue += 135;
		} else if (inventoryIncrease) {
			prioValue += 132;
		} else if (notSeenYet) {
			prioValue += 131;
		} else if (!killedOnColision && useActionEffective) {
			if(gotScoreThroughUseAction){
				foundScoreLastTimeCount++;
				prioValue += 130;
			}
			else
				prioValue += 90;
		} else if (iTypeChange) {
			prioValue += 100;
		} else if (killedOnColision && useActionEffective) {
			if(gotScoreThroughUseAction){
				if(YoloKnowledge.instance.getNpcMovesEveryXTicks(YoloKnowledge.instance.itypeToIndex(observation.itype))>2)
					prioValue += 125;
				else
					prioValue += 105;
				foundScoreLastTimeCount++;
			}
			else
				prioValue += 85;
		} else if (killedOnColision && !useActionEffective) {
			prioValue -= 100;
		}
		
//		prioValue = prioValue/10;
//		if(cooldownList[YoloKnowledge.instance.itypeToIndex(observation.itype)] > state.getGameTick()){
//			prioValue -= 50000;
//		}
		
		if(observation.obsID == softlockTarget){
			prioValue += 25;
		}
		
		//Bonus for longer not in target:
		int notTargetBonus = Math.min(100, (state.getGameTick() - lastTimeInTarget[index])/10);
		prioValue += notTargetBonus;
		
		target.setPriorityValue(prioValue);
		target.setWinCondition(winState);
		target.setIsUseable(useActionEffective);
		target.setUnseen(notSeenYet);
		target.setDistance(distanceToAvatar);;
		target.setScoreIncrease(gotScoreThroughUseAction || scoreIncrease);
		return prioValue;
	}

	private boolean exploit(YoloState state) {
		// TODO Entscheide ob man gewinnen soll oder farmen.
		
		boolean isFastMovingDeadlyStochasticEnemy = false;
		if(state.getNpcPositions() != null && state.getNpcPositions().length > 0){
			for (int i = 0; i < state.getNpcPositions().length; i++) {
				if(state.getNpcPositions()[i] != null && !state.getNpcPositions()[i].isEmpty()){
					int enemyItype = state.getNpcPositions()[i].get(0).itype;
					int enemyIndex = YoloKnowledge.instance.itypeToIndex(enemyItype);
					
					if(YoloKnowledge.instance.isStochasticEnemy(enemyIndex)){
						PlayerEvent enemyEvent = YoloKnowledge.instance.getPlayerEvent(state.getAvatar().itype, enemyItype, true);
						YoloEvent event = enemyEvent.getEvent(state.getInventoryArray());
						if(event.getKill() /*&& !YoloKnowledge.instance.canInteractWithUse(state.getAvatar().itype, enemyItype)*/){
							isFastMovingDeadlyStochasticEnemy = true;
						}
					}
				}
			}
		}
		
		if(state.getGameTick() >= 1900 || isFastMovingDeadlyStochasticEnemy || possibleTargets == null || possibleTargets.isEmpty()){
			//Wollen das spiel beenden!
			MCTHandler.winH.setIntrestedInWinning(true);
			return false;
		}
		
//		if(foundUnseenObjectTimeCount > 0){
//			//Gibt noch unbekannte Objekte --> aufsuchen!
//			MCTHandler.winH.setIntrestedInWinning(false);
//			return true;
//		}

		int scoreViaWinCount = 0;
		int winCount = 0;
		for (InterestingTarged winningTarget : possibleTargets) {
			if(winningTarget == null || !winningTarget.isWinCondition())
				continue;
			winCount++;
			if(winningTarget.isScoreIncrease())
				scoreViaWinCount++;
		}
		if(winCount > 0){
			if(scoreViaWinCount == foundScoreLastTimeCount){
				//Win ist die einzige moeglichkeit den score zu erhoehen ---> Will gewinnen!
				MCTHandler.winH.setIntrestedInWinning(true);
				return false;			
			}else{
				//Da gibts noch mehr Punkte -> einsammeln!
				MCTHandler.winH.setIntrestedInWinning(false);
				return true;
			}
		}else{
			//Kenne keine Win-Conditions --> Will gewinnen!
			MCTHandler.winH.setIntrestedInWinning(true);
			return false;			
			
		}	
	}

	private boolean canBeSpawned(int itype) {
		if(iTypeFoundCount == null)
			return false;

		int spawner = YoloKnowledge.instance.getSpawnerIndexOfSpawned(itype);
		if(spawner == -1 || spawner == YoloKnowledge.instance.itypeToIndex(itype))
			return false;
		else
			return iTypeFoundCount[spawner]>0;
		
		
	}

	private boolean isNearPotentialEndTick(YoloState state) {
		if ((state.getGameTick() > 0 && state.getGameTick() < 450)
				|| (state.getGameTick() > 500 && state.getGameTick() < 950)
				|| (state.getGameTick() > 1000 && state.getGameTick() < 1450)
				|| (state.getGameTick() > 1500 && state.getGameTick() < 1900)){
			return false;
		}else
			return true;
	}

	private boolean shouldChangeTarget(YoloState state) {
		boolean shouldChange = true;
		
		
		return shouldChange;
	}

	public boolean isOnBlacklist(Observation obs, YoloState state) {
		int index = YoloKnowledge.instance.itypeToIndex(obs.itype);
		if(cooldownList[index] > state.getGameTick()){
			return true;
		}else if(singleObjectCooldownSet.containsKey(obs.obsID))
			return true;
		return false;
	}

}
