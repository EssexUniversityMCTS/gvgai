package controllers.Return42.algorithms.deterministic.puzzleSolver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import controllers.Return42.util.StateObservationUtils;
import core.game.StateObservation;

/**
 * Utility class for finding correlations between movables and possible sinks ( sink = place where a movable should be moved).
 * Guesses the correlation based on the number of occurrences of both types.
 */
public class MovableSinkCorrelationFinder {

	
	public static MovableSinkCorrelation searchForCorrelation( StateObservation state ) {
		Map<Integer,Integer> movableCounts = StateObservationUtils.countTypes( state.getMovablePositions() );
		Map<Integer,Integer> immovableCounts = StateObservationUtils.countTypes( state.getImmovablePositions() );
		
		Map<Integer,Set<Integer>> correlated = correlateMovablesWithImmovables( movableCounts, immovableCounts );
		return chooseBestMatch( correlated, movableCounts, immovableCounts );
	}

	private static Map<Integer, Set<Integer>> correlateMovablesWithImmovables( Map<Integer, Integer> movableCounts, Map<Integer, Integer> immovableCounts) {
		Map<Integer,Set<Integer>> result = new HashMap<>();
		
		for( Integer movableType: movableCounts.keySet() ) {
			int numberOfMovablesOfType = movableCounts.get(movableType);
			Set<Integer> matchingTypes = findMatchingImmovables( numberOfMovablesOfType, immovableCounts ); 

			if (!matchingTypes.isEmpty()) {
				result.put( movableType, matchingTypes);
			}
		}
		
		return result;
	}

	private static Set<Integer> findMatchingImmovables(int numberOfMovablesOfType, Map<Integer, Integer> immovableCounts) {
		Set<Integer> matchingTypes = new HashSet<>();
		
		for( Map.Entry<Integer,Integer> entry: immovableCounts.entrySet() ) {
			if (entry.getValue() == numberOfMovablesOfType ) {
				matchingTypes.add( entry.getKey() );
			}
		}
		
		return matchingTypes;
	}

	private static MovableSinkCorrelation chooseBestMatch(
			Map<Integer, Set<Integer>> correlated,
			Map<Integer, Integer> movableCounts,
			Map<Integer, Integer> immovableCounts) {

		if (correlated.isEmpty()) {
			return new MovableSinkCorrelation(false, 0, 0);
		}
		
		Map.Entry<Integer, Set<Integer>> firstCorrelation = correlated.entrySet().iterator().next();
		
		// todo: This can be improved. Currently, we're just taking the first match.
		// We could think about e.g. taking the match with the highest element count.
		return new MovableSinkCorrelation(true, firstCorrelation.getKey(), firstCorrelation.getValue().iterator().next());
	}

	public static class MovableSinkCorrelation {

		private final boolean didFindCorrelation;
		private final int sinkType;
		private final int movableType;
		
		public MovableSinkCorrelation(boolean didFindCorrelation, int movableType, int sinkType ) {
			this.didFindCorrelation = didFindCorrelation;
			this.sinkType = sinkType;
			this.movableType = movableType;
		}
		
		public boolean didFindCorrelation() {
			return didFindCorrelation;
		}
		
		public int getSinkType() {
			return sinkType;
		}
		
		public int getMovableType() {
			return movableType;
		}
	}
}
