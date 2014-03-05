package com.puresoltechnologies.maven.plugins.license.internal;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class IOUtilitiesTest {

	@Test
	public void testSplit() {
		String[] split = IOUtilities.split("1,2,3");
		assertEquals(3, split.length);
		assertEquals("1", split[0]);
		assertEquals("2", split[1]);
		assertEquals("3", split[2]);
	}

	@Test
	public void testSplit2() {
		String[] split = IOUtilities.split("1,\"2\",3");
		assertEquals(3, split.length);
		assertEquals("1", split[0]);
		assertEquals("2", split[1]);
		assertEquals("3", split[2]);
	}

}
