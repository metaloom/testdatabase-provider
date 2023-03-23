package io.metaloom.maven.provider;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import io.metaloom.maven.provider.container.PostgreSQLPoolContainer;
import io.metaloom.test.container.provider.common.ContainerState;
import io.metaloom.test.container.provider.common.ContainerStateHelper;
import io.metaloom.test.container.provider.container.DatabaseProviderContainer;

@Mojo(name = "start", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST)
public class ProviderStartMojo extends AbstractProviderMojo {

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("Starting postgreSQL container");
    PostgreSQLPoolContainer db = new PostgreSQLPoolContainer(128, 128).withReuse(true);
    db.start();

    getLog().info("Starting database provider container");
    DatabaseProviderContainer provider = new DatabaseProviderContainer()
      .withDatabase(db.getHost(), db.getPort(), db.getUsername(), db.getPassword());
    provider.start();
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
