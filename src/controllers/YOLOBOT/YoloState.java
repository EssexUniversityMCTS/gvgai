package controllers.YOLOBOT;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

import controllers.YOLOBOT.Util.SimpleState;
import controllers.YOLOBOT.Util.StochasticKillmap;
import controllers.YOLOBOT.Util.Wissensdatenbank.YoloKnowledge;
import ontology.Types;
import ontology.Types.ACTIONS;
import ontology.Types.WINNER;
import tools.Vector2d;
import core.game.Event;
import core.game.Observation;
import core.game.StateObservation;

public class YoloState {

	private StateObservation _stateObservation = null;
	private ArrayList<ACTIONS> _availableActions = null;
	private ArrayList<ACTIONS> _availableActionsIncludeNil = null;
	private ArrayList<ACTIONS> _advancedActions = null;
	private ACTIONS _avatarLastAction = null;
	private Vector2d _avatarOrientation = null;
	private Vector2d _avatarPosition = null;
	private HashMap<Integer, Integer> _avatarResources = null;
	private double _avatarSpeed = -1.0;
	private int _blockSize = -1;
	private TreeSet<Event> _eventHistory = null;
	private ArrayList<Observation>[] _fromAvatarSpritesPositions = null;
	private double _gameScore = -1.0;
	private int _gameTick = -1;
	private WINNER _gameWinner = WINNER.PLAYER_DISQ;
	private ArrayList<Observation>[] _immovablePositions = null;
	private ArrayList<Observation>[] _movablePositions = null;
	private ArrayList<Observation>[] _npcPositions = null;
	private ArrayList<Observation>[][] _observationGrid = null;
	private ArrayList<Observation>[] _portalPositions = null;
	private ArrayList<Observation>[] _resourcesPositions = null;

	private ArrayList<Observation>[] _immovablePositionsToAvatar = null;
	private ArrayList<Observation>[] _movablePositionsToAvatar = null;
	private ArrayList<Observation>[] _npcPositionsToAvatar = null;
	private ArrayList<Observation>[] _portalPositionsToAvatar = null;
	private ArrayList<Observation>[] _resourcesPositionsToAvatar = null;
	private ArrayList<Observation>[] _fromAvatarSpritesPositionsToAvatar = null;

	private ArrayList<Observation> _allObservations = null;
	private HashSet<Integer> _allItypes = null;

	private Dimension _worldDimension = null;

	// Add by Tobi:
	private Observation _agent = null;
	private int _agentX = -1;
	private int _agentY = -1;
	private byte[] _inventoryArray = null;
	private int _inventoryArrayUsageSize = -1;
	private SimpleState _simpleState = null;
	private LinkedList<Integer> _objectIdsUnderObservation = null;
	public static int advanceCounter;
	public static int advanceCounterPerRun;
	private ArrayList<ACTIONS> _availableActionsStochastic,
			_availableActionsNonStochastic;
	private boolean _oneDimensionalIsSet;
	private boolean _oneDimensional;
	private int _maxObsId = -1;
	private double _targetReachedCost;
	private boolean _isGameOverSet = false;
	private boolean _isGameOver;
	public static double avgAdvanceStepTimeNeeded;
	private StochasticKillmap _stochasticKillMap;

	// Add by Elvir:
	public static double currentGameScore;

	public YoloState(StateObservation so) {
		_stateObservation = so;
		_advancedActions = new ArrayList<ACTIONS>();
	}

	private YoloState(StateObservation so, ArrayList<ACTIONS> advancedActions) {
		_stateObservation = so;
		_advancedActions = advancedActions;
	}

	public StateObservation getStateObservation() {
		return _stateObservation;
	}

