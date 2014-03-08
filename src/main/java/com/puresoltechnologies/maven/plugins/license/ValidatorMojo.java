/*
 * Copyright 2013 PureSol Technologies
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.puresoltechnologies.maven.plugins.license;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.License;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import com.puresoltechnologies.maven.plugins.license.internal.ArtifactUtilities;
import com.puresoltechnologies.maven.plugins.license.internal.DependencyTree;
import com.puresoltechnologies.maven.plugins.license.internal.IOUtilities;
import com.puresoltechnologies.maven.plugins.license.parameter.ApprovedDependency;
import com.puresoltechnologies.maven.plugins.license.parameter.ArtifactInformation;
import com.puresoltechnologies.maven.plugins.license.parameter.KnownLicense;
import com.puresoltechnologies.maven.plugins.license.parameter.ValidLicense;
import com.puresoltechnologies.maven.plugins.license.parameter.ValidationResult;

/**
 * This class is a Maven Mojo to check the validity of licenses specified in the
 * dependencies of a maven module.
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
 */
@Mojo(//
name = "validate", //
requiresDirectInvocation = false, //
requiresProject = true, //
requiresReports = false, //
requiresOnline = false, //
inheritByDefault = true, //
threadSafe = true,//
requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,//
requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME//
)
@Execute(//
goal = "validate",//
phase = LifecyclePhase.VALIDATE//
)
public class ValidatorMojo extends AbstractValidationMojo {

	private static final String TEST_SCOPE_NAME = "test";

	@Parameter(alias = "knownLicenses", required = true)
	private Set<KnownLicense> knownLicenses;

	/**
	 * This list contains the list of valid license names.
	 */
	@Parameter(alias = "validLicenses", required = true)
	private Set<ValidLicense> validLicenses;

	/**
	 * This list contains the regexp string for approved artifacts.
	 */
	@Parameter(alias = "approvedDependencies", required = true)
	private Set<ApprovedDependency> approvedDependencies;

	@Parameter(alias = "outputDirectory", required = false, defaultValue = "${project.build.directory}/licenses")
	private File outputDirectory;

	/**
	 * This parameter contains whether this Mojo shall fail fast (with first
	 * occurrence of an invalid license) or should do all the work first before
	 * failing. Default value is false.
	 */
	@Parameter(alias = "failFast", required = false, defaultValue = "false")
	private boolean failFast;

	/**
	 * This parameter contains whether or not the dependency should be checked
	 * recursively or not. Default is true.
	 */
	@Parameter(alias = "recursive", required = false, defaultValue = "true")
	private boolean recursive;

	/**
	 * Specifies whether or not the dependencies in test scope should be
	 * skipped. Default is false.
	 */
	@Parameter(alias = "skipTestScope", required = false, defaultValue = "false")
	private boolean skipTestScope;

	/**
	 * This field contains the writer for the validation result file.
	 */
	private OutputStreamWriter writer;

	/**
	 * Contains the log file.
	 */
	private final Log log;

	public ValidatorMojo() {
		log = getLog();
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		storeSettings();
		DependencyTree dependencyTree = loadArtifacts(recursive, skipTestScope);
		log.debug("Artifact which are going to be checked:");
		for (DependencyTree dependency : dependencyTree) {
			log.debug("  * "
					+ ArtifactUtilities.toString(dependency.getArtifact()));
		}
		validateArtifacts(dependencyTree);
	}

	private void storeSettings() throws MojoExecutionException {
		File file = IOUtilities.createNewSettingsFile(log, outputDirectory);
		try (FileOutputStream fileOutputStream = new FileOutputStream(file);
				OutputStreamWriter propertiesWriter = new OutputStreamWriter(
						fileOutputStream, Charset.defaultCharset())) {
			Properties properties = new Properties();
			properties.setProperty("recursive", Boolean.toString(recursive));
			properties.setProperty("skipTestScope",
					Boolean.toString(skipTestScope));
			properties
					.store(propertiesWriter, "license-maven-plugin settings.");
		} catch (IOException e) {
			throw new MojoExecutionException(
					"Could not write settings.properties.", e);
		}
	}

