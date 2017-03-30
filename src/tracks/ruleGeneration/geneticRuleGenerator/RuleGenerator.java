package tracks.ruleGeneration.geneticRuleGenerator;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import core.game.SLDescription;
import core.game.StateObservation;
import core.game.GameDescription.SpriteData;
import core.generator.AbstractRuleGenerator;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.LevelAnalyzer;
import tools.LevelMapping;

public class RuleGenerator extends AbstractRuleGenerator{

	
	/** Some necessary tools to make our generator work **/
	/** Random object to help in generation **/
	private Random random;
	/** A constructor to help speed up the creation of random generators **/
	private static tracks.ruleGeneration.randomRuleGenerator.RuleGenerator randomGen;
	/** A constructor to help speed up the creation of constructive generators */
	private static tracks.ruleGeneration.constructiveRuleGenerator.RuleGenerator constructGen;
	/** contains info about sprites and current level **/
	private SLDescription sl;
	/** amount of time allowed **/
	private ElapsedCpuTimer time;
	/** Level mapping of the best chromosome **/
	private String[][] bestChromosomeRuleset;
	/** The best chromosome fitness across generations **/
	private ArrayList<Double> bestFitness;
	/** number of feasible chromosomes across generations **/
	private ArrayList<Integer> numOfFeasible;
	/** number of infeasible chromosomes across generations **/
	private ArrayList<Integer> numOfInFeasible;
	