	public void advance(ACTIONS action) {
		advanceCounter++;
		advanceCounterPerRun++;
		double timeBeforAdvance = Agent.curElapsedTimer.remainingTimeMillis();
		_stateObservation.advance(action);
		double timeAfterAdvance = Agent.curElapsedTimer.remainingTimeMillis();
		avgAdvanceStepTimeNeeded = ((avgAdvanceStepTimeNeeded * (advanceCounterPerRun-1)) + (timeBeforAdvance - timeAfterAdvance))/advanceCounterPerRun;
		//System.out.println("Avarage time for Advance needed: " + avgAdvanceStepTimeNeeded);
		_advancedActions.add(action);
		clear();

		// if(!isGameOver()){
		// KnowledgeBasedAStar aStar = new KnowledgeBasedAStar(this);
		// aStar.calculate(getAvatarX(), getAvatarY(), getAvatar().itype, new
		// int[0], true);
		// }

	}

	private void clear() {
		_availableActions = null;
		_availableActionsIncludeNil = null;
		_avatarLastAction = null;
		_avatarOrientation = null;
		_avatarPosition = null;
		_avatarResources = null;
		_avatarSpeed = -1.0;
		_blockSize = -1;
		_eventHistory = null;
		_fromAvatarSpritesPositions = null;
		_gameScore = -1.0;
		_gameTick = -1;
		_gameWinner = WINNER.PLAYER_DISQ;
		_immovablePositions = null;
		_movablePositions = null;
		_npcPositions = null;
		_observationGrid = null;
		_portalPositions = null;
		_resourcesPositions = null;
		_worldDimension = null;

		// Add by Tobi:
		_agent = null;
		_agentX = -1;
		_agentY = -1;
		_inventoryArray = null;
		_inventoryArrayUsageSize = -1;
		_simpleState = null;
		_objectIdsUnderObservation = null;
		_availableActionsStochastic = null;
		_availableActionsNonStochastic = null;
		_oneDimensionalIsSet = false;
		_maxObsId = -1;
		_targetReachedCost = -1;
		_isGameOverSet = false;
		_stochasticKillMap = null;
	}

	public YoloState copy() {
		return new YoloState(_stateObservation.copy(), _advancedActions);
	}

	public YoloState copyAdvanceLearn(ACTIONS action) {
		YoloState advancedState = copy();
		advancedState.advance(action);
		YoloKnowledge.instance.learnFrom(advancedState, this, action);
		return advancedState;
	}

	public ArrayList<ACTIONS> getAvailableActions() {
		if (_availableActions == null){
			_availableActions = _stateObservation.getAvailableActions();

			if(_availableActions.contains(ACTIONS.ACTION_USE) && YoloKnowledge.instance.avatarLooksOutOfGame(this))
				_availableActions.remove(ACTIONS.ACTION_USE);
		}
		return _availableActions;
	}

	public ArrayList<ACTIONS> getAvailableActions(boolean includeNil) {
		if (_availableActionsIncludeNil == null){
			_availableActionsIncludeNil = _stateObservation
					.getAvailableActions(includeNil);
			if(_availableActionsIncludeNil.contains(ACTIONS.ACTION_USE) && YoloKnowledge.instance.avatarLooksOutOfGame(this))
				_availableActionsIncludeNil.remove(ACTIONS.ACTION_USE);
		}
		return _availableActionsIncludeNil;
	}

	public ArrayList<ACTIONS> getAdvancedActions() {
		return _advancedActions;
	}

	public ACTIONS getAvatarLastAction() {
		if (_avatarLastAction == null)
			_avatarLastAction = _stateObservation.getAvatarLastAction();
		return _avatarLastAction;
	}

	public ACTIONS getLastAdvancedAction() {
		if (_advancedActions == null)
			return ACTIONS.ACTION_NIL;
		return _advancedActions.get(_advancedActions.size() - 1);
	}

	public Vector2d getAvatarOrientation() {
		if (_avatarOrientation == null)
			_avatarOrientation = _stateObservation.getAvatarOrientation();
		return _avatarOrientation;
	}

	public Vector2d getAvatarPosition() {
		if (_avatarPosition == null)
			_avatarPosition = _stateObservation.getAvatarPosition();
		return _avatarPosition;
	}

