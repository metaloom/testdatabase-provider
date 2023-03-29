package io.metaloom.test.container.provider.common.version;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class VersionTest {

	@Test
	public void testVersion() {
		String version = Version.getPlainVersion();
		System.out.println(version);
		assertNotNull(version);
		String buildTS = Version.getBuildInfo().getBuildtimestamp();
		assertNotNull(buildTS);
	}
}
