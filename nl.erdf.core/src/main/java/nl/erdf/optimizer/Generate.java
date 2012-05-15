package nl.erdf.optimizer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;

import nl.erdf.constraints.Constraint;
import nl.erdf.constraints.impl.StatementPatternConstraint;
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
			Set<String> changed = new HashSet<String>();

			// Clone the parent
			Solution child = parent.clone();

			// Build a roulette with the reward of all the variables
			Roulette roulette = new Roulette();
			for (Variable variable : child.getVariables()) {
				double p = 1.0 / (1.0 + variable.getReward());
				roulette.add(variable.getName(), p);
			}

			// Get a variable to change at random
			roulette.prepare();
			String variable = (String) roulette.nextElement();

			// Pick one of the providers at random
			Roulette roulette2 = new Roulette();
			logger.info(request.getResourceProvidersFor(variable).toString());
			for (ResourceProvider provider : request.getResourceProvidersFor(variable)) {
				roulette2.add(provider, 1.0);
				/*
				 * if (provider instanceof StatementPatternProvider) {
				 * StatementPatternProvider prov = (StatementPatternProvider)
				 * provider; StatementPattern ptrn = prov.getStatement(); Value
				 * s = ptrn.getSubjectVar().getName().equals(variable) ? null :
				 * Convert.getValue( ptrn.getSubjectVar(), child); Value p =
				 * ptrn.getPredicateVar().getName().equals(variable) ? null :
				 * Convert.getValue( ptrn.getPredicateVar(), child); Value o =
				 * ptrn.getObjectVar().getName().equals(variable) ? null :
				 * Convert.getValue( ptrn.getObjectVar(), child);
				 * 
				 * Triple t = new Triple((Resource) s, (URI) p, o);
				 * 
				 * if (t.getNumberNulls() == 1) { long nb =
				 * dataLayer.getNumberOfResources(t); if (nb > 0) { double pp =
				 * 0.5; if (dataLayer.isValid(Convert.toTriple(ptrn, child))) pp
				 * *= 2; logger.info(child.hashCode() + "=>  " + variable + " "
				 * + t.toString() + " " + pp); roulette2.add(provider, pp); } }
				 * }
				 */
			}
			if (roulette2.isEmpty())
				continue;

			ResourceProvider p = (ResourceProvider) roulette2.nextElement();

			// Get a new value
			Value v = p.getResource(variable, child, dataLayer);
			child.getVariable(variable).setValue(v);
			// logger.info("Assign " + v + " to " + variable);

			// Add this variable to the changed variables
			changed.add(variable);

			// Do cascading changes
			propagateChange(child, variable, changed);

			// Add the new individual
			if (!target.contains(child))
				target.add(child);
		}
	}

	/**
	 * @param variable
	 * @param changed
	 */
	private void propagateChange(Solution child, String variable, Set<String> changed) {
		// Iterate over the constraints
		for (Constraint cstr : request.constraints()) {
			if (cstr instanceof StatementPatternConstraint) {
				Set<String> vars = cstr.getVariables();
				if (vars.remove(variable) && vars.size() == 1) {
					String secondVariable = (String) vars.toArray()[0];
					if (!changed.contains(secondVariable)) {
						// Use all the providers to get a new value
						ArrayList<Value> v = new ArrayList<Value>();
						for (ResourceProvider provider : request.getResourceProvidersFor(secondVariable)) {
							if (provider.getVariables().contains(variable)) {
								// Assign a new value
								Value v2 = provider.getResource(secondVariable, child, dataLayer);
								if (v2 != null)
									v.add(v2);
							}
						}
						Random rand = new Random();
						if (!v.isEmpty())
							child.getVariable(secondVariable).setValue(v.get(rand.nextInt(v.size())));
						else
							child.getVariable(secondVariable).setValue(null);

						// logger.info("Change " + secondVariable + "=" + v +
						// " after " + variable);

						// See what can be changed from here
						changed.add(secondVariable);
						propagateChange(child, secondVariable, changed);
					}
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

				Solution child = firstSol.clone();
				for (Variable variable : child.getVariables()) {
					double firstR = firstSol.getVariable(variable.getName()).getReward();
					double secondR = secondSol.getVariable(variable.getName()).getReward();
					if (secondR > firstR)
						child.getVariable(variable.getName()).setValue(
								secondSol.getVariable(variable.getName()).getValue());

				}

				if (!target.contains(child))
					target.add(child);
			}
		}
	}
}
