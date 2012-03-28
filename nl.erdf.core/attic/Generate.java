package nl.erdf.optimizer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import nl.erdf.datalayer.DataLayer;
import nl.erdf.model.Request;
import nl.erdf.model.ResourceProvider;
import nl.erdf.model.Solution;

import org.openrdf.model.Value;
import org.openrdf.query.algebra.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tolgam
 * 
 */
public class Generate {
	/** Logger */
	final Logger logger = LoggerFactory.getLogger(Generate.class);

	private class Pair {
		final Var variable;
		final ResourceProvider provider;

		Pair(Var variable, ResourceProvider provider) {
			this.variable = variable;
			this.provider = provider;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((provider == null) ? 0 : provider.hashCode());
			result = prime * result + ((variable == null) ? 0 : variable.hashCode());
			return result;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Pair other = (Pair) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (provider == null) {
				if (other.provider != null)
					return false;
			} else if (!provider.equals(other.provider))
				return false;
			if (variable == null) {
				if (other.variable != null)
					return false;
			} else if (!variable.equals(other.variable))
				return false;

			return true;
		}

		private Generate getOuterType() {
			return Generate.this;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Pair [variable=" + variable + ", provider=" + provider + "] ";
		}
	}

	private final DataLayer datalayer;
	private final Request request;

	// Assign an expected reward to each provider
	private final Map<Pair, Double> providerRewards = new HashMap<Pair, Double>();

	// Keeps track of the usage of the providers
	private final Map<Solution, Pair> providerUsage = new HashMap<Solution, Pair>();

	/**
	 * @param datalayer
	 * @param request
	 *            the request to solve
	 */
	public Generate(DataLayer datalayer, Request request) {
		this.datalayer = datalayer;
		this.request = request;

		// Init the rewards
		for (Var variable : request.variables())
			for (ResourceProvider provider : request.getProvidersFor(variable))
				for (Var prodvar : provider.getVariables())
					providerRewards.put(new Pair(prodvar, provider), 1.0d);

		// for (Entry<Pair, Double> a : providerRewards.entrySet())
		// logger.info(a.getKey() + "  " + a.getValue());
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
		// Clear the usage stats for the providers
		providerUsage.clear();

		// Generate offspringSize new solution per parent
		mutate(population, target);

		// Do crossover among the population
		crossover(population, target);

		// Enforce the values of some variable using the value of some others
		enforce(population, target);

		// logger.info("Created " + target.size() + " new individuals from " +
		// population.size() + " parents");
	}

