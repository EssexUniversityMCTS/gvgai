package controllers.singlePlayer.playTraces;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import core.game.Event;
import core.game.Observation;
import core.game.StateObservation;
import ontology.Types.ACTIONS;

public class CommonData {
    public static int index;
    public static int iteration;
    public static String gameName;
    public static String agentName;
    public static String outputPath;
    private static BufferedWriter bw;

    private static void drawGrid(ArrayList<Observation>[][] grid, int itype) throws IOException {
	bw.write(itype + "\n");
	for (int y = 0; y < grid[0].length; y++) {
	    String line = "";
	    for (int x = 0; x < grid.length; x++) {
		boolean exist = false;
		for (Observation obs : grid[x][y]) {
		    if (obs.itype == itype) {
			exist = true;
			break;
		    }
		}
		if (exist) {
		    line += "1";
		} else {
		    line += "0";
		}
	    }
	    bw.write(line + "\n");
	}
    }

    private static void saveGameState(StateObservation obs, ACTIONS act) throws IOException {
	bw.write("#" + obs.getGameTick() + ", " + obs.getGameScore() + ", " + obs.getGameWinner().toString() + ", "
		+ act + "#\n");
	bw.write("Interactions\n");
	for (Event e : obs.getEventsHistory()) {
	    if (e.gameStep == obs.getGameTick()) {
		bw.write(e.fromAvatar + ", " + e.activeTypeId + ", " + e.passiveTypeId);
	    }
	}
    }

    private static void saveAvatar(StateObservation obs) throws IOException {
	bw.write("Avatar\n");
	bw.write(obs.getAvatarHealthPoints() + ", " + obs.getAvatarOrientation() + ", " + obs.getAvatarSpeed() + "\n");
	drawGrid(obs.getObservationGrid(), obs.getAvatarType());

	bw.write("Avatar Resources\n");
	HashMap<Integer, Integer> res = obs.getAvatarResources();
	for (Map.Entry<Integer, Integer> entry : res.entrySet()) {
	    bw.write(entry.getKey() + ", " + entry.getValue());
	}
    }

    public static void openFile(){
	try {
	    bw = new BufferedWriter(
	    	    new FileWriter(outputPath + agentName + "_" + gameName + "_" + 
	    		    iteration + "_" + index + ".txt"));
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }
    
    public static void closeFile(){
	try {
	    bw.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }
    
    public static void saveData(StateObservation obs, ACTIONS act) {
	HashMap<Integer, Boolean> unique = new HashMap<>();
	ArrayList<Observation>[][] inter = obs.getObservationGrid();
	for (int i = 0; i < inter.length; i++) {
	    for (int j = 0; j < inter[i].length; j++) {
		for (Observation o : inter[i][j]) {
		    if (!unique.containsKey(o.itype)) {
			if (obs.getAvatarType() != o.itype) {
			    unique.put(o.itype, true);
			}
		    }
		}
	    }
	}

	try {
	    saveGameState(obs, act);
	    saveAvatar(obs);
	    bw.write("Other Sprites\n");
	    for (Integer k : unique.keySet()) {
		drawGrid(inter, k);
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }
}
