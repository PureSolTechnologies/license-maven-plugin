package com.puresoltechnologies.maven.plugins.license.stubs;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Build;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;

/**
 * This is a project stub for testing License Maven Plugin.
 *
 * @author Rick-Rainer Ludwig
 */
public class TestProjectStub extends MavenProjectStub {

    public TestProjectStub() {

        /// com.puresoltechnologies.maven.plugins.license-maven-plugin/src/test/resources/test-poms/javafx-rcp-pom.xml

        setGroupId("com.puresoltechnologies.maven.plugins.license");
        setArtifactId("stubs.test");
        setVersion("1.2.3");
        setPackaging("jar");

        Build build = new Build();
        build.setFinalName(getArtifactId());
        build.setDirectory(getBasedir() + "/target");
        build.setSourceDirectory(getBasedir() + "/src/main/java");
        build.setOutputDirectory(getBasedir() + "/target/classes");
        build.setTestSourceDirectory(getBasedir() + "/src/test/java");
        build.setTestOutputDirectory(getBasedir() + "/target/test-classes");
        setBuild(build);

        List<String> compileSourceRoots = new ArrayList<>();
        compileSourceRoots.add(getBasedir() + "/src/main/java");
        setCompileSourceRoots(compileSourceRoots);

        List<String> testCompileSourceRoots = new ArrayList<>();
        testCompileSourceRoots.add(getBasedir() + "/src/test/java");
        setTestCompileSourceRoots(testCompileSourceRoots);
    }

}
