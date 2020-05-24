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
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
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
import com.puresoltechnologies.maven.plugins.license.parameter.ArtifactInformation;
import com.puresoltechnologies.maven.plugins.license.parameter.KnownLicense;
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
        name = "verify", //
        requiresDirectInvocation = false, //
        requiresProject = true, //
        requiresReports = false, //
        requiresOnline = false, //
        inheritByDefault = true, //
        threadSafe = true, //
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, //
        requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME//
)
@Execute(//
        goal = "verify", //
        phase = LifecyclePhase.VERIFY //
)
public class ValidatorMojo extends AbstractValidationMojo {

    private static final String TEST_SCOPE_NAME = "test";

    @Parameter(alias = "knownLicenses", required = true)
    private Set<KnownLicense> knownLicenses;

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
     * Specifies whether or not the dependencies in test scope should be skipped.
     * Default is false.
     */
    @Parameter(alias = "skipTestScope", required = false, defaultValue = "true")
    private boolean skipTestScope;

    /**
     * Specifies whether or not the dependencies in provided scope should be
     * skipped. Default is false.
     */
    @Parameter(alias = "skipProvidedScope", required = false, defaultValue = "true")
    private boolean skipProvidedScope;

    /**
     * Specified whether or not to skip archetypes with optional flag.
     */
    @Parameter(alias = "skipOptionals", required = false, defaultValue = "true")
    private boolean skipOptionals;

