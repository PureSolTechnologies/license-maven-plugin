package com.puresoltechnologies.maven.plugins.license;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Properties;

import org.apache.maven.doxia.module.xhtml.decoration.render.RenderingContext;
import org.apache.maven.doxia.siterenderer.sink.SiteRendererSink;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.reporting.MavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.doxia.sink.Sink;

import com.puresoltechnologies.maven.plugins.license.internal.DependencyTree;
import com.puresoltechnologies.maven.plugins.license.internal.IOUtilities;

@SuppressWarnings("deprecation")
@Mojo(//
name = "generate-report", //
requiresDirectInvocation = false, //
requiresProject = true, //
requiresReports = true, //
requiresOnline = false, //
inheritByDefault = true, //
threadSafe = true,//
requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,//
requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME//
)
@Execute(//
goal = "generate-report",//
phase = LifecyclePhase.GENERATE_SOURCES//
)
public class ReportMojo extends AbstractValidationMojo implements MavenReport {

	/**
	 * Specifies the destination directory where documentation is to be saved
	 * to.
	 */
	@Parameter(property = "destDir", alias = "destDir", defaultValue = "${project.build.directory}/licenses", required = true)
	protected File outputDirectory;

	@Parameter(alias = "resultsDirectory", required = false, defaultValue = "${project.build.directory}/licenses")
	private File resultsDirectory;

	private final Log log;
	private DependencyTree dependencyTree = null;
	private boolean recursive = true;
	private boolean skipTestScope = false;

