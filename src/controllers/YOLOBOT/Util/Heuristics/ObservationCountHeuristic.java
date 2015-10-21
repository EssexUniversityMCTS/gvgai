package controllers.YOLOBOT.Util.Heuristics;

import java.util.ArrayList;

import controllers.YOLOBOT.YoloState;
import core.game.Observation;

public class ObservationCountHeuristic extends IHeuristic {

	private int itype;
	public static double max = 0;
	
	private ArrayList<Integer> ids = new ArrayList<Integer>();
	
	public ObservationCountHeuristic(int itype){
		this.itype = itype;
	}
	
	@Override
	public HeuristicType GetType() {
		return HeuristicType.ObservationCountHeuristic;
	}

	@Override
	public double Evaluate(YoloState ys) {
		return EvaluateWithoutNormalisation(ys)/max;
	}

	@Override
	public double EvaluateWithoutNormalisation(YoloState ys) {
		if(ys == null) return 0;
		
		ids = new ArrayList<Integer>();
		ArrayList<Observation>[][] grid = ys.getObservationGrid();
		for(ArrayList<Observation>[] listArray : grid){
			for(ArrayList<Observation> list : listArray){
				for(Observation observation : list){
					if(observation.itype == this.itype){
						if(!ids.contains(observation.obsID)){
							ids.add(observation.obsID);
						}
					}
				}
			}
			
		}
		
		double tmp = ids.size();
		if(max < tmp) max = tmp;
		
		return ids.size();
	}

	@Override
	public double GetAbsoluteMax() {
		return max;
	}
}
