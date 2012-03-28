package nl.erdf.optimizer;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;

import nl.erdf.datalayer.DataLayer;
import nl.erdf.model.Request;
import nl.erdf.model.ResourceProvider;
import nl.erdf.model.Solution;
import nl.erdf.model.Variable;

import org.openrdf.model.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tolgam
 * 
 */
public class Generate {
	/** Logger */
	final Logger logger = LoggerFactory.getLogger(Generate.class);

	// Data layer
	private final DataLayer dataLayer;

	// Request
	private final Request request;

	/**
	 * @param dataLayer
	 * @param request
	 *            the request to solve
	 */
	public Generate(DataLayer dataLayer, Request request) {
		this.dataLayer = dataLayer;
		this.request = request;
	}

	/**
	 * Create a set of new candidate solutions. The set created is duplicate
	 * free and a duplicate count is set for all individuals
	 * 
	 * @param population
	 * @param target
	 * 
	 */
	public void createPopulation(SortedSet<Solution> population, Set<Solution> target) {
		// Enforce the values of some variable using the value of some others
		enforce(population, target);

		// Do crossover among the population
		crossover(population, target);

		logger.info("Created " + target.size() + " new individuals from " + population.size() + " parents");
	}

	/**
	 * @param population
	 * @param target
	 */
	private void enforce(SortedSet<Solution> population, Set<Solution> target) {
		for (Solution parent : population) {
			// Clone the parent
			Solution child = parent.clone();

			// Build a roulette with the reward of all the variables
			Roulette roulette = new Roulette();
			for (Variable variable : child.getVariables())
				roulette.add(variable, 1.0 / (1.0 + variable.getReward()));

			// Get a variable to change at random
			roulette.prepare();
			Variable variable = (Variable) roulette.nextElement();

			// Pick one of the providers at random
			List<ResourceProvider> providers = request.getResourceProvidersFor(variable.getName());
			Random rand = new Random();
			ResourceProvider p = providers.get(rand.nextInt(providers.size()));

			Value v = p.getResource(variable.getName(), child, dataLayer);
			child.getVariable(variable.getName()).setValue(v);

			// FIXME check if the equals for Solution is accurate
			target.add(child);
		}
	}

	/**
	 * @param population
	 * @param target
	 */
	private void crossover(SortedSet<Solution> population, Set<Solution> target) {
		// Prepare an array version of the source population for easy rotate
		Solution[] sources = new Solution[population.size()];
		int i = 0;
		for (Solution solution : population)
			sources[i++] = solution;

		// Take all parents by pair
		for (int first = 0; first != population.size() - 1; first++) {
			Solution firstSol = sources[first];
			for (int second = first + 1; second != population.size(); second++) {
				Solution secondSol = sources[second];

				Solution child = firstSol.clone();
				for (Variable variable : child.getVariables()) {
					double firstR = firstSol.getVariable(variable.getName()).getReward();
					double secondR = secondSol.getVariable(variable.getName()).getReward();
					if (secondR > firstR)
						child.getVariable(variable.getName()).setValue(
								secondSol.getVariable(variable.getName()).getValue());

				}

				target.add(child);
			}
		}
	}
}
