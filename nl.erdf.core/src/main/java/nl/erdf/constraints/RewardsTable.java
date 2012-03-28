/**
 * 
 */
package nl.erdf.constraints;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class RewardsTable {
	private final Map<String, Double> rewards = new HashMap<String, Double>();

	/**
	 * @return the rewards
	 */
	public Set<Entry<String, Double>> getRewards() {
		return rewards.entrySet();
	}

	/**
	 * @param variable
	 * @param reward
	 */
	public void set(String variable, Double reward) {
		rewards.put(variable, new Double(reward));
	}

	/**
	 * @param variable
	 * @return the reward associated to the variable
	 */
	public double get(String variable) {
		return rewards.get(variable).doubleValue();
	}
}
