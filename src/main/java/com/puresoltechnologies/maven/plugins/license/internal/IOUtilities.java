package com.puresoltechnologies.maven.plugins.license.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import com.puresoltechnologies.maven.plugins.license.parameter.ArtifactInformation;
import com.puresoltechnologies.maven.plugins.license.parameter.KnownLicense;
import com.puresoltechnologies.maven.plugins.license.parameter.ValidLicense;
import com.puresoltechnologies.maven.plugins.license.parameter.ValidationResult;

/**
 * This class contains some utilities for the license maven plugin.
 * 
 * @author Rick-Rainer Ludwig
 */
public class IOUtilities {

	public static final String LICENSE_RESULTS_FILE = "licenses.csv";
	public static final String LICENSE_SETTINGS_FILE = "settings.properties";

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
	 * Creates a new and empty settings file.
	 * 
	 * @param outputDirectory
	 * @return
	 * @throws MojoExecutionException
	 */
	public static File createNewSettingsFile(Log log, File outputDirectory)
			throws MojoExecutionException {
		createDirectoryIfNotPresent(log, outputDirectory);
		File settingsFile = new File(outputDirectory, LICENSE_SETTINGS_FILE);
		deleteFileIfPresent(log, settingsFile);
		createFileIfNotPresent(log, settingsFile);
		return settingsFile;
	}

	public static File getSettingsFile(Log log, File outputDirectory)
			throws MojoExecutionException {
		File resultsFile = new File(outputDirectory,
				IOUtilities.LICENSE_SETTINGS_FILE);
		if (!resultsFile.exists()) {
			throw new MojoExecutionException("Settings file '" + resultsFile
					+ "' is not present.");
		} else if (!resultsFile.isFile()) {
			throw new MojoExecutionException("Settings file '" + resultsFile
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

	public static void writeResult(Writer writer,
			ValidationResult validationResult) throws MojoExecutionException {
		try {
			if (writer == null) {
				return;
			}
			ArtifactInformation artifactInformation = validationResult
					.getArtifactInformation();
			String groupId = artifactInformation.getGroupId();
			String artifactId = artifactInformation.getArtifactId();
			String version = artifactInformation.getVersion();
			String classifier = artifactInformation.getClassifier();
			String type = artifactInformation.getType();
			String scope = artifactInformation.getScope();
			KnownLicense license = validationResult.getLicense();
			String licenseName = null;
			String licenseURL = null;
			String licenseKey = null;
			if (license != null) {
				licenseName = license.getName();
				licenseURL = license.getUrl().toString();
				licenseKey = license.getKey();
			}
			ValidLicense originalLicense = validationResult
					.getOriginalLicense();
			String originalLicenseName = originalLicense.getName();
			String comment = validationResult.getComment();
			boolean valid = validationResult.isValid();
			writer.write(groupId + "," + artifactId + "," + version + ","
					+ classifier + "," + type + "," + scope + ",\""
					+ licenseName + "\"," + licenseURL + "," + licenseKey + ","
					+ originalLicenseName + ",\"" + comment + "\","
					+ String.valueOf(valid) + "\n");
		} catch (IOException e) {
			throw new MojoExecutionException(
					"Could not write result to results file.", e);
		}
	}

	public static ValidationResult readResult(BufferedReader bufferedReader)
			throws MojoExecutionException {
		try {
			if (bufferedReader == null) {
				throw new IllegalArgumentException("Reader must not be null!");
			}
			String line = bufferedReader.readLine();
			if (line == null) {
				return null;
			}
			String[] splits = IOUtilities.split(line);
			String groupId = splits[0];
			String artifactId = splits[1];
			String version = splits[2];
			String classifier = splits[3];
			String type = splits[4];
			String scope = splits[5];
			String licenseName = splits[6];
			URL licenseURL = new URL(splits[7]);
			String licenseKey = splits[8];
			String originalLicenseName = splits[9];
			String comment = splits[10];
			boolean valid = Boolean.valueOf(splits[11]);
			ArtifactInformation artifactInformation = new ArtifactInformation(
					groupId, artifactId, version, classifier, type, scope);
			KnownLicense license = new KnownLicense(licenseKey, licenseName,
					licenseURL);
			ValidLicense originalLicense = new ValidLicense(licenseKey,
					originalLicenseName);
			ValidationResult validationResult = new ValidationResult(
					artifactInformation, license, originalLicense, comment,
					valid);
			return validationResult;
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
	 * Stores any {@link Serializable} object into a file.
	 * 
	 * @param file
	 * @param object
	 * @throws IOException
	 */
	public static void storeObject(File file, Serializable object)
			throws IOException {
		try (FileOutputStream fileOutputStream = new FileOutputStream(file);
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(
						fileOutputStream)) {
			objectOutputStream.writeObject(object);
		}
	}

	/**
	 * This method returns a former serialized object from a file.
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static <T> T restoreObject(File file) throws IOException {
		try (FileInputStream fileInputStream = new FileInputStream(file);
				ObjectInputStream objectInputStream = new ObjectInputStream(
						fileInputStream)) {
			@SuppressWarnings("unchecked")
			T t = (T) objectInputStream.readObject();
			return t;
		} catch (ClassNotFoundException e) {
			throw new IOException("Could not restore object.", e);
		}

	}

	/**
	 * Private default constructor to avoid instantiation.
	 */
	private IOUtilities() {
	}
}
