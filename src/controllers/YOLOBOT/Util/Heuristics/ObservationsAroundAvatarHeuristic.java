package controllers.YOLOBOT.Util.Heuristics;

import java.util.ArrayList;

import controllers.YOLOBOT.YoloState;
import core.game.Observation;
import tools.Vector2d;

public class ObservationsAroundAvatarHeuristic extends IHeuristic {

	private ArrayList<Integer> ids = new ArrayList<Integer>();
	public static double max = 0;

	@Override
	public HeuristicType GetType() {
		return HeuristicType.ObservationsAroundAvatarHeuristic;
	}

	@Override
	public double Evaluate(YoloState ys) {
		return EvaluateWithoutNormalisation(ys)/max;
	}

	@Override
	public double EvaluateWithoutNormalisation(YoloState ys) {
		ids = new ArrayList<Integer>();
		if (ys == null)
			return 0;

		Vector2d gridPosition = ys.getAvatarGridPosition();
		for(int x = (int) (gridPosition.x -1); x <= gridPosition.x +1; x++){
			for(int y = (int) (gridPosition.y -1); y <= gridPosition.y +1;y++){
				if(x >= 0 && x < ys.getObservationGrid()[0].length && y >= 0 && y < ys.getObservationGrid().length){
					for(Observation obs : ys.getObservationGrid()[x][y]){
						if(!ids.contains(obs.obsID)){
							ids.add(obs.obsID);
						}
					}
				}
			}
		}
		
		if(max < ids.size()) max = ids.size();
		
		return ids.size();
	}

	@Override
	public double GetAbsoluteMax() {
		return max;
	}
}
