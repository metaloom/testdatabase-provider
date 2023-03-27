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
		this(0);
	}

	public PostgreSQLPoolContainer(int liveTmpFsSizeInMB) {
		this(DEFAULT_IMAGE, liveTmpFsSizeInMB);
	}

	public PostgreSQLPoolContainer(String imageName, int liveTmpFsSizeInMB) {
		super(DockerImageName.parse(imageName).asCompatibleSubstituteFor(DEFAULT_IMAGE));
		withDatabaseName(DEFAULT_DATABASE_NAME);
		withUsername(DEFAULT_USERNAME);
		withPassword(DEFAULT_PASSWORD);
		if (liveTmpFsSizeInMB != 0) {
			withEnv("PGDATA", "/live/pgdata");
			withTmpFs(tmpFs(liveTmpFsSizeInMB));
		}
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
