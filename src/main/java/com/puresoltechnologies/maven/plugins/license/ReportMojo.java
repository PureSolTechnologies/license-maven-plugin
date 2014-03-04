package com.puresoltechnologies.maven.plugins.license;

import java.io.File;
import java.util.Locale;

import org.apache.maven.doxia.module.xhtml.decoration.render.RenderingContext;
import org.apache.maven.doxia.siterenderer.sink.SiteRendererSink;
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
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.reporting.MavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.apache.maven.settings.Settings;
import org.codehaus.doxia.sink.Sink;

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
public class ReportMojo extends AbstractMojo implements MavenReport {

	/**
	 * Specifies the destination directory where documentation is to be saved
	 * to.
	 */
	@Parameter(property = "destDir", alias = "destDir", defaultValue = "${project.build.directory}/licenses", required = true)
	protected File outputDirectory;

	@Component
	private MavenProject project;

	@Component
	private PluginDescriptor plugin;

	@Component
	private Settings settings;

	@Component
	private MavenProjectBuilder projectBuilder;

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
		log.info("Creating report for licenses.");
		log.info(getReportOutputDirectory().getPath());
		try {
			sink.head();
			sink.title();
			sink.text("Licenses Report");
			sink.title_();
			sink.head_();
			sink.body();
			sink.section1();
			sink.sectionTitle1();
			sink.text("Licenses Report");
			sink.sectionTitle1_();
			sink.section1_();
			sink.table();
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
			sink.text("License");
			sink.tableHeaderCell_();
			sink.tableRow_();
			sink.tableRow();
			sink.tableCell();
			sink.text("group");
			sink.tableCell_();
			sink.tableCell();
			sink.text("artifact");
			sink.tableCell_();
			sink.tableCell();
			sink.text("version");
			sink.tableCell_();
			sink.tableCell();
			sink.text("name/url");
			sink.tableCell_();
			sink.tableRow_();
			sink.table_();
			sink.body_();
			sink.flush();
		} finally {
			sink.close();
		}
	}

	@Override
	public String getCategoryName() {
		return CATEGORY_PROJECT_REPORTS;
	}

	@Override
	public String getDescription(Locale arg0) {
		return "Reports all licenses for all dependencies for audit.";
	}

	@Override
	public String getName(Locale arg0) {
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
