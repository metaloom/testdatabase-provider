package io.metaloom.maven.provider.container;

import java.util.HashMap;
import java.util.Map;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class PostgreSQLPoolContainer extends PostgreSQLContainer<PostgreSQLPoolContainer> {

	public static final String DEFAULT_IMAGE = "postgres:13.2";

	public static final String DEFAULT_USERNAME = "sa";

	public static final String DEFAULT_PASSWORD = "sa";

	public static final String DEFAULT_DATABASE_NAME = "postgres";

	public PostgreSQLPoolContainer() {
		this(DEFAULT_IMAGE);
	}

	public PostgreSQLPoolContainer(String imageName) {
		super(DockerImageName.parse(imageName).asCompatibleSubstituteFor(DEFAULT_IMAGE));
		withDatabaseName(DEFAULT_DATABASE_NAME);
		withUsername(DEFAULT_USERNAME);
		withPassword(DEFAULT_PASSWORD);
	}

	/**
	 * Enable the usage of a tmpFs to store the actual database data.
	 * 
	 * @param liveTmpFsSizeInMB
	 * @return
	 */
	public PostgreSQLPoolContainer withTmpFs(int liveTmpFsSizeInMB) {
		if (liveTmpFsSizeInMB != 0) {
			withEnv("PGDATA", "/live/pgdata");
			withTmpFs(tmpFs(liveTmpFsSizeInMB));
		}
		return this;
	}

	private Map<String, String> tmpFs(int liveSizeMB) {
		Map<String, String> mapping = new HashMap<>();
		mapping.put("/live", "rw,size=" + liveSizeMB + "m");
		return mapping;
	}

	public String getShortJdbcUrl() {
		return ("jdbc:postgresql://" +
			getHost() +
			":" +
			getMappedPort(POSTGRESQL_PORT) +
			"/");
	}

	public int getPort() {
		return getFirstMappedPort();
	}

}
