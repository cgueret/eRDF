package nl.erdf.optimizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 * @author tolgam
 * 
 */
public class Roulette implements Enumeration<Object> {
	/** An entry of the roulette */
	public class Entry {
		/**
		 * 
		 */
		public Object object;
		/**
		 * 
		 */
		public double value;

		/**
		 * @param object
		 * @param value
		 */
		public Entry(Object object, double value) {
			this.object = object;
			this.value = value;
		}
	}

	// Randomizer
	private static final Random random = new Random();

	// Entries of the roulette
	private List<Entry> content = new ArrayList<Entry>();

	// Sum of their weights
	private double total = 0;

	/**
	 * @param object
	 * @param value
	 */
	public void add(Object object, double value) {
		content.add(new Entry(object, value));
	}

	/**
	 * @return the content
	 */
	public Collection<Entry> content() {
		return content;
	}

	/**
	 * Normalize the content of the wheel
	 */
	public void prepare() {
		total = 0;
		for (Entry entry : content)
			total += entry.value;

		if (total == 0) {
			for (Entry entry : content) {
				entry.value = 1;
				total += 1;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Enumeration#hasMoreElements()
	 */
	public boolean hasMoreElements() {
		return (!content.isEmpty());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Enumeration#nextElement()
	 */
	public Object nextElement() {
		if (!hasMoreElements())
			throw new NoSuchElementException();

		double value = random.nextFloat() * total;
		double min = 0;
		for (Entry entry : content) {
			double entryVal = entry.value;
			if ((min <= value) && (value < min + entryVal))
				return entry.object;
			min += entryVal;
		}

		// FIXME BUG !!! if this is printed
		// System.out.println("BUG");

		return content.get(0).object;
	}
}
