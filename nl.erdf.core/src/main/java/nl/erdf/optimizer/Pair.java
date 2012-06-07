/**
 * 
 */
package nl.erdf.optimizer;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class Pair implements Comparable<Pair> {
	/**
	 * @param key
	 * @param value
	 */
	public Pair(String key, int value) {
		this.key = key;
		this.value = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + value;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pair other = (Pair) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (value != other.value)
			return false;
		return true;
	}

	/**
	 * @return the value
	 */
	public int getValue() {
		return value;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	private final String key;
	private final int value;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Pair arg0) {
		Integer a = new Integer(value);
		Integer b = new Integer(arg0.value);
		return b.compareTo(a);
	}

}
