package controllers.YOLOBOT.Util.Heuristics;

import ontology.Types;
import tools.Vector2d;
import controllers.YOLOBOT.YoloState;
import controllers.YOLOBOT.Util.Wissensdatenbank.YoloKnowledge;
import core.game.Observation;

public class AStarDistantsHeuristic extends IModdableHeuristic{

	private int[][] distance;
	private boolean[][] isWall;
	private double upperBound;
	private double lowerBound;
	private boolean isActive;
	private int targetX;
	private int targetY;
	private int targetItypeIndexMask;
	
	
	
	public AStarDistantsHeuristic(int[][] distance) {
		
		this.distance = distance;
		upperBound = -Double.MAX_VALUE;
		lowerBound = Double.MAX_VALUE;
		isActive = distance != null;
	}
	
	@Override
	public HeuristicType GetType() {
		return HeuristicType.AStarDistantsHeuristic;
	}

	@Override
	public double Evaluate(YoloState ys) {
		return EvaluateWithoutNormalisation(ys)/GetAbsoluteMax();
	}

	@Override
	public double EvaluateWithoutNormalisation(YoloState ys) {
		return getModdedHeuristic(ys, ys.getAvatarX(), ys.getAvatarY(), ys.getAvatarOrientation());
	}

	@Override
	public double getModdedHeuristic(YoloState state, int agentX, int agentY, Vector2d avatarOrientation) {
		if(distance == null || state.isGameOver()){
			return 0;
		}

		//Calculate targetReached Early:
		int mask = state.getSimpleState().getMask(targetX, targetY);
		if(targetIsToUse && (mask & YoloKnowledge.instance.getFromAvatarMask()) != 0){
			//There is a FromAvatar at the target!
			return 0;
		}
		if((mask & targetItypeIndexMask) == 0 ){
			//Itype is no more at targetPosition --> TargetReached
			return 0;
		}
		
		
		double value = 0;
		if (agentX >= 0 && agentY >= 0 && agentX < isWall.length && agentY < isWall[0].length ){
			if(isWall[agentX][agentY] || distance[agentX][agentY] == 0)
				value = lowerBound;
			else
				value = -distance[agentX][agentY] + 1;
		}

		if(value != 0 && !avatarOrientation.equals(YoloKnowledge.ORIENTATION_NULL)){
			//Have orientation and am not on target --> interpolate result:
			
			//TODO: Kollision auf weg besser bewerten!

			if(avatarOrientation.equals(YoloKnowledge.ORIENTATION_DOWN))
				agentY++;
			else if(avatarOrientation.equals(YoloKnowledge.ORIENTATION_UP))
				agentY--;
			else if(avatarOrientation.equals(YoloKnowledge.ORIENTATION_RIGHT))
				agentX++;
			else if(avatarOrientation.equals(YoloKnowledge.ORIENTATION_LEFT))
				agentX--;
			
			if(YoloKnowledge.instance.positionAufSpielfeld(agentX, agentY) && canStepOn(agentX, agentY)){
				//Bonus because looking in right direction:
				value = (value+(-distance[agentX][agentY] + 1))/2.0;
				
				//Bonus if move is canceled:
				int avatarIndex = YoloKnowledge.instance.itypeToIndex(state.getAvatar().itype);
				int fieldMask = state.getSimpleState().getMask(agentX, agentY);
				
				boolean surelyWillNotBlock = (YoloKnowledge.instance.getBlockingMask(avatarIndex) & fieldMask) == 0;
				
				if(!surelyWillNotBlock){
					value += 0.25;
				}
			}
			
		}
			
		if(value < lowerBound)
			lowerBound = value;
		if(value > upperBound)
			upperBound = value;
		return value;
	}

	@Override
	public double GetAbsoluteMax() {
		return Math.abs(lowerBound);
	}
	
	public void setDistance(int[][] distance, boolean[][] isWall, int targetX, int targetY, int targetItype) {
		this.distance = distance;
		this.isWall = isWall;
		isActive = distance != null;
		this.targetX = targetX;
		this.targetY = targetY;
		this.targetItypeIndexMask = 1<<YoloKnowledge.instance.itypeToIndex(targetItype);
	}

	public int[][] getDistance() {
		return distance;
	}

	public void disable() {
		this.distance = null;
		isActive = false;
	}
	
	@Override
	public boolean isActive() {
		return isActive;
	}

	@Override
	public boolean canStepOn(int x, int y) {
		return !isWall[x][y];
	}
}
