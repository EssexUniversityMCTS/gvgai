package controllers.Return42.algorithms.melee;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import ontology.Types.ACTIONS;

public class ScoreKeeper {

	private final List<ScoreEntry> entries;
	
	public ScoreKeeper(List<ACTIONS> actions) {
		if (actions.isEmpty())
			throw new IllegalArgumentException( "Actions may not be empty" );
		
		this.entries = buildInitialEntryList( actions );
	}

	private List<ScoreEntry> buildInitialEntryList(List<ACTIONS> actions) {
		List<ACTIONS> copy = new LinkedList<>( actions );
		Collections.shuffle(copy);
		
		List<ScoreEntry> entries = new LinkedList<>();
		for( ACTIONS action: copy ) {
			entries.add( new ScoreEntry(action) );
		}
		
		return entries;
	}

	public void addScore(ACTIONS action, double score) {
		for( ScoreEntry entry: entries ) {
			if (entry.getAction() == action ) {
				entry.addScore( score );
				return;
			}
		}
		
		throw new IllegalArgumentException( "Tried to add unknown action "+action );
	}

	public ACTIONS getBestAction() {
		double bestScore = Double.NEGATIVE_INFINITY;
		ACTIONS bestAction = null;
		
		for( ScoreEntry entry: entries ) {
			if (entry.getAvgScore() > bestScore) {
				bestScore = entry.getAvgScore();
				bestAction = entry.getAction();
			}
		}
		
		return bestAction;
	}

	
	private static class ScoreEntry {
		private final ACTIONS action;
		private double sumOfScores;
		private int occurrences;
		
		public ScoreEntry( ACTIONS action ) {
			this.action = action;
			this.sumOfScores = 0;
			this.occurrences = 0;
		}

		public ACTIONS getAction() {
			return action;
		}
		
		public double getAvgScore() {
			return sumOfScores / occurrences;
		}
		
		public void addScore( double score ) {
			occurrences++;
			sumOfScores += score;
		}
	}
}
