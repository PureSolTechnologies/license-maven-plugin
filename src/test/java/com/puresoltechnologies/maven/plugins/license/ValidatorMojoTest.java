package com.puresoltechnologies.maven.plugins.license;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

@Ignore("Testing seems not to work in the current configuration. This needs to be fixed later.")
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

		assertNotNull(pom);
		assertTrue(pom.exists());

		ValidatorMojo myMojo = (ValidatorMojo) rule.lookupMojo("validate", pom);
		assertNotNull(myMojo);
		myMojo.execute();
	}

}
