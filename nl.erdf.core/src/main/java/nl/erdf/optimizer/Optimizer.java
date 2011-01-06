package nl.erdf.optimizer;

import java.util.HashSet;
import java.util.Observable;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import nl.erdf.datalayer.DataLayer;
import nl.erdf.model.Binding;
import nl.erdf.model.Request;
import nl.erdf.model.Solution;
import nl.erdf.model.Variable;
import nl.erdf.model.wod.SPARQLRequest;
import nl.erdf.model.wod.TripleSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;

/**
 * @author tolgam
 * 
 */
public class Optimizer extends Observable implements Runnable {
	/** Population size */
	private static final int POPULATION_SIZE = 6;

	/** Population size */
	private static final int OFFSPRING_SIZE = 10;

	/** Maximum generation to wait before finding an optima */
	private static final int MAXIMUM_GENERATION = OFFSPRING_SIZE;

	/** Logger */
	protected final Logger logger = LoggerFactory.getLogger(Optimizer.class);

	/** Population */
	protected final SortedSet<Solution> population = new TreeSet<Solution>();

	/** Hall of fame to put all the results found */
	private final TripleSet blackListedTriples = new TripleSet();

	/** Mutation operator used to generate new populations */
	private final Generate generateOp;

	/** Evaluation operator to evaluate all the candidates */
	private final Evaluate evaluateOp;

	/** Counter for statistics about the number of evaluations */
	private int evaluationsCounter = 0;
	private final SPARQLRequest request;

	/** Activity control */
	private boolean isPaused = false;
	private boolean isTerminated = false;
	private ReentrantLock pauseLock = new ReentrantLock();
	private Condition unpaused = pauseLock.newCondition();

	// Generation counter
	private int generation = 0;

	private DataLayer datalayer;

	/**
	 * Optimizer
	 * 
	 * @param datalayer
	 * @param request
	 * @param executor
	 * 
	 */
	public Optimizer(final DataLayer datalayer, final Request request, final ExecutorService executor) {
		// Save a pointer to the request and the datalayer
		this.request = (SPARQLRequest) request;
		this.datalayer = datalayer;

		// Create the operators
		this.generateOp = new Generate(datalayer, request);
		this.evaluateOp = new Evaluate(request, blackListedTriples, executor);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// Do not run something terminated
		if (isTerminated)
			return;

		// The best so far guy
		Solution best = null;
		int age = 0;

		//
		// Initialise the population with a dummy individual
		//
		Solution solution = new Solution();
		for (Variable variable : request.variables())
			solution.add(new Binding(variable, Node.NULL));
		best = (Solution) solution.clone();
		population.add(solution);

		logger.info("Run optimizer");
		while (true) {
			pauseLock.lock();
			try {
				while (isPaused)
					unpaused.await();
				if (isTerminated)
					return;
			} catch (InterruptedException ie) {
				// Finish
				return;
			} finally {
				pauseLock.unlock();
			}

			// Increment the generation counter
			++generation;

			//
			// Generate a new set of offspring and copy the parents into it
			// first
			//
			// logger.info("Generate");
			Set<Solution> newPopulation = new HashSet<Solution>();
			newPopulation.addAll(population);// Add the parents
			generateOp.createPopulation(population, newPopulation, OFFSPRING_SIZE);

			//
			// Evaluate all of them
			//
			// logger.info("Evaluate " + newPopulation.size());
			// Counts the number of different solutions
			evaluationsCounter += newPopulation.size() - population.size();
			evaluateOp.evaluatePopulation(newPopulation);
			/*
			 * String buffer = "Fitnesses "; for (Solution s : newPopulation)
			 * buffer += s.getFitness() + " "; logger.info(buffer);
			 */

			//
			// Get rid of the previous population and insert the kids
			//
			// logger.info("Cut");
			population.clear();
			population.addAll(newPopulation);
			while (population.size() > POPULATION_SIZE)
				population.remove(population.first());

			// for (Solution s :population)
			// logger.info(s.toString());

			//
			// Track for optimality
			//
			Solution challenger = population.last();
			if (challenger.equals(best)) {
				best = (Solution) challenger.clone();// Clone it anyway to take
				// the new fitness in
				// account
				// if (best.isCertain())
				age++;
			} else {
				best = (Solution) challenger.clone();
				age = 0;
			}
			boolean opt = ((age >= MAXIMUM_GENERATION) || (best.getFitness() == 1));
			best.setOptimal(opt);
			logger.info("Generation " + generation + " fitness best invididual: " + best.getFitness());
			// logger.info(best.toString());

			//
			// If the solution is optimal add its (valid!) triples to the
			// black list
			//
			synchronized (blackListedTriples) {
				if (best.isOptimal())
					blackListedTriples.addAll(request.getTripleSet(best));
			}

			//
			// Notify observers that a loop has been done
			//
			setChanged();
			notifyObservers(best);

			datalayer.waitForLatencyBuffer();
		}
	}

	/**
	 * Stop the execution of the optimizer
	 */
	public void terminate() {
		logger.info("Terminate optimizer");
		pauseLock.lock();
		try {
			// Set the status to true
			isTerminated = true;
		} finally {
			pauseLock.unlock();
		}
	}

	/**
	 * @return true if the optimizer is stopped
	 */
	public boolean isTerminated() {
		boolean res;
		pauseLock.lock();
		try {
			res = isTerminated;
		} finally {
			pauseLock.unlock();
		}
		return res;
	}

	/**
	 * Pause the algorithm
	 */
	public void pause() {
		logger.info("Pause optimizer " + this);
		pauseLock.lock();
		try {
			isPaused = true;
		} finally {
			pauseLock.unlock();
		}
	}

	/**
	 * @return true if the search algorithm is paused
	 */
	public boolean isPaused() {
		boolean res;
		pauseLock.lock();
		try {
			res = isPaused;
		} finally {
			pauseLock.unlock();
		}
		return res;
	}

	/**
	 * Continue the execution
	 */
	public void resume() {
		logger.info("Resume optimizer " + this);
		pauseLock.lock();
		try {
			isPaused = false;
			unpaused.signalAll();
		} finally {
			pauseLock.unlock();
		}
	}

	/**
	 * @return the evaluations counter
	 */
	public int getEvaluationsCounter() {
		return evaluationsCounter;
	}

	/**
	 * @return the generations counter
	 */
	public int getGenerationsCounter() {
		return generation;
	}
}
