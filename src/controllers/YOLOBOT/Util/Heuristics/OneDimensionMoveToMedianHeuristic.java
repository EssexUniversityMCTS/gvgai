package controllers.YOLOBOT.Util.Heuristics;

import java.util.ArrayList;
import java.util.Arrays;

import controllers.YOLOBOT.YoloState;
import controllers.YOLOBOT.Util.Wissensdatenbank.YoloKnowledge;
import core.game.Observation;

public class OneDimensionMoveToMedianHeuristic extends IHeuristic {

	public static double max = Double.MIN_VALUE;
	private boolean onlyXAxis;
	private int myFixAxisPos;
	private int nearThreshold;
	private int mediumThreshold;
	private double fixMedian;
	
	public OneDimensionMoveToMedianHeuristic(YoloState startState) {
		this.onlyXAxis = true;	//TODO: machen!
		int maxDistance, worldBorder;
		if(onlyXAxis){
			myFixAxisPos = (int)startState.getAvatar().position.y;
			worldBorder = startState.getWorldDimension().height;			
		}else{
			myFixAxisPos = (int)startState.getAvatar().position.x;
			worldBorder = startState.getWorldDimension().width;	
		}
		maxDistance = Math.max(myFixAxisPos, worldBorder-myFixAxisPos);
		nearThreshold = maxDistance/3;
		mediumThreshold = 2*nearThreshold;
		
	}
	
	@Override
	public double Evaluate(YoloState ys) {
		double result =   (EvaluateWithoutNormalisation(ys) / max);
		result *= 2;
		result -= 1;
		return result;
	}

	@Override
	public HeuristicType GetType() {
		return HeuristicType.OneDimensionMoveToMedianHeuristic;
	}

	@Override
	public double EvaluateWithoutNormalisation(YoloState ys) {
		max = onlyXAxis ? ys.getWorldDimension().getWidth() : ys.getWorldDimension().getHeight();
		if(ys != null) {
			double median = fixMedian;//calculateMedian(ys);
			
			double result =   max - Math.abs( ys.getAvatarPosition().x - median);
			return result;
		}
		return 0;
	}	

	public double calculateMedian(YoloState ys) {
		ArrayList<Integer> dynamic = new ArrayList<Integer>();
		for (int i = 0; i < 32; i++) {
			if(YoloKnowledge.instance.isDynamic(i)){
				dynamic.add(YoloKnowledge.instance.indexToItype(i));
			}
		}
		int nearestDistance = Integer.MAX_VALUE;
		Observation nearestObs = null;
		
		ArrayList<Observation> obs = new ArrayList<Observation>();
		for(int itype : dynamic){
			for (Observation observation : ys.getObservationsByItype(itype)) {
				if(isUninterestingObservation(observation, ys))
					break;
//				if(syso)
//					syso = true;
				int distanceToPlayer;
				if(onlyXAxis){
					distanceToPlayer = Math.abs((int)observation.position.y-myFixAxisPos);
				}else{
					distanceToPlayer = Math.abs((int)observation.position.x-myFixAxisPos);
				}
				
				if(distanceToPlayer < nearestDistance){
					nearestDistance = distanceToPlayer;
					nearestObs = observation;
				}
				
				if(distanceToPlayer < nearThreshold){
					//Is near, is important:
					obs.add(observation);
					obs.add(observation);
					obs.add(observation);
//					if(syso)
//						System.out.println("MedianCalc Near: " + observation.itype + " (" + observation.obsID + ")");
				}else if ( distanceToPlayer < mediumThreshold){
					//Is not far away, is a bit important:
					obs.add(observation);
					obs.add(observation);
//					if(syso)
//						System.out.println("MedianCalc Med : " + observation.itype + " (" + observation.obsID + ")");
				}else{
					//Is far away
					obs.add(observation);
//					if(syso)
//						System.out.println("MedianCalc Far : " + observation.itype + " (" + observation.obsID + ")");
				}
			}
		}

		//Test: naehestes objekt
		if(nearestObs != null){
			if(onlyXAxis){
				return nearestObs.position.x;
			}else{
				return nearestObs.position.y;
			}
		}
		
		double[] positions = new double[obs.size()] ;
		int index = 0;
		if(onlyXAxis){
			for(Observation o : obs){
				positions[index] = o.position.x;
				index++;
			}
		}else{
			for(Observation o : obs){
				positions[index] = o.position.y;
				index++;
			}
		}
		
		Arrays.sort(positions);
		
		int n = positions.length;
		double median = -1;
		
		if(n == 0)
			median = 0;//TODO: was clevereres ueberlegen
		else if(n <= 2) 
			median = positions[0];
		else if(n % 2 == 0 ){
			 median = (positions[n/2]+ positions[n/2 +1]) /2;
			
		}else{
			 median = positions[(n)/2];
		}
		return median;
	}

	private boolean isUninterestingObservation(Observation observation,
			YoloState ys) {
		boolean isUninteresting = false;
		
		//Kills me and is no score:
		isUninteresting |= YoloKnowledge.instance.getPlayerEvent(ys.getAvatar().itype, observation.itype, true).getEvent(ys.getInventoryArray()).getKill() & !YoloKnowledge.instance.getIncreaseScoreIfInteractWith(ys.getAvatar().itype, observation.itype);
		
		return isUninteresting;
	}

	@Override
	public double GetAbsoluteMax() {
		return max;
	}

	public void setFixMedian(YoloState state) {
		fixMedian = calculateMedian(state);
	}

	public double getFixMedian() {
		return fixMedian;
	}
}
