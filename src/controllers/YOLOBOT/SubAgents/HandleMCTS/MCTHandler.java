package controllers.YOLOBOT.SubAgents.HandleMCTS;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Random;

import controllers.YOLOBOT.Agent;
import controllers.YOLOBOT.YoloState;
import controllers.YOLOBOT.SubAgents.SubAgent;
import controllers.YOLOBOT.SubAgents.HandleMCTS.RolloutPolicies.EpsilonGreedyBestFirstRolloutPolicy;
import controllers.YOLOBOT.SubAgents.HandleMCTS.RolloutPolicies.HeuristicRolloutPolicy;
import controllers.YOLOBOT.SubAgents.HandleMCTS.RolloutPolicies.RandomNotDeadRolloutPolicy;
import controllers.YOLOBOT.SubAgents.HandleMCTS.RolloutPolicies.RolloutPolicy;
import controllers.YOLOBOT.Util.Heuristics.AStarDistantsHeuristic;
import controllers.YOLOBOT.Util.Heuristics.DeadendHeuristic;
import controllers.YOLOBOT.Util.Heuristics.DistanceToNpcsHeuristic;
import controllers.YOLOBOT.Util.Heuristics.HeatMapHeuristic;
import controllers.YOLOBOT.Util.Heuristics.HeuristicList;
import controllers.YOLOBOT.Util.Heuristics.HeuristicType;
import controllers.YOLOBOT.Util.Heuristics.IModdableHeuristic;
import controllers.YOLOBOT.Util.Heuristics.OneDimensionMoveToMedianHeuristic;
import controllers.YOLOBOT.Util.Heuristics.ScoreHeuristic;
import controllers.YOLOBOT.Util.Heuristics.ScoreLookaheadHeuristic;
import controllers.YOLOBOT.Util.Heuristics.SectorHeatMapHeuristic;
import controllers.YOLOBOT.Util.Heuristics.SimulateDepthHeuristic;
import controllers.YOLOBOT.Util.Heuristics.WinHeuristic;
import controllers.YOLOBOT.Util.TargedChooser.TargetChooser;
import controllers.YOLOBOT.Util.Wissensdatenbank.YoloKnowledge;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import core.game.Observation;

public class MCTHandler extends SubAgent {


	public final static boolean DEBUG_TRACE = true && !Agent.UPLOAD_VERSION;
	private static final int DEFAULT_ROLLOUT_DEPTH = 4;
	static boolean useNonNegativeScore = true;


	public static HeuristicList heuristics = new HeuristicList();
	public static Random rnd;

	public static YoloState rootState;
	public static int ROLLOUT_DEPTH = DEFAULT_ROLLOUT_DEPTH;
	public static double epsilon = 1e-6;
	public static AStarDistantsHeuristic aSDH;
	public static DistanceToNpcsHeuristic npcH;
	public static WinHeuristic winH;
	public static SimulateDepthHeuristic simDH;
	public static DeadendHeuristic deH;
	public static ScoreHeuristic scoreHeuristic;
	public static OneDimensionMoveToMedianHeuristic oneDimenstionMedianHeuristic;
	public static ScoreLookaheadHeuristic scoreLookaheadHeuristic;
	public boolean olNode;
	public final static int closeDistance = 5;
	

	public static RolloutPolicy rolloutPolicy;
	public static RolloutPolicy randomPolicy;
	public static HeuristicRolloutPolicy scoreLookaheadPolicy;

	// private AStarTargetChooser aStarTargetChooser;
	private TargetChooser targetChooser;

	private int deadendPos;

	public static IModdableHeuristic heuristicToUse;

	public static boolean largerRollouts;


