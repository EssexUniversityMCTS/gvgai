package controllers.YOLOBOT.SubAgents.Planner;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.LinkedList;

import controllers.YOLOBOT.Agent;
import controllers.YOLOBOT.YoloState;
import controllers.YOLOBOT.SubAgents.SubAgent;
import controllers.YOLOBOT.SubAgents.SubAgentStatus;
import controllers.YOLOBOT.Util.Planner.KnowledgeBasedAStar;
import controllers.YOLOBOT.Util.Planner.KnowledgeBasedPushSlidePlanner;
import controllers.YOLOBOT.Util.Planner.Zustand;
import controllers.YOLOBOT.Util.Wissensdatenbank.YoloKnowledge;
import ontology.Types;
import ontology.Types.ACTIONS;
import ontology.Types.WINNER;
import tools.ElapsedCpuTimer;
import core.game.Observation;

public class PlannerAgent extends SubAgent {

	/**
	 * Observation grid.
	 */
	protected ArrayList<Observation> grid[][];

	/**
	 * block size
	 */
	protected int block_size;

	private YoloState curState;

	private KnowledgeBasedAStar aStar;

	public LinkedList<ACTIONS> actionsToDo;

	private Zustand pushPlannerZustand;

	private int object_to_push, object_to_push_to;

	private int targetNumberToPushTo, objectNumberToPush;
	
	private int itypeNrToPush;
	
	private boolean tryPushToStatic;
	
	private int failedTries;

	/**
	 * Public constructor with state observation and time due.
	 * 
	 * @param startYoloState
	 *            state observation of the current game.
	 * @param elapsedTimer
	 *            Timer for the controller creation.
	 */
	public PlannerAgent(YoloState startYoloState, ElapsedCpuTimer elapsedTimer) {
		startYoloState.advance(ACTIONS.ACTION_NIL);
		block_size = startYoloState.getBlockSize();
		actionsToDo = new LinkedList<Types.ACTIONS>();
		targetNumberToPushTo = 0;
		objectNumberToPush = 0;
		itypeNrToPush = 0;
		tryPushToStatic = false;
		failedTries = 0;
	}

	private void guessTask(YoloState yoloState) {
		if(!Agent.UPLOAD_VERSION)
			System.out.println("Guess Task with knowledge:\n" + YoloKnowledge.instance.toString());
		if(YoloKnowledge.instance.getPushableITypes().size() > 0){
			if(YoloKnowledge.instance.getPushableITypes().size() >= itypeNrToPush)
				itypeNrToPush = 0;
			object_to_push = YoloKnowledge.instance.getPushableITypes().get(itypeNrToPush);
			if(getCountOfIType(yoloState, object_to_push) >= 5)
				object_to_push = -1;
		}else{
			//Found nothing to push!
			object_to_push = -1;
		}
		if(object_to_push == -1){

			if(!Agent.UPLOAD_VERSION)
				System.out.println("Nothing to push!");
			Status = SubAgentStatus.POSTPONED;
			return;
		}
		int indexToPushTo = YoloKnowledge.instance.getPushTargetIndex(YoloKnowledge.instance.itypeToIndex(object_to_push));
		object_to_push_to = indexToPushTo==-1?-1:YoloKnowledge.instance.indexToItype(indexToPushTo);
		
		if(object_to_push_to == -1){
			//Noch kein bekanntes Ziel fuer das Objekt gefunden!
			//Rate eins:

			if(tryPushToStatic){

				if(yoloState.getImmovablePositions() == null){
					object_to_push_to = -1;
					return;
				}
				//Suche haeufigstes und seltenstes Static:
				int mostFrequentStatic = 0;
				int mostFrequentStatic_Count = 0;
				int rarestStatic = 0;
				int rarestStatic_Count = Integer.MAX_VALUE;
				for (ArrayList<Observation> obs : yoloState.getImmovablePositions()) {
					int count = obs.size();
					if(count > 0 && object_to_push != obs.get(0).itype){
						if(count > mostFrequentStatic_Count){
							mostFrequentStatic = obs.get(0).itype;
							mostFrequentStatic_Count = count;
						}
						if(count <= rarestStatic_Count){
							rarestStatic = obs.get(0).itype;
							rarestStatic_Count = count;
						}
					}
				}
				object_to_push_to = rarestStatic;
			}else{
				if(yoloState.getMovablePositions() == null){
					object_to_push_to = -1;
					return;
				}
					
				//Suche haeufigstes und seltenstes Moveable:
				int mostFrequentMoveable = 0;
				int mostFrequentMoveable_Count = 0;
				int rarestMoveable = 0;
				int rarestMoveable_Count = Integer.MAX_VALUE;
				for (ArrayList<Observation> obs : yoloState.getMovablePositions()) {
					int count = obs.size();
					if(count > 0 && object_to_push != obs.get(0).itype){
						if(count > mostFrequentMoveable_Count){
							mostFrequentMoveable = obs.get(0).itype;
							mostFrequentMoveable_Count = count;
						}
						if(count <= rarestMoveable_Count){
							rarestMoveable = obs.get(0).itype;
							rarestMoveable_Count = count;
						}
					}
				}
				object_to_push_to = rarestMoveable;
			}

		}
		

		if(!Agent.UPLOAD_VERSION)
			System.out.println("Object to Push: " + object_to_push);
		if(!Agent.UPLOAD_VERSION)
			System.out.println("Target: " + object_to_push_to);
		
	}