	public Vector2d getAvatarGridPosition() {
		return getGridPosition(getAvatarPosition());
	}

	public Vector2d getGridPosition(Vector2d position) {
		Vector2d vec = position;
		vec.x = vec.x / getBlockSize();
		vec.y = vec.y / getBlockSize();
		return vec;
	}

	public HashMap<Integer, Integer> getAvatarResources() {
		if (_avatarResources == null)
			_avatarResources = _stateObservation.getAvatarResources();
		return _avatarResources;
	}

	public double getAvatarSpeed() {
		if (_avatarSpeed == -1.0)
			_avatarSpeed = _stateObservation.getAvatarSpeed();
		return _avatarSpeed;
	}

	public int getBlockSize() {
		if (_blockSize == -1.0)
			_blockSize = _stateObservation.getBlockSize();
		return _blockSize;
	}

	public TreeSet<Event> getEventsHistory() {
		if (_eventHistory == null) {
			_eventHistory = _stateObservation.getEventsHistory();
		}
		return _eventHistory;
	}

	public ArrayList<Observation>[] getFromAvatarSpritesPositions() {
		if (_fromAvatarSpritesPositions == null)
			_fromAvatarSpritesPositions = _stateObservation
					.getFromAvatarSpritesPositions();
		return _fromAvatarSpritesPositions;
	}

	public ArrayList<Observation>[] getFromAvatarSpritesPositions(
			Vector2d reference) {
		return _stateObservation.getFromAvatarSpritesPositions(reference);
	}

	public ArrayList<Observation>[] getFromAvatarSpritesPositionsToAvatar() {
		if (_fromAvatarSpritesPositionsToAvatar == null)
			_fromAvatarSpritesPositionsToAvatar = _stateObservation
					.getFromAvatarSpritesPositions(getAvatarPosition());
		return _fromAvatarSpritesPositionsToAvatar;
	}

	public double getGameScore() {
		if (_gameScore == -1.0)
			_gameScore = _stateObservation.getGameScore();
		return _gameScore;
	}

	public int getGameTick() {
		if (_gameTick == -1)
			_gameTick = _stateObservation.getGameTick();
		return _gameTick;
	}

	public WINNER getGameWinner() {
		if (_gameWinner == WINNER.PLAYER_DISQ)
			_gameWinner = _stateObservation.getGameWinner();
		return _gameWinner;
	}

	public boolean isGameOver() {
		if(!_isGameOverSet){
			_isGameOverSet = true;
			_isGameOver = _stateObservation.isGameOver();
		}
		return _isGameOver;
	}

	public ArrayList<Observation>[] getImmovablePositions() {
		if (_immovablePositions == null)
			_immovablePositions = _stateObservation.getImmovablePositions();
		return _immovablePositions;
	}

	public ArrayList<Observation>[] getImmovablePositionsToAvatar() {
		if (_immovablePositionsToAvatar == null)
			_immovablePositionsToAvatar = _stateObservation
					.getImmovablePositions(getAvatarPosition());
		return _immovablePositionsToAvatar;
	}

	public ArrayList<Observation>[] getImmovablePositions(Vector2d reference) {
		return _stateObservation.getImmovablePositions(reference);
	}

	public ArrayList<Observation>[] getMovablePositions() {
		if (_movablePositions == null)
			_movablePositions = _stateObservation.getMovablePositions();
		return _movablePositions;
	}

	public ArrayList<Observation>[] getMovablePositionsToAvatar() {
		if (_movablePositionsToAvatar == null)
			_movablePositionsToAvatar = _stateObservation
					.getMovablePositions(getAvatarPosition());
		return _movablePositionsToAvatar;
	}

	public ArrayList<Observation>[] getMovablePositions(Vector2d reference) {
		return _stateObservation.getMovablePositions(reference);
	}

