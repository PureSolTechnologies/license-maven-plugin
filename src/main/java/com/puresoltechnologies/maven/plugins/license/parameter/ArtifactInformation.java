package com.puresoltechnologies.maven.plugins.license.parameter;

import java.io.Serializable;

import org.apache.maven.artifact.Artifact;

/**
 * This class contains artifact information to identify it.
 * 
 * @author Rick-Rainer Ludwig
 */
public class ArtifactInformation implements Serializable {

	private static final long serialVersionUID = -6665948751674552705L;

	private final String groupId;
	private final String artifactId;
	private final String version;
	private final String classifier;
	private final String type;
	private final String scope;

	public ArtifactInformation(String groupId, String artifactId,
			String version, String classifier, String type, String scope) {
		super();
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
		this.classifier = classifier;
		this.type = type;
		this.scope = scope;
	}

	public ArtifactInformation(Artifact artifact) {
		this(artifact.getGroupId(), artifact.getArtifactId(), artifact
				.getVersion(), artifact.getClassifier(), artifact.getType(),
				artifact.getScope());
	}

	public String getGroupId() {
		return groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public String getVersion() {
		return version;
	}

	public String getClassifier() {
		return classifier;
	}

	public String getType() {
		return type;
	}

	public String getScope() {
		return scope;
	}

	/**
	 * This method returns the artifact identifier. The identifier returned here
	 * is a single string containing the artifactId, groupId and version
	 * separated by colons:
	 * 
	 * ${artifactId}:${groupId}:${version}
	 * 
	 * @return A {@link String} is returned containing the identifier.
	 */
	public String getIdentifier() {
		return groupId + ":" + artifactId + ":" + version;
	}
}
