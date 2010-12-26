package nl.erdf.model.wod;

/**
 * @author tolgam
 * 
 */
public enum Usage {
	/**
	 * Defines resources used as subject
	 */
	SUBJECT,

	/**
	 * Defines resources used as predicate
	 */
	PREDICATE,

	/**
	 * Defines resources used as object
	 */
	OBJECT,

	/**
	 * 
	 */
	UNKNOWN;

	/**
	 * @return a string representation of that property
	 */
	public final String toProperty() {
		switch (this) {
		case SUBJECT:
			return "#s";
		case PREDICATE:
			return "#p";
		case OBJECT:
			return "#o";
		default:
			return "";
		}
	}
}