	/**
	 * This method checks a set of artifacts for validity.
	 * 
	 * @param dependencies
	 *            is a {@link Set} of {@link Artifact} which is to be checked
	 *            for validity.
	 * @throws MojoExecutionException
	 *             is throw if the execution was faulty.
	 * @throws MojoFailureException
	 *             is thrown if an invalid license is found.
	 */
	private void validateArtifacts(DependencyTree dependencyTree)
			throws MojoExecutionException, MojoFailureException {
		File licenseResultsFile = IOUtilities.createNewResultsFile(log,
				outputDirectory);
		try (FileOutputStream outputStream = new FileOutputStream(
				licenseResultsFile)) {
			writer = new OutputStreamWriter(outputStream,
					Charset.defaultCharset());
			try {
				boolean valid = true;
				for (DependencyTree dependency : dependencyTree) {
					if (dependency.getArtifact() == getMavenProject()
							.getArtifact()) {
						// skip self, it is not needed to be evaluated
						continue;
					}
					if (!isArtifactValid(dependency)) {
						if (failFast) {
							throw new MojoFailureException(
									"Invalid license(s) was/were found!");
						}
						valid = false;
					}
				}
				if (!valid) {
					throw new MojoFailureException(
							"Invalid license(s) was/were found!");
				}
			} finally {
				try {
					writer.close();
				} finally {
					writer = null;
				}
			}
		} catch (IOException e) {
			throw new MojoExecutionException(
					"Could not write validation result to '"
							+ licenseResultsFile + "'.", e);
		}
	}

	/**
	 * This method checks the validity of a single artifact.
	 * 
	 * @param artifact
	 *            is the {@link Artifact} to be checked for validity.
	 * @return <code>true</code> is returned if the license is valid.
	 *         <code>false</code> is returned otherwise.
	 * @throws MojoFailureException
	 *             is thrown if {@link #failFast} is set to <code>true</code>
	 *             and the license is invalid to enforce the fail fast behavior
	 *             requested.
	 * @throws MojoExecutionException
	 *             is thrown in case of a faulty Maven run.
	 */
	private boolean isArtifactValid(DependencyTree dependency)
			throws MojoFailureException, MojoExecutionException {
		Artifact artifact = dependency.getArtifact();
		ArtifactInformation artifactInformation = new ArtifactInformation(
				artifact);
		log.debug("Check " + artifactInformation.getIdentifier() + ".");

		if (skipTestScope) {
			if (TEST_SCOPE_NAME.equals(artifact.getScope())) {
				ValidationResult result = new ValidationResult(
						artifactInformation, null, null, "test scope", true);
				logArtifactResult(result);
				return true;
			}
		}

		List<License> licenses = dependency.getLicenses();
		if (licenses.size() == 0) {
			if (isApprovedDependency(artifactInformation)) {
				KnownLicense knownLicense = findKnownLicense(artifactInformation);
				ValidationResult result = new ValidationResult(
						artifactInformation, knownLicense, null,
						"no license found, but dependency is approved", true);
				logArtifactResult(result);
				return true;
			} else {
				ValidationResult result = new ValidationResult(
						artifactInformation, null, null,
						"no license found and artifact is not approved", false);
				logArtifactResult(result);
				return false;
			}
		}
		boolean valid = true;
		for (License license : licenses) {
			if (!isValidLicense(license)) {
				ValidationResult result = new ValidationResult(
						artifactInformation, null, new ValidLicense(null,
								license.getName()), "license is not approved",
						false);
				logArtifactResult(result);
				valid = false;
			} else {
				KnownLicense knownLicense = findKnowLicense(license);
				ValidationResult result = new ValidationResult(
						artifactInformation, knownLicense, new ValidLicense(
								knownLicense.getKey(), license.getName()),
						"license is approved", true);
				logArtifactResult(result);
			}
		}
		return valid;
	}

	/**
	 * Finds a known license for the given {@link Artifact}.
	 * 
	 * @param artifactInformation
	 *            is the {@link Artifact} for which the known license is to be
	 *            looked up.
	 * @return A {@link KnownLicense} is returned.
	 * @throws MojoFailureException
	 */
	private KnownLicense findKnownLicense(
			ArtifactInformation artifactInformation)
			throws MojoFailureException {
		String licenseKey = findLicenseKey(artifactInformation);
		return getKnownLicense(licenseKey);
	}

	/**
	 * Finds the license key for the given {@link Artifact}.
	 * 
	 * @param artifact
	 *            is the {@link Artifact} for which the license key is to be
	 *            found.
	 * @return A {@link String} containing the key is returned.
	 * @throws MojoFailureException
	 */
	private String findLicenseKey(ArtifactInformation artifactInformation)
			throws MojoFailureException {
		ApprovedDependency approvedDependency = findApprovedDependency(artifactInformation);
		return approvedDependency == null ? null : approvedDependency.getKey();
	}

	/**
	 * This method returns the normalized name of the license.
	 * 
	 * @param license
	 *            is the {@link License} object which is to be looked up.
	 * @return A {@link KnownLicense} is returned containing the known license.
	 * @throws MojoFailureException
	 *             is thrown if the normalized name cannot be looked up due to
	 *             missing configuration.
	 */
	private KnownLicense findKnowLicense(License license)
			throws MojoFailureException {
		String licenseKey = findLicenseKey(license);
		return getKnownLicense(licenseKey);
	}

