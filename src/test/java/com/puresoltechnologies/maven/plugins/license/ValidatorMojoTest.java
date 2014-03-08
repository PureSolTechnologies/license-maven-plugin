package com.puresoltechnologies.maven.plugins.license;

import java.io.File;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.Ignore;

@Ignore("The Maven testing frameworks seem not to work properly...")
public class ValidatorMojoTest extends AbstractMojoTestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void test() throws Exception {
		File testPom = new File(getBasedir(), "src/test/resources/config.xml");

		ValidatorMojo mojo = (ValidatorMojo) lookupMojo("validate", testPom);

		assertNotNull(mojo);
	}

}