	public ReportMojo() {
		log = getLog();
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			RenderingContext context = new RenderingContext(outputDirectory,
					getOutputName() + ".html");
			SiteRendererSink sink = new SiteRendererSink(context);
			Locale locale = Locale.getDefault();
			generate(sink, locale);
		} catch (MavenReportException e) {
			throw new MojoFailureException("An error has occurred in "
					+ getName(Locale.ENGLISH) + " report generation", e);
		}
	}

	@Override
	public boolean canGenerateReport() {
		return true;
	}

	@Override
	public void generate(Sink sink, Locale locale) throws MavenReportException {
		try {
			readSettings();
			dependencyTree = loadArtifacts(recursive, skipTestScope);
			generate(sink);
		} catch (MojoExecutionException e) {
			throw new MavenReportException("Could not generate report.", e);
		}
	}

	private void readSettings() throws MojoExecutionException {
		File file = new File(resultsDirectory, "settings.xml");
		try (FileInputStream fileOutputStream = new FileInputStream(file);
				InputStreamReader propertiesReader = new InputStreamReader(
						fileOutputStream, Charset.defaultCharset())) {
			Properties properties = new Properties();
			properties.load(propertiesReader);
			recursive = Boolean.valueOf(properties.getProperty("recursive",
					"true"));
			skipTestScope = Boolean.valueOf(properties.getProperty(
					"skipTestScope", "false"));
		} catch (IOException e) {
			throw new MojoExecutionException(
					"Could not write settings.properties.", e);
		}
	}

	private void generate(Sink sink) throws MavenReportException {
		log.info("Creating report for licenses.");
		log.info(getReportOutputDirectory().getPath());
		try {
			generateHead(sink);
			generateBody(sink);
			sink.flush();
		} finally {
			sink.close();
		}
	}

	private void generateHead(Sink sink) {
		sink.head();
		sink.title();
		sink.text("Licenses Report");
		sink.title_();
		sink.head_();
	}

	private void generateBody(Sink sink) throws MavenReportException {
		sink.body();
		sink.section1();
		sink.sectionTitle1();
		sink.text("Licenses Report");
		sink.sectionTitle1_();
		sink.section1_();
		generateDirectDependencyTable(sink);
		generateTransitiveDependencyTable(sink);
		sink.body_();
	}

	private void generateDirectDependencyTable(Sink sink)
			throws MavenReportException {
		sink.table();
		sink.tableCaption();
		sink.text("Licenses of dependencies");
		sink.tableCaption_();
		generateTableHead(sink);
		generateDirectDependenciesTableContent(sink);
		sink.table_();
	}

	private void generateTableHead(Sink sink) {
		sink.tableRow();
		sink.tableHeaderCell();
		sink.text("GroupId");
		sink.tableHeaderCell_();
		sink.tableHeaderCell();
		sink.text("ArtifactId");
		sink.tableHeaderCell_();
		sink.tableHeaderCell();
		sink.text("Version");
		sink.tableHeaderCell_();
		sink.tableHeaderCell();
		sink.text("Classifier");
		sink.tableHeaderCell_();
		sink.tableHeaderCell();
		sink.text("Type");
		sink.tableHeaderCell_();
		sink.tableHeaderCell();
		sink.text("Scope");
		sink.tableHeaderCell_();
		sink.tableHeaderCell();
		sink.text("License");
		sink.tableHeaderCell_();
		sink.tableHeaderCell();
		sink.text("Comment");
		sink.tableHeaderCell_();
		sink.tableHeaderCell();
		sink.text("Result");
		sink.tableHeaderCell_();
		sink.tableRow_();
	}

	private void generateDirectDependenciesTableContent(Sink sink)
			throws MavenReportException {
		try {
			dependencyTree.getAllDependencies(); // TODO
			File resultsFile = IOUtilities
					.getResultsFile(log, resultsDirectory);
			try (FileInputStream inputStream = new FileInputStream(resultsFile);
					InputStreamReader inputStreamReader = new InputStreamReader(
							inputStream, Charset.defaultCharset());
					BufferedReader bufferedReader = new BufferedReader(
							inputStreamReader)) {
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					String[] split = IOUtilities.split(line);
					sink.tableRow();
					sink.tableCell();
					sink.text(split[0]);
					sink.tableCell_();
					sink.tableCell();
					sink.text(split[1]);
					sink.tableCell_();
					sink.tableCell();
					sink.text(split[2]);
					sink.tableCell_();
					sink.tableCell();
					sink.text(split[3]);
					sink.tableCell_();
					sink.tableCell();
					sink.text(split[4]);
					sink.tableCell_();
					sink.tableCell();
					sink.text(split[5]);
					sink.tableCell_();
					sink.tableCell();
					sink.link(split[7]);
					sink.text(split[6]);
					sink.link_();
					sink.tableCell_();
					sink.tableCell();
					sink.text(split[8]);
					sink.tableCell_();
					sink.tableCell();
					sink.text(split[9]);
					sink.tableCell_();
					sink.tableRow_();
				}
			} catch (IOException e) {
			}
		} catch (MojoExecutionException e) {
			throw new MavenReportException("Could not generate report.", e);
		}
	}

	private void generateTransitiveDependencyTable(Sink sink)
			throws MavenReportException {
		sink.table();
		sink.tableCaption();
		sink.text("Transitive Licenses");
		sink.tableCaption_();
		generateTableHead(sink);
		generateTransitiveDependenciesTableContent(sink);
		sink.table_();
	}

	private void generateTransitiveDependenciesTableContent(Sink sink)
			throws MavenReportException {
		try {
			File resultsFile = IOUtilities
					.getResultsFile(log, resultsDirectory);
			try (FileInputStream inputStream = new FileInputStream(resultsFile);
					InputStreamReader inputStreamReader = new InputStreamReader(
							inputStream, Charset.defaultCharset());
					BufferedReader bufferedReader = new BufferedReader(
							inputStreamReader)) {
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					String[] split = IOUtilities.split(line);
					sink.tableRow();
					sink.tableCell();
					sink.text(split[0]);
					sink.tableCell_();
					sink.tableCell();
					sink.text(split[1]);
					sink.tableCell_();
					sink.tableCell();
					sink.text(split[2]);
					sink.tableCell_();
					sink.tableCell();
					sink.text(split[3]);
					sink.tableCell_();
					sink.tableCell();
					sink.text(split[4]);
					sink.tableCell_();
					sink.tableCell();
					sink.text(split[5]);
					sink.tableCell_();
					sink.tableCell();
					sink.link(split[7]);
					sink.text(split[6]);
					sink.link_();
					sink.tableCell_();
					sink.tableCell();
					sink.text(split[8]);
					sink.tableCell_();
					sink.tableCell();
					sink.text(split[9]);
					sink.tableCell_();
					sink.tableRow_();
				}
			} catch (IOException e) {
			}
		} catch (MojoExecutionException e) {
			throw new MavenReportException("Could not generate report.", e);
		}
	}

	@Override
	public String getCategoryName() {
		return CATEGORY_PROJECT_REPORTS;
	}

	@Override
	public String getDescription(Locale locale) {
		return "Reports all licenses for all dependencies for audit.";
	}

	@Override
	public String getName(Locale locale) {
		return "Licenses Report";
	}

	@Override
	public String getOutputName() {
		return "dependency-licenses-report";
	}

	@Override
	public File getReportOutputDirectory() {
		return outputDirectory;
	}

	@Override
	public boolean isExternalReport() {
		return false;
	}

	@Override
	public void setReportOutputDirectory(File outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

}
