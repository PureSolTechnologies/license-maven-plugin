package com.puresoltechnologies.maven.plugins.license;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Locale;

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
			File resultsFile = IOUtilities
					.getResultsFile(log, resultsDirectory);
			generate(sink, resultsFile);
		} catch (MojoExecutionException e) {
			throw new MavenReportException("Could not generate report.", e);
		}
	}

	private void generate(Sink sink, File resultsFile) {
		try (FileInputStream inputStream = new FileInputStream(resultsFile);
				InputStreamReader inputStreamReader = new InputStreamReader(
						inputStream, Charset.defaultCharset());
				BufferedReader bufferedReader = new BufferedReader(
						inputStreamReader)) {
			log.info("Creating report for licenses.");
			log.info(getReportOutputDirectory().getPath());
			try {
				generateHead(sink);
				generateBody(sink, bufferedReader);
				sink.flush();
			} finally {
				sink.close();
			}
		} catch (IOException e) {
		}
	}

	private void generateHead(Sink sink) {
		sink.head();
		sink.title();
		sink.text("Licenses Report");
		sink.title_();
		sink.head_();
	}

	private void generateBody(Sink sink, BufferedReader bufferedReader)
			throws IOException {
		sink.body();
		sink.section1();
		sink.sectionTitle1();
		sink.text("Licenses Report");
		sink.sectionTitle1_();
		sink.section1_();
		generateTable(sink, bufferedReader);
		sink.body_();
	}

	private void generateTable(Sink sink, BufferedReader bufferedReader)
			throws IOException {
		sink.table();
		sink.tableCaption();
		sink.text("Licenses of dependencies");
		sink.tableCaption_();
		generateTableHead(sink);
		generateTableContent(sink, bufferedReader);
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

	private void generateTableContent(Sink sink, BufferedReader bufferedReader)
			throws IOException {
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
