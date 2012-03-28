/**
 * 
 */
package nl.erdf.util;

import java.util.Random;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class RandomNumber {
	private static final Random twister = new Random();

	/**
	 * @param n
	 * @return an integer
	 */
	public static int nextInt(int n) {
		return twister.nextInt(n);
	}
}
