package controllers.YOLOBOT.Util.Heuristics;

import controllers.YOLOBOT.YoloState;

public class WinHeuristic extends IHeuristic {
	
	private boolean intrestedInWinning = true;
	
	
	@Override
	public HeuristicType GetType() {
		return HeuristicType.WinHeuristic;
	}

	@Override
	public double Evaluate(YoloState ys) {
		return EvaluateWithoutNormalisation(ys);
	}

	@Override
	public double EvaluateWithoutNormalisation(YoloState ys) {
		switch (ys.getGameWinner()) {
			case PLAYER_LOSES:
				return -1;
		
			case PLAYER_WINS:
				return intrestedInWinning ? 1:-0.5;
			default:
				return 0;
			}
		
	}

	@Override
	public double GetAbsoluteMax() {
		return 1;
	}
	
	public boolean isIntrestedInWinning() {
		return intrestedInWinning;
	}

	public void setIntrestedInWinning(boolean notIntrestedInWinning) {
		this.intrestedInWinning = notIntrestedInWinning;
	}
}
