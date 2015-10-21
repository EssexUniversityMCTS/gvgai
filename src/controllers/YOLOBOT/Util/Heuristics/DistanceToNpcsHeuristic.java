package controllers.YOLOBOT.Util.Heuristics;

import ontology.Types;
import tools.Vector2d;
import controllers.YOLOBOT.YoloState;
import controllers.YOLOBOT.Util.SimpleState;
import controllers.YOLOBOT.Util.Wissensdatenbank.YoloKnowledge;
import core.game.Observation;

public class DistanceToNpcsHeuristic extends IModdableHeuristic {

	private double max;
	private int npcObsId;
	private boolean[][] isWall;
	private int[][] distances;
	private boolean isActive;
	
	@Override
	public HeuristicType GetType() {
		return HeuristicType.DistanceToNpcsHeuristic;
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
	public double getModdedHeuristic(YoloState ys, int avatarX, int avatarY, Vector2d avatarOrientation) {
		if(distances == null || !YoloKnowledge.instance.positionAufSpielfeld(avatarX, avatarY) || !YoloKnowledge.instance.positionAufSpielfeld(ys.getAvatarX(), ys.getAvatarY()) || ys.isGameOver())
			return 0;
		SimpleState simpleState = ys.getSimpleState();
		Observation npc = simpleState.getObservationWithIdentifier(npcObsId);
		
		if(npc == null){
			return 0;
		}
		int npcX = (int)(npc.position.x / ys.getBlockSize());
		int npcY = (int)(npc.position.y / ys.getBlockSize());
		
		if(targetIsToUse && YoloKnowledge.instance.positionAufSpielfeld(npcX, npcY)){
			for (Observation obs : ys.getObservationGrid()[npcX][npcY]) {
				if(obs.category == Types.TYPE_FROMAVATAR)
					return 0;
			}
		}
		double distance;
		double unmoddedDistance = distances[ys.getAvatarX()][ys.getAvatarY()];
		double aStarDistance = distances[avatarX][avatarY];
		if(unmoddedDistance<=3){
			//USe AirDistance
			int airDistance = Math.abs(npcX - avatarX)+Math.abs(npcY - avatarY);
			distance = airDistance;
		}else{
			//Use AStarDistance
			distance = aStarDistance;
			
			if(!avatarOrientation.equals(YoloKnowledge.ORIENTATION_NULL)){
				//Have orientation and am not on target --> interpolate result:

				if(avatarOrientation.equals(YoloKnowledge.ORIENTATION_DOWN))
					avatarY++;
				else if(avatarOrientation.equals(YoloKnowledge.ORIENTATION_UP))
					avatarY--;
				else if(avatarOrientation.equals(YoloKnowledge.ORIENTATION_RIGHT))
					avatarX++;
				else if(avatarOrientation.equals(YoloKnowledge.ORIENTATION_LEFT))
					avatarX--;
				
				if(YoloKnowledge.instance.positionAufSpielfeld(avatarX, avatarY) && canStepOn(avatarX, avatarY)){
					aStarDistance = (aStarDistance+(distances[avatarX][avatarY] + 1))/2.0;
				}
			}
			
		}
		if(max < distance)
			max = distance;
		
		return -distance;
	}

	@Override
	public double GetAbsoluteMax() {
		return max;
	}
	
	public int getNpcObsId() {
		return npcObsId;
	}
	
	public void setNpc(int obsId, int[][] distances, boolean[][] isWall){
		this.distances = distances;
		this.npcObsId = obsId;
		this.isWall = isWall;
		isActive = true;
	}
	
	public void disable(){
		this.distances = null;
		this.npcObsId = -1;
		isActive = false;
	}
	
	@Override
	public boolean isActive() {
		return isActive;
	}
	
	public int[][] getDistances() {
		return distances;
	}

	@Override
	public boolean canStepOn(int x, int y) {
		return !isWall[x][y];
	}

}
