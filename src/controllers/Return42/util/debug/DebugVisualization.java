package controllers.Return42.util.debug;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

import controllers.Return42.knowledgebase.GameInformation;
import controllers.Return42.knowledgebase.observation.WalkableSpace;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.Vector2d;
import core.game.StateObservation;

public class DebugVisualization {
	
	public static void drawWalkableSapce(WalkableSpace[][] walkableGrid, GameInformation gameInformation, Graphics2D g) {
		if(walkableGrid == null){
			return;
		}
		
		Color oldColor = g.getColor();

		for (int i = 0; i < walkableGrid.length; i++) {
			for (int j = 0; j < walkableGrid[0].length; j++) {
				if (walkableGrid[i][j] != WalkableSpace.WALKABLE) {
					if (walkableGrid[i][j] == WalkableSpace.BLOCKED) {
						g.setColor(Color.RED);
					} else {
						g.setColor(Color.YELLOW);
					}
					Vector2d obsPosition = gameInformation.gridPositionToGamePosition(new Vector2d(i, j));
					double blockSize = gameInformation.getBlockSize();
					g.fillRect((int) (obsPosition.x + (blockSize / 2)),
							(int) (obsPosition.y + (blockSize / 2)), 10, 10);
				}
			}
		}

		g.setColor(oldColor);
	}
	
	public static void drawScoreMap(double[][] scoreMap, GameInformation gameInformation, Graphics2D g) {
		if (scoreMap == null) {
			return;
		}

		for (int i = 0; i < scoreMap.length; i++) {
			for (int j = 0; j < scoreMap[0].length; j++) {
				Vector2d obsPosition = gameInformation.gridPositionToGamePosition(new Vector2d(i, j));
				double blockSize = gameInformation.getBlockSize();
				g.drawString("" + scoreMap[i][j], (int) (obsPosition.x + (blockSize / 2)),
						(int) (obsPosition.y + (blockSize / 2)));
			}
		}
	}

	public static void drawPath( StateObservation state, List<ACTIONS> actions, Graphics2D g) {
    	g.setColor(Types.DARKBLUE);
    	g.setStroke(new BasicStroke(10));
        int halfBlock = state.getBlockSize() / 2;
    	
    	int lastX = (int) state.getAvatarPosition().x + halfBlock;
    	int lastY = (int) state.getAvatarPosition().y + halfBlock;
    	
    	for( ACTIONS action: actions ) {
    		state.advance(action);
    		int nextX = (int) state.getAvatarPosition().x + halfBlock;
        	int nextY = (int) state.getAvatarPosition().y + halfBlock;

        	g.drawLine( lastX, lastY, nextX, nextY );
        	
        	lastX = nextX;
        	lastY = nextY;
    	}
    	

	}

}