    /**
     * Specified whether or not to skip the whole plugin execution.
     */
    @Parameter(alias = "skip", required = false, defaultValue = "false")
    private boolean skip;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        storeSettings();
        if (!skip) {
            DependencyTree dependencyTree = loadArtifacts(recursive, skipTestScope, skipProvidedScope, skipOptionals);
            validateArtifacts(dependencyTree);
        }
    }

    private void storeSettings() throws MojoExecutionException {
        File file = IOUtilities.createNewSettingsFile(getLog(), outputDirectory);
        try (FileOutputStream fileOutputStream = new FileOutputStream(file);
                OutputStreamWriter propertiesWriter = new OutputStreamWriter(fileOutputStream,
                        Charset.defaultCharset())) {
            Properties properties = new Properties();
            properties.setProperty("recursive", Boolean.toString(recursive));
            properties.setProperty("skipTestScope", Boolean.toString(skipTestScope));
            properties.setProperty("skipProvidedScope", Boolean.toString(skipProvidedScope));
            properties.setProperty("skipOptionals", Boolean.toString(skipOptionals));
            properties.setProperty("skip", Boolean.toString(skip));
            properties.store(propertiesWriter, "license-maven-plugin settings.");
        } catch (IOException e) {
            throw new MojoExecutionException("Could not write settings.properties.", e);
        }
    }

    /**
     * This method checks a set of artifacts for validity.
     *
     * @param dependencies is a {@link Set} of {@link Artifact} which is to be
     *                     checked for validity.
     * @throws MojoExecutionException is throw if the execution was faulty.
     * @throws MojoFailureException   is thrown if an invalid license is found.
     */
    private void validateArtifacts(DependencyTree dependencyTree) throws MojoExecutionException, MojoFailureException {
        File licenseResultsFile = IOUtilities.createNewResultsFile(getLog(), outputDirectory);
        try (FileOutputStream outputStream = new FileOutputStream(licenseResultsFile);
                OutputStreamWriter writer = new OutputStreamWriter(outputStream, Charset.defaultCharset())) {
            boolean valid = true;
            List<String> checkedArtifact = new ArrayList<>();
            for (DependencyTree dependency : dependencyTree) {
                Artifact artifact = dependency.getArtifact();
                if (artifact == getMavenProject().getArtifact()) {
                    // skip self, it is not needed to be evaluated
                    continue;
                }
                String artifactIdentifier = ArtifactUtilities.toString(artifact);
                if (checkedArtifact.contains(artifactIdentifier)) {
                    continue;
                }
                checkedArtifact.add(artifactIdentifier);
                if (!isArtifactValid(dependency, writer)) {
                    if (failFast) {
                        throw new MojoFailureException("Invalid license(s) was/were found!");
                    }
                    valid = false;
                }
            }
            if (!valid) {
                throw new MojoFailureException("Invalid license(s) was/were found!");
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Could not write validation result to '" + licenseResultsFile + "'.", e);
        }
    }

    /**
     * This method checks the validity of a single artifact.
     *
     * @param artifact is the {@link Artifact} to be checked for validity.
     * @return <code>true</code> is returned if the license is valid.
     *         <code>false</code> is returned otherwise.
     * @throws MojoFailureException   is thrown if {@link #failFast} is set to
     *                                <code>true</code> and the license is invalid
     *                                to enforce the fail fast behavior requested.
     * @throws MojoExecutionException is thrown in case of a faulty Maven run.
     */
    private boolean isArtifactValid(DependencyTree dependency, OutputStreamWriter writer)
            throws MojoFailureException, MojoExecutionException {
        Artifact artifact = dependency.getArtifact();
        ArtifactInformation artifactInformation = new ArtifactInformation(artifact);

        if (skipTestScope) {
            String scope = artifact.getScope();
            if ((scope != null) && (TEST_SCOPE_NAME.equals(scope.toLowerCase()))) {
                ValidationResult result = new ValidationResult(artifactInformation, null, null, null, "test scope",
                        true);
                logArtifactResult(result, writer);
                return true;
            }
        }

        List<License> licenses = dependency.getLicenses();
        if (licenses.size() == 0) {
            KnownLicense knownLicense = findKnownLicense(artifactInformation);
            if (knownLicense != null) {
                ValidationResult result = new ValidationResult(artifactInformation, knownLicense, null, null,
                        "no license found, but dependency is approved", true);
                logArtifactResult(result, writer);
                return true;
            } else {
                ValidationResult result = new ValidationResult(artifactInformation, null, null, null,
                        "no license found and artifact is not approved", false);
                logArtifactResult(result, writer);
                return false;
            }
        }
        boolean valid = true;
        for (License license : licenses) {
            String licenseName = license.getName();
            URL licenseURL;
            try {
                licenseURL = new URL(license.getUrl());
            } catch (MalformedURLException e) {
                licenseURL = null;
            }
            KnownLicense knownLicense = findKnownLicense(artifactInformation);
            if (knownLicense != null) {
                ValidationResult result = new ValidationResult(artifactInformation, knownLicense, licenseName,
                        licenseURL, "license is approved by artifact", true);
                logArtifactResult(result, writer);
            } else {
                knownLicense = findKnownLicense(artifactInformation, license);
                if (knownLicense != null) {
                    ValidationResult result = new ValidationResult(artifactInformation, knownLicense, licenseName,
                            licenseURL, "license is approved", true);
                    logArtifactResult(result, writer);
                } else {
                    ValidationResult result = new ValidationResult(artifactInformation, null, licenseName, licenseURL,
                            "license is not approved", false);
                    logArtifactResult(result, writer);
                    valid = false;
                }
            }
        }
        return valid;
    }

    /**
     * This method returns the normalized name of the license.
     *
     * @param artifactInformation
     *
     * @param license             is the {@link License} object which is to be
     *                            looked up.
     * @return A {@link KnownLicense} is returned containing the known license.
     * @throws MojoFailureException is thrown if the normalized name cannot be
     *                              looked up due to missing configuration.
     */
    private KnownLicense findKnownLicense(ArtifactInformation artifactInformation, License license)
            throws MojoFailureException {
        for (KnownLicense knownLicense : knownLicenses) {
            if (knownLicense.getName().equals(license.getName())) {
                return knownLicense;
            }
            for (String alias : knownLicense.getAliases()) {
                if ((alias == null) || (alias.isEmpty())) {
                    throw new MojoFailureException("An alias was found without identifier.");
                }
                if (alias.equals(license.getName())) {
                    return knownLicense;
                }
            }
        }
        return null;
    }

    private KnownLicense findKnownLicense(ArtifactInformation artifactInformation) throws MojoFailureException {
        for (KnownLicense knownLicense : knownLicenses) {
            for (String approvedDependency : knownLicense.getApprovedDependencies()) {
                if ((approvedDependency == null) || (approvedDependency.isEmpty())) {
                    throw new MojoFailureException("An approved dependency was found without identifier.");
                }
                if (Pattern.matches(approvedDependency, artifactInformation.getIdentifier())) {
                    return knownLicense;
                }
            }
        }
        return null;
    }

    /**
     * This method is used to log results with Maven log {@link Log}.
     *
     * @param artifact                 is the {@link Artifact} which was checked for
     *                                 validity.
     * @param validationResult         is the {@link ValidationResult} of the check.
     * @param licenseOrApprovalMessage is the message to be printed containing the
     *                                 license or the appoval message.
     * @param knownLicense
     * @throws MojoExecutionException
     * @throws IOException
     * @throws MojoFailureException
     */
    private void logArtifactResult(ValidationResult validationResult, OutputStreamWriter writer)
            throws MojoExecutionException, MojoFailureException {
        StringBuffer buffer = new StringBuffer();
        buffer.append("License ");
        String originalLicenseName = validationResult.getOriginalLicenseName();
        if (originalLicenseName != null) {
            buffer.append("'");
            buffer.append(originalLicenseName);
            buffer.append("' ");
            URL originalLicenseURL = validationResult.getOriginalLicenseURL();
            if (originalLicenseURL != null) {
                buffer.append("(");
                buffer.append(originalLicenseURL.toString());
                buffer.append(") ");
            }
        }
        buffer.append("checked for artifact '");
        ArtifactInformation artifactInformation = validationResult.getArtifactInformation();
        buffer.append(artifactInformation.getIdentifier());
        buffer.append("': \n     >> ");
        if (validationResult.isValid()) {
            buffer.append("valid as '");
            KnownLicense license = validationResult.getLicense();
            buffer.append(license.getName());
            buffer.append("' (");
            buffer.append(validationResult.getComment());
            buffer.append(")");
            getLog().info(buffer.toString());
        } else {
            buffer.append("invalid (");
            buffer.append(validationResult.getComment());
            buffer.append(")");
            getLog().error(buffer.toString());
        }
        IOUtilities.writeResult(writer, validationResult);
    }
}