	private int getCountOfIType(YoloState state, int itype) {
		
		int count;
		
		count = getCountOfItypeOfCategory(state, itype, Types.TYPE_MOVABLE);
		if(count == 0)
			count = getCountOfItypeOfCategory(state, itype, Types.TYPE_STATIC);
		
		return count;	
	}

	private int getCountOfItypeOfCategory(YoloState state, int itype, int category) {
		ArrayList<Observation>[] obsList = state.getObservationList(category);
		
		if(obsList == null)
			return 0;
		for (ArrayList<Observation> observations : obsList) {
			if(observations.isEmpty())
				continue;
			else{
				if(observations.get(0).itype == itype)
					return observations.size();
			}
		}
		return 0;
	}

	/**
	 * Picks an action. This function is called every game step to request an
	 * action from the player.
	 * 
	 * @param stateObs
	 *            Observation of the current state.
	 * @param elapsedTimer
	 *            Timer when the action returned is due.
	 * @return An action for the current state
	 */
	public Types.ACTIONS act(YoloState yoloState,
			ElapsedCpuTimer elapsedTimer) {
		curState = yoloState;
		if(curState.getGameTick() == 0)
			curState.advance(ACTIONS.ACTION_NIL);
		

		grid = curState.getObservationGrid();
		
		
		if(actionsToDo.isEmpty()){
			//Es gibt keinen Plan --> Erstelle einen
			while(elapsedTimer.remainingTimeMillis() > 20 && !isPlanExecuteable(pushPlannerZustand)){
				failedTries++;
				if(failedTries > 2){
					Status = SubAgentStatus.POSTPONED;
					failedTries = 0;
					pushPlannerZustand = null;
					if(!Agent.UPLOAD_VERSION)
						System.out.println("Planner Remaing:" + elapsedTimer.remainingTimeMillis());
					return ACTIONS.ACTION_NIL;
				}
				if(!Agent.UPLOAD_VERSION)
					System.out.println("Create Plan!");
				guessTask(yoloState);
				
				if(object_to_push == -1){
					Status = SubAgentStatus.POSTPONED;
					pushPlannerZustand = null;
					return ACTIONS.ACTION_NIL;
				}
				
				pushSlide();
				if(pushPlannerZustand == null){
					objectNumberToPush = 0;
					tryPushToStatic = !tryPushToStatic;
				}
			}
			pushPlannerZustand = null;
			if(!Agent.UPLOAD_VERSION)
				System.out.println("Found a plan!");
		}
		if(!Agent.UPLOAD_VERSION)
			System.out.println("Planner Remaing:" + elapsedTimer.remainingTimeMillis());
		
		
		if (actionsToDo.size() > 0){
			//Es gibt einen Plan!
			failedTries = 0;
			return actionsToDo.removeFirst();
		}
		return Types.ACTIONS.ACTION_NIL;
	}
	
