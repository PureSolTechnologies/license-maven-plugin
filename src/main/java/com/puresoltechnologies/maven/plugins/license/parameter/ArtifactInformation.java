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
		this.classifier = classifier == null ? "" : classifier;
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

	/**
	 * This method returns the artifact identifier. The identifier returned here
	 * is a single string containing the artifactId, groupId and version
	 * separated by colons:
	 * 
	 * ${artifactId}:${groupId}:${version}
	 * 
	 * @return A {@link String} is returned containing the identifier.
	 */
	@Override
	public String toString() {
		return groupId + ":" + artifactId + ":" + version + ":" + classifier
				+ ":" + type + ":" + scope;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((artifactId == null) ? 0 : artifactId.hashCode());
		result = prime * result
				+ ((classifier == null) ? 0 : classifier.hashCode());
		result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
		result = prime * result + ((scope == null) ? 0 : scope.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ArtifactInformation other = (ArtifactInformation) obj;
		if (artifactId == null) {
			if (other.artifactId != null) {
				return false;
			}
		} else if (!artifactId.equals(other.artifactId)) {
			return false;
		}
		if (classifier == null) {
			if (other.classifier != null) {
				return false;
			}
		} else if (!classifier.equals(other.classifier)) {
			return false;
		}
		if (groupId == null) {
			if (other.groupId != null) {
				return false;
			}
		} else if (!groupId.equals(other.groupId)) {
			return false;
		}
		if (scope == null) {
			if (other.scope != null) {
				return false;
			}
		} else if (!scope.equals(other.scope)) {
			return false;
		}
		if (type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!type.equals(other.type)) {
			return false;
		}
		if (version == null) {
			if (other.version != null) {
				return false;
			}
		} else if (!version.equals(other.version)) {
			return false;
		}
		return true;
	}

}
