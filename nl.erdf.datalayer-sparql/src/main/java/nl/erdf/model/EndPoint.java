package nl.erdf.model;

import java.net.URI;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class EndPoint {
	public enum EndPointType {
		OWLIM, VIRTUOSO, HBASE, UNKNOWN
	}

	private final String defaultGraph;
	private final URI uri;
	private final EndPointType type;

	// By default, the end point is enabled
	private boolean enabled = true;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((defaultGraph == null) ? 0 : defaultGraph.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
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
		EndPoint other = (EndPoint) obj;
		if (defaultGraph == null) {
			if (other.defaultGraph != null)
				return false;
		} else if (!defaultGraph.equals(other.defaultGraph))
			return false;
		if (type != other.type)
			return false;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "EndPoint [defaultGraph=" + defaultGraph + ", uri=" + uri + ", type=" + type + "]";
	}

	/**
	 * @param uri
	 * @param defaultGraph
	 */
	public EndPoint(String uri, String defaultGraph, EndPointType type) {
		this.uri = URI.create(uri);
		this.defaultGraph = defaultGraph;
		this.type = type;
	}

	/**
	 * @return
	 */
	public String getDefaultGraph() {
		return defaultGraph;
	}

	/**
	 * @return the type
	 */
	public EndPointType getType() {
		return type;
	}

	/**
	 * @return
	 */
	public URI getURI() {
		return uri;
	}

	/**
	 * @return the enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * @param enabled
	 *            the enabled to set
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

}
