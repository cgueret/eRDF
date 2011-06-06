package nl.erdf.optimizer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.graph.Triple;

/**
 * @author tolgam
 * 
 */
public class Optimizer extends Observable implements Runnable {
	/** Population size */
	private static final int POPULATION_SIZE = 6;

	/** Population size */
	private static final int OFFSPRING_SIZE = 12;

	/** Maximum generation to wait before finding an optima */
	private static final int MAXIMUM_GENERATION = POPULATION_SIZE*4;

	/** Logger */
	protected final Logger logger = LoggerFactory.getLogger(Optimizer.class);

	/** Population */
	protected final SortedSet<Solution> population = new TreeSet<Solution>();

	/** Hall of fame to put all the results found */
	private final Set<Triple> blackListedTriples = new HashSet<Triple>();

	/** Mutation operator used to generate new populations */
	private final Generate generateOp;

	/** Evaluation operator to evaluate all the candidates */
	private final Evaluate evaluateOp;

	/** Counter for statistics about the number of evaluations */
	private int evaluationsCounter = 0;
	private final Request request;

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
		this.request = request;
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

			//
			// Initialise the population with a dummy individual
			//
			if (population.isEmpty()) {
				Solution solution = new Solution();
				for (Node_Variable variable : request.variables())
					solution.add(new Binding(variable, Node.NULL));
				population.add(solution);
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

			//
			// Track for optimality
			//
			double topFitness = population.last().getFitness();
			for (Solution s : population) {
				// Increment age
				if (s.getFitness() != topFitness)
					s.resetAge();
				s.incrementAge();

				// Check optimality
				boolean isOptimal = ((s.getAge() >= MAXIMUM_GENERATION && s.getFitness() > 0) || (s.getFitness() == 1));
				s.setOptimal(isOptimal);

				// If the solution is optimal add its (valid!) triples to the black
				// list
				if (isOptimal) {
					synchronized (blackListedTriples) {
						blackListedTriples.addAll(request.getTripleSet(s));
					}
				}

				// Print solution
				// logger.info(s.toString());
			}

			logger.info("Generation " + generation + ", best fitness=" + topFitness);

			//
			// Notify observers that a loop has been done
			//
			setChanged();
			notifyObservers(population);

			//
			// Wait a bit for the data layer
			//
			datalayer.waitForLatencyBuffer();

			//
			// Remove all optimum individuals from the population
			//
			List<Solution> toRemove = new ArrayList<Solution>();
			for (Solution s : population)
				if (s.isOptimal())
					toRemove.add(s);
			population.removeAll(toRemove);
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
