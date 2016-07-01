package levelGenerators.jnicho;

import core.game.GameDescription;
import core.generator.AbstractLevelGenerator;
import tools.ElapsedCpuTimer;
import tools.GameAnalyzer;
import tools.LevelMapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

public class LevelGenerator extends AbstractLevelGenerator {

	//ArrayList<Double> bestFitness;
	private LevelMapping bestChromosomeLevelMapping;



	public LevelGenerator(GameDescription game, ElapsedCpuTimer elapsedTimer) {
		Constants.gameDescription = game;
		Constants.gameAnalyzer = new GameAnalyzer(game);
		Constants.random = new Random();
		//bestFitness = null;

	}


	@Override
	public String generateLevel(GameDescription game, ElapsedCpuTimer elapsedTimer) {

		//bestFitness = new ArrayList<Double>();
		Constants.gameDescription = game;

		int size = 0;
		if (Constants.gameAnalyzer.getSolidSprites().size() > 0)
			size = 2;

		int width = (int)Math.max(Constants.minSize + size, game.getAllSpriteData().size() * (1 + 0.25 * Constants.random.nextDouble()) + size);
		int height = (int)Math.max(Constants.minSize + size, game.getAllSpriteData().size() * (1 + 0.25 * Constants.random.nextDouble()) + size);
		width = (int)Math.min(width, Constants.maxSize + size);
		height = (int)Math.min(height, Constants.maxSize + size);


		ArrayList<Chromosome> population = new ArrayList<>();

		for (int i=0; i<Constants.populationSize; i++) {

			Chromosome chromosome = new Chromosome(width, height);
			chromosome.Randomise();

			chromosome.calculateFitness(Constants.evaluationTime);

			population.add(chromosome);


		}

		double worstTime = Constants.evaluationTime * Constants.populationSize;
		double avgTime = worstTime;
		double totalTime = 0;
		double numberOfIts = 0;

		System.out.println("Time left:"+elapsedTimer.remainingTimeMillis() + " " + avgTime + " " + worstTime);
		while (elapsedTimer.remainingTimeMillis() > (2* avgTime)
				&& elapsedTimer.remainingTimeMillis() > worstTime) {

			ElapsedCpuTimer timer = new ElapsedCpuTimer();
			System.out.println("Generation #" + (numberOfIts + 2) + " "+"Time left:"+elapsedTimer.remainingTimeMillis() + " " + avgTime + " " + worstTime);

			ArrayList<Chromosome> chromosomes = getNextPopulation(population);
			population.clear();
			for (Chromosome c: chromosomes) {
				population.add(c);
			}

			numberOfIts++;
			totalTime += timer.elapsedMillis();
			avgTime = totalTime/numberOfIts;

		}

		Collections.sort(population);
		bestChromosomeLevelMapping = population.get(0).getLevelMapping();
		return population.get(0).getLevelString(bestChromosomeLevelMapping);
	}

	private ArrayList<Chromosome> getNextPopulation(ArrayList<Chromosome> population) {
		ArrayList<Chromosome> newPop = new ArrayList<>();

		while (newPop.size() < Constants.populationSize) {

			ArrayList<Chromosome> oldPop = population;
			Chromosome parent1 = oldPop.get(Constants.random.nextInt(oldPop.size()));
			Chromosome parent2 = oldPop.get(Constants.random.nextInt(oldPop.size()));
			Chromosome child1 = parent1.clone();
			Chromosome child2 = parent1.clone();

			if (Constants.random.nextDouble() < Constants.crossOverProb) {
				ArrayList<Chromosome> children = parent1.crossOver(parent2);
				child1 = children.get(0);
				child2 = children.get(1);

				if (Constants.random.nextDouble() < Constants.mutationProb)
					child1.mutate();
				if (Constants.random.nextDouble() < Constants.mutationProb)
					child2.mutate();
			}

			else if (Constants.random.nextDouble() < Constants.mutationProb)
				child1.mutate();
			else if (Constants.random.nextDouble() < Constants.mutationProb)
				child2.mutate();

			newPop.add(child1);
			newPop.add(child2);
		}

		for (int i=0; i<newPop.size(); i++) {
			newPop.get(i).calculateFitness(Constants.evaluationTime);
		}

		Collections.sort(newPop);
		for (int i=Constants.populationSize-Constants.elitism; i<newPop.size(); i++) {
			newPop.remove(i);
		}

		Collections.sort(population);
		for (int i=0; i<Constants.elitism; i++) {
			newPop.add(population.get(i));
		}
		return newPop;
	}

	@Override
	public HashMap<Character, ArrayList<String>> getLevelMapping(){
		return bestChromosomeLevelMapping.getCharMapping();
	}
}
