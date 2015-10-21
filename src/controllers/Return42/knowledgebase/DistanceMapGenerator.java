package controllers.Return42.knowledgebase;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import controllers.Return42.knowledgebase.observation.WalkableSpace;
import controllers.Return42.util.StateObservationUtils;
import core.game.StateObservation;

public class DistanceMapGenerator {

	private final WalkableSpaceGenerator walkableSpaceGenerator;

	public DistanceMapGenerator(WalkableSpaceGenerator walkableSpaceGenerator) {
		this.walkableSpaceGenerator = walkableSpaceGenerator;
	};

	public double[][] generateDistanceMap( StateObservation state ) {
		int x = (int) (state.getAvatarPosition().x / state.getBlockSize());
		int y = (int) (state.getAvatarPosition().y / state.getBlockSize());
		int typeId = StateObservationUtils.getAvatarType( state );
		
		return generateDistanceMap( state, x, y, typeId );
	}
	
	/**
	 * Generates a 2d array of shortest distances starting at the given point.
	 * Considers walkable space.
	 * 
	 * The returned distances are grid-distances. 
	 * Two neighboring fields have distance 1.
	 * Distance Double.Max means not reachable.
	 */
	public double[][] generateDistanceMap( StateObservation state, int gridX, int gridY, int typeId ) {
		WalkableSpace[][] walkable = walkableSpaceGenerator.getWalkableSpace( state, typeId );
		double[][] distances = new double[ walkable.length ][ walkable[0].length ];

		for( double[] entry: distances ) {
			Arrays.fill( entry, Double.MAX_VALUE );
		}

		if (gridX < 0 || gridY < 0 || gridX >= walkable.length || gridY >= walkable[0].length )
			return distances;

		bfs( walkable, distances, gridX, gridY );
		
		return distances;
	}

	private void bfs(WalkableSpace[][] walkable, double[][] distances,	int gridX, int gridY) {
		distances[gridX][gridY] = 0;
		Queue<Position> fieldsToVisit = new LinkedList<>();
		fieldsToVisit.add( new Position( gridX, gridY ) );

		while( !fieldsToVisit.isEmpty() ) {
			visitNext( walkable, distances, fieldsToVisit );
		}
	}

	private void visitNext(WalkableSpace[][] walkable, double[][] distances, Queue<Position> fieldsToVisit) {
		Position next = fieldsToVisit.poll();
		double nextDist = distances[next.x][next.y] +1;
		int width = distances.length;
		int height = distances[0].length;
		
		boolean shouldVisitLeft  = (next.x -1 >= 0)		 && (walkable[next.x-1][next.y  ] != WalkableSpace.BLOCKED) && ( distances[next.x -1][next.y  ] > nextDist ); 
		boolean shouldVisitRight = (next.x +1 < width)   && (walkable[next.x+1][next.y  ] != WalkableSpace.BLOCKED) && ( distances[next.x +1][next.y  ] > nextDist ); 
		boolean shouldVisitUp    = (next.y -1 >= 0) 	 && (walkable[next.x  ][next.y-1] != WalkableSpace.BLOCKED) && ( distances[next.x   ][next.y-1] > nextDist ); 
		boolean shouldVisitDown  = (next.y +1 < height)	 && (walkable[next.x  ][next.y+1] != WalkableSpace.BLOCKED) && ( distances[next.x   ][next.y+1] > nextDist ); 
		
		if ( shouldVisitLeft ) {
			distances[next.x -1][next.y] = nextDist;
			fieldsToVisit.add( new Position( next.x -1, next.y ) );
		}
		if ( shouldVisitRight ) {
			distances[next.x +1][next.y] = nextDist;
			fieldsToVisit.add( new Position( next.x +1, next.y ) );
		}
		if ( shouldVisitUp ) {
			distances[next.x][next.y -1] = nextDist;
			fieldsToVisit.add( new Position( next.x   , next.y -1) );
		}
		if ( shouldVisitDown ) {
			distances[next.x][next.y +1] = nextDist;
			fieldsToVisit.add( new Position( next.x   , next.y +1) );
		}
	}
	
	private static class Position {
		public final int x;
		public final int y;

		public Position(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}
}