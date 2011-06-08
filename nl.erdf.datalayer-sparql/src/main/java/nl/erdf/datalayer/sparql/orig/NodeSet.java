/**
 * 
 */
package nl.erdf.datalayer.sparql.orig;

import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

/**
 * @author cgueret
 * 
 */
public class NodeSet {
	// Logging stuff
	protected final Logger logger = LoggerFactory.getLogger(NodeSet.class);

	/** Constant for an empty set */
	public final static NodeSet EMPTY_SET = new NodeSet(null);

	// Actual content of the set
	private final Set<Node> content = Collections
			.synchronizedSet(new HashSet<Node>());

	// The query pattern this set of resources correspond to
	private final Triple pattern;

	// Locking system to wait for first result to arrive or completion of update
	// process
	private final ReentrantLock contentLock = new ReentrantLock();
	private boolean hasResult = false;
	private boolean isBeingUpdated = false;
	private final Condition resultArrived = contentLock.newCondition();
	private final Condition noMoreUpdates = contentLock.newCondition();

	private int updateTasksCounter = 0;

	/**
	 * A resource set must be associated to a given pattern
	 * 
	 * @param p
	 */
	public NodeSet(Triple p) {
		this.pattern = p;
	}

	/**
	 * Returns the query pattern associated to this set of resources
	 * 
	 * @return the QueryPattern of the set
	 */
	public Triple getPattern() {
		return pattern;
	}

	/**
	 * Add a resource to the set
	 * 
	 * @param resource
	 *            the resource to add
	 */
	public void add(Node resource) {
		// Lock the access
		contentLock.lock();

		try {
			// Add the resource to the set
			content.add(resource);

			// Inform all the processes waiting for some content
			if (!hasResult) {
				hasResult = true;
				resultArrived.signalAll();
			}
		} finally {
			// Release the lock
			contentLock.unlock();
		}
	}

	/**
	 * Get a random resource from the set
	 * 
	 * @param random
	 *            a random number generator
	 * @return a resource from the set or URI.BLANK if the set is empty
	 */
	public Node get(Random random) {
		// Lock access to the content
		contentLock.lock();

		try {
			// If the resource set is empty, we return a blank
			if (content.isEmpty())
				return Node.NULL;

			// Uniformly pick one of the resources
			int index = random.nextInt(content.size());
			return (Node) content.toArray()[index];
		} finally {
			// Release the lock
			contentLock.unlock();
		}
	}

	/**
	 * Tells if a given Resource is in the set of not
	 * 
	 * @param resource
	 *            a Resource to check
	 * @return true if the resource is in the set
	 */
	public boolean contains(Node resource) {
		// Lock access to the content
		contentLock.lock();

		try {
			// TODO Implement different similarity measures here

			return content.contains(resource);
		} finally {
			// Release the lock
			contentLock.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		// Easy answer
		if (!(obj instanceof NodeSet))
			return false;

		// Lock access to the content
		contentLock.lock();

		try {
			// Compare
			return content.equals(((NodeSet) obj).content);
		} finally {
			// Release the lock
			contentLock.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		// Lock access to the content
		contentLock.lock();
		try {
			return content.hashCode();
		} finally {
			// Release the lock
			contentLock.unlock();
		}
	}

	/**
	 * @return the size of the set
	 */
	public int size() {
		// Lock access to the content
		contentLock.lock();
		try {
			return content.size();
		} finally {
			// Release the lock
			contentLock.unlock();
		}
	}

	/**
	 * Shortcut to test if the size is equal to zero
	 * 
	 * @return true if the set is empty
	 */
	public boolean isEmpty() {
		return (this.size() == 0);
	}

	/**
	 * @param value
	 */
	public void setUpdateTasksCounter(int value) {
		// Lock access to the content
		contentLock.lock();

		try {
			updateTasksCounter = value;
			isBeingUpdated = true;
		} finally {
			// Release the lock
			contentLock.unlock();
		}
	}

	/**
	 */
	public void decreaseUpdateTasksCounter() {
		// Lock access to the content
		contentLock.lock();

		try {
			updateTasksCounter = updateTasksCounter - 1;
			if (updateTasksCounter == 0) {
				// Announce that no more updates will be provided
				if (isBeingUpdated) {
					isBeingUpdated = false;
					noMoreUpdates.signalAll();
				}

				// The threads waiting for something to be added to the set can
				// also continue their activity
				if (!hasResult) {
					hasResult = true;
					resultArrived.signalAll();
				}
			}
		} finally {
			// Release the lock
			contentLock.unlock();
		}
	}

	/**
	 * @return true if there is no thread scheduled to update that resource set
	 */
	public boolean isFinal() {
		// Lock access to the content
		contentLock.lock();
		try {
			return (updateTasksCounter == 0);
		} finally {
			// Release the lock
			contentLock.unlock();
		}
	}

	/**
	 * Call to this method will block until some first results are added to the
	 * (empty) set or no more update processes are attached to it. Both event
	 * will switch the variable "resultArrived" to true
	 */
	public void waitForSomeContent() {
		// Lock access to the content
		contentLock.lock();
		try {
			while (!hasResult)
				resultArrived.await();
		} catch (InterruptedException ignore) {
		} finally {
			// Release the lock
			contentLock.unlock();
		}
	}

	/**
	 * Call to this method will block until no process are being updating the
	 * content of the set
	 */
	public void waitForFinalContent() {
		// Lock access to the content
		contentLock.lock();
		try {
			while (isBeingUpdated)
				noMoreUpdates.await();
		} catch (InterruptedException ignore) {
		} finally {
			// Release the lock
			contentLock.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		contentLock.lock();
		try {
			for (Node node : content)
				buffer.append(node.toString() + "(" + node.getClass() + ") ");
			return buffer.toString();
		} finally {
			// Release the lock
			contentLock.unlock();
		}

	}

	/**
	 * @return
	 */
	public Set<Node> content() {
		return content;
	}
}