	/**
	 * @param population
	 * @param target
	 */
	private void enforce(SortedSet<Solution> population, Set<Solution> target) {
		for (Solution parent : population) {
			// logger.info("Enforce " + parent);

			for (Binding b : parent.bindings()) {
				Var variable = b.getVariable();
				Solution solution = (Solution) parent.clone();
				Binding binding = solution.getBinding(variable);
				ResourceProvider provider = getProvider(variable, parent, 2);

				if (provider != null) {
					// Get a new binding
					Value resource = provider.getResource(variable, solution, datalayer);
					if (!resource.equals(Node.NULL) && !binding.getValue().equals(resource)) {
						binding.setValue(resource);

						// Add to the target population
						if (!target.contains(solution)) {
							providerUsage.put(solution, new Pair(variable, provider));
							target.add(solution);
						}
					}
					// logger.info(binding.getVariable().getName() + "=" +
					// resource + " (" + provider.toString() + ")");
				}
			}
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

				Solution solution = (Solution) firstSol.clone();
				for (Binding b : solution.bindings()) {
					if (secondSol.getBinding(b.getVariable()).getReward() > firstSol.getBinding(b.getVariable())
							.getReward())
						b.setValue(secondSol.getValue(b.getVariable()));
					else if (b.getValue().equals(Node.NULL) && !secondSol.getValue(b.getVariable()).equals(Node.NULL))
						b.setValue(secondSol.getBinding(b.getVariable()).getValue());
				}

				if (!target.contains(solution))
					target.add(solution);
			}
		}
	}

	/**
	 * @param population
	 * @param target
	 * @param offspringSize
	 */
	private void mutate(SortedSet<Solution> population, Set<Solution> target) {
		for (Solution parent : population) {
			// logger.info("Mutate " + parent);

			for (Binding b : parent.bindings()) {
				Node_Variable variable = b.getVariable();
				Solution solution = (Solution) parent.clone();
				Binding binding = solution.getBinding(variable);
				ResourceProvider provider = getProvider(variable, solution, 1);
				if (provider != null) {
					// Get a new binding
					Node resource = provider.getResource(variable, solution, datalayer);
					if (!resource.equals(Node.NULL) && !binding.getValue().equals(resource)) {
						binding.setValue(resource);

						// Add to the target population
						if (!target.contains(solution)) {
							providerUsage.put(solution, new Pair(variable, provider));
							target.add(solution);
						}
					}
					// logger.info(binding.getVariable().getName() + "=" +
					// resource + " (" + provider.toString() + ")");
				}
			}
		}
	}

	private ResourceProvider getProvider(Node_Variable variable, Solution solution, int nbvars) {
		// logger.info("Find provider for " + variable);

		// Find the highest selectivity
		// float maxS = -1;
		// for (ResourceProvider provider : request.getProvidersFor(variable)) {
		// QueryPattern query = provider.getQuery(variable, solution);
		// if (!query.contains(Node.NULL)) {
		// long selectivity = datalayer.getNumberOfResources(query);
		// long selectivity = 1;// FIXME Hack
		// if (selectivity > maxS)
		// maxS = selectivity;
		// }
		// }

		double max = 0;
		for (ResourceProvider provider : request.getProvidersFor(variable)) {
			if (provider.getVariables().size() == 1 && providerRewards.get(new Pair(variable, provider)) > max) {
				max = providerRewards.get(new Pair(variable, provider));
			}
		}

		// Build a roulette
		Roulette rouletteProvider = new Roulette();
		for (ResourceProvider provider : request.getProvidersFor(variable)) {
			// Don't use a provider that doesn't use the requested number of var
			if (provider.getVariables().size() != nbvars)
				continue;

			// Don't use a provider that would have a NULL in its query
			boolean skip = false;
			for (Node_Variable v : provider.getVariables())
				if (!v.equals(variable) && solution.getBinding(v).getValue().equals(Node.NULL))
					skip = true;
			if (skip)
				continue;

			// QueryPattern query = provider.getQuery(variable, solution);
			// if (!query.contains(Node.NULL)) {
			// long selectivity = datalayer.getNumberOfResources(query);
			// long selectivity = 1; // FIXME Hack

			// Initial expectation depends on the reward
			// double expectation = provider.getExpectedReward(request,
			// variable, solution);
			double expectation = providerRewards.get(new Pair(variable, provider));
			if (max > 0)
				expectation = expectation / max;
			// expectation = 1;

			// Adjust with the selectivity
			/*
			 * double minProb = 0.1; double maxProb = 0.9; double expo = (maxS
			 * == 1 ? 0 : (Math.log(minProb) - Math.log(maxProb)) /
			 * Math.log(maxS)); if (selectivity > 0) expectation *= maxProb *
			 * Math.pow(selectivity, expo); else if (selectivity < 0)
			 * expectation *= minProb; else if (selectivity == 0) expectation =
			 * 0;
			 */

			// Add to the roulette
			rouletteProvider.add(provider, expectation);
			// logger.info(expectation + " " + provider);
			// }
		}

		if (!rouletteProvider.hasMoreElements())
			return null;

		rouletteProvider.prepare();
		return (ResourceProvider) rouletteProvider.nextElement();
	}

	/**
	 * @param solutions
	 */
	public void updateProviderRewards(Set<Solution> solutions) {
		for (Solution solution : solutions) {
			Pair pair = providerUsage.get(solution);
			if (pair != null) {
				providerRewards.put(pair, providerRewards.get(pair) + solution.getBinding(pair.variable).getReward());
				// logger.info(pair.variable +"=" +
				// solution.getBinding(pair.variable).getValue() + " got " +
				// solution.getBinding(pair.variable).getReward() + " using " +
				// pair.provider);
			}
		}

	}
}
