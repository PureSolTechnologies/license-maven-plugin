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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import com.puresoltechnologies.maven.plugins.license.parameter.ArtifactInformation;
import com.puresoltechnologies.maven.plugins.license.parameter.KnownLicense;
import com.puresoltechnologies.maven.plugins.license.parameter.ValidationResult;

/**
 * This class contains some utilities for the license maven plugin.
 *
 * @author Rick-Rainer Ludwig
 */
public class IOUtilities {

    /**
     * The constant for the license validation result file.
     */
    public static final String LICENSE_RESULTS_FILE = "licenses.csv";

    /**
     * This is the constant for the settings properties file.
     */
    public static final String LICENSE_SETTINGS_FILE = "settings.properties";

    /**
     * Creates a new and empty results file.
     *
     * @param log             is the {@link Log} to write to.
     * @param outputDirectory is the directory for the output file.
     * @return A {@link File} object is returned pointing to the newly created
     *         results file.
     * @throws MojoExecutionException is thrown in case the file could not be
     *                                created.
     */
    public static File createNewResultsFile(Log log, File outputDirectory) throws MojoExecutionException {
        createDirectoryIfNotPresent(log, outputDirectory);
        File resultsFile = new File(outputDirectory, IOUtilities.LICENSE_RESULTS_FILE);
        deleteFileIfPresent(log, resultsFile);
        createFileIfNotPresent(log, resultsFile);
        return resultsFile;
    }

    /**
     * Returns the position of the results file.
     *
     * @param log             is the {@link Log} to write to.
     * @param outputDirectory is the directory for the output file.
     * @return A {@link File} object is returned pointing to the results file.
     * @throws MojoExecutionException is thrown if no file is found.
     */
    public static File getResultsFile(Log log, File outputDirectory) throws MojoExecutionException {
        File resultsFile = new File(outputDirectory, IOUtilities.LICENSE_RESULTS_FILE);
        if (!resultsFile.exists()) {
            throw new MojoExecutionException("Results file '" + resultsFile + "' is not present.");
        } else if (!resultsFile.isFile()) {
            throw new MojoExecutionException(
                    "Results file '" + resultsFile + "' is not a file, but is supposed to be.");
        }
        return resultsFile;
    }

    /**
     * Creates a new and empty settings file.
     *
     * @param log             is the {@link Log} to write to.
     * @param outputDirectory is the directory for the output file.
     * @return A {@link File} object is returned pointing to the newly created
     *         settings file.
     * @throws MojoExecutionException is thrown in case the file could not be
     *                                created.
     */
    public static File createNewSettingsFile(Log log, File outputDirectory) throws MojoExecutionException {
        createDirectoryIfNotPresent(log, outputDirectory);
        File settingsFile = new File(outputDirectory, LICENSE_SETTINGS_FILE);
        deleteFileIfPresent(log, settingsFile);
        createFileIfNotPresent(log, settingsFile);
        return settingsFile;
    }

    /**
     * This method checks and returns the position of the settings file.
     *
     * @param log             is the {@link Log} to write to.
     * @param outputDirectory is the directory for the output file.
     * @return A {@link File} object is returned pointing to the settings file.
     * @throws MojoExecutionException is thrown in case the file could not be found.
     */
    public static File getSettingsFile(Log log, File outputDirectory) throws MojoExecutionException {
        File resultsFile = new File(outputDirectory, IOUtilities.LICENSE_SETTINGS_FILE);
        if (!resultsFile.exists()) {
            throw new MojoExecutionException("Settings file '" + resultsFile + "' is not present.");
        } else if (!resultsFile.isFile()) {
            throw new MojoExecutionException(
                    "Settings file '" + resultsFile + "' is not a file, but is supposed to be.");
        }
        return resultsFile;
    }

    /**
     * This method checks a directory for presence. If it is not existing, it will
     * be created.
     *
     * @param log       is the {@link Log} to write to.
     * @param directory is the directory to be checked and created if needed.
     * @throws MojoExecutionException is thrown if the directory is not present and
     *                                could not be created.
     */
    public static void createDirectoryIfNotPresent(Log log, File directory) throws MojoExecutionException {
        if (!directory.exists()) {
            log.debug("Directory '" + directory + "' is not present. Creating it...");
            if (!directory.mkdirs()) {
                throw new MojoExecutionException("Could not create directory '" + directory + "'.");
            }
        } else if (!directory.isDirectory()) {
            throw new MojoExecutionException("'" + directory + "' is not a directory, but is supposed to be.");
        }
    }

