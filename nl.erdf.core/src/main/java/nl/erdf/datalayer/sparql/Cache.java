package nl.erdf.datalayer.sparql;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import nl.erdf.datalayer.QueryPattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MTF-HashTables based implementation of a cache
 * 
 * 
 * @author tolgam
 * 
 */
public class Cache {
	/** Logger instance */
	protected final static Logger logger = LoggerFactory.getLogger(Cache.class);

	// The directory contains the list of end points to use
	private final Directory directory;

	// The cache content
	private class Bucket {
		public final ReentrantLock lock = new ReentrantLock();
		public final LinkedList<NodeSet> content = new LinkedList<NodeSet>();
	}

	private List<Bucket> cache;

	/** Defines the cache size per bucket */
	private static final int CACHE_SIZE = 4;

	/** Defines the number of buckets */
	private static final int NB_BUCKETS = 500000;

	/**
	 * Create a new cache instance
	 * 
	 * @param httpclient
	 * @param executorService
	 */
	public Cache(Directory directory) {
		this.directory = directory;

		clear();
	}

	/**
	 * Returns a bucket for a given hash code
	 * 
	 * @param hashCode
	 * @return the bucket associated to the hash code
	 */
	private Bucket getBucket(QueryPattern pattern) {
		return cache.get(Math.abs(pattern.hashCode()) % NB_BUCKETS);
	}

	/**
	 * @param hashCode
	 *           the hashCode of the object to return
	 * @return the object associated to the hasCode
	 */
	public NodeSet get(QueryPattern pattern) {
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
		resources.setUpdateTasksCounter(directory.endPoints().size());

		// Iterate over all the end points and execute a new update task
		for (EndPoint endpoint : directory.endPoints())
			if (endpoint.getErrorsCounter() < 4)
				endpoint.executeCacheUpdateTask(resources);
	}

	/**
	 * @return
	 */
	public Directory getDirectory() {
		return directory;
	}

}
