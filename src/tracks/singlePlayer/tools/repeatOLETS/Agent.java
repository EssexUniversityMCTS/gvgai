package tracks.singlePlayer.tools.repeatOLETS;

import java.util.ArrayList;
import java.util.Random;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types.ACTIONS;
import ontology.Types.WINNER;
import tools.ElapsedCpuTimer;

public class Agent extends AbstractPlayer {

    /**
     * probability it will react to surprise
     */
    public static double surpriseProb = 1;
    /**
     * probability it will react to not hit walls
     */
    public static double nonMoveProb = 1;

    /**
     * different states the player can be at
     */
    private final int DECIDE_ACTION = 0;
    private final int REPEAT_MOVE = 1;
    private final int REPEAT_NIL = 2;

    /**
     * the previous action taken by the agent
     */
    private ACTIONS pastAction;
    /**
     * the amount of moves it has to repeat
     */
    private double moves;
    /**
     * the amount of nil moves it has to repeat
     */
    private double nilMoves;
    /**
     * the current state of the agent
     */
    private int currentState;
    /**
     * the automated player used for playing
     */
    private AbstractPlayer automatedPlayer;
    /**
     * random object for deciding the distribution
     */
    private Random random;
    /**
     * the action repetition distribution
     */
    private ArrayList<Double> actDist;
    /**
     * the nil repetition distribution
     */
    private ArrayList<Double> nilDist;

    /**
     * Initialize the parameters and construct the automated player
     * 
     * @param stateObs
     *            Observation of the current state.
     * @param elapsedTimer
     *            Timer when the action returned is due.
     */
    public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
	moves = 0;
	nilMoves = 0;
	pastAction = ACTIONS.ACTION_NIL;
	currentState = DECIDE_ACTION;
	automatedPlayer = new tracks.singlePlayer.advanced.olets.Agent(stateObs, elapsedTimer);
	random = new Random();

	actDist = new ArrayList<Double>();
	nilDist = new ArrayList<Double>();

	double[] actionValues = { 0.0, 0.15518707483, 0.459821428571, 0.211734693878, 0.0442176870748, 0.0248724489796,
		0.0218962585034, 0.0182823129252, 0.014880952381, 0.0108418367347, 0.0112670068027, 0.00914115646259,
		0.00659013605442, 0.0046768707483, 0.00276360544218, 0.0014880952381, 0.000425170068027,
		0.00106292517007, 0.000850340136054 };
	for (double v : actionValues) {
	    actDist.add(v);
	}

