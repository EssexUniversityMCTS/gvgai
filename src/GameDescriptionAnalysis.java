import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import core.VGDLFactory;
import core.VGDLParser;
import core.VGDLRegistry;
import core.game.Game;
import core.game.GameDescription;
import core.game.GameDescription.SpriteData;
import core.game.GameDescription.TerminationData;
import ontology.Types.ACTIONS;
import tools.GameAnalyzer;

public class GameDescriptionAnalysis {
	public static void main(String[] args){
		String folderPath = "examples/gridphysics/";
		String dataFile = "examples/gameDescriptionAnalysisWeka.txt";
		
		ArrayList<String> games = new ArrayList<String>();
		File[] files = new File(folderPath).listFiles();
		for(File f:files){
			if(f.getName().contains("_lvl") || (f.getName().split("\\.")[0]).length() == 0){
				continue;
			}
			games.add(f.getName());
		}

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(dataFile, "UTF-8");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        for(String g : games){
        	VGDLFactory.GetInstance().init();
            VGDLRegistry.GetInstance().init();
            System.out.println(g);
        	Game toPlay = new VGDLParser().parseGame(folderPath + g);
        	GameDescription description = null;
        	try{
        		description = new GameDescription(toPlay);
        	}
        	catch(Exception e){
        		continue;
        	}
        	GameAnalyzer analyzer = new GameAnalyzer(description);
        	
        	String gameName = g.substring(0, g.indexOf('.'));
        	double isVertical = description.getAvailableActions(false).contains(ACTIONS.ACTION_UP) || 
        			description.getAvailableActions(false).contains(ACTIONS.ACTION_DOWN)? 1:0;
        	double isUse = description.getAvailableActions(false).contains(ACTIONS.ACTION_USE)? 1: 0;
        	double playerDead = 0;
        	double isSurvival = 1;
        	double winCondition = 0;
        	double loseCondition = 0;
        	double solidNumber = analyzer.getSolidSprites().size() * 1.0 / description.getAllSpriteData().size();
        	double avatarNumber = analyzer.getAvatarSprites().size() * 1.0 / description.getAllSpriteData().size();
        	double harmfulNumber = analyzer.getHarmfulSprites().size() * 1.0 / description.getAllSpriteData().size();
        	double collectNumber = analyzer.getCollectableSprites().size() * 1.0 / description.getAllSpriteData().size();
        	double goalNumber = analyzer.getGoalSprites().size() * 1.0 / description.getAllSpriteData().size();
        	double otherNumber = analyzer.getOtherSprites().size() * 1.0 / description.getAllSpriteData().size();
        	double createNumber = 0;
        	double maxInteraction = 0;
        	double totalInteraction = 0;
        	for(SpriteData s:description.getAllSpriteData()){
        		if(analyzer.checkIfSpawned(s.name) == 0){
        			createNumber += 1;
        		}
        		if(analyzer.getPriorityNumber(s.name) > maxInteraction){
        			maxInteraction = analyzer.getPriorityNumber(s.name);
        		}
        		for(SpriteData s2:description.getAllSpriteData()){
        			totalInteraction += description.getInteraction(s.name, s2.name).size();
        		}
        	}
        	createNumber /= description.getAllSpriteData().size();
        	
        	ArrayList<TerminationData> ts = description.getTerminationConditions();
        	ArrayList<SpriteData> avatars = description.getAvatar();
        	for(TerminationData t:ts){
        		boolean containAvatar = false;
        		for(SpriteData a:avatars){
        			if(t.sprites.contains(a.name)){
        				containAvatar = true;
        			}
        		}
        		if(!t.win){
        			loseCondition += 1;
        			if(containAvatar){
        				playerDead = 1;
        			}
        		}
        		else{
        			winCondition += 1;
        			if(!t.type.toLowerCase().equals("timeout")){
        				isSurvival = 0;
        			}
        		}
        	}
        	
        	double total = winCondition + loseCondition;
        	winCondition /= total;
        	loseCondition /= total;
        	
        	writer.println(gameName + ", " + isVertical + ", " + isUse + ", " + playerDead + ", " + isSurvival +
        			", " + loseCondition + ", " + winCondition + ", " + solidNumber + ", " + avatarNumber + 
        			", " + harmfulNumber + ", " + collectNumber + ", " + goalNumber + ", " + otherNumber +
        			", " + createNumber + ", " + (maxInteraction / totalInteraction));
        }
        if(writer != null){
        	writer.close();
        }
	}
}
