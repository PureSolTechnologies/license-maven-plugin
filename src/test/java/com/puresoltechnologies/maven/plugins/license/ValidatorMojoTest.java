package com.puresoltechnologies.maven.plugins.license;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.maven.model.Model;
import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.project.MavenProject;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

@Ignore
public class ValidatorMojoTest {

    @Rule
    public final MojoRule rule = new MojoRule() {

        @Override
        protected void before() throws Throwable {
        };

        @Override
        protected void after() {
        };
    };

    @Test
    public void test() throws Exception {
        File pom = new File("src/test/resources/config.xml");
        Model model = new Model();
        model.setGroupId("com.puresoltechnologies.javafx");
        model.setArtifactId("rcp");
        model.setVersion("0.9.0");
        MavenProject project = new MavenProject(model);

        assertNotNull(pom);
        assertTrue(pom.exists());

        ValidatorMojo myMojo = (ValidatorMojo) rule.lookupConfiguredMojo(project, "install");
        assertNotNull(myMojo);
        myMojo.execute();
    }

}
