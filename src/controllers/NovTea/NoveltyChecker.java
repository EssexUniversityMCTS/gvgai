package controllers.NovTea;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import core.game.Observation;
import core.game.StateObservation;
import tools.Vector2d;

public class NoveltyChecker {

	public static HashMap<Integer, PositionSet> mapItypePos;
	public static HashSet<Double> setScore;
	public static HashSet<AvatarState> setAvatarState;
	
	public NoveltyChecker(){}
	
	public NoveltyChecker(StateObservation state){
		reset();
		this.addEntireState(state);
	}

	public void reset() {
		mapItypePos = new HashMap<Integer, PositionSet>(50);
		setScore = new HashSet<Double>(100);
		setAvatarState = new HashSet<AvatarState>(600);
	}

	private void addEntireState(StateObservation state) {
		this.add(state.getGameScore());
		this.addAvatarState(this.loadAvatarState(state));
		this.addItypesPositions(state);
	}

	private void addItypesPositions(StateObservation state) {
		ArrayList<Observation>[][] gridObs = state.getObservationGrid();
		ArrayList<Observation> obsTempList;
		Observation obsTemp;
		
		int size1 = gridObs.length;
		int size2 = gridObs[0].length;
		int i, j, k, sizeTemp;
		
		for (i = 0; i < size1; i++){
			for (j = 0; j < size2; j++){
				obsTempList = gridObs[i][j];
				sizeTemp = obsTempList.size();
				for (k = 0; k < sizeTemp; k++){
					obsTemp = obsTempList.get(k);
					this.addItypePos(obsTemp.itype, obsTemp.position, state.getBlockSize());
				}
			}
		}
	}

	public void addItypePos(int itype, Vector2d pos, int factor){
		if (mapItypePos.containsKey(itype)){
			mapItypePos.get(itype).add(pos.x, pos.y, factor);
		}
		else{
			mapItypePos.put(itype, new PositionSet(pos.x, pos.y, factor));
		}
	}
	
	public boolean containsItypePos(int itype, Vector2d pos, int factor){
		if (mapItypePos.containsKey(itype)){
			if (mapItypePos.get(itype).contains(pos.x, pos.y, factor)){
				return true;
			}
		}
		return false;
	}
	

	public boolean shouldExpand(StateObservation actualState) {
		double score = actualState.getGameScore();
		ArrayList<Observation>[][] gridObs = actualState.getObservationGrid();
		ArrayList<Observation> obsTempList;
		Observation obsTemp;
				
		int size1 = gridObs.length;
		int size2 = gridObs[0].length;
		int i, j, k, sizeTemp;
		
		boolean expand = false;
		
		if (!this.contains(score)){
			expand = true;
			this.add(score);
		}
		
		for (i = 0; i < size1; i++){
			for (j = 0; j < size2; j++){
				obsTempList = gridObs[i][j];
				sizeTemp = obsTempList.size();
				for (k = 0; k < sizeTemp; k++){
					obsTemp = obsTempList.get(k);
					if (!this.containsItypePos(obsTemp.itype, obsTemp.position, actualState.getBlockSize())){
						expand = true;
						this.addItypePos(obsTemp.itype, obsTemp.position, actualState.getBlockSize());
					}
				}
			}
		}
		
		AvatarState avatarState = this.loadAvatarState(actualState);
		if (!this.containsAvatarState(avatarState)){
			expand = true;
			this.addAvatarState(avatarState);
		}
			
		return expand;		
	}
	
	
	private void addAvatarState(AvatarState avatarState) {
		setAvatarState.add(avatarState);
	}


	private boolean containsAvatarState(AvatarState avatarState) {
		return setAvatarState.contains(avatarState);
	}
	
	
	private AvatarState loadAvatarState(StateObservation state) {
		return new AvatarState(state);
	}
	
	
	private void add(double score) {
		setScore.add(score);
	}

	
	private boolean contains(double score) {
		return setScore.contains(score);
	}

}
