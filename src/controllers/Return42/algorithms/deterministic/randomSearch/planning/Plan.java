package controllers.Return42.algorithms.deterministic.randomSearch.planning;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Plan implements Comparable<Plan> {

	private final Queue<Step> steps;
	private final double score;
	private final boolean isWinningPlan;
	
	public Plan( List<Step> steps, double score, boolean isWinningPlan ) {
		if (steps.isEmpty())
			throw new IllegalArgumentException( "Cannot create an empty plan." );
		
		this.steps = new LinkedList<>( steps );
		this.score = score;
		this.isWinningPlan = isWinningPlan;
	}
	
	public void pollFirstStep() {
		steps.poll();		
	}
	
	public Step getNextStep() {
		if (steps.isEmpty())
			throw new IllegalStateException( "Empty plans have no actions" );
		
		return steps.peek();
	}
	
	public double getScore() {
		return score;
	}
	
	public int getNumberOfSteps() {
		return steps.size();
	}

	public boolean isWinningPlan() {
		return isWinningPlan;
	}

	@Override
	public int compareTo(Plan other) {
		// higher score > lower score
		int scoreValue = Double.compare( this.score, other.score );
		if (scoreValue != 0)
			return scoreValue;
		
		// shorter plan > longer plan
		return -Integer.compare( this.getNumberOfSteps(), other.getNumberOfSteps() );
	}
}
