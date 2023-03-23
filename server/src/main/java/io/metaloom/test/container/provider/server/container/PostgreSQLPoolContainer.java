package io.metaloom.test.container.provider.server.container;

import java.util.HashMap;
import java.util.Map;

import org.testcontainers.containers.PostgreSQLContainer;

public class PostgreSQLPoolContainer extends PostgreSQLContainer<PostgreSQLPoolContainer> {

  public static final String DEFAULT_IMAGE = "postgres:13.2";

  public PostgreSQLPoolContainer(int liveTmpFsSizeInMB, int snapshotTmpFsSizeInMB) {
    super(DEFAULT_IMAGE);
    withDatabaseName("postgres");
    withUsername("sa");
    withPassword("sa");
    withEnv("PGDATA", "/live/pgdata");
    withTmpFs(tmpFs(liveTmpFsSizeInMB));
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