	/**
	 * This is an evolutionary rule generator
	 * @param sl	contains information about sprites and current level
	 * @param time	amount of time allowed to generate
	 */
	public RuleGenerator(SLDescription sl, ElapsedCpuTimer time) {
		this.sl = sl;
		this.time = time;
		SharedData.usefulSprites = new ArrayList<String>();
		SharedData.random = new Random();
		SharedData.la = new LevelAnalyzer(sl);
		
		randomGen = new tracks.ruleGeneration.randomRuleGenerator.RuleGenerator(sl, time);
		constructGen = new tracks.ruleGeneration.constructiveRuleGenerator.RuleGenerator(sl, time);
		
		try {
			SharedData.output = new PrintWriter(SharedData.filename);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String[][] currentLevel = sl.getCurrentLevel();
		//Just get the useful sprites from the current level
		for(SpriteData sd : sl.getGameSprites()) {
			SharedData.usefulSprites.add(sd.name);
		}
	}
	
	/**
	 * Get the next population based on the current feasible infeasible population
	 * @param fPopulation	array of the current feasible chromosomes
	 * @param iPopulation	array of the current infeasible chromosomes
	 * @return				array of the new chromosomes at the new population
	 */
	private ArrayList<Chromosome> getNextPopulation(ArrayList<Chromosome> fPopulation, ArrayList<Chromosome> iPopulation){
		ArrayList<Chromosome> newPopulation = new ArrayList<Chromosome>();

		//collect some statistics about the current generation
		ArrayList<Double> fitnessArray = new ArrayList<Double>();
		for(int i=0;i<fPopulation.size();i++){
			fitnessArray.add(fPopulation.get(i).getFitness().get(0));
		}

		Collections.sort(fitnessArray);
		if(fitnessArray.size() > 0){
			bestFitness.add(fitnessArray.get(fitnessArray.size() - 1));
		}
		else{
			bestFitness.add((double) 0);
		}
		numOfFeasible.add(fPopulation.size());
		numOfInFeasible.add(iPopulation.size());



		while(newPopulation.size() < SharedData.POPULATION_SIZE){
			//choosing which population to work on with 50/50 probability
			//of selecting either any of them
			ArrayList<Chromosome> population = fPopulation;
			if(fPopulation.size() <= 0){
				population = iPopulation;
			}
			if(SharedData.random.nextDouble() < 0.5){
				population = iPopulation;
				if(iPopulation.size() <= 0){
					population = fPopulation;
				}
			}


			//select the parents using roulettewheel selection
			Chromosome parent1 = rankSelection(population);//rouletteWheelSelection(population);
			Chromosome parent2 = rankSelection(population);//rouletteWheelSelection(population);
			Chromosome child1 = parent1.clone();
			Chromosome child2 = parent2.clone();
			//do cross over
			if(SharedData.random.nextDouble() < SharedData.CROSSOVER_PROB){
				ArrayList<Chromosome> children = parent1.crossover(parent2);
				child1 = children.get(0);
				child2 = children.get(1);


				//do mutation to the children
				if(SharedData.random.nextDouble() < SharedData.MUTATION_PROB){
					child1.mutate();
				}
				if(SharedData.random.nextDouble() < SharedData.MUTATION_PROB){
					child2.mutate();
				}
			}

			//mutate the copies of the parents
			else if(SharedData.random.nextDouble() < SharedData.MUTATION_PROB){
				child1.mutate();
			}
			else if(SharedData.random.nextDouble() < SharedData.MUTATION_PROB){
				child2.mutate();
			}



			//add the new children to the new population
			newPopulation.add(child1);
			newPopulation.add(child2);
		}


		//calculate fitness of the new population chromosomes
		for(int i=0;i<newPopulation.size();i++){
			newPopulation.get(i).calculateFitness(SharedData.EVALUATION_TIME);
			if(newPopulation.get(i).getConstrainFitness() < 1){
				System.out.println("\tChromosome #" + (i+1) + " Constrain Fitness: " + newPopulation.get(i).getConstrainFitness());
			}
			else{
				System.out.println("\tChromosome #" + (i+1) + " Fitness: " + newPopulation.get(i).getFitness());
			}
		}

			//add the best chromosome(s) from old population to the new population
		Collections.sort(newPopulation);
		for(int i=SharedData.POPULATION_SIZE - SharedData.ELITISM_NUMBER;i<newPopulation.size();){
			newPopulation.remove(i);
		}

		if(fPopulation.isEmpty()){
			Collections.sort(iPopulation);
			for(int i=0;i<SharedData.ELITISM_NUMBER;i++){
				newPopulation.add(iPopulation.get(i));
			}
		}
		else{
			Collections.sort(fPopulation);
			for(int i=0;i<SharedData.ELITISM_NUMBER;i++){
				newPopulation.add(fPopulation.get(i));
			}
		}

		for(int i=0;i<newPopulation.size();i++){
			try (FileWriter fw = new FileWriter(SharedData.filename, true);
					BufferedWriter bw = new BufferedWriter(fw);
					PrintWriter out = new PrintWriter(bw)) {
				out.println("*****");
				out.println("Chromosome " + (i+1) + " : Fitness = " + newPopulation.get(i).getFitness());
				for (String[] s : newPopulation.get(i).getRuleset()) {
					out.println(" ");
					for (String q : s) {
						out.println(q);
					}
				}
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
		}
		return newPopulation;
	}

	/**
	 * Performs rank selection on the given population
	 * @param population 	the population to be performed upon
	 * @return
	 */
	private Chromosome rankSelection(ArrayList<Chromosome> population) {

		double[] probabilities = new double[population.size()];
		double sum = 0.0;
		probabilities[0] = 1.0;
		for(int i = 1; i < population.size(); i++) {
			probabilities[i] = probabilities[i-1] + i;
		}
		for(int i = 0; i < probabilities.length; i++) {
			probabilities[i] = probabilities[i] / probabilities[probabilities.length - 1];
		}

		double chosen = SharedData.random.nextDouble();
		for(int i = 0; i < probabilities.length; i++) {
			if(chosen < probabilities[i]) {
				return population.get(i);
			}
		}
		return population.get(0);

	}
	/**
	 * Generates the rules using evolution
	 * @param sl	the SL description
	 * @param time	the time allowed for the generator to loop
	 */
	@Override
	public String[][] generateRules(SLDescription sl, ElapsedCpuTimer time) {
		
		//initialize the statistics objects
 		bestFitness = new ArrayList<Double>();
		numOfFeasible = new ArrayList<Integer>();
		numOfInFeasible = new ArrayList<Integer>();

		System.out.println("Generation #0: ");
		ArrayList<Chromosome> fChromosomes = new ArrayList<Chromosome>();
		ArrayList<Chromosome> iChromosomes = new ArrayList<Chromosome>();
		ArrayList<Chromosome> allChromosomes = new ArrayList<Chromosome>();
		int counter = 1;
		randomGen = new tracks.ruleGeneration.randomRuleGenerator.RuleGenerator(sl, time);
		constructGen = new tracks.ruleGeneration.constructiveRuleGenerator.RuleGenerator(sl, time);
		double avgFitness = 0.0;
		
		int count = (int) (SharedData.POPULATION_SIZE * SharedData.INIT_RANDOM_PERCENT);
		for(int i = 0; i < count; i++) {
			Chromosome c = new Chromosome(randomGen.generateRules(sl, time), sl, time);
			
			for(int q = 0; q < c.getRuleset().length; q++) {
				System.out.println("=====");
				for(int w = 0; w < c.getRuleset()[q].length; w++) {
					System.out.println(c.getRuleset()[q][w]);
				}
			}
			
			c.calculateFitness(SharedData.EVALUATION_TIME);
			if(c.getConstrainFitness() < 1){
				iChromosomes.add(c);
				System.out.println("\tChromosome #" + (counter) + " Constrain Fitness: " + c.getConstrainFitness());
			}
			else{
				fChromosomes.add(c);
				System.out.println("\tChromosome #" + (counter) + " Fitness: " + c.getFitness());
			}
			/**
			 * Writing stuff
			 */
			try (FileWriter fw = new FileWriter(SharedData.filename, true);
					BufferedWriter bw = new BufferedWriter(fw);
					PrintWriter out = new PrintWriter(bw)) {
				out.println("*****");
				out.println("Chromosome " + (counter) + " : Fitness = " + c.getFitness());
				out.println(" ");
				// print out chromosome
				for (String[] q : c.getRuleset()) {
					out.println(" ");
					for(String s : q) {
						out.println(s);
					}
				}
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
			/**
			 * End Writing stuff
			 */
			
			avgFitness += c.getFitness().get(1);
			counter++;
			allChromosomes.add(c);
		}
		for(int i = 0; i < SharedData.POPULATION_SIZE * SharedData.INIT_CONSTRUCT_PERCENT; i++) {
			Chromosome c = new Chromosome(constructGen.generateRules(sl, time), sl, time);
			for(int q = 0; q < c.getRuleset().length; q++) {
				System.out.println("=====");
				for(int w = 0; w < c.getRuleset()[q].length; w++) {
					System.out.println(c.getRuleset()[q][w]);
				}
			}
			c.calculateFitness(SharedData.EVALUATION_TIME);
			if(c.getConstrainFitness() < 1){
				iChromosomes.add(c);
				System.out.println("\tChromosome #" + (counter) + " Constrain Fitness: " + c.getConstrainFitness());
			}
			else{
				fChromosomes.add(c);
				System.out.println("\tChromosome #" + (counter) + " Fitness: " + c.getFitness());
			}
			/**
			 * Writing Stuff
			 */
			try (FileWriter fw = new FileWriter(SharedData.filename, true);
					BufferedWriter bw = new BufferedWriter(fw);
					PrintWriter out = new PrintWriter(bw)) {
				out.println("*****");
				out.println("Chromosome " + (counter) + " : Fitness = " + c.getFitness());
				out.println(" ");
				// print out chromosome
				for (String[] q : c.getRuleset()) {
					out.println(" ");
					for(String s : q) {
						out.println(s);
					}
				}

			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
			/**
			 * End Writing stuff
			 */
			avgFitness += c.getFitness().get(1);
			allChromosomes.add(c);
			counter++;
		}
		for(int i = 0; i < SharedData.POPULATION_SIZE * SharedData.INIT_MUT_PERCENT; i++) {
			Chromosome c = allChromosomes.get(SharedData.random.nextInt(allChromosomes.size()));
			for(int j = 0; j < SharedData.INIT_MUTATION_AMOUNT; j++) {
				c.mutate();
			}
			for(int q = 0; q < c.getRuleset().length; q++) {
				System.out.println("=====");
				for(int w = 0; w < c.getRuleset()[q].length; w++) {
					System.out.println(c.getRuleset()[q][w]);
				}
			}
			c.calculateFitness(SharedData.EVALUATION_TIME);
			if(c.getConstrainFitness() < 1){
				iChromosomes.add(c);
				System.out.println("\tChromosome #" + (counter) + " Constrain Fitness: " + c.getConstrainFitness());
			}
			else{
				fChromosomes.add(c);
				System.out.println("\tChromosome #" + (counter) + " Fitness: " + c.getFitness());
			}
			try (FileWriter fw = new FileWriter(SharedData.filename, true);
					BufferedWriter bw = new BufferedWriter(fw);
					PrintWriter out = new PrintWriter(bw)) {
				out.println("*****");
				out.println("Chromosome " + (counter) + " : Fitness = " + c.getFitness());
				out.println(" ");
				// print out chromosome
				for (String[] q : c.getRuleset()) {
					out.println(" ");
					for(String s : q) {
						out.println(s);
					}
				}

			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
			avgFitness += c.getFitness().get(1);
			allChromosomes.add(c);
			counter++;
		}


		//some variables to make sure not getting out of time
		double worstTime = SharedData.EVALUATION_TIME * SharedData.POPULATION_SIZE;
		double avgTime = worstTime;
		double totalTime = 0;
		int numberOfIterations = 0;
		avgFitness = avgFitness / 50;

		//System.out.println(time.remainingTimeMillis() + " avgTime: " + avgTime + " worstTime: " + worstTime);
		System.out.println("Average Fitness: " + avgFitness);
		try (FileWriter fw = new FileWriter(SharedData.filename, true);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(bw)) {
			out.println("Average Fitness: " + avgFitness);
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
		while(time.remainingTimeMillis() > 2 * avgTime &&
				time.remainingTimeMillis() > worstTime){
			ElapsedCpuTimer timer = new ElapsedCpuTimer();

			System.out.println("Generation #" + (numberOfIterations + 1) + ": ");
			try (FileWriter fw = new FileWriter(SharedData.filename, true);
					BufferedWriter bw = new BufferedWriter(fw);
					PrintWriter out = new PrintWriter(bw)) {
				out.println("### Generation : " + (numberOfIterations + 1) + " ###");
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
			//get the new population and split it to a the feasible and infeasible populations
			ArrayList<Chromosome> chromosomes = getNextPopulation(fChromosomes, iChromosomes);
			fChromosomes.clear();
			iChromosomes.clear();
			avgFitness = 0.0;
			for(Chromosome c:chromosomes){
				if(c.getConstrainFitness() < 1){
					iChromosomes.add(c);
				}
				else{
					fChromosomes.add(c);
				}
				avgFitness += c.getFitness().get(1);
			}
			avgFitness = avgFitness / 50;
			numberOfIterations += 1;
			totalTime += timer.elapsedMillis();
			avgTime = totalTime / numberOfIterations;
			Collections.sort(chromosomes);
			//System.out.println("Best Chromosome Fitness: " + chromosomes.get(0).getFitness());
			//System.out.println(fChromosomes.get(0).getRuleset());
			System.out.println("Average Fitness: " + avgFitness);
			try (FileWriter fw = new FileWriter(SharedData.filename, true);
					BufferedWriter bw = new BufferedWriter(fw);
					PrintWriter out = new PrintWriter(bw)) {
				out.println("\n\nGeneration : " + (numberOfIterations) + " Avg Fitness: " + avgFitness);
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
		}


		//return the best infeasible chromosome
		if(fChromosomes.isEmpty()){
			for(int i=0;i<iChromosomes.size();i++){
				iChromosomes.get(i).calculateFitness(SharedData.EVALUATION_TIME);
			}

			Collections.sort(iChromosomes);
			bestChromosomeRuleset = iChromosomes.get(0).getRuleset();
			System.out.println("Best Fitness: " + iChromosomes.get(0).getConstrainFitness());
			return iChromosomes.get(0).getRuleset();
		}

		//return the best feasible chromosome otherwise and print some statistics
		for(int i=0;i<fChromosomes.size();i++){
			fChromosomes.get(i).calculateFitness(SharedData.EVALUATION_TIME);
		}
		Collections.sort(fChromosomes);
		bestChromosomeRuleset = fChromosomes.get(0).getRuleset();
		System.out.println("Best Chromosome Fitness: " + fChromosomes.get(0).getFitness());
		System.out.println(bestFitness);
		System.out.println(numOfFeasible);
		System.out.println(numOfInFeasible);
		return fChromosomes.get(0).getRuleset();
	}

}
