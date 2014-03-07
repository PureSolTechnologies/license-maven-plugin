package com.puresoltechnologies.maven.plugins.license;

import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.License;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.settings.Settings;

import com.puresoltechnologies.maven.plugins.license.internal.DependencyTree;
import com.puresoltechnologies.maven.plugins.license.internal.DependencyUtilities;

/**
 * This abstract class provides basic functionality for all license validations.
 * 
 * @author Rick-Rainer Ludwig
 */
public abstract class AbstractValidationMojo extends AbstractMojo {

	@Component
	private MavenProject mavenProject;

	@Component
	private PluginDescriptor plugin;

	@Component
	private Settings settings;

	@Component
	private MavenProjectBuilder mavenProjectBuilder;

	/**
	 * This field contains the remote artifact repositories.
	 */
	@Parameter(required = false, defaultValue = "${project.remoteArtifactRepositories}", readonly = true)
	private List<ArtifactRepository> remoteArtifactRepositories;

	/**
	 * This field contains the local repository.
	 */
	@Parameter(required = false, defaultValue = "${localRepository}", readonly = true)
	private ArtifactRepository localRepository;

	private final Log log;

	protected AbstractValidationMojo() {
		log = getLog();
	}

	/**
	 * This method returns the current {@link MavenProject}.
	 * 
	 * @return A {@link MavenProject} object is returned referencing the current
	 *         Maven project.
	 */
	protected final MavenProject getMavenProject() {
		return mavenProject;
	}

	/**
	 * This method returns a {@link MavenProjectBuilder}.
	 * 
	 * @return A {@link MavenProjectBuilder} is returned.
	 */
	protected final MavenProjectBuilder getProjectBuilder() {
		return mavenProjectBuilder;
	}

	/**
	 * This method returns the license of an artifact.
	 * 
	 * @param artifact
	 *            is the {@link Artifact} where the license is to be read from.
	 * @return A {@link List} of {@link License} is returned containing the
	 *         licenses specified in the artifact.
	 * @throws MojoExecutionException
	 *             is throws if the Maven run is faulty.
	 */
	protected final List<License> retrieveLicenses(Artifact artifact)
			throws MojoExecutionException {
		try {
			MavenProject project = getProjectBuilder().buildFromRepository(
					artifact, remoteArtifactRepositories, localRepository);
			@SuppressWarnings("unchecked")
			List<License> licenses = project.getLicenses();
			return licenses;
		} catch (ProjectBuildingException e) {
			throw new MojoExecutionException("Cannot create repository.", e);
		}
	}

	/**
	 * This method retrievs all artifacts of the current Maven module.
	 * 
	 * <b>Attention(!):</b> This method uses
	 * {@link MavenProject#getDependencyArtifacts()} and
	 * {@link MavenProject#getArtifacts()} which are lazily filled with the
	 * artifacts.
	 * 
	 * @return A {@link Set} of {@link Artifact} is returned containing the
	 *         artifacts found.
	 */
	protected Set<Artifact> getArtifacts(boolean recursive) {
		if (recursive) {
			log.info("Recursive license validation is enabled. All direct and transitive dependency artifacts are going to be checked.");
			@SuppressWarnings("unchecked")
			Set<Artifact> set = mavenProject.getArtifacts();
			return set;
		} else {
			log.info("Recursive license validation is disabled. All only direct dependency artifacts are going to be checked.");
			@SuppressWarnings("unchecked")
			Set<Artifact> set = mavenProject.getDependencyArtifacts();
			return set;
		}
	}

	/**
	 * This method retrievs all artifacts of the current Maven module.
	 * 
	 * @return A {@link Set} of {@link Artifact} is returned containing the
	 *         artifacts found.
	 * @throws MojoExecutionException
	 */
	protected DependencyTree loadArtifacts(boolean recursive,
			boolean skipTestScope) throws MojoExecutionException {
		return loadArtifacts(mavenProject.getArtifact(), recursive,
				skipTestScope);
	}

	private DependencyTree loadArtifacts(Artifact parentArtifact,
			boolean recursive, boolean skipTestScope)
			throws MojoExecutionException {
		try {
			MavenProject parentArtifactProject = mavenProjectBuilder
					.buildFromRepository(parentArtifact,
							remoteArtifactRepositories, localRepository);
			@SuppressWarnings("unchecked")
			List<Dependency> dependencies = parentArtifactProject
					.getDependencies();
			@SuppressWarnings("unchecked")
			List<License> licenses = parentArtifactProject.getLicenses();
			DependencyTree dependencyTree = new DependencyTree(parentArtifact,
					licenses);
			if ((dependencies != null)
					&& ((recursive) || (parentArtifact == mavenProject
							.getArtifact()))) {
				for (Dependency dependency : dependencies) {
					log.debug("Found dependency: " + dependency.toString());
					if (skipTestScope
							&& Artifact.SCOPE_TEST
									.equals(dependency.getScope())) {
						continue;
					}
					Artifact dependencyArtifact = DependencyUtilities
							.buildArtifact(parentArtifact, dependency);
					dependencyTree.addDependency(loadArtifacts(
							dependencyArtifact, recursive, skipTestScope));
				}
			}
			return dependencyTree;
		} catch (ProjectBuildingException e) {
			throw new MojoExecutionException(
					"Could not load artifacts recursively.", e);
		}
	}
}
