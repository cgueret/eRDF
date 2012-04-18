package nl.erdf.datalayer.sparql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.openrdf.query.algebra.StatementPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MTF-HashTables based implementation of a cache
 * 
 * 
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 *         TODO replace with http://ehcache.org/downloads/catalog
 */
public class Cache {
	/** Logger instance */
	protected final static Logger logger = LoggerFactory.getLogger(Cache.class);

	// The executors for the SPARQL queries
	private final Collection<EndPointExecutor> executors;

	// The cache content
	private class Bucket {
		final ReentrantLock lock = new ReentrantLock();
		final LinkedList<NodeSet> content = new LinkedList<NodeSet>();
	}

	private List<Bucket> cache;

	/** Defines the cache size per bucket */
	private static final int CACHE_SIZE = 4;

	/** Defines the number of buckets */
	private static final int NB_BUCKETS = 500000;

	/**
	 * Create a new cache instance
	 * 
	 * @param directory
	 *            the collection of {@link EndPointExecutor} to use
	 * 
	 */
	public Cache(Collection<EndPointExecutor> executors) {
		this.executors = executors;

		clear();
	}

	/**
	 * Returns a bucket for a given hash code
	 * 
	 * @param hashCode
	 * @return the bucket associated to the hash code
	 */
	private Bucket getBucket(StatementPattern pattern) {
		return cache.get(Math.abs(pattern.hashCode()) % NB_BUCKETS);
	}

	/**
	 * @param pattern
	 * @return the object associated to the hasCode
	 */
	public NodeSet get(StatementPattern pattern) {
		// Get the bucket
		Bucket bucket = getBucket(pattern);

		bucket.lock.lock();
		try {
			// Find the entry
			NodeSet entry = null;
			Iterator<NodeSet> iterator = bucket.content.iterator();
			while (iterator.hasNext() && (entry == null)) {
				NodeSet tmp = iterator.next();
				if (tmp.getPattern().equals(pattern))
					entry = tmp;
			}

			// If the content is not in the cache, add it
			// and trigger a request to fill it. Otherwise,
			// just move it to the front
			if (entry == null) {
				// Create the new resource set
				entry = new NodeSet(pattern);

				// Emit requests to fill it up
				emitRequests(entry);

				// Append the element to the beginning of the list
				bucket.content.addFirst(entry);

				// If the list is too big, remove the last element
				if (bucket.content.size() == CACHE_SIZE)
					bucket.content.removeLast();

			} else {
				// Move it to the front
				bucket.content.remove(entry);
				bucket.content.addFirst(entry);
			}

			// Return the result
			return entry;
		} finally {
			bucket.lock.unlock();
		}
	}

	/**
	 * 
	 */
	public void clear() {
		// Create the buckets;
		cache = null;
		cache = new ArrayList<Bucket>();
		for (int index = 0; index < NB_BUCKETS; index++)
			cache.add(new Bucket());
	}

	/**
	 * Emits requests to populate a cache entry
	 * 
	 * @param resources
	 */
	private void emitRequests(NodeSet resources) {
		// Declare to the resourceSet the number of updating tasks for it
		resources.setUpdateTasksCounter(executors.size());

		// Queue an update task for that end point
		for (EndPointExecutor executor : executors) {
			executor.executeCacheUpdateTask(resources);
		}
	}
}
