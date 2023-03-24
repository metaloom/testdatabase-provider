package io.metaloom.maven.provider;

import java.io.IOException;
import java.net.URI;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;

import io.metaloom.maven.provider.container.PostgreSQLPoolContainer;
import io.metaloom.test.container.provider.common.ContainerState;
import io.metaloom.test.container.provider.common.ContainerStateHelper;
import io.metaloom.test.container.provider.container.DatabaseProviderContainer;

/**
 * The start operation will provide the needed testdatabase provider daemon and optionally also a database which will automatically be configured to work in
 * conjunction with the started daemon.
 */
@Mojo(name = "start", defaultPhase = LifecyclePhase.INITIALIZE)
public class ProviderStartMojo extends AbstractProviderMojo {

  public static final String TEST_DATABASE_NETWORK_ALIAS = "testdb";

  /**
   * Whether a postgreSQL server should be started automatically. The properties maven.provider.db.url, maven.provider.db.username and
   * maven.provider.db.password will automatically be set and can be uses by other plugins after the execution of the plugin goal.
   */
  @Parameter
  private boolean startPostgreSQL = true;

  /**
   * Database settings which may be used to provide an external database to the provider daemon.
   */
  @Parameter
  private PoolDatabase database;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {

    try {
      ContainerState oldState = ContainerStateHelper.readState();
      if (oldState != null) {
        getLog().info("Found state file. This means the provider is probably still running. Aborting start");
        return;
      }
    } catch (IOException e) {
      getLog().error("Failure while reading original state", e);
    }
    ContainerState state = new ContainerState();
    Network network = Network.builder().build();

    String jdbcURL = database != null ? database.getJdbcUrl() : null;
    String username = database != null ? database.getDatabaseUsername() : null;
    String password = database != null ? database.getDatabasePassword() : null;

    if (startPostgreSQL) {
      getLog().info("Starting postgreSQL container");
      // TODO configure tmpfs
      PostgreSQLPoolContainer db = new PostgreSQLPoolContainer(128, 128)
        .withReuse(true)
        .withNetwork(network)
        .withNetworkAliases(TEST_DATABASE_NETWORK_ALIAS);
      db.start();
      state.setDatabaseContainerId(db.getContainerId());
      getLog().debug("Container JDBCUrl:" + db.getJdbcUrl());
      getLog().debug("Container Username:" + db.getUsername());
      getLog().debug("Container Password:" + db.getPassword());

      project.getProperties().put("maven.provider.db.url", db.getJdbcUrl());
      project.getProperties().put("maven.provider.db.username", db.getUsername());
      project.getProperties().put("maven.provider.db.password", db.getPassword());
      if (database == null) {
        getLog().info("External database was not configured. Using provided database container instead");
        jdbcURL = db.getJdbcUrl();
        username = db.getUsername();
        password = db.getPassword();
      }
    }

    getLog().info("Starting database provider container");

    String databaseHost = "localhost";
    int databasePort = 5432;

    if (startPostgreSQL) {
      databaseHost = TEST_DATABASE_NETWORK_ALIAS;
      databasePort = PostgreSQLContainer.POSTGRESQL_PORT;
    } else {
      String cleanURI = jdbcURL.substring(5);
      URI uri = URI.create(cleanURI);
      // String type = uri.getScheme();
      databaseHost = uri.getHost();
      databasePort = uri.getPort();
    }
    getLog().info("Starting provider with db " + jdbcURL);
    DatabaseProviderContainer provider = new DatabaseProviderContainer()
      .withDatabase(databaseHost, databasePort, username, password)
      .withNetwork(network);
    provider.start();

    try {
      state.setProviderHost(provider.getHost());
      state.setProviderPort(provider.getPort());
      state.setProviderContainerId(provider.getContainerId());
      ContainerStateHelper.writeState(state);
    } catch (Exception e) {
      getLog().error("Error while writing state to " + ContainerStateHelper.stateFile().getAbsolutePath(), e);
    }
  }

}