	public boolean isPlanExecuteable(Zustand zustand){
		if(zustand == null)
				return false;
		YoloState state = curState.copy();
		actionsToDo.clear();
		while(zustand != null && zustand.lastDirection != 127){
			LinkedList<ACTIONS> actions = planToActions(zustand, state);
			actionsToDo.addAll(actions);
			
			if(state.getGameTick() <= 1){
				//Sometimes in the first tick there is no move done!
				ACTIONS action = actions.removeFirst();
				int xBefore = state.getAvatarX();
				int yBefore = state.getAvatarY();
				state.advance(action);
				if(xBefore == state.getAvatarX() && yBefore == state.getAvatarY()){
					actions.addFirst(action);
					actionsToDo.addFirst(action);
				}
			}
			boolean lastRun = zustand.nachfolger == null || zustand.nachfolger.lastDirection == 127;
			while (actions.size() > 0){
				
				if(lastRun && actions.size() == 1){
					//Letzte Ausfuehrung des letzten Aufrufs:
					int score = (int) state.getGameScore();
					state = state.copyAdvanceLearn(actions.removeFirst());
					if(state.getGameScore() > score){
						//Ist guter push!
						YoloKnowledge.instance.setPushTargetIndex(YoloKnowledge.instance.itypeToIndex(object_to_push), YoloKnowledge.instance.itypeToIndex(object_to_push_to));
					}
				}else{
					state.advance(actions.removeFirst());
				}
			}
			if(state.getAvatarX() == zustand.lastX && state.getAvatarY() == zustand.lastY || state.getGameWinner() == WINNER.PLAYER_WINS){
				//Hat geklappt!
			}else{
				//Sind wo anders rausgekommen!
				return false;
			}
			zustand = zustand.nachfolger;
		}
		return true;
	}

	private LinkedList<ACTIONS> planToActions(Zustand zustand, YoloState state) {
		LinkedList<ACTIONS> actions = new LinkedList<Types.ACTIONS>();
		if(zustand.lastDirection == 127){
			//Ist der End-Zustand!
			return actions;
		}
		int agentX = state.getAvatarX();
		int agentY = state.getAvatarY();
		
		if(zustand.lastPlayerX != agentX || zustand.lastPlayerY != agentY){
		//Player has to move to position!

			KnowledgeBasedAStar wayFinding = new KnowledgeBasedAStar(state);
			wayFinding.setIllegalMove(zustand.lastX, zustand.lastY, object_to_push);
			
			wayFinding.calculate(agentX, agentY, state.getAvatar().itype, new int[]{}, false);
			actions = wayFinding.extractActions(zustand.lastPlayerX, zustand.lastPlayerY);
			aStar = wayFinding;

		}

		actions.addLast(zustand.getAction());
		return actions;
	}

