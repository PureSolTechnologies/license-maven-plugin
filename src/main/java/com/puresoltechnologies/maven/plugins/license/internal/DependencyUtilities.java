package com.puresoltechnologies.maven.plugins.license.internal;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.model.Dependency;

/**
 * This method contains some helpers to deal with {@link Dependency} objects.
 */
public class DependencyUtilities {

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
		return groupId + ":" + groupId + ":" + artifactId + ":" + version + ":"
				+ classifier + ":" + type + " (" + scope + ")";
	}

	/**
	 * This method builds a new {@link Artifact} from a {@link Dependency}.
	 * 
	 * @param parentArtifact
	 *            is a parent Artifact which is used to get an
	 *            {@link ArtifactHandler} from.
	 * @param dependency
	 *            is the {@link Dependency} object whose information is to be
	 *            used to build a new artifact.
	 * @return An {@link Artifact} is returned build from the {@link Dependency}
	 *         parameter.
	 */
	public static Artifact buildArtifact(Artifact parentArtifact,
			Dependency dependency) {
		DefaultArtifact dependencyArtifact = new DefaultArtifact(
				dependency.getGroupId(), dependency.getArtifactId(),
				dependency.getVersion(), dependency.getScope(),
				dependency.getType(), dependency.getClassifier(),
				parentArtifact.getArtifactHandler());
		dependencyArtifact.setOptional(dependency.isOptional());
		return dependencyArtifact;
	}

	/**
	 * Private default constructor to avoid instantiation.
	 */
	private DependencyUtilities() {
	}
}
