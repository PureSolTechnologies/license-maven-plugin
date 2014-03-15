package com.puresoltechnologies.maven.plugins.license;

import java.util.ArrayList;
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

import com.puresoltechnologies.maven.plugins.license.internal.ArtifactUtilities;
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
	 * This method retrieves all artifacts of the current Maven module.
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
		Log log = getLog();
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
			boolean skipTestScope, boolean skipProvidedScope,
			boolean skipOptionals) throws MojoExecutionException {
		return loadArtifacts(0, null, mavenProject.getArtifact(), recursive,
				skipTestScope, skipProvidedScope, skipOptionals);
	}

	/**
	 * Loads the artifact recursively.
	 * 
	 * @param artifact
	 * @param recursive
	 *            specified whether all dependencies should be loaded
	 *            recursively.
	 * @param skipTestScope
	 *            specified whether to skip test scoped artifacts or not.
	 * @return A {@link DependencyTree} object is returned.
	 * @throws MojoExecutionException
	 *             is thrown if anything unexpected goes wrong.
	 */
	private DependencyTree loadArtifacts(int depth,
			DependencyTree parentDependencyTree, Artifact artifact,
			boolean recursive, boolean skipTestScope,
			boolean skipProvidedScope, boolean skipOptionals)
			throws MojoExecutionException {
		Log log = getLog();
		MavenProject parentArtifactProject;
		try {
			parentArtifactProject = mavenProjectBuilder.buildFromRepository(
					artifact, remoteArtifactRepositories, localRepository);
		} catch (ProjectBuildingException e) {
			log.warn("Could not load artifacts recursively. For artifact '"
					+ ArtifactUtilities.toString(artifact)
					+ "' the project creation failed.", e);
			return null;
		}
		@SuppressWarnings("unchecked")
		List<Dependency> dependencies = parentArtifactProject.getDependencies();
		@SuppressWarnings("unchecked")
		List<License> licenses = parentArtifactProject.getLicenses();
		DependencyTree dependencyTree = new DependencyTree(artifact, licenses);
		if (parentDependencyTree != null) {
			parentDependencyTree.addDependency(dependencyTree);
		}
		if ((dependencies != null)
				&& ((recursive) || (artifact == mavenProject.getArtifact()))) {
			for (Dependency dependency : dependencies) {
				StringBuffer buffer = new StringBuffer();
				if (log.isDebugEnabled()) {
					for (int i = 0; i < depth; i++) {
						buffer.append("    ");
					}
					buffer.append("\\-> ");
					log.debug(buffer.toString()
							+ ArtifactUtilities.toString(dependency));
				}
				if (skipTestScope
						&& Artifact.SCOPE_TEST.equals(dependency.getScope())) {
					if (log.isDebugEnabled()) {
						log.debug(buffer.toString()
								+ " >> test scope is skipped");
					}
					continue;
				}
				if (skipProvidedScope
						&& Artifact.SCOPE_PROVIDED
								.equals(dependency.getScope())) {
					if (log.isDebugEnabled()) {
						log.debug(buffer.toString()
								+ " >> provided scope is skipped");
					}
					continue;
				}
				if (skipOptionals && dependency.isOptional()) {
					if (log.isDebugEnabled()) {
						log.debug(buffer.toString() + " >> optional is skipped");
					}
					continue;
				}
				if (hasCycle(dependencyTree, dependency)) {
					if (log.isDebugEnabled()) {
						log.debug(buffer.toString()
								+ " >> cylce found and needs to be skipped");
					}
					continue;
				}
				Artifact dependencyArtifact = DependencyUtilities
						.buildArtifact(artifact, dependency);
				loadArtifacts(depth + 1, dependencyTree, dependencyArtifact,
						recursive, skipTestScope, skipProvidedScope,
						skipOptionals);
			}
		}
		return dependencyTree;
	}

	private boolean hasCycle(DependencyTree dependencyTree,
			Dependency dependency) {
		Log log = getLog();
		List<DependencyTree> path = new ArrayList<>();
		while (dependencyTree != null) {
			path.add(0, dependencyTree);
			Artifact artifact = dependencyTree.getArtifact();
			String artifactString = ArtifactUtilities.toString(artifact);
			String dependencyString = ArtifactUtilities.toString(dependency);
			if (artifactString.equals(dependencyString)) {
				while (dependencyTree != null) {
					path.add(0, dependencyTree);
					dependencyTree = dependencyTree.getParent();
				}
				log.warn("WARNING! Cycle detected for '" + artifactString
						+ "':");
				for (int i = 0; i < path.size(); i++) {
					DependencyTree node = path.get(i);
					StringBuffer buffer = new StringBuffer();
					for (int col = 0; col < i; col++) {
						buffer.append("    ");
					}
					buffer.append("\\-> ");
					buffer.append(ArtifactUtilities.toString(node.getArtifact()));
					log.warn(buffer.toString());
				}
				StringBuffer buffer = new StringBuffer();
				buffer.append(" !! ");
				for (int col = 0; col < path.size(); col++) {
					buffer.append("    ");
				}
				buffer.append("\\-> ");
				buffer.append(artifactString);
				buffer.append(" !! ");
				log.warn(buffer.toString());
				return true;
			}
			dependencyTree = dependencyTree.getParent();
		}
		return false;
	}
}