	public ArrayList<Observation>[] getNpcPositions() {
		if (_npcPositions == null)
			_npcPositions = _stateObservation.getNPCPositions();
		return _npcPositions;
	}

	public ArrayList<Observation>[] getNpcPositionsToAvatar() {
		if (_npcPositionsToAvatar == null)
			_npcPositionsToAvatar = _stateObservation
					.getNPCPositions(getAvatarPosition());
		return _npcPositionsToAvatar;
	}

	public ArrayList<Observation>[] getNpcPositions(Vector2d reference) {
		return _stateObservation.getNPCPositions(reference);
	}

	public ArrayList<Observation>[][] getObservationGrid() {
		if (_observationGrid == null)
			_observationGrid = _stateObservation.getObservationGrid();
		return _observationGrid;
	}

	public ArrayList<Observation>[] getPortalsPositions() {
		if (_portalPositions == null)
			_portalPositions = _stateObservation.getPortalsPositions();
		return _portalPositions;
	}

	public ArrayList<Observation>[] getPortalsPositionsToAvatar() {
		if (_portalPositionsToAvatar == null)
			_portalPositionsToAvatar = _stateObservation
					.getPortalsPositions(getAvatarPosition());
		return _portalPositionsToAvatar;
	}

	public ArrayList<Observation>[] getPortalsPositions(Vector2d reference) {
		return _stateObservation.getPortalsPositions(reference);
	}

	public ArrayList<Observation>[] getResourcesPositions() {
		if (_resourcesPositions == null)
			_resourcesPositions = _stateObservation.getResourcesPositions();
		return _resourcesPositions;
	}

	public ArrayList<Observation>[] getResourcesPositionsToAvatar() {
		if (_resourcesPositionsToAvatar == null)
			_resourcesPositionsToAvatar = _stateObservation
					.getResourcesPositions(getAvatarPosition());
		return _resourcesPositionsToAvatar;
	}

	public ArrayList<Observation>[] getResourcesPositions(Vector2d reference) {
		return _stateObservation.getResourcesPositions(reference);
	}

	public Dimension getWorldDimension() {
		if (_worldDimension == null)
			_worldDimension = _stateObservation.getWorldDimension();
		return _worldDimension;
	}

	public ArrayList<Observation> geObservationsInActionDirection(ACTIONS action) {
		Vector2d vec = getAvatarGridPosition();

		int x = (int) vec.x;
		int y = (int) vec.y;

		switch (action) {
		case ACTION_LEFT:
			x -= 1;
			break;
		case ACTION_RIGHT:
			x += 1;
			break;
		case ACTION_UP:
			y -= 1;
			break;
		case ACTION_DOWN:
			y += 1;
			break;

		default:
			break;
		}

		if (x < 0 || x >= getObservationGrid().length || y < 0
				|| y >= getObservationGrid()[0].length)
			return null;
		return getObservationGrid()[x][y];

	}

	public ArrayList<Observation> getObservationsAtAvatarPosition() {
		return getObservationGrid()[(int) getAvatarGridPosition().x][(int) getAvatarGridPosition().y];
	}

	// Add by Tobi:
	public int getAvatarX() {
		if (_agentX == -1)
			getAvatar();
		return _agentX;
	}

	public int getAvatarY() {
		if (_agentY == -1)
			getAvatar();
		return _agentY;
	}

	public Observation getAvatar() {
		if (_agent != null)
			return _agent;

		// init needed values:
		if (_blockSize == -1)
			getBlockSize();
		if (_avatarPosition == null)
			getAvatarPosition();
		if (_observationGrid == null)
			getObservationGrid();

		_agentX = (int) _avatarPosition.x / _blockSize;
		_agentY = (int) _avatarPosition.y / _blockSize;
		int nr = 0;
		if (_agentX >= 0 && _agentY >= 0 && _agentX < _observationGrid.length
				&& _agentY < _observationGrid[0].length)
			for (Observation obs : _observationGrid[_agentX][_agentY]) {
				if (obs.category == ontology.Types.TYPE_AVATAR) {
					if (obs.itype > nr) {
						_agent = obs;
						nr = obs.itype;
					}
				}
			}
		return _agent;
	}

