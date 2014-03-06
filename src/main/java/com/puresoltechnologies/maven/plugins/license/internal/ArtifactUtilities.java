package com.puresoltechnologies.maven.plugins.license.internal;

import org.apache.maven.artifact.Artifact;

/**
 * This method contains some helpers to deal with {@link Artifact}s.
 */
public class ArtifactUtilities {

	/**
	 * This method returns a string representing the {@link Artifact} given as
	 * parameter.
	 * 
	 * @param artifact
	 *            is the {@link Artifact} to be named.
	 * @return A {@link String} is returned containing the name.
	 */
	public static String toString(Artifact artifact) {
		String groupId = artifact.getGroupId();
		String artifactId = artifact.getArtifactId();
		String version = artifact.getVersion();
		String classifier = artifact.getClassifier();
		String type = artifact.getType();
		String scope = artifact.getScope();
		return groupId + ":" + groupId + ":" + artifactId + ":" + version + ":"
				+ classifier + ":" + type + " (" + scope + ")";
	}

	/**
	 * Private default constructor to avoid instantiation.
	 */
	private ArtifactUtilities() {
	}
}
