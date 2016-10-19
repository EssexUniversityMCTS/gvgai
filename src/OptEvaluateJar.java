import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import core.competition.CompetitionParameters;
import core.optimization.OptimizationObjective;
import core.optimization.ucbOptimization.UCBEvoEquation;
import core.optimization.ucbOptimization.UCBOptimization;

public class OptEvaluateJar {
	private static void writeOuputs(String outputPath, int currentRuns, double[] values) 
			throws FileNotFoundException, UnsupportedEncodingException{
		if(values == null){
			PrintWriter writer = new PrintWriter(outputPath + "output" + currentRuns + ".txt", "UTF-8");
			writer.println("FALSE");
			writer.close();
			return;
		}
		PrintWriter writer = new PrintWriter(outputPath + "output" + currentRuns + ".txt", "UTF-8");
		writer.println("TRUE");
		for(int i=0; i<values.length; i++){
			writer.println(values[i]);
		}
		writer.close();
	}
	
	public static void main(String[] args) 
			throws FileNotFoundException, UnsupportedEncodingException{
		String gamesPath = "examples/gridphysics/";
		String parameterFilePath = "examples/parameters.txt";
		String dataRuns = "examples/dataRuns.txt";
		String outputPath = "outputs/";
		
		String[] engineParameters = new tools.IO().readFile(parameterFilePath);
		ArrayList<String> games = new ArrayList<String>();
		ArrayList<String> levels = new ArrayList<String>();
		ArrayList<String> uniqueGames = new ArrayList<String>();
		ArrayList<Integer> numLevels = new ArrayList<Integer>();
		for(int i=0; i<engineParameters.length; i++){
			String type = engineParameters[i].split(":")[0].toLowerCase().trim();
			String value = engineParameters[i].split(":")[1].toLowerCase().trim();
			switch(type){
			case "game":
				String[] parts = value.split(",");
				uniqueGames.add(gamesPath + parts[0].trim() + ".txt");
				numLevels.add(parts.length - 1);
				for(int j=1; j<parts.length; j++){
					games.add(gamesPath + parts[0].trim() + ".txt");
					levels.add(gamesPath + parts[0].trim() + "_lvl" + parts[j].trim() + ".txt");
				}
				break;
			case "maxsteps":
				CompetitionParameters.MAX_TIMESTEPS = Integer.parseInt(value);
				break;
			case "repetitions":
				CompetitionParameters.OPTIMIZATION_REPEATITION = Integer.parseInt(value);
				break;
			case "evaluations":
				CompetitionParameters.OPTIMIZATION_EVALUATION = Integer.parseInt(value);
				break;
			case "safetymargin":
				controllers.singlePlayer.ucbOptimizerAgent.Agent.safetyMargin = Integer.parseInt(value);
				break;
			}
		}
		
		String[] data = new tools.IO().readFile(dataRuns);
		int currentRuns = Integer.parseInt(data[1].split(":")[1].trim());
		currentRuns += 1;
		if(currentRuns <= CompetitionParameters.OPTIMIZATION_EVALUATION){
			PrintWriter writer = new PrintWriter(dataRuns, "UTF-8");
			writer.println(data[0]);
			writer.println("current runs: " + currentRuns);
			writer.close();
		}
		else{
			writeOuputs(outputPath, currentRuns, null);
			return;
		}
		
		//run optimization process on ucb equation for an MCTS player
		double[] parameters = new double[args.length];
		for(int i=0; i<args.length; i++){
			parameters[i] = Double.parseDouble(args[i]);
		}
		
		String[] tempGames = new String[games.size()];
		String[] tempLevels = new String[levels.size()];
		for(int i=0; i<games.size(); i++){
			tempGames[i] = games.get(i);
			tempLevels[i] = levels.get(i);
		}
		
		OptimizationObjective obj = new UCBOptimization(tempGames, tempLevels, 
				CompetitionParameters.OPTIMIZATION_REPEATITION, 
				CompetitionParameters.OPTIMIZATION_EVALUATION, new UCBEvoEquation());
		double[] results = obj.evaluate(parameters);
		ArrayList<Double> correctResults = new ArrayList<Double>();
		int index = 0;
		for(int i=0; i<uniqueGames.size(); i++){
			correctResults.add(0.0);
			for(int j=0; j<numLevels.get(i); j++){
				correctResults.set(i, correctResults.get(i) + results[index]);
				index += 1;
			}
		}
		results = new double[uniqueGames.size()];
		for(int i=0; i<results.length; i++){
			results[i] = correctResults.get(i).doubleValue() / 1000.0;
		}
		writeOuputs(outputPath, currentRuns, results);
	}
}