    /**
     * This method checks a file for presence. If it is not existing, it will be
     * created.
     *
     * @param log  is the {@link Log} to write to.
     * @param file is the directory to be checked and created if needed.
     * @throws MojoExecutionException is thrown if the directory is not present and
     *                                could not be created.
     */
    public static void createFileIfNotPresent(Log log, File file) throws MojoExecutionException {
        try {
            if (!file.exists()) {
                log.debug("file '" + file + "' is not present. Creating it...");
                if (!file.createNewFile()) {
                    throw new MojoExecutionException("Could not create directory '" + file + "'.");
                }
            } else if (!file.isFile()) {
                throw new MojoExecutionException("'" + file + "' is not a file, but is supposed to be.");
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Could not create a new file '" + file + "'.", e);
        }
    }

    /**
     * This method checks the presence of a file. If it is present, it is deleted.
     *
     * @param log  is the Maven logger {@link Log}.
     * @param file is the files to be deleted if present.
     * @throws MojoExecutionException is thrown in cases of IO issues.
     */
    public static void deleteFileIfPresent(Log log, File file) throws MojoExecutionException {
        if (file.exists()) {
            log.debug("Results file exists. Delete it to remove obsolete results.");
            if (!file.delete()) {
                throw new MojoExecutionException("Could not delete license results file '" + file + "'.");
            }
        }
    }

    /**
     * Writes a CSV file line with the given {@link ValidationResult}.
     *
     * @param writer           is the {@link Writer} to write to.
     * @param validationResult is the {@link ValidationResult} object which is to be
     *                         written to the writer.
     * @throws MojoExecutionException is thrown in cases of IO issues.
     */
    public static void writeResult(Writer writer, ValidationResult validationResult) throws MojoExecutionException {
        try {
            if (writer == null) {
                return;
            }
            ArtifactInformation artifactInformation = validationResult.getArtifactInformation();
            String groupId = artifactInformation.getGroupId();
            String artifactId = artifactInformation.getArtifactId();
            String version = artifactInformation.getVersion();
            String classifier = artifactInformation.getClassifier();
            String type = artifactInformation.getType();
            String scope = artifactInformation.getScope();
            KnownLicense license = validationResult.getLicense();
            String licenseName = "";
            String licenseURL = "";
            if (license != null) {
                licenseName = license.getName();
                licenseURL = license.getUrl().toString();
            }
            String originalLicenseName = validationResult.getOriginalLicenseName();
            if (originalLicenseName == null) {
                originalLicenseName = "";
            }
            URL originalLicenseURL = validationResult.getOriginalLicenseURL();
            if (originalLicenseURL == null) {
                originalLicenseURL = new URL("http://opensource.org/");
            }
            String comment = validationResult.getComment();
            boolean valid = validationResult.isValid();
            writer.write(groupId + "," + artifactId + "," + version + "," + classifier + "," + type + "," + scope
                    + ",\"" + licenseName + "\"," + licenseURL + ",\"" + originalLicenseName + "\","
                    + originalLicenseURL.toString() + ",\"" + comment + "\"," + String.valueOf(valid) + "\n");
        } catch (IOException e) {
            throw new MojoExecutionException("Could not write result to results file.", e);
        }
    }

    /**
     * Reads a single CSV line from a {@link BufferedReader} and converts it into a
     * {@link ValidationResult}.
     *
     * @param bufferedReader is the {@link BufferedReader} to read from.
     * @return A {@link ValidationResult} is returned containing the read
     *         information.
     * @throws MojoExecutionException is thrown in case of IO issues.
     */
    public static ValidationResult readResult(Log log, BufferedReader bufferedReader) throws MojoExecutionException {
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
            URL licenseURL = null;
            try {
                licenseURL = new URL(splits[7]);
            } catch (MalformedURLException e) {
                log.warn("Malformed license URL '" + splits[7] + "' was found.");
            }
            String originalLicenseName = splits[8];
            URL originalLicenseURL = null;
            try {
                originalLicenseURL = new URL(splits[9]);
            } catch (MalformedURLException e) {
                log.warn("Malformed original license URL '" + splits[9] + "' was found.");
            }
            String comment = splits[10];
            boolean valid = Boolean.valueOf(splits[11]);
            ArtifactInformation artifactInformation = new ArtifactInformation(groupId, artifactId, version, classifier,
                    type, scope);
            KnownLicense license = new KnownLicense(licenseName, licenseURL, valid, new HashSet<String>(),
                    new HashSet<String>());
            ValidationResult validationResult = new ValidationResult(artifactInformation, license, originalLicenseName,
                    originalLicenseURL, comment, valid);
            return validationResult;
        } catch (IOException e) {
            throw new MojoExecutionException("Could not write result to results file.", e);
        }
    }

    /**
     * This method splits a single CSV line into junks of {@link String}.
     *
     * @param line is the line to be split.
     * @return An array of {@link String} is returned.
     */
    protected static String[] split(String line) {
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
     * @param file   is the file where the object is to be stored to.
     * @param object is the objec which is to be stored.
     * @throws IOException is thrown in cases of IO issues.
     */
    public static void storeObject(File file, Serializable object) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(file);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {
            objectOutputStream.writeObject(object);
        }
    }

    /**
     * This method returns a former serialized object from a file.
     *
     * @param <T>  is the actual type of the object read from the file.
     * @param file is the file to read the object from.
     * @return An object T is returned which was read form the file.
     * @throws IOException is thrown in cases of IO issues.
     */
    public static <T> T restoreObject(File file) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(file);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {
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
