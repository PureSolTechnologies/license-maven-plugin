package com.puresol.maven.plugins.license;

import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.License;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.settings.Settings;

/**
 * This class implements the conversion into Eclipse Help files.
 * 
 * Used to configure injection of Plexus components by
 * MavenPluginManager.getConfiguredMojo(...) and special Maven objects as well:
 * 
 * mojoExecution org.apache.maven.plugin.MojoExecution project
 * org.apache.maven.project.MavenProject session
 * org.apache.maven.execution.MavenSession settings
 * org.apache.maven.settings.Settings plugin (Maven-3 only)
 * org.apache.maven.plugin.descriptor.PluginDescriptor
 * 
 * @author Rick-Rainer Ludwig
 * 
 */
@Mojo(//
name = "validate", //
requiresDirectInvocation = false, //
requiresProject = true, //
requiresReports = false, //
requiresOnline = false, //
inheritByDefault = true, //
threadSafe = true//
)
@Execute(//
goal = "validate",//
phase = LifecyclePhase.VALIDATE//
)
public class ValidatorMojo extends AbstractMojo {

	private static final String TEST_SCOPE_NAME = "test";

	@Parameter(required = false, defaultValue = "${project.remoteArtifactRepositories}", readonly = true)
	private List<ArtifactRepository> remoteArtifactRepositories;

	@Parameter(required = false, defaultValue = "${localRepository}", readonly = true)
	private ArtifactRepository localRepository;

	@Parameter(alias = "validLicenses", required = true)
	private Set<String> validLicenses;

	@Parameter(alias = "failFast", required = false, defaultValue = "false")
	private boolean failFast;

	@Parameter(alias = "recursive", required = false, defaultValue = "true")
	private boolean recursive;

	@Parameter(alias = "skipTestScope", required = false, defaultValue = "false")
	private boolean skipTestScope;

	@Component
	private MavenProject project;

	@Component
	// for Maven 3 only
	private PluginDescriptor plugin;

	@Component
	private Settings settings;

	@Component
	private MavenProjectBuilder projectBuilder;

	private final Log log;

	public ValidatorMojo() {
		log = getLog();
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		Set<Artifact> artifacts = retrieveArtifacts();
		boolean valid = validateArtifacts(artifacts);
		if (!valid) {
			throw new MojoFailureException("Invalid license(s) was/were found!");
		}
	}

	private Set<Artifact> retrieveArtifacts() {
		Set<Artifact> artifacts;
		if (recursive) {
			@SuppressWarnings("unchecked")
			Set<Artifact> set = project.getArtifacts();
			artifacts = set;
		} else {
			@SuppressWarnings("unchecked")
			Set<Artifact> set = project.getDependencyArtifacts();
			artifacts = set;
		}
		return artifacts;
	}

	private boolean validateArtifacts(Set<Artifact> artifacts)
			throws MojoExecutionException, MojoFailureException {
		boolean valid = true;
		for (Artifact artifact : artifacts) {
			if (!isArtifactValid(artifact)) {
				valid = false;
			}
		}
		return valid;
	}

	private boolean isArtifactValid(Artifact artifact)
			throws MojoFailureException, MojoExecutionException {
		if (log.isDebugEnabled()) {
			log.debug("Checking artifact '" + getArtifactIdentifier(artifact)
					+ "'...");
		}
		if (skipTestScope) {
			if (TEST_SCOPE_NAME.equals(artifact.getScope())) {
				if (log.isDebugEnabled()) {
					log.debug("Artifact is in '" + TEST_SCOPE_NAME
							+ "' scope and this scope is valid per default.");
				}
				return true;
			}
		}
		boolean valid = true;
		List<License> licenses = retrieveLicenses(artifact);
		String artifactName = getArtifactIdentifier(artifact);
		if (licenses.size() == 0) {
			log.error("No license for artifact " + artifactName + " found.");
			if (failFast) {
				throw new MojoFailureException(
						"Invalid license(s) was/were found!");
			}
			valid = false;
		}
		for (License license : licenses) {
			if (!isLicenseValid(license)) {
				String licenseName = license.getName();
				log.error("Invalid or unknown license '" + licenseName
						+ "' for artifact " + artifactName + " found.");
				if (failFast) {
					throw new MojoFailureException(
							"Invalid license(s) was/were found!");
				}
				valid = false;
			}
		}
		return valid;
	}

	private List<License> retrieveLicenses(Artifact artifact)
			throws MojoExecutionException {
		try {
			MavenProject repository = projectBuilder.buildFromRepository(
					artifact, remoteArtifactRepositories, localRepository);
			@SuppressWarnings("unchecked")
			List<License> licenses = repository.getLicenses();
			return licenses;
		} catch (ProjectBuildingException e) {
			throw new MojoExecutionException("Cannot create repository.", e);
		}
	}

	private String getArtifactIdentifier(Artifact artifact) {
		String groupId = artifact.getGroupId();
		String artifactId = artifact.getArtifactId();
		String version = artifact.getVersion();
		String artifactName = groupId + ":" + artifactId + ":" + version;
		return artifactName;
	}

	private boolean isLicenseValid(License license) {
		String licenseName = license.getName();
		if (log.isDebugEnabled()) {
			log.debug("Check license '" + licenseName + "'...");
		}
		if ((licenseName == null) || (licenseName.isEmpty())) {
			return false;
		}
		return validLicenses.contains(licenseName);
	}
}