	private void pushSlide() {		
		int agentX = curState.getAvatarX();
		int agentY = curState.getAvatarY();
		
		int firstValidXTarget = -1, xTarget = -1;
		int firstValidYTarget = -1, yTarget = -1;
		int foundTarget = 0;
		//Determine Target:
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[0].length; j++) {
				for (Observation obs : grid[i][j]) {
					if(obs.itype == object_to_push_to){
						//Found a valid target
						if(foundTarget == targetNumberToPushTo){
							xTarget = i;
							yTarget = j;
						}else if(foundTarget == 0){
							firstValidXTarget = i;
							firstValidYTarget = j;
						}
						foundTarget++;
					}
				}
			}
		}
		
		if(xTarget == -1 && firstValidXTarget != -1){
			//Habe Ziel gefunden, aber keins mit gewuenschter nummer
			targetNumberToPushTo = 0;
			xTarget = firstValidXTarget;
			yTarget = firstValidYTarget;
		}
		
		if(xTarget == -1){
			//Keine Moeglichkeit gefunden!
			//TODO: Jetzt muesste der Plan geaendert werden oder abgebrochen werden!
			pushPlannerZustand = null;
			if(!Agent.UPLOAD_VERSION)
				System.out.println("Not Found!");
			return;
		}
		targetNumberToPushTo++;
		if(!Agent.UPLOAD_VERSION)
			System.out.println("PushSlide: " + xTarget + "|" + yTarget);


		actionsToDo.clear();

		KnowledgeBasedPushSlidePlanner planner = new KnowledgeBasedPushSlidePlanner(curState, 1);
		long time = System.currentTimeMillis();
		Zustand found = planner.compute(object_to_push, xTarget, yTarget, false,
				new int[] {}, agentX, agentY, objectNumberToPush);
		objectNumberToPush++;
		if(!Agent.UPLOAD_VERSION)
			System.out.println("Time: " + (System.currentTimeMillis() - time));
		pushPlannerZustand = found;
	}

	private void walkTo(int xTarget, int yTarget) {
		if(!Agent.UPLOAD_VERSION)
			System.out.println("Move to: " + xTarget + "|" + yTarget);


		aStar = new KnowledgeBasedAStar(curState);
		aStar.disableIllegalMove();
		aStar.setStopEarly(xTarget, yTarget);

		aStar.calculate(curState.getAvatarX(), curState.getAvatarY(), curState.getAvatar().itype, new int[] {}, true);

		actionsToDo.addAll(0, aStar.extractActions(xTarget, yTarget));
	}


	/**
	 * Gets the player the control to draw something on the screen. It can be
	 * used for debug purposes.
	 * 
	 * @param g
	 *            Graphics device to draw to.
	 */
	public void draw(Graphics2D g) {
		if (curState == null)
			return;

		int half_block = (int) (block_size * 0.5);
		/*
		 * Vector2d target = null; for (ArrayList<Observation> obList :
		 * curState.getImmovablePositions()) { for (Observation observation :
		 * obList) { if (observation == null) continue;
		 * 
		 * // if(target == null) target = observation.position; //
		 * observation.update(observation.itype, observation.obsID, // target,
		 * observation.reference, observation.category);
		 * 
		 * // g.setColor(new Color(100, 100, 100, 100)); g.setColor(new
		 * Color(Color.magenta.getRed(), Color.magenta .getGreen(),
		 * Color.magenta.getBlue(), 50)); int x = (int) observation.position.x;
		 * int y = (int) observation.position.y; g.fillRoundRect(x, y,
		 * block_size, block_size, half_block, half_block); } }
		 */

		g.setColor(Color.black);

		for (int j = 0; j < grid[0].length; ++j) {
			for (int i = 0; i < grid.length; ++i) {
				if (grid[i][j].size() > 0) {
					int rows = grid[i][j].size();
					int offset = rows * 4;
					for (int row = 0; row < rows; row++) {
						try {
							Observation rowObs = grid[i][j].get(row); // grid[i][j].size()-1
							// Three interesting options:
							String print = "C" + rowObs.category + "|T"
									+ rowObs.itype; // rowObs.itype;
													// //rowObs.obsID;
													// //rowObs.category;
							g.drawString(print, i * block_size, j * block_size
									+ half_block - offset + row * 10);

							//Print Position:
							g.drawString(i + "|" + j, i * block_size, j * block_size
									+ half_block - offset + row * 10 - 10);
							
							//Print 2:
							if(rowObs.itype == 2)
								g.drawString("########", i * block_size, j * block_size
										+ half_block - offset + row * 10 + 10);
						} catch (IndexOutOfBoundsException e) {
						}
						;
					}
				}
			}
		}

		if(aStar != null){
			
			g.setColor(Color.magenta);
			for (int j = 0; j < grid[0].length; ++j) {
				for (int i = 0; i < grid.length; ++i) {
	
					String print = aStar.distance[i][j] + "";
	
					g.drawString(print, i * block_size + half_block, j * block_size
							+ half_block);
				}
			}
			
		}

		Zustand z1 = pushPlannerZustand;
		int nr = 0;
		
		while (z1 != null) {
			g.setColor(Color.cyan);
			g.fillOval(z1.lastX * block_size + half_block / 2, z1.lastY
					* block_size + half_block / 2, half_block, half_block);
			g.setColor(Color.black);
			g.drawString("" + nr, z1.lastX * block_size + half_block, z1.lastY
					* block_size + half_block);
			nr++;
			z1 = z1.nachfolger;
		}
		if(pushPlannerZustand != null){
			g.setColor(Color.GREEN);
			g.fillOval(pushPlannerZustand.lastPlayerX * block_size + half_block / 2, pushPlannerZustand.lastPlayerY
					* block_size + half_block / 2, half_block, half_block);
		}

	}

	@Override
	public double EvaluateWeight(YoloState yoloState) {
		return 1000;
	}

	@Override
	public void preRun(YoloState yoloState, ElapsedCpuTimer elapsedTimer) {
		//Nothing to do
	}
}