	double[] nilValues = { 0.0793982448809, 0.227747597158, 0.268700376097, 0.0888006686168, 0.0495194316757,
		0.0432511491851, 0.0321771834517, 0.0307145842039, 0.0215211032177, 0.0185959047221, 0.0158796489762,
		0.0150438779774, 0.0183869619724, 0.0200585039699, 0.0156707062265, 0.0131633932303, 0.00835770998746,
		0.00647722524028, 0.00480568324279, 0.0031341412453, 0.0031341412453, 0.00334308399499,
		0.00188048474718, 0.00208942749687, 0.00146259924781, 0.00208942749687, 0.00125365649812,
		0.00229837024655, 0.00104471374843 };
	for (double v : nilValues) {
	    nilDist.add(v);
	}
    }

    /**
     * get CDF distribution of the distribution sent
     * 
     * @param dist
     *            an array of probabilities
     * @return return CDF array
     */
    private ArrayList<Double> getCDF(ArrayList<Double> dist) {
	ArrayList<Double> array = new ArrayList<Double>();

	array.add(dist.get(0));
	for (int i = 1; i < dist.size(); i++) {
	    array.add(array.get(i - 1) + dist.get(i));
	}
	return array;
    }

    /**
     * get a random number for the input distribution
     * 
     * @param dist
     *            an array of probabilities
     * @return return a number that is sampled from this dist
     */
    private int getNextEmpericalDist(ArrayList<Double> dist) {
	ArrayList<Double> cdf = getCDF(dist);
	double value = random.nextDouble();
	for (int i = 0; i < cdf.size(); i++) {
	    if (value < cdf.get(i)) {
		return i;
	    }
	}
	return dist.size();
    }

    /**
     * decide the next action to be done (either repeating same action or nil or
     * deciding new action)
     * 
     * @param stateObs
     *            Observation of the current state.
     * @param elapsedTimer
     *            Timer when the action returned is due.
     * @return the most suitable action
     */
    @Override
    public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
	ACTIONS currentAction = ACTIONS.ACTION_NIL;

	// respond to surprises by giving adrienctx the control again
	if (random.nextDouble() < surpriseProb) {
	    StateObservation tempState = stateObs.copy();
	    tempState.advance(pastAction);
	    if (tempState.getGameWinner() == WINNER.PLAYER_LOSES) {
		moves = 0;
		nilMoves = 0;
		currentState = DECIDE_ACTION;
		automatedPlayer = new tracks.singlePlayer.advanced.olets.Agent(stateObs, elapsedTimer);
		random = new Random();

		actDist = new ArrayList<Double>();
		nilDist = new ArrayList<Double>();

		double[] actionValues = { 0.0, 0.15518707483, 0.459821428571, 0.211734693878, 0.0442176870748,
			0.0248724489796, 0.0218962585034, 0.0182823129252, 0.014880952381, 0.0108418367347,
			0.0112670068027, 0.00914115646259, 0.00659013605442, 0.0046768707483, 0.00276360544218,
			0.0014880952381, 0.000425170068027, 0.00106292517007, 0.000850340136054 };
		for (double v : actionValues) {
		    actDist.add(v);
		}

		double[] nilValues = { 0.0793982448809, 0.227747597158, 0.268700376097, 0.0888006686168,
			0.0495194316757, 0.0432511491851, 0.0321771834517, 0.0307145842039, 0.0215211032177,
			0.0185959047221, 0.0158796489762, 0.0150438779774, 0.0183869619724, 0.0200585039699,
			0.0156707062265, 0.0131633932303, 0.00835770998746, 0.00647722524028, 0.00480568324279,
			0.0031341412453, 0.0031341412453, 0.00334308399499, 0.00188048474718, 0.00208942749687,
			0.00146259924781, 0.00208942749687, 0.00125365649812, 0.00229837024655, 0.00104471374843 };
		for (double v : nilValues) {
		    nilDist.add(v);
		}
	    }
	}

	// respond to walking into walls by giving adrienctx the control again
	if (random.nextDouble() < nonMoveProb && pastAction != ACTIONS.ACTION_USE && pastAction != ACTIONS.ACTION_NIL) {
	    StateObservation tempState = stateObs.copy();
	    tempState.advance(pastAction);
	    if (tempState.getAvatarPosition().equals(stateObs.getAvatarPosition())
		    && tempState.getAvatarOrientation().equals(stateObs.getAvatarOrientation())) {
		moves = 0;
		nilMoves = 0;
		currentState = DECIDE_ACTION;
	    }
	}

	// handling different states of the controller
	switch (currentState) {
	// give the control to adrienctx to decide what i gonna do
	case DECIDE_ACTION:
	    int temp = getNextEmpericalDist(nilDist);

	    if (pastAction == ACTIONS.ACTION_NIL || (pastAction != ACTIONS.ACTION_NIL && temp == 0)) {
		currentAction = automatedPlayer.act(stateObs, elapsedTimer);
		moves = getNextEmpericalDist(actDist);
		if (moves > 1) {
		    currentState = REPEAT_MOVE;
		}
	    } else {
		currentAction = ACTIONS.ACTION_NIL;
		nilMoves = temp;
		if (temp > 1) {
		    currentState = REPEAT_NIL;
		}
	    }
	    break;
	// repeat the previous move multiple time
	case REPEAT_MOVE:
	    currentAction = pastAction;
	    if (moves >= 1) {
		moves -= 1;
	    } else {
		currentState = DECIDE_ACTION;
	    }
	    break;
	// repeat the nil move between two different actions
	case REPEAT_NIL:
	    currentAction = ACTIONS.ACTION_NIL;
	    if (nilMoves >= 1) {
		nilMoves -= 1;
	    } else {
		currentState = DECIDE_ACTION;
	    }
	    break;
	}

	pastAction = currentAction;

	return currentAction;
    }

}