	public MCTHandler(YoloState rootState) {
		olNode = false;
		rnd = new Random();

		scoreHeuristic = new ScoreHeuristic();
		HeatMapHeuristic heatMaph = new HeatMapHeuristic();
		SectorHeatMapHeuristic sectorheatMaph = new SectorHeatMapHeuristic();
		oneDimenstionMedianHeuristic = new OneDimensionMoveToMedianHeuristic(
				rootState);
		scoreLookaheadHeuristic = new ScoreLookaheadHeuristic();
		scoreLookaheadPolicy = new HeuristicRolloutPolicy(true);
		
		winH = new WinHeuristic();
		aSDH = new AStarDistantsHeuristic(null);
		simDH = new SimulateDepthHeuristic();
		npcH = new DistanceToNpcsHeuristic();
		deH = new DeadendHeuristic();

		heuristics.Put(scoreHeuristic);
		heuristics.SetWeight(HeuristicType.ScoreHeuristic, 50);

		heuristics.Put(heatMaph);
		heuristics.SetWeight(HeuristicType.HeatMapHeuristic,
				rootState.isOneDimensionGame() ? 0 : 1);

		heuristics.Put(winH);
		heuristics.SetWeight(HeuristicType.WinHeuristic, 1000);

		heuristics.Put(aSDH);
		heuristics.SetWeight(HeuristicType.AStarDistantsHeuristic,
				rootState.isOneDimensionGame() ? 0 : 50);

		heuristics.Put(npcH);
		heuristics.SetWeight(HeuristicType.DistanceToNpcsHeuristic,
				rootState.isOneDimensionGame() ? 0 : 50);

		heuristics.Put(simDH);
		heuristics.SetWeight(HeuristicType.SimulateDepthHeuristic,
				rootState.isOneDimensionGame() ? 0 : 5);

		heuristics.Put(oneDimenstionMedianHeuristic);
		heuristics.SetWeight(HeuristicType.OneDimensionMoveToMedianHeuristic,
				rootState.isOneDimensionGame() ? 50 : 0);

		if (rootState.isOneDimensionGame())
			ROLLOUT_DEPTH = 10;

		// heuristics.Put(deH);
		// heuristics.SetWeight(HeuristicType.DeadendHeuristic,
		// rootState.isOneDimensionGame()?0:10);

		// Set Policy:
		rolloutPolicy = new EpsilonGreedyBestFirstRolloutPolicy();
		randomPolicy = new RandomNotDeadRolloutPolicy();
		// Init AStar Target Chooser
		// aStarTargetChooser = new AStarTargetChooser(aSDH);
		targetChooser = new TargetChooser(aSDH, npcH, deH);

		deadendPos = heuristics
				.getIndexOfHeuristic(HeuristicType.DeadendHeuristic);
		
	}

	private void randomize(YoloState state) {
		state.setNewSeed((int) Math.random() * 10000);
	}

