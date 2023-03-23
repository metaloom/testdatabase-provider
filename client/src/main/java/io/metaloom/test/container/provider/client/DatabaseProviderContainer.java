package io.metaloom.test.container.provider.client;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class DatabaseProviderContainer extends GenericContainer<DatabaseProviderContainer> {

  private static final DockerImageName DEFAULT_IMAGE = DockerImageName.parse("metaloom/postgresql-testdatabase-provider:0.0.1-SNAPSHOT");

  public DatabaseProviderContainer() {
    super(DEFAULT_IMAGE);
    withExposedPorts(8080);
    withReuse(true);
  }

  public int getPort() {
    return getFirstMappedPort();
  }
}
