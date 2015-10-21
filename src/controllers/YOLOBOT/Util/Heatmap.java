package controllers.YOLOBOT.Util;

import controllers.YOLOBOT.YoloState;

public class Heatmap {

	/**
	 * z.B. 2 -> Sektor = 2^2 mal 2^2 Bloecke gross
	 */
	public final int SECTORSIZELOG = 2;
	public final int SECTORSIZE;

	public static Heatmap instance;

	public int[][] heatmap;
	/**
	 * Der letzte Tick, wann dieses Feld beschrieben wurde
	 */
	public int[][] lastTick;

	public int[][] sectorHeatmap;

	private final int timeUntilCooling = 10;

	private int currentGameTick;

	private int maxAbstractValue;
	private int maxValueApproximation;

	private int maxValueApproximationSetOnTick;

	public Heatmap(YoloState initState) {
		SECTORSIZE = SECTORSIZELOG * SECTORSIZELOG;
		int sizeX = initState.getWorldDimension().width;
		int sizeY = initState.getWorldDimension().height;
		heatmap = new int[sizeX][sizeY];
		lastTick = new int[sizeX][sizeY];
		sectorHeatmap = new int[sizeX >> SECTORSIZELOG][sizeY >> SECTORSIZELOG];
		currentGameTick = initState.getGameTick();
	}

	public void stepOn(YoloState state) {
		currentGameTick = state.getGameTick();

		// Reduce maxValue
		if (maxValueApproximationSetOnTick + timeUntilCooling < currentGameTick)
			maxValueApproximation--;

		int x = state.getAvatarX();
		int y = state.getAvatarY();

		if (x > 0 && y > 0 && x < heatmap.length && y < heatmap[0].length) {

			refreshField(x, y);

			lastTick[x][y] = currentGameTick;
			int newValue = ++heatmap[x][y];
			if (newValue >= maxValueApproximation) {
				maxValueApproximation = newValue;
				maxValueApproximationSetOnTick = currentGameTick;
			}

			// Set AbstractHeat
			int newAbstract = ++sectorHeatmap[x >> SECTORSIZELOG][y >> SECTORSIZELOG];
			if (maxAbstractValue < newAbstract)
				maxAbstractValue = newAbstract;
		}

	}

	private void refreshField(int x, int y) {

		int diff = (currentGameTick - lastTick[x][y]) - timeUntilCooling;
		if (diff > 0) {
			// Cooling!
			lastTick[x][y] += diff;
			if (heatmap[x][y] > diff)
				heatmap[x][y] -= diff;
			else
				heatmap[x][y] = 0;
		}

	}

	public int getMaximumAbstractHeatValue() {
		return maxAbstractValue;
	}

	public int getHeatValue(int x, int y) {

		if (x > 0 && y > 0 && x < heatmap.length && y < heatmap[0].length) {
			refreshField(x, y);
			return heatmap[x][y];
		}else{
			return 0;
		}
	}

	public int getAbstractHeatValue(int x, int y) {

		if (x > 0 && y > 0 && x < heatmap.length && y < heatmap[0].length) {
			return sectorHeatmap[x >> SECTORSIZELOG][y >> SECTORSIZELOG];
		}else{
			return 0;
		}
	}

	public int getMaxValueApproximation() {
		return maxValueApproximation;
	}
}