	public ACTIONS act(YoloState rootState, ElapsedCpuTimer elapsedTimer) {

		if (rootState.isOneDimensionGame())
			ROLLOUT_DEPTH = 10;
		else{
			if(largerRollouts){
				ROLLOUT_DEPTH = 10;
			}else{
				ROLLOUT_DEPTH = DEFAULT_ROLLOUT_DEPTH;
			}
		}
		// oneDimenstionMedianHeuristic.calculateMedian(rootState, true);

		if (YoloKnowledge.instance.haveEverGotScoreWithoutWinning()){
			heuristics.SetWeight(HeuristicType.ScoreHeuristic, 50);
			useNonNegativeScore = false;
		}else{
			heuristics.SetWeight(HeuristicType.ScoreHeuristic, 250);
			useNonNegativeScore = true;
		}
		MCTHandler.rootState = rootState;
		if (!rootState.isOneDimensionGame())
			targetChooser.handle(rootState);
		else
			oneDimenstionMedianHeuristic.setFixMedian(rootState);

		heuristicToUse = null;
		if (MCTHandler.aSDH.isActive())
			heuristicToUse = MCTHandler.aSDH;
		else if (MCTHandler.npcH.isActive())
			heuristicToUse = MCTHandler.npcH;

		olNode = true;
		List<Observation>[] npcPositions = rootState.getNpcPositions(rootState
				.getAvatarPosition());
		if (npcPositions != null) {
			for (List<Observation> oneTypeNpcs : npcPositions) {
				if (oneTypeNpcs.size() > 0
						&& (Math.sqrt(oneTypeNpcs.get(0).sqDist) / rootState
								.getBlockSize()) < closeDistance) {
					olNode = true;
					break;
				}
			}
		}

		double avgTimeTaken = 0;
		long remaining = elapsedTimer.remainingTimeMillis();
		int numIters = 0;

		int remainingLimit = 5;

		MCTNode rootNode;

		if (olNode) {
			OLNode.currentState = rootState.copy();
			rootNode = new OLNode(ACTIONS.ACTION_NIL);
			if (!Agent.UPLOAD_VERSION)
				System.out.println("Using OLNode");
		} else {
			rootNode = new CLNode(ACTIONS.ACTION_NIL, rootState);
			if (!Agent.UPLOAD_VERSION)
				System.out.println("Using CLNode");
		}

		MCTNode node;
		while (remaining > 2 * avgTimeTaken && remaining > remainingLimit
				|| remaining > 10) {

			node = rootNode;

			if (olNode) {
				OLNode.currentState = rootState.copy();
				randomize(OLNode.currentState);
			}

			// Expand or Select
			if (DEBUG_TRACE)
				System.out.print("Expand/Select: \n\t");
			node = node.expandOrSelect();

			// Simulate
			if (DEBUG_TRACE)
				System.out.print("\nSimulate: \n\t");
			boolean useScoreLookahead = false;//numIters == 1;
			YoloState endState = node.simulate(useScoreLookahead, numIters == 0);
			// YoloState endState = node.getState();
			if (DEBUG_TRACE)
				System.out.print("\nScore: "
						+ (endState.getGameScore() - rootState.getGameScore())
						+ "\n");

			double[] heuristicValues = heuristics.EvaluateAll(endState);

			if (deadendPos != -1) {
				if (rootNode.lastSelectedChild != null)
					heuristicValues[deadendPos] = deH
							.Evaluate(rootNode.lastSelectedChild.action);
			}

			while (node != null) {
				node.update(heuristicValues);
				node = node.getParent();
			}

			// Hier wird die Verbleibende Zeit Aktualisiert
			numIters++;
			avgTimeTaken = elapsedTimer.elapsedMillis() / numIters;
			remaining = elapsedTimer.remainingTimeMillis();
		}
		if (DEBUG_TRACE)
			System.out.println("\nDone!");

		ACTIONS actionToDo = rootNode.bestAction();
		return actionToDo;

	}

	@Override
	public double EvaluateWeight(YoloState yoloState) {
		return 0;
	}

	@Override
	public void preRun(YoloState yoloState, ElapsedCpuTimer elapsedTimer) {
		// Nothing to do
	}

