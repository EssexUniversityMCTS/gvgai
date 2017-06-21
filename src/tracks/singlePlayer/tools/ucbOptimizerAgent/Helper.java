package tracks.singlePlayer.tools.ucbOptimizerAgent;

import java.util.ArrayList;

import core.game.Observation;
import ontology.Types;
import tools.Vector2d;

public class Helper {
	public static int TREE_CHILD_DEPTH = 0;
	public static int TREE_CHILD_VALUE = 1;
	public static int TREE_PARENT_VISITS = 2;
	public static int TREE_CHILD_VISITS = 3;
	public static int TREE_CHILD_MAX_VALUE = 4;
	
	public static int HISTORY_REVERSE_VALUE = 5;
	public static int HISTORY_REPEATING_VALUE = 6;
	public static int USELESS_MOVE_VALUE = 7;
	public static int SPACE_EXPLORATION_VALUE = 8;
	public static int SPACE_EXPLORATION_MAX_VALUE = 9;
		
	public static int DISTANCE_MIN_IMMOVABLE = 10;
	public static int DISTANCE_TOT_IMMOVABLE = 11;
	public static int DISTANCE_MAX_IMMOVABLE = 12;
	public static int DISTANCE_MIN_MOVABLE = 13;
	public static int DISTANCE_TOT_MOVABLE = 14;
	public static int DISTANCE_MAX_MOVABLE = 15;
	public static int DISTANCE_MIN_NPC = 16;
	public static int DISTANCE_TOT_NPC = 17;
	public static int DISTANCE_MAX_NPC = 18;
	public static int DISTANCE_MIN_PORTAL = 19;
	public static int DISTANCE_TOT_PORTAL = 20;
	public static int DISTANCE_MAX_PORTAL = 21;
	public static int DISTANCE_MIN_RESOURCE = 22;
	public static int DISTANCE_TOT_RESOURCE = 23;
	public static int DISTANCE_MAX_RESOURCE = 24;
	public static int NUMBER_IMMOVABLE = 25;
	public static int NUMBER_MOVABLE = 26;
	public static int NUMBER_NPC = 27;
	public static int NUMBER_PORTAL = 28;
	public static int NUMBER_RESOURCE = 29;
	public static int GRID_WIDTH = 30;
	public static int GRID_HEIGHT = 31;
	
	public static int getObservationLength(ArrayList<Observation>[] list){
		if(list == null) return 0;
		
    	int result = 0;
    	for(int i=0; i<list.length; i++){
    		result += list[i].size();
    	}
    	return result;
    }
    
    public static double getMaxObservation(ArrayList<Observation>[] list, Vector2d reference){
    	if(list == null) return 0;
    	
    	double result = 0;
    	
    	for(int i=0; i<list.length; i++){
    		for(int j=0; j<list[i].size(); j++){
    			double distance = list[i].get(j).position.dist(reference);
    			if(distance > result){
    				result = distance;
    			}
    		}
    	}
    	
    	return result;
    }
    
    public static double getMinObservation(ArrayList<Observation>[] list, Vector2d reference){
    	if(list == null) return 0;
    	
    	double result = Double.MAX_VALUE;
    	
    	for(int i=0; i<list.length; i++){
    		for(int j=0; j<list[i].size(); j++){
    			double distance = list[i].get(j).position.dist(reference);
    			if(distance < result){
    				result = distance;
    			}
    		}
    	}
    	
    	if(result == Double.MAX_VALUE){
    		return 0;
    	}
    	
    	return result;
    }
	
    public static double getTotObservation(ArrayList<Observation>[] list, Vector2d reference){
    	if(list == null) return 0;
    	
    	double result = 0;
    	
    	for(int i=0; i<list.length; i++){
    		for(int j=0; j<list[i].size(); j++){
    			double distance = list[i].get(j).position.dist(reference);
    			result += distance;
    		}
    	}
    	
    	return result;
    }
    
    public static boolean isOpposite(Types.ACTIONS a1, Types.ACTIONS a2){
    	if(a1 == Types.ACTIONS.ACTION_LEFT && a2 == Types.ACTIONS.ACTION_RIGHT){
    		return true;
    	}
    	if(a2 == Types.ACTIONS.ACTION_LEFT && a1 == Types.ACTIONS.ACTION_RIGHT){
    		return true;
    	}
    	
    	if(a1 == Types.ACTIONS.ACTION_UP && a2 == Types.ACTIONS.ACTION_DOWN){
    		return true;
    	}
    	if(a2 == Types.ACTIONS.ACTION_UP && a1 == Types.ACTIONS.ACTION_DOWN){
    		return true;
    	}
    	
    	return false;
    }
    
    public static int[][] updateTilesValue(int[][] tiles, int x, int y){
    	int[][] result = new int[tiles.length][tiles[0].length];
    	
    	for(int i=0; i<tiles.length; i++){
    		for(int j=0; j<tiles[i].length; j++){
    			result[i][j] = tiles[i][j];
    		}
    	}
    	
    	if(x >= 0 && y >= 0 && x < tiles.length && y < tiles[0].length) tiles[x][y] += 1;
    	
    	return result;
    }
}
