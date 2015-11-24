package levelGenerators.constructiveLevelGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class LevelData {
	private String[][] level;
	private HashMap<String, Character> levelMapping;
	
	public LevelData(int width, int length){
		level = new String[width][length];
		levelMapping = new HashMap<String, Character>();
	}
	
	public String getLevel(){
		String result = "";
		char mapChar = 'a';
		
		for(int y=0; y<level[0].length; y++){
			for(int x=0; x<level.length; x++){
				if(level[x][y] == null){
					result += " ";
				}
				else{
					if(!levelMapping.containsKey(level[x][y])){
						levelMapping.put(level[x][y], mapChar);
						mapChar += 1;
					}
					result += levelMapping.get(level[x][y]);
				}
			}
			result += "\n";
		}
		result = result.substring(0, result.length() - 1);
		
		return result;
	}
	
	public void set(int x, int y, String stype){
		level[x][y] = stype;
	}
	
	public String get(int x, int y){
		return level[x][y];
	}
	
	public int getWidth(){
		return level.length;
	}
	
	public int getHeight(){
		return level[0].length;
	}
	
	public boolean checkConnectivity(int x1, int y1, int x2, int y2){
		if(level[x1][y1] != null || level[x2][y2] != null){
			return false;
		}
		ArrayList<Point> queue = new ArrayList<Point>();
		boolean[][] visited = new boolean[getWidth()][getHeight()];
		Point[] directions = new Point[]{new Point(0, 1), new Point(1, 0), new Point(0, -1), new Point(-1, 0)};
		queue.add(new Point(x1, y1));
		while(queue.size() > 0){
			Point current = queue.remove(0);
			if(current.x == x2 && current.y == y2){
				return true;
			}
			for(int i=0; i<directions.length; i++){
				Point newPoint = new Point(current.x + directions[i].x, current.y + directions[i].y);
				if(!checkInLevel(newPoint.x, newPoint.y)){
					continue;
				}
				if(!visited[newPoint.x][newPoint.y] && level[newPoint.x][newPoint.y] == null){
					visited[newPoint.x][newPoint.y] = true;
					queue.add(newPoint);
				}
			}
		}
		return false;
	}
	
	public boolean checkConnectivity(int x, int y){
		boolean result = false;
		set(x, y, "wall");
		if(x + 1 < getWidth() - 1 && x - 1 > 0){
			result |= checkConnectivity(x + 1, y, x - 1, y);
		}
		if(y + 1 < getHeight() - 1 && y - 1 > 0){
			result |= checkConnectivity(x, y + 1, x, y - 1);
		}
		set(x, y, null);
		return result;
	}
	
	public boolean checkInLevel(int x, int y){
		return (x >= 0 && y >=0 && x < getWidth() && y < getHeight());
	}
	
	public ArrayList<Point> getAllFreeSpots(){
		ArrayList<Point> result = new ArrayList<Point>();
		for(int x=0; x<level.length; x++){
			for(int y=0; y<level[0].length; y++){
				if(level[x][y] == null){
					result.add(new Point(x, y));
				}
			}
		}
		
		return result;
	}
	
	public HashMap<Character, ArrayList<String>> getLevelMapping(){
		HashMap<Character, ArrayList<String>> result = new HashMap<Character, ArrayList<String>>();
		for(Entry<String,Character> entry:levelMapping.entrySet()){
			ArrayList<String> list = new ArrayList<String>();
			list.add(entry.getKey());
			result.put(entry.getValue(), list);
		}
		return result;
	}
	
	public static class Point{
		public int x;
		public int y;
		
		public Point(){
			this.x = 0;
			this.y = 0;
		}
		
		public Point(int x, int y){
			this.x = x;
			this.y = y;
		}
		
		public ArrayList<Point> getSurroundingPoints(){
			ArrayList<Point> result = new ArrayList<Point>();
			result.add(new Point(this.x + 1, this.y));
			result.add(new Point(this.x - 1, this.y));
			result.add(new Point(this.x, this.y + 1));
			result.add(new Point(this.x, this.y - 1));
			
			return result;
		}
		
		public double getDistance(Point p){
			return Math.sqrt(Math.pow(this.x - p.x, 2) + Math.pow(this.y - p.y, 2));
		}
	}
}
