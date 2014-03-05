package com.puresoltechnologies.maven.plugins.license.internal;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

import com.puresoltechnologies.maven.plugins.license.parameter.ValidationResult;

/**
 * This class contains some utilities for the license maven plugin.
 * 
 * @author Rick-Rainer Ludwig
 */
public class IOUtilities {

	public static final String LICENSE_RESULTS_FILE = "licenses.csv";

	/**
	 * Creates a new and empty results file.
	 * 
	 * @param outputDirectory
	 * @return
	 * @throws MojoExecutionException
	 */
	public static File createNewResultsFile(Log log, File outputDirectory)
			throws MojoExecutionException {
		createDirectoryIfNotPresent(log, outputDirectory);
		File resultsFile = new File(outputDirectory,
				IOUtilities.LICENSE_RESULTS_FILE);
		deleteFileIfPresent(log, resultsFile);
		createFileIfNotPresent(log, resultsFile);
		return resultsFile;
	}

	public static File getResultsFile(Log log, File outputDirectory)
			throws MojoExecutionException {
		File resultsFile = new File(outputDirectory,
				IOUtilities.LICENSE_RESULTS_FILE);
		if (!resultsFile.exists()) {
			throw new MojoExecutionException("Results file '" + resultsFile
					+ "' is not present.");
		} else if (!resultsFile.isFile()) {
			throw new MojoExecutionException("Results file '" + resultsFile
					+ "' is not a file, but is supposed to be.");
		}
		return resultsFile;
	}

	/**
	 * This method checks a directory for presence. If it is not existing, it
	 * will be created.
	 * 
	 * @param directory
	 *            is the directory to be checked and created if needed.
	 * @throws MojoExecutionException
	 *             is thrown if the directory is not present and could not be
	 *             created.
	 */
	public static void createDirectoryIfNotPresent(Log log, File directory)
			throws MojoExecutionException {
		if (!directory.exists()) {
			log.debug("Directory '" + directory
					+ "' is not present. Creating it...");
			if (!directory.mkdirs()) {
				throw new MojoExecutionException("Could not create directory '"
						+ directory + "'.");
			}
		} else if (!directory.isDirectory()) {
			throw new MojoExecutionException("'" + directory
					+ "' is not a directory, but is supposed to be.");
		}
	}

	/**
	 * This method checks a file for presence. If it is not existing, it will be
	 * created.
	 * 
	 * @param file
	 *            is the directory to be checked and created if needed.
	 * @throws MojoExecutionException
	 *             is thrown if the directory is not present and could not be
	 *             created.
	 */
	public static void createFileIfNotPresent(Log log, File file)
			throws MojoExecutionException {
		try {
			if (!file.exists()) {
				log.debug("file '" + file + "' is not present. Creating it...");
				if (!file.createNewFile()) {
					throw new MojoExecutionException(
							"Could not create directory '" + file + "'.");
				}
			} else if (!file.isFile()) {
				throw new MojoExecutionException("'" + file
						+ "' is not a file, but is supposed to be.");
			}
		} catch (IOException e) {
			throw new MojoExecutionException("Could not create a new file '"
					+ file + "'.", e);
		}
	}

	/**
	 * This method checks the presence of a file. If it is present, it is
	 * deleted.
	 * 
	 * @param log
	 *            is the Maven logger {@link Log}.
	 * @param resultsFile
	 * @throws MojoExecutionException
	 */
	public static void deleteFileIfPresent(Log log, File resultsFile)
			throws MojoExecutionException {
		if (resultsFile.exists()) {
			log.debug("Results file exists. Delete it to remove obsolete results.");
			if (!resultsFile.delete()) {
				throw new MojoExecutionException(
						"Could not delete license results file '" + resultsFile
								+ "'.");
			}
		}
	}

	public static void writeResult(Writer writer, Artifact artifact,
			ValidationResult valid, String licenseName, String licenseURL,
			String comment) throws MojoFailureException, MojoExecutionException {
		try {
			if (writer == null) {
				return;
			}
			String groupId = artifact.getGroupId();
			if (groupId == null) {
				groupId = "";
			}
			String artifactId = artifact.getArtifactId();
			if (artifactId == null) {
				artifactId = "";
			}
			String version = artifact.getVersion();
			if (version == null) {
				version = "";
			}
			String classifier = artifact.getClassifier();
			if (classifier == null) {
				classifier = "";
			}
			String type = artifact.getType();
			if (type == null) {
				type = "";
			}
			String scope = artifact.getScope();
			if (scope == null) {
				scope = "";
			}
			writer.write(groupId + "," + artifactId + "," + version + ","
					+ classifier + "," + type + "," + scope + ",\""
					+ licenseName + "\"," + licenseURL + ",\"" + comment
					+ "\"," + String.valueOf(valid) + "\n");
		} catch (IOException e) {
			throw new MojoExecutionException(
					"Could not write result to results file.", e);
		}
	}

	public static String[] split(String line) {
		List<String> results = new ArrayList<>();
		StringBuffer buffer = new StringBuffer(line);
		while (buffer.length() > 0) {
			if (buffer.indexOf("\"") == 0) {
				int endIndex = buffer.indexOf("\"", 1);
				String result = buffer.substring(1, endIndex);
				results.add(result);
				buffer.delete(0, endIndex + 2);
			} else {
				int index = buffer.indexOf(",");
				if (index < 0) {
					results.add(buffer.toString());
					buffer.delete(0, buffer.length());
				} else {
					String result = buffer.substring(0, index);
					results.add(result);
					buffer.delete(0, index + 1);
				}
			}
		}
		return results.toArray(new String[results.size()]);
	}

	/**
	 * Private default constructor to avoid instantiation.
	 */
	private IOUtilities() {
	}
}
