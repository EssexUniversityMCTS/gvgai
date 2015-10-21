package controllers.YOLOBOT.Util.TargedChooser;

import core.game.Observation;

public class InterestingTarged implements Comparable<InterestingTarged>{
	
	private final static double SOFTMAX_TEMPERATURE = 1;
	
	private Observation obs;
	private double priorityValue;
	private boolean isWinCondition;
	private boolean useActionEffective;
	private boolean scoreIncrease;
	private boolean unseen;
	private int distance;
	
	
	public InterestingTarged(Observation obs){
		this.obs = obs;
		isWinCondition = false;
	}

	public boolean isWinCondition() {
		return isWinCondition;
	}

	public void setWinCondition(boolean isWinCondition) {
		this.isWinCondition = isWinCondition;
	}

	@Override
	public int compareTo(InterestingTarged o) {
		return (int) (o.priorityValue - this.priorityValue);
	}

	public Observation getObs() {
		return obs;
	}

	public double getPriorityValue() {
		return priorityValue;
	}
	
	public void setPriorityValue(double priorityValue) {
		this.priorityValue = priorityValue;
	}

	public void setIsUseable(boolean useActionEffective) {
		this.useActionEffective = useActionEffective;
	}

	public boolean isUseable() {
		return useActionEffective;
	}
	
	public double getSoftMaxValue(double prioSum, double priorityBonus) {
		return Math.exp(((priorityValue+priorityBonus)/prioSum)/SOFTMAX_TEMPERATURE);
	}
	
	public void setScoreIncrease(boolean scoreIncrease) {
		this.scoreIncrease = scoreIncrease;
	}
	public boolean isScoreIncrease(){
		return scoreIncrease;
	}

	boolean isUnseen() {
		return unseen;
	}

	void setUnseen(boolean unseen) {
		this.unseen = unseen;
	}

	int getDistance() {
		return distance;
	}

	void setDistance(int distance) {
		this.distance = distance;
	}
	
}
