package nl.erdf.optimizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import nl.erdf.constraints.TripleConstraint;
import nl.erdf.model.Constraint;
import nl.erdf.model.Request;
import nl.erdf.model.Solution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Triple;

/**
 * @author tolgam
 * 
 */
public class Evaluate {
	// Logger instance
	protected final Logger logger = LoggerFactory.getLogger(Evaluate.class);

	// The request to work on
	private final Request request;

	// The parallel executor to speed stuff up
	private ExecutorService executor;

	/**
	 * @param request
	 * @param blackListedTriples
	 * @param executor
	 */
	public Evaluate(Request request, Set<Triple> blackListedTriples, ExecutorService executor) {
		// Save the request
		this.request = request;

		// Save the executor
		this.executor = executor;

		// Set the blacklisted triples reference to all the TripleConstraints
		for (final Constraint cstr : request.constraints()) {
			if (cstr instanceof TripleConstraint) {
				((TripleConstraint) cstr).setBlackListedTriples(blackListedTriples);
			}
		}
	}

	/**
	 * Evaluate all the candidate solutions, this is a threaded operation
	 * 
	 * @param population
	 */
	public void evaluatePopulation(Collection<Solution> population) {
		if (executor == null) {

			// No executor, sequential code
			for (final Solution solution : population) {
				double relevancy = request.evaluate(solution);
				solution.setFitness(relevancy);
			}

		} else {

			// Start all the evaluations
			List<Future<?>> list = new ArrayList<Future<?>>();
			for (final Solution solution : population) {
				Future<?> job = executor.submit(new Runnable() {
					public void run() {
						double relevancy = request.evaluate(solution);
						solution.setFitness(relevancy);
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
}
