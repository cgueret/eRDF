/**
 * 
 */
package nl.erdf.util;

import java.util.Random;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class Randomizer {
	// The seed for the number generator
	private static long seed = System.currentTimeMillis() / 1000L;
	private static Random rand;

	/**
	 * @return the random number generator
	 * 
	 */
	public static Random instance() {
		if (rand == null)
			rand = new Random(seed);
		return rand;
	}

	/**
	 * @param newSeed
	 */
	public static void setSeed(long newSeed) {
		seed = newSeed;
		rand = null;
	}
}