	/**
	 * This method checks whether or not an artifact is already approved. For
	 * that the {@link #approvedDependencies} list is checked for a pattern
	 * matching the artifact provided as parameter to this method.
	 * 
	 * @param artifact
	 *            is the {@link Artifact} to be checked for approval.
	 * @return <code>true</code> is returned if the artifact is already
	 *         approved. <code>false</code> is returned otherwise.
	 * @throws MojoFailureException
	 */
	private boolean isApprovedDependency(ArtifactInformation artifactInformation)
			throws MojoFailureException {
		return findApprovedDependency(artifactInformation) != null;
	}

	private ApprovedDependency findApprovedDependency(
			ArtifactInformation artifactInformation)
			throws MojoFailureException {
		String artifactIdentifier = artifactInformation.getIdentifier();
		for (ApprovedDependency approvedDependency : approvedDependencies) {
			String approvedDependencyIdentifier = approvedDependency
					.getIdentifier();
			if ((approvedDependencyIdentifier == null)
					|| (approvedDependencyIdentifier.isEmpty())) {
				throw new MojoFailureException(
						"An approved dependency was found without identifier.");
			}
			if (Pattern.matches(approvedDependencyIdentifier,
					artifactIdentifier)) {
				return approvedDependency;
			}
		}
		return null;
	}

	/**
	 * This method is used to log results with Maven log {@link Log}.
	 * 
	 * @param artifact
	 *            is the {@link Artifact} which was checked for validity.
	 * @param validationResult
	 *            is the {@link ValidationResult} of the check.
	 * @param licenseOrApprovalMessage
	 *            is the message to be printed containing the license or the
	 *            appoval message.
	 * @param knownLicense
	 * @throws MojoExecutionException
	 * @throws IOException
	 * @throws MojoFailureException
	 */
	private void logArtifactResult(ValidationResult validationResult)
			throws MojoExecutionException, MojoFailureException {
		ArtifactInformation artifactInformation = validationResult
				.getArtifactInformation();
		if (validationResult.isValid()) {
			log.info("License check for artifact '"
					+ artifactInformation.getIdentifier() + "': " + "valid"
					+ " (" + validationResult.getComment() + ")");
		} else {

			log.error("License check for artifact '"
					+ artifactInformation.getIdentifier() + "': " + "invalid"
					+ " (" + validationResult.getComment() + ")");
		}
		IOUtilities.writeResult(writer, validationResult);
	}

	/**
	 * This method checks the validity of a single license.
	 * 
	 * @param license
	 *            is the {@link License} to be checked.
	 * @return <code>true</code> is returned if the license is valid.
	 *         <code>false</code> is returned otherwise.
	 * @throws MojoFailureException
	 */
	private boolean isValidLicense(License license) throws MojoFailureException {
		return findValidLicense(license) != null;
	}

	private ValidLicense findValidLicense(License license)
			throws MojoFailureException {
		String licenseName = license.getName();
		if ((licenseName == null) || (licenseName.isEmpty())) {
			return null;
		}
		for (ValidLicense validLicense : validLicenses) {
			String validLicenseName = validLicense.getName();
			if ((validLicenseName == null) || (validLicenseName.isEmpty())) {
				throw new MojoFailureException(
						"A valid license without name was found.");
			}
			if (validLicenseName.equals(licenseName)) {
				return validLicense;
			}
		}
		return null;
	}

	/**
	 * This method find the license key for the found license object.
	 * 
	 * @param license
	 *            is the {@link License} object to be looked up.
	 * @return A {@link String} is returned containing the license key.
	 * @throws MojoFailureException
	 *             is thrown in case of missing configuration.
	 */
	private String findLicenseKey(License license) throws MojoFailureException {
		ValidLicense validLicense = findValidLicense(license);
		return validLicense == null ? null : validLicense.getKey();
	}

	/**
	 * This method looks for a known license for a given license key.
	 * 
	 * @param licenseKey
	 *            is the key of the license.
	 * @return A {@link KnownLicense} object is returned containing the license.
	 *         <code>null</code> is returned if no license with the given key
	 *         was found.
	 * @throws MojoFailureException
	 */
	private KnownLicense getKnownLicense(String licenseKey)
			throws MojoFailureException {
		for (KnownLicense knownLicense : knownLicenses) {
			if (knownLicense.getKey().equals(licenseKey)) {
				return knownLicense;
			}
		}
		throw new MojoFailureException("No known license for license key '"
				+ licenseKey + "' found.");
	}
}