	public void draw(Graphics2D g) throws ConcurrentModificationException {
		if (rootState == null)
			return;

		int block_size = rootState.getBlockSize();
		int half_block = (int) (block_size * 0.5);

		g.setColor(Color.black);
		int[][] aStarDistance = aSDH.getDistance();
		if (aStarDistance == null)
			aStarDistance = npcH.getDistances();
		int npcChasing = npcH.getNpcObsId();
		Observation npc = rootState.getSimpleState()
				.getObservationWithIdentifier(npcChasing);
		if (npc != null && !rootState.isOneDimensionGame()) {
			// System.out.println("Current distance to chasing enemy: " +
			// npcH.EvaluateWithoutNormalisation(rootState) );
			int npcX = (int) (npc.position.x / block_size);
			int npcY = (int) (npc.position.y / block_size);

			g.setColor(Color.RED);
			g.setStroke(new BasicStroke(6));
			g.drawOval(npcX * block_size, npcY * block_size, block_size,
					block_size);
			g.setStroke(new BasicStroke());
			g.setColor(Color.black);
		}

		if (rootState.isOneDimensionGame()) {
			// Draw median:
			int median = (int) oneDimenstionMedianHeuristic.getFixMedian()
					+ half_block;
			g.setColor(Color.orange);

			g.setStroke(new BasicStroke(4));
			g.drawLine(median, 0, median, rootState.getWorldDimension().height);
			g.setStroke(new BasicStroke(1));

		}

		for (int j = 0; j < rootState.getObservationGrid()[0].length; ++j) {
			for (int i = 0; i < rootState.getObservationGrid().length; ++i) {
				String print;
				g.setColor(new Color(1f, 0f, 0f, 0.2f));
				if (!Agent.DRAW_TARGET_ONLY)
					for (Observation obs : rootState.getObservationGrid()[i][j]) {
						if (targetChooser.isOnBlacklist(obs, rootState)) {
							g.setStroke(new BasicStroke(6));
							g.drawLine(i * block_size, j * block_size, (i + 1)
									* block_size, (j + 1) * block_size);
							g.drawLine((i + 1) * block_size, j * block_size, i
									* block_size, (j + 1) * block_size);
							g.setStroke(new BasicStroke(1));
						}
					}

//				g.setColor(Color.green);
//				if (rootState.getObservationGrid()[i][j].size() > 0) {
//					print = ""
//							+ rootState.getObservationGrid()[i][j].get(0).category
//							+ " | "
//							+ rootState.getObservationGrid()[i][j].get(0).itype;
//					if (!Agent.DRAW_TARGET_ONLY)
//						g.drawString(print, i * block_size, j * block_size
//								+ half_block);
//				}

				g.setColor(Color.black);
				if (aStarDistance != null) {
					print = " " + aStarDistance[i][j];

					if (!Agent.DRAW_TARGET_ONLY)
						g.drawString(print, i * block_size, j * block_size
								+ half_block);
					if (aStarDistance[i][j] == 1) {
						g.setColor(Color.red);
						g.setStroke(new BasicStroke(6));
						g.drawRect(i * block_size, j * block_size, block_size,
								block_size);
						g.setStroke(new BasicStroke());
						g.setColor(Color.black);
					}
				}

				// //simulationEnds Draws:
				// if(simulationEndsToDraw != null){
				// int[] ends = simulationEndsToDraw[i][j];
				// String print;
				//
				// int xCenter = i * block_size + half_block -5;
				// int xLeft = xCenter - 12;
				// int xRight = xCenter + 12;
				//
				// int yCenter = (j) * block_size + half_block;
				// int yTop = yCenter - 12;
				// int yBottom = yCenter + 12;
				//
				// g.setColor(Color.black);
				// if(rootState.getAvailableActions(true).contains(ACTIONS.ACTION_DOWN)){
				// int nr =
				// ends[rootState.getAvailableActions(true).indexOf(ACTIONS.ACTION_DOWN)];
				// if(nr > 0)
				// g.setColor(Color.magenta);
				// print = "" + nr;
				//
				// g.drawString(print, xCenter, yBottom);
				// }
				//
				// g.setColor(Color.black);
				// if(rootState.getAvailableActions(true).contains(ACTIONS.ACTION_UP)){
				// int nr =
				// ends[rootState.getAvailableActions(true).indexOf(ACTIONS.ACTION_UP)];
				// if(nr > 0)
				// g.setColor(Color.magenta);
				// print = "" + nr;
				// g.drawString(print, xCenter, yTop);
				// }
				//
				// g.setColor(Color.black);
				// if(rootState.getAvailableActions(true).contains(ACTIONS.ACTION_LEFT)){
				// int nr =
				// ends[rootState.getAvailableActions(true).indexOf(ACTIONS.ACTION_LEFT)];
				// if(nr > 0)
				// g.setColor(Color.magenta);
				// print = "" + nr;
				// g.drawString(print, xLeft, yCenter);
				// }
				//
				// g.setColor(Color.black);
				// if(rootState.getAvailableActions(true).contains(ACTIONS.ACTION_RIGHT)){
				// int nr =
				// ends[rootState.getAvailableActions(true).indexOf(ACTIONS.ACTION_RIGHT)];
				// if(nr > 0)
				// g.setColor(Color.magenta);
				// print = "" + nr;
				// g.drawString(print, xRight, yCenter);
				// }
				//
				// g.setColor(Color.black);
				// if(rootState.getAvailableActions(true).contains(ACTIONS.ACTION_NIL)){
				// int nr =
				// ends[rootState.getAvailableActions(true).indexOf(ACTIONS.ACTION_NIL)];
				// if(nr > 0)
				// g.setColor(Color.magenta);
				// print = "" + nr;
				// g.drawString(print, xCenter, yCenter);
				// }
				//
				// //
				// if(rootState.getAvailableActions().contains(ACTIONS.ACTION_USE)){
				// // print = " " +
				// ends[rootState.getAvailableActions().indexOf(ACTIONS.ACTION_USE)];
				// // g.drawString(print, i * block_size + third_block, j *
				// third_block);
				// // }
				// }

			}
		}

	}

}
