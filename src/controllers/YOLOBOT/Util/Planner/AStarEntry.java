package controllers.YOLOBOT.Util.Planner;

import tools.Vector2d;


public class AStarEntry implements Comparable<AStarEntry>{
	private int x;
	private int y;
	private int itype;
	private int itypeAusnahme;
	private byte originAusrichtung;
	private int distance;
	private int xFirstItypeChange, yFirstItypeChange;
	
	public AStarEntry(int x, int y, int itype, int itypeAusnahme,
			byte originAusrichtung, int distance, int yChangeItype, int xChangeItype) {
		super();
		this.x = x;
		this.y = y;
		this.itype = itype;
		this.itypeAusnahme = itypeAusnahme;
		this.originAusrichtung = originAusrichtung;
		this.distance = distance;
		this.xFirstItypeChange = xChangeItype;
		this.yFirstItypeChange = yChangeItype;
	}
	
	
	int getX() {
		return x;
	}
	int getY() {
		return y;
	}
	int getItype() {
		return itype;
	}
	int getItypeAusnahme() {
		return itypeAusnahme;
	}
	byte getOriginAusrichtung() {
		return originAusrichtung;
	}
	public int getDistance() {
		return distance;
	}	
	public int getxFirstItypeChange() {
		return xFirstItypeChange;
	}
	public int getyFirstItypeChange() {
		return yFirstItypeChange;
	}

	@Override
	public int compareTo(AStarEntry other) {
		return distance - other.distance;
	}
	
}
