package com.puresoltechnologies.maven.plugins.license.parameter;

import java.io.Serializable;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.plugins.annotations.Parameter;

/**
 * This class contains a single known license. This license knowledge is used to
 * be mapped to a dependency or artifact for reporting in a normalized form and
 * with license text attached.
 * 
 * @author Rick-Rainer Ludwig
 */
public class KnownLicense implements Serializable {

	private static final long serialVersionUID = 8553746493922750000L;

	/**
	 * This is the official, full name of the license to be shown in reports.
	 */
	@Parameter(alias = "name", required = true)
	private String name;

	/**
	 * This field contains a URL to the full license text.
	 */
	@Parameter(alias = "url", required = true)
	private URL url;

	/**
	 * Specifies whether the configured license is a valid license or not. The
	 * default value is set to <code>true</code>, because the main purpose is to
	 * check for valid licenses and all other licenses are treated as invalid.
	 * Check for explicitly invalid licenses is optional.
	 */
	@Parameter(alias = "valid", required = false, defaultValue = "false")
	private boolean valid = false;

	/**
	 * Contains alias names of the license as it can be found in pom.xml files
	 * in &lt;license&gt; tags.
	 */
	@Parameter(alias = "aliases", required = false)
	private final Set<String> aliases = new HashSet<>();

	/**
	 * This field contains the approved dependencies. The nomenclature is:
	 * 
	 * <pre>
	 * &lt;groupId&gt;:&lt;artifactId&gt;:&lt;version&gt;
	 * </pre>
	 * 
	 * The content of &lt;...&gt; supports regular expressions.
	 */
	@Parameter(alias = "approvedDependencies", required = false)
	private final Set<String> approvedDependencies = new HashSet<>();

	public KnownLicense() {
	}

	public KnownLicense(String name, URL url, boolean valid,
			Set<String> aliases, Set<String> approvedDependencies) {
		super();
		this.name = name;
		this.url = url;
		this.valid = valid;
		this.aliases.addAll(aliases);
		this.approvedDependencies.addAll(approvedDependencies);
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

	/**
	 * Set returns the {@link #valid} flag.
	 * 
	 * @return <code>true</code> is returned if the license is valid.
	 *         <code>false</code> is returned otherwise.
	 */
	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	/**
	 * Returns the {@link #aliases} {@link Set}.
	 * 
	 * @return A {@link Set} of {@link String} is returned containing the alias
	 *         names.
	 */
	public Set<String> getAliases() {
		return aliases;
	}

	/**
	 * Sets a new {@link #aliases} {@link Set}.
	 * 
	 * @param aliases
	 *            is a {@link Set} of {@link String} containing aliases for the
	 *            license.
	 */
	public void setAliases(Set<String> aliases) {
		this.aliases.clear();
		this.aliases.addAll(aliases);
	}

	/**
	 * Returns the {@link #approvedDependencies} {@link Set}.
	 * 
	 * @return A {@link Set} of {@link String} is returned containing names of
	 *         dependencies which are approved.
	 */
	public Set<String> getApprovedDependencies() {
		return approvedDependencies;
	}

	/**
	 * Sets a new {@link #approvedDependencies} {@link Set}.
	 * 
	 * @param approvedDependencies
	 *            is a {@link Set} of {@link String} which contains dependencies
	 *            which are approved.
	 */
	public void setApprovedDependencies(Set<String> approvedDependencies) {
		this.approvedDependencies.clear();
		this.approvedDependencies.addAll(approvedDependencies);
	}

	@Override
	public String toString() {
		return name + " (" + url + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((aliases == null) ? 0 : aliases.hashCode());
		result = prime
				* result
				+ ((approvedDependencies == null) ? 0 : approvedDependencies
						.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((url == null) ? 0 : url.toString().hashCode());
		result = prime * result + (valid ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		KnownLicense other = (KnownLicense) obj;
		if (aliases == null) {
			if (other.aliases != null)
				return false;
		} else if (!aliases.equals(other.aliases))
			return false;
		if (approvedDependencies == null) {
			if (other.approvedDependencies != null)
				return false;
		} else if (!approvedDependencies.equals(other.approvedDependencies))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.toString().equals(other.url.toString()))
			return false;
		if (valid != other.valid)
			return false;
		return true;
	}

}
