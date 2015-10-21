package controllers.NovTea;

import java.util.HashSet;


public class PositionSet {

	public HashSet<Integer> setPos = new HashSet<Integer>(250);
	
	
	public PositionSet(double posX, double posY, int factor) {
		int posValue = this.loadPositionValue(((int)posX) / factor, ((int)posY) / factor);
		this.setPos.add(posValue);
	}

	
	public void add(double posX, double posY, int factor) {
		int posValue = this.loadPositionValue(((int)posX) / factor, ((int)posY) / factor);
		this.setPos.add(posValue);
	}

	
	public boolean contains(double posX, double posY, int factor) {
		int posValue = this.loadPositionValue(((int)posX) / factor, ((int)posY) / factor);
		return setPos.contains(posValue);
	}

	
	private int loadPositionValue(int posX, int posY) {
		return posX * 100 + posY;
	}

}
