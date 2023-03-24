package io.metaloom.maven.provider;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;

import io.metaloom.maven.provider.container.PostgreSQLPoolContainer;
import io.metaloom.test.container.provider.common.ContainerState;
import io.metaloom.test.container.provider.common.ContainerStateHelper;
import io.metaloom.test.container.provider.container.DatabaseProviderContainer;

@Mojo(name = "start", defaultPhase = LifecyclePhase.INITIALIZE)
public class ProviderStartMojo extends AbstractProviderMojo {

  public static final String TEST_DATABASE_ALIAS = "testdb";

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {

    Network network = Network.newNetwork();

    getLog().info("Starting postgreSQL container");
    PostgreSQLPoolContainer db = new PostgreSQLPoolContainer(128, 128)
      .withReuse(true)
      .withNetwork(network)
      .withNetworkAliases(TEST_DATABASE_ALIAS);
    db.start();

    getLog().info("Starting database provider container");
    DatabaseProviderContainer provider = new DatabaseProviderContainer()
      .withDatabase(TEST_DATABASE_ALIAS, PostgreSQLContainer.POSTGRESQL_PORT, db.getUsername(), db.getPassword())
      .withNetwork(network);

    provider.start();

    project.getProperties().put("maven.provider.db.url", db.getJdbcUrl());
    project.getProperties().put("maven.provider.db.username", db.getUsername());
    project.getProperties().put("maven.provider.db.password", db.getPassword());

    try {
      ContainerState state = new ContainerState();
      state.setProviderHost(provider.getHost());
      state.setProviderPort(provider.getPort());
      state.setProviderContainerId(provider.getContainerId());
      state.setDatabaseContainerId(db.getContainerId());
      ContainerStateHelper.writeState(state);
    } catch (Exception e) {
      getLog().error("Error while writing state to " + ContainerStateHelper.stateFile().getAbsolutePath(), e);
    }
  }

}
