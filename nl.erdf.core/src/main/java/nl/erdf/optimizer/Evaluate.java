package nl.erdf.optimizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import nl.erdf.constraints.Constraint;
import nl.erdf.constraints.RewardsTable;
import nl.erdf.datalayer.DataLayer;
import nl.erdf.model.Request;
import nl.erdf.model.Solution;
import nl.erdf.model.TripleSet;
import nl.erdf.model.Variable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tolgam
 * 
 */
public class Evaluate {
	// Logger instance
	protected final Logger logger = LoggerFactory.getLogger(Evaluate.class);

	// Request to work on
	private final Request request;

	// Parallel executor to speed stuff up
	private ExecutorService executor;

	// Set of black listed triples
	private TripleSet blackListedTriples;

	// Data layer
	private DataLayer dataLayer;

	/**
	 * @param request
	 * @param dataLayer
	 * @param blackListedTriples
	 * @param executor
	 */
	public Evaluate(Request request, DataLayer dataLayer, TripleSet blackListedTriples, ExecutorService executor) {
		this.request = request;
		this.dataLayer = dataLayer;
		this.executor = executor;
		this.blackListedTriples = blackListedTriples;
	}

	/**
	 * Evaluate all the candidate solutions, this is a threaded operation
	 * 
	 * @param population
	 */
	public void evaluatePopulation(Collection<Solution> population) {
		if (executor == null) {

			// No executor, sequential code
			for (final Solution solution : population)
				evaluate(solution);

		} else {

			// Start all the evaluations
			List<Future<?>> list = new ArrayList<Future<?>>();
			for (final Solution solution : population) {
				Future<?> job = executor.submit(new Runnable() {
					public void run() {
						evaluate(solution);
					}
				});
				list.add(job);
			}

			// Wait for all the results
			for (Future<?> future : list) {
				try {
					future.get();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}

		}
	}

	/**
	 * The fitness of that candidate solution is defined as the average of the
	 * individual rewards each binding received.
	 * 
	 * @param solution
	 */
	public void evaluate(Solution solution) {
		// Reset the rewards of that solution
		solution.resetScores();

		// Test all the constraints
		for (Constraint cstr : request.constraints()) {
			RewardsTable rewards = cstr.getRewards(solution, dataLayer, blackListedTriples);

			// Increment the reward of the variables
			for (Entry<String, Double> r : rewards.getRewards()) {
				Variable variable = solution.getVariable(r.getKey());
				double reward = variable.getReward();
				variable.setReward(reward + r.getValue().doubleValue());
			}
		}

		// Set the fitness of the solution
		solution.setFitness(solution.getTotalReward() / request.getMaximumReward());
	}
}
