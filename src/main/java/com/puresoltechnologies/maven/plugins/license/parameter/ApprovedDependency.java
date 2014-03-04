package com.puresoltechnologies.maven.plugins.license.parameter;


/**
 * This class contains a single approved dependency.
 * 
 * @author Rick-Rainer Ludwig
 */
public class ApprovedDependency {

	/**
	 * This field contains the pattern of the dependency.
	 */
	private String identifier;
	/**
	 * This field contains the unique key of the license. This key needs to be
	 * set with {@link KnownLicense#setKey(String)}
	 */
	private String key;

	/**
	 * This is the default constructor.
	 */
	public ApprovedDependency() {
	}

	/**
	 * This method sets the license's name.
	 * 
	 * @param identifier
	 *            is set to {@link #identifier}.
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * This method returns the license name.
	 * 
	 * @return Returns {@link #identifier} as {@link String}.
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Sets the license's key.
	 * 
	 * @param key
	 *            is set to {@link #key}.
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * This method returns the license key.
	 * 
	 * @return Returns {@link #key} as {@link String}.
	 */
	public String getKey() {
		return key;
	}

}
