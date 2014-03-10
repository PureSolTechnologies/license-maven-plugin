package com.puresoltechnologies.maven.plugins.license.internal;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;

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
		boolean optional = artifact.isOptional();
		if (optional) {
			scope += "/optional";
		}
		return groupId + ":" + artifactId + ":" + version + ":" + classifier
				+ ":" + type + " (" + scope + ")";
	}

	/**
	 * This method returns a string representing the {@link Dependency} given as
	 * parameter.
	 * 
	 * @param dependency
	 *            is the {@link Dependency} to be named.
	 * @return A {@link String} is returned containing the name.
	 */
	public static String toString(Dependency dependency) {
		String groupId = dependency.getGroupId();
		String artifactId = dependency.getArtifactId();
		String version = dependency.getVersion();
		String classifier = dependency.getClassifier();
		String type = dependency.getType();
		String scope = dependency.getScope();
		boolean optional = dependency.isOptional();
		if (optional) {
			scope += "/optional";
		}
		return groupId + ":" + artifactId + ":" + version + ":" + classifier
				+ ":" + type + " (" + scope + ")";
	}

	/**
	 * Private default constructor to avoid instantiation.
	 */
	private ArtifactUtilities() {
	}
}
