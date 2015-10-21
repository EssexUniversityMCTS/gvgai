package controllers.Return42.algorithms.melee;

import java.util.List;
import java.util.Random;

import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import core.game.StateObservation;
import core.player.AbstractPlayer;

public class MeleeAgent extends AbstractPlayer {

	private static final int ROLLOUT_DEPTH = 4;
	
    private final Random random;

	public MeleeAgent( StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        this.random = new Random();
    }
	
	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		List<ACTIONS> actions = stateObs.getAvailableActions( true );
		ScoreKeeper scoreKeeper = new ScoreKeeper( actions );
		
		while( hasTimeForOneMoreIteration( elapsedTimer ) ) {
			ACTIONS randomAction = actions.get( random.nextInt( actions.size() ) );
			double score = randomRolloutStartingWith( stateObs, actions, randomAction );
			scoreKeeper.addScore( randomAction, score );			
		}
		
		return scoreKeeper.getBestAction();
	}

	private boolean hasTimeForOneMoreIteration(ElapsedCpuTimer elapsedTimer) {
		return elapsedTimer.remainingTimeMillis() > 5;
	}
	
	private double randomRolloutStartingWith(StateObservation startState, List<ACTIONS> actions, ACTIONS firstAction ) {
		StateObservation rolledState = startState.copy();
		rolledState.advance( firstAction );
		
		for( int i = 0; i < ROLLOUT_DEPTH; i++ ) {
			if (rolledState.isGameOver()) {
				return MeleeScore.evaluate( startState, rolledState );
			}
			
			ACTIONS randomAction = actions.get( random.nextInt( actions.size() ) );
			rolledState.advance( randomAction );
		}
		
		return MeleeScore.evaluate( startState, rolledState );
	}
}
