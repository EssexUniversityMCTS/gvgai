package controllers.Return42.knowledgebase.observation;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents an score entry.
 * 
 * When a score change happens and more then one event happened in that tick,
 * it's not possible to identify which one changed the score. So we hope that
 * the collision repeats some time later and we can use the average to get the real score.
 *
 */
public class ScoreProperty {
	private Set<Double> scoreChanges;

	public ScoreProperty() {
		super();
		scoreChanges = new HashSet<>();
	}

	public boolean needsExploration() {
		return scoreChanges.isEmpty();
	}
	
	public void addScoreChange(double scoreChange) {
		scoreChanges.add(scoreChange);
	}

	public double getScore() {
		if(scoreChanges.isEmpty()){
			return 0;
		}
		
		double total = 0;
		for( double score: scoreChanges ) {
			total += score;
		}
		
		return total / scoreChanges.size();
	}
}