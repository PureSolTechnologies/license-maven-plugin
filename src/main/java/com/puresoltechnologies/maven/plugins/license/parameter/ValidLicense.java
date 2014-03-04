package com.puresoltechnologies.maven.plugins.license.parameter;


/**
 * This class contains a single valid license.
 * 
 * @author Rick-Rainer Ludwig
 */
public class ValidLicense {

	/**
	 * This field contains the name of the license how it is set in the
	 * &lt;license&gt; tag in Maven's pom.xml.
	 */
	private String name;
	/**
	 * This field contains the unique key of the license. This key needs to be
	 * set with {@link KnownLicense#setKey(String)}
	 */
	private String key;

	/**
	 * This is the default constructor.
	 */
	public ValidLicense() {
	}

	/**
	 * This method sets the license's name.
	 * 
	 * @param name
	 *            is set to {@link #name}.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * This method returns the license name.
	 * 
	 * @return Returns {@link #name} as {@link String}.
	 */
	public String getName() {
		return name;
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
