package controllers.Return42.knowledgebase;

import java.util.LinkedList;
import java.util.Queue;

import controllers.Return42.knowledgebase.observation.WalkableSpace;
import controllers.Return42.util.Position;

public class ValuesSpreader {

	private WalkableSpace[][] walkableSpace;

	public ValuesSpreader(WalkableSpace[][] walkableSpace) {
		this.walkableSpace = walkableSpace;
	}

	public void spreadValues(final double[][] heightMap, Queue<Position> initalPositions) {
		if(initalPositions.isEmpty()){
			return;
		}

		Queue<Position> openPositions = new LinkedList<Position>();
		openPositions.addAll(initalPositions);
		
		while (!openPositions.isEmpty()) {
			Position currentPos = openPositions.poll();
			int x = currentPos.x;
			int y = currentPos.y;
			Double ownValue = heightMap[x][y];
			
			update(heightMap, openPositions, x + 1, y, ownValue);
			update(heightMap, openPositions, x - 1, y, ownValue);
			update(heightMap, openPositions, x, y + 1, ownValue);
			update(heightMap, openPositions, x, y - 1, ownValue);
		}
	}

	private void update(double[][] heightMap, Queue<Position> openPositions, int x, int y, double ownValue) {
		if (isValid(x, y, heightMap)) {
			if (walkableSpace[x][y] == WalkableSpace.WALKABLE) {
				Position newPosition = new Position(x, y);
				if (heightMap[x][y] < newValue(ownValue)) {
					heightMap[x][y] = newValue(ownValue);
					// Value of heightMap has changed => trigger reinsertion at
					// the correct position
					openPositions.remove(newPosition);
					openPositions.add(newPosition);
				}
			}
		}
	}

	private double newValue(double ownValue) {
		return Math.max(ownValue - 1, 0);
	}

	private boolean isValid(int x, int y, double[][] heightMap) {
		if (x < 0 || x >= heightMap.length) {
			return false;
		}
		if (y < 0 || y >= heightMap[0].length) {
			return false;
		}

		return true;
	}
	

}
