package controllers.YOLOBOT.Util.Heuristics;

import java.util.HashMap;
import java.util.Map;

import controllers.YOLOBOT.YoloState;

public class HeuristicList {
	public static HeuristicList instance;
	private final Map<HeuristicType, IHeuristic> heuristics;
	
	public HeuristicList() {
		heuristics = new HashMap<>();
	}

	public void Put(IHeuristic heuristic) {
		heuristics.put(heuristic.GetType(), heuristic);
	}
	
	public void Remove(HeuristicType heuristicType) {
		heuristics.remove(heuristicType);
	}
	
	public int Length() {
		return heuristics.size();
	}
	
	public void SetWeight(HeuristicType heuristicType, int weight) {
		heuristics.get(heuristicType).Weight = weight;
	}

	public double Evaluate(YoloState ys) {
		double sum = 0;
		double weights = 0;
		
		for(IHeuristic heuristic : heuristics.values()) {
			double value = heuristic.Evaluate(ys);
			if (value != 0)  {
				weights += heuristic.Weight;				
			}
			sum += value * heuristic.Weight;
		}
		
		return sum/weights;
	}
	
	public double[] Evaluate(double[][] values) {
		if (values[0].length != heuristics.size()) {
			throw new IllegalArgumentException("Array should have same length as heuristicList");
		}
		
		double[] sum = new double[values.length];
		boolean[] hasNoneZeroValue = new boolean[heuristics.size()];
		double weightSum = Double.MIN_VALUE;
		int j = 0;
		for(IHeuristic heuristic : heuristics.values()) {
			for (int i = 0; i < values.length; i++) {
				if (!hasNoneZeroValue[j] && values[i][j] != 0)
				{
					hasNoneZeroValue[j] = true;
					weightSum += heuristic.Weight;
				}
				
				if (hasNoneZeroValue[j]) {
					double value = values[i][j] * heuristic.Weight / heuristic.GetAbsoluteMax();
					if(!Double.isNaN(value))
						sum[i] += value;
					else
						System.out.println("NAN!");
				}
			}
			j++;
		}
		

		for (int i = 0; i < values.length; i++) {
			sum[i] = sum[i]/weightSum;
		}
		
		return sum;
	}
	
	public double[] EvaluateAll(YoloState ys) {
		double[] values = new double[heuristics.size()];
		
		int i = 0;
		for(IHeuristic heuristic : heuristics.values()) {
			values[i] = heuristic.EvaluateWithoutNormalisation(ys);
			i++;
		}
		
		return values;
	}

	public double[] getBadHeuristicValues(){
		double[] values = new double[heuristics.values().size()];
		
		int i = 0;
		for(IHeuristic heuristic : heuristics.values()) {
			if(heuristic.GetType() == HeuristicType.WinHeuristic)
				values[i] = -1;
			else
				values[i] = 0;
			i++;
		}
		
		return values;
	}
	
	public int getIndexOfHeuristic(HeuristicType type){
		int i = 0;
		for(IHeuristic heuristic : heuristics.values()) {
			if(type == heuristic.GetType())
				return i;
			i++;
		}
		return -1;
	}
}