	public byte[] getInventoryArray() {
		if (_inventoryArray == null) {
			getAvatarResources();
			_inventoryArray = YoloKnowledge.instance
					.getInventoryArray(_avatarResources);
			_inventoryArrayUsageSize = _avatarResources.keySet().size();
		}
		return _inventoryArray;
	}

	public int getInventoryArrayUsageSize() {
		if (_inventoryArrayUsageSize == -1)
			getInventoryArray();
		return _inventoryArrayUsageSize;
	}

	public SimpleState getSimpleState() {
		if (_simpleState == null){
			_simpleState = new SimpleState(this, false);
			_simpleState.fullInit();
		}
		return _simpleState;
	}

	public void setNewSeed(int seed) {
		_stateObservation.setNewSeed(seed);
	}

	public long getHash(boolean ignoreNPCs) {
		int itype = (getAvatar() != null) ? _agent.itype : -1;

		Vector2d orientation = getAvatarOrientation();
		return getModifiedHash(ignoreNPCs, getAvatarX(), getAvatarY(), itype,
				orientation.x, orientation.y);
	}

	public long getModifiedHash(boolean ignoreNPCs, int avatarX, int avatarY,
			int avatarId, double avatarOrientationX, double avatarOrientationY) {
		long prime = 31;

		long result = 17;
		result = result * prime + avatarX;
		result = result * prime + avatarY;
		result = result * prime + avatarId;
		result = result * prime + Double.doubleToLongBits(avatarOrientationX);
		result = result * prime + Double.doubleToLongBits(avatarOrientationY);

		for (int i = 0; i < getObservationGrid().length; i++) {
			result = result * prime + i;
			for (int j = 0; j < getObservationGrid()[i].length; j++) {
				result = result * prime + j;
				for (Observation obs : getObservationGrid()[i][j]) {
					if (obs.category != Types.TYPE_AVATAR
							&& (obs.category != Types.TYPE_NPC || !ignoreNPCs)) {
						result = result * prime + obs.obsID;
						result = result * prime + obs.itype;
					}
				}
			}
		}

		HashMap<Integer, Integer> inventory = getAvatarResources();
		for (int itemId : inventory.keySet()) {
			result = result * prime + itemId;
			result = result * prime + inventory.get(itemId);
		}

		// StringBuilder sb = new StringBuilder();
		// sb.append(avatarOrientationX);
		// sb.append(avatarOrientationY);
		// sb.append(">");
		// sb.append(avatarX);
		// sb.append(",");
		// sb.append(avatarY);
		// sb.append(",");
		// sb.append(avatarId);
		// sb.append(">");
		// for (int i = 0; i < getObservationGrid().length; i++) {
		// for (int j = 0; j < getObservationGrid()[i].length; j++) {
		// sb.append(i);
		// sb.append(",");
		// sb.append(j);
		// sb.append("_");
		// for (Observation obs : getObservationGrid()[i][j]) {
		// if(obs.category != Types.TYPE_AVATAR && (obs.category !=
		// Types.TYPE_NPC || ignoreNPCs)){
		// sb.append(obs.obsID);
		// sb.append(obs.itype);
		// sb.append(";");
		// }
		// }
		// }
		// }
		// sb.append(" ");
		// //Res:
		// HashMap<Integer, Integer> inventory = getAvatarResources();
		// for (int item : inventory.keySet()) {
		// sb.append(item);
		// sb.append(":");
		// sb.append(inventory.get(item));
		// }
		// retVal += "Score:" + state.getGameScore();

		return result;
	}

