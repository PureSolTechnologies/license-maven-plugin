package com.puresoltechnologies.maven.plugins.license.parameter;

import java.net.URL;

/**
 * This class contains a single known license. This license knowledge is used to
 * be mapped to a dependency or artifact for reporting in a normalized form and
 * with license text attached.
 * 
 * @author Rick-Rainer Ludwig
 */
public class KnownLicense {

	/**
	 * This field contains a unique key for a license to be used as internal
	 * identifier. It is suggested to use an abbreviation for a license like
	 * 'asl2' for 'Apache Software License 2.0'.
	 */
	private String key;
	/**
	 * This is the official, full name of the license to be shown in reports.
	 */
	private String name;
	/**
	 * This field contains a URL to the full license text.
	 */
	private URL url;

	/**
	 * This is the default constructor.
	 */
	public KnownLicense() {
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
	 * Returns the URL to the license text.
	 * 
	 * @return The {@link #url} is returned.
	 */
	public URL getUrl() {
		return url;
	}

	/**
	 * This method sets a URL to the full license text.
	 * 
	 * @param url
	 *            is the URL to be set to {@link #url}.
	 */
	public void setUrl(URL url) {
		this.url = url;
	}

}
