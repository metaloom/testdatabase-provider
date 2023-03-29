package io.metaloom.test.container.provider.common.version;

import java.util.Properties;

public final class Version {

	private static BuildInfo buildInfo = null;

	private Version() {
	}

	/**
	 * Return the mesh build information.
	 * 
	 * @return Provider version and build timestamp.
	 */
	public static BuildInfo getBuildInfo() {
		try {
			if (buildInfo == null) {
				Properties buildProperties = new Properties();
				buildProperties.load(Version.class.getResourceAsStream("/provider.build.properties"));
				// Cache the build information
				buildInfo = new BuildInfo(buildProperties);
			}
			return buildInfo;
		} catch (Exception e) {
			return new BuildInfo("unknown", "unknown");
		}
	}

	/**
	 * Return the mesh version (without build timestamp)
	 *
	 * @return Provider version
	 */
	public static String getPlainVersion() {
		return getBuildInfo().getVersion();
	}

}