	public ArrayList<Observation>[] getObservationList(int category) {
		switch (category) {
		case Types.TYPE_FROMAVATAR:
			return getFromAvatarSpritesPositions();
		case Types.TYPE_MOVABLE:
			return getMovablePositions();
		case Types.TYPE_NPC:
			return getNpcPositions();
		case Types.TYPE_PORTAL:
			return getPortalsPositions();
		case Types.TYPE_RESOURCE:
			return getResourcesPositions();

		default:
			// case Types.TYPE_STATIC:
			return getImmovablePositions();
		}
	}

	public LinkedList<Integer> getObjectIdsUnderObservation() {
		return _objectIdsUnderObservation;
	}

	public void addObjectToObservate(int objectIdentifier) {
		if (_objectIdsUnderObservation == null)
			_objectIdsUnderObservation = new LinkedList<Integer>();
		_objectIdsUnderObservation.add(objectIdentifier);
	}

	public ArrayList<ACTIONS> getValidActions(
			boolean ignoreStochasticEnemyKilling) {

		if (ignoreStochasticEnemyKilling
				&& _availableActionsNonStochastic != null)
			return _availableActionsNonStochastic;
		else if (!ignoreStochasticEnemyKilling
				&& _availableActionsStochastic != null)
			return _availableActionsStochastic;

		// ansonsten muss berechnet werden

		ArrayList<ACTIONS> validActions = new ArrayList<Types.ACTIONS>(
				getAvailableActions(true));

		for (Iterator<ACTIONS> iterator = validActions.iterator(); iterator
				.hasNext();) {
			ACTIONS actions = (ACTIONS) iterator.next();
			// TODO: stehen bleiben check -> kill?
			if (YoloKnowledge.instance.moveWillCancel(this, actions, true,
					ignoreStochasticEnemyKilling)) {
				iterator.remove();
			}
		}
		if (ignoreStochasticEnemyKilling)
			_availableActionsNonStochastic = validActions;
		else
			_availableActionsStochastic = validActions;

		return validActions;
	}

	public boolean isOneDimensionGame() {
		if (_oneDimensionalIsSet)
			return _oneDimensional;
		getAvailableActions();
		boolean yAxis = _availableActions.contains(ACTIONS.ACTION_DOWN)
				&& _availableActions.contains(ACTIONS.ACTION_UP);
		boolean xAxis = _availableActions.contains(ACTIONS.ACTION_LEFT)
				&& _availableActions.contains(ACTIONS.ACTION_RIGHT);

		_oneDimensionalIsSet = true;
		_oneDimensional = (yAxis != xAxis);

		return _oneDimensional;
	}

	public int getMaxObsId() {
		return _maxObsId;
	}

	public int setMaxObsId(int id) {
		return _maxObsId = id;
	}

	public ArrayList<Observation> getObservationsByItype(int itype) {
		ArrayList<Observation> dynamics = new ArrayList<Observation>();
		ArrayList<Integer> ids = new ArrayList<Integer>();
		for (ArrayList<Observation>[] arr : getObservationGrid()) {
			for (ArrayList<Observation> list : arr) {
				for (Observation o : list) {
					if (o.itype == itype && !ids.contains(o.obsID)){
						dynamics.add(o);
						ids.add(o.obsID);
					}
				}
			}
		}

		return dynamics;
	}

	public HashSet<Integer> getAllItypes() {
		if (_allItypes == null) {
			_allItypes = new HashSet<Integer>();

			for (ArrayList<Observation>[] arr : getObservationGrid()) {
				for (ArrayList<Observation> list : arr) {
					for (Observation o : list) {
						_allItypes.add(o.itype);
					}
				}
			}
		}

		return _allItypes;
	}

	public void setTargetReachedCost(double targetReachedCost) {
		_targetReachedCost = targetReachedCost;		
	}
	
	public double getTargetReachedDepth() {
		return _targetReachedCost;
	}
	
	public StochasticKillmap getStochasticKillMap() {
		if(_stochasticKillMap == null){
			_stochasticKillMap = new StochasticKillmap(this);
		}
		return _stochasticKillMap;
	}
}
