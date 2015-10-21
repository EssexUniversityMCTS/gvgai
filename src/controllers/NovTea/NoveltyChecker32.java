package controllers.NovTea;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import core.game.Observation;
import core.game.StateObservation;
import tools.Vector2d;

public class NoveltyChecker32 {
	
	public static HashSet<PairObjects> setOO;
	public static HashSet<PairScoreObject> setSO;
	public static HashSet<PairObjectAvatar> setOA;
	
	public static HashSet<ObjectState> setO;
	public static HashSet<AvatarState> setA;
	public static HashSet<Double> setS;
	
	
	public NoveltyChecker32(){}
	
	
	public void reset() {
		setOO = new HashSet<PairObjects>(10000);
		setSO = new HashSet<PairScoreObject>(10000);
		setOA = new HashSet<PairObjectAvatar>(10000);
		
		setO = new HashSet<ObjectState>(2000);
		setA = new HashSet<AvatarState>(2000);
		setS = new HashSet<Double>(200);
	}
	
	
	public NoveltyChecker32(StateObservation state){
		reset();
		this.addEntireState(state);
	}

	
	private void addEntireState(StateObservation state) {
		ArrayList<Observation>[][] grid = state.getObservationGrid();
		int size1 = grid.length;
		int size2 = grid[0].length;
		
		AvatarState avatarState = this.loadAvatarState(state);
		setA.add(avatarState);
		ArrayList<Observation> obsList1;
		ObjectState os1;
		double score = state.getGameScore();
		setS.add(score);
		
		for (int i1 = 0; i1 < size1; i1++){
			for (int j1 = 0; j1 < size2; j1++){
				obsList1 = grid[i1][j1];
				int numObs1 = obsList1.size();
				for (int k1 = 0; k1 < numObs1; k1++){
			
					os1 = new ObjectState(obsList1.get(k1));
					setO.add(os1);
					setOA.add(new PairObjectAvatar(os1, avatarState));
					setSO.add(new PairScoreObject(score, os1));
					
				}
			}
		}
		
	}
	

	
	public boolean shouldExpand(StateObservation state) {
		boolean expand = false;
		
		ArrayList<Observation>[][] grid = state.getObservationGrid();
		int size1 = grid.length;
		int size2 = grid[0].length;		
		
		AvatarState avatarState = this.loadAvatarState(state);
		ArrayList<Observation> obsList1;
		ObjectState os1;
		double score = state.getGameScore();
		
		for (int i1 = 0; i1 < size1; i1++){
			for (int j1 = 0; j1 < size2; j1++){
				obsList1 = grid[i1][j1];
				int numObs1 = obsList1.size();
				for (int k1 = 0; k1 < numObs1; k1++){
			
					os1 = new ObjectState(obsList1.get(k1));
					PairObjectAvatar poa = new PairObjectAvatar(os1, avatarState);
					PairScoreObject pso = new PairScoreObject(score, os1);
					
					if (!setOA.contains(poa)){
						expand = true;
						setOA.add(poa);
					}
					
					if (!setSO.contains(pso)){
						expand = true;
						setSO.add(pso);
					}
				}
			}
		}
		
		return expand;		
	}
	
	
	
	public int getNovelty(StateObservation state) {
		int nov = 0;
		
		ArrayList<Observation>[][] grid = state.getObservationGrid();
		int size1 = grid.length;
		int size2 = grid[0].length;		
		
		AvatarState avatarState = this.loadAvatarState(state);
		ArrayList<Observation> obsList1;
		ObjectState os1;
		double score = state.getGameScore();
		
		if (!setS.contains(score)){
			setS.add(score);
			nov = 1;
		}
		
		if (!setA.contains(avatarState)){
			setA.add(avatarState);
			nov = 1;
		}
		
		for (int i1 = 0; i1 < size1; i1++){
			for (int j1 = 0; j1 < size2; j1++){
				obsList1 = grid[i1][j1];
				int numObs1 = obsList1.size();
				for (int k1 = 0; k1 < numObs1; k1++){
			
					os1 = new ObjectState(obsList1.get(k1));
					PairObjectAvatar poa = new PairObjectAvatar(os1, avatarState);
					PairScoreObject pso = new PairScoreObject(score, os1);
					
					if (!setO.contains(os1)){
						setO.add(os1);
						nov = 1;
					}
					
					if (!setOA.contains(poa)){
						setOA.add(poa);
						if (nov == 0) nov = 32;
					}
					
					if (!setSO.contains(pso)){
						setSO.add(pso);
						if (nov == 0) nov = 32;
					}
				}
			}
		}
		
		return nov;
	}
	
	
	private AvatarState loadAvatarState(StateObservation state) {
		return new AvatarState(state);
	}
}
