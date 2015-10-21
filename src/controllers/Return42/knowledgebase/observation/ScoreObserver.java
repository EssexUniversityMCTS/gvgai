package controllers.Return42.knowledgebase.observation;

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Observable;

import controllers.Return42.knowledgebase.GameInformation;
import controllers.Return42.util.MapUtil;
import controllers.Return42.util.ResourcesAndTypeKey;
import controllers.Return42.util.Util;
import ontology.Types;
import tools.Vector2d;
import core.game.Event;
import core.game.Observation;
import core.game.StateObservation;

/**
 * Observed the game and extracts information about the reward of collision of
 * sprites. It takes the inventory of the avatar into consideration.
 *  
 * @author Frederik Buss-Joraschek
 *
 */
public class ScoreObserver extends Observable implements GameObserver {

	public final static double GAME_LOSE_SCORE = -1000;
	private final static int GAME_TICK_LIMIT = 2000;
	
	private double highscore;
	private int highscoreTick;
	private long observedTicks;
	
	private double lastScore = 0;
	private HashMap<Integer, Integer> lastResources;

	private Map<ResourcesAndTypeKey, Map<Integer, ScoreProperty>> scoreMap = new HashMap<>();
	private GameInformation gameInformation;

	public ScoreObserver( GameInformation gameInformation ) {
		this.gameInformation = gameInformation;
		this.highscore = 0;
		this.highscoreTick = 0;
		this.observedTicks = 0;
	}

	public void preStepObserve(StateObservation stateObs) {
		lastScore = stateObs.getGameScore();
		lastResources = stateObs.getAvatarResources();	
	}

	public void postStepObserve(StateObservation stateObs) {
		double currentScore = stateObs.getGameScore();
		observedTicks++;
		
		if (stateObs.getGameScore() > highscore) {
			highscore = stateObs.getGameScore();
			highscoreTick = stateObs.getGameTick();
		}
		
		Event lastEvent = new Event(stateObs.getGameTick() - 1, true, -1, -1, -1, -1, null);
		NavigableSet<Event> eventsInLastTurn = stateObs.getEventsHistory().tailSet(lastEvent, false);
		for (Event event : eventsInLastTurn) {
			ScoreProperty property = getOrCreateProperty(lastResources, event.activeTypeId, event.passiveTypeId);
			
			double scoreChange;
			if (stateObs.getGameWinner() == Types.WINNER.PLAYER_LOSES && stateObs.getGameTick() < GAME_TICK_LIMIT) {
				scoreChange = GAME_LOSE_SCORE;
			} else if (currentScore != Types.SCORE_DISQ){
				scoreChange = currentScore - lastScore;
			} else {
				scoreChange = 0;
			}
			
			double oldScoreChange = property.getScore();
			property.addScoreChange(scoreChange);
			double newScoreChange = property.getScore();
			if (oldScoreChange != newScoreChange) {
				setChanged();
				// System.out.println("Found new score information:");
				// System.out.println(String.format("\t%d collides with %d => %f",
				// event.activeTypeId,
				// event.passiveTypeId, newScore));
			}

		}
		
		int avatarId = 1; //TODO Get real current avatar type id (alternate avatar)
		Vector2d avatarPos = gameInformation.gamePositionToGridPosition(stateObs.getAvatarPosition());
		if (Util.isGridPositionValid(stateObs, avatarPos)) {
			for (Observation observation : stateObs.getObservationGrid()[(int) avatarPos.x][(int) avatarPos.y]) {
				ScoreProperty property = getOrCreateProperty(lastResources, avatarId, observation.itype);
				if (property.needsExploration()) {
					// We haven't got an collision but we have the same position
					// => Assume it isn't collectible and brings no score
					property.addScoreChange(0);
				}
			}
		}

		notifyObservers();
	}

	private Map<Integer, ScoreProperty> getOrCreateEntry(Map<Integer, Integer> avatarResources, int activeTypeId) {
		ResourcesAndTypeKey key = new ResourcesAndTypeKey(avatarResources, activeTypeId);
		Map<Integer, ScoreProperty> entry = MapUtil.getOrCreate(scoreMap, key, new HashMap<Integer, ScoreProperty>());

		return entry;
	}
	
	private ScoreProperty getOrCreateProperty(Map<Integer, Integer> resources, int activeTypeId, int passiveTypeId) {	
		return MapUtil.getOrCreate(getOrCreateEntry(resources, activeTypeId), passiveTypeId, new ScoreProperty());
	}
		
	
	public Map<Integer, ScoreProperty> getScoringTypes(Map<Integer, Integer> resources, int activeTypeId) {
		return getOrCreateEntry(resources, activeTypeId);
	}
	
	public ScoreProperty getScoreChange(Map<Integer, Integer> resources, int activeTypeId, int passiveTypeId) {
		return getOrCreateProperty(resources, activeTypeId, passiveTypeId);
	}
	
	public boolean isDeadly(Map<Integer, Integer> resources, int activeTypeId, int passiveTypeId) {
		return getScoreChange(resources, activeTypeId, passiveTypeId).getScore() == GAME_LOSE_SCORE;
	}

	public double getMaxScorePerStep() {
		if (highscoreTick == 0)
			return 0;
		else
			return highscore / highscoreTick;
	}
	
	public long getNumberOfObservedTicks() {
		return observedTicks;
	}
	
}


