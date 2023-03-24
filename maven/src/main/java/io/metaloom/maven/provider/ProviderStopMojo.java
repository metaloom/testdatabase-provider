package io.metaloom.maven.provider;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.testcontainers.DockerClientFactory;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.StopContainerCmd;

import io.metaloom.test.container.provider.common.ContainerState;
import io.metaloom.test.container.provider.common.ContainerStateHelper;

/**
 * The stop operation will terminate previously started databases and the testdatabase provider daemon container. 
 */
@Mojo(name = "stop", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST)
public class ProviderStopMojo extends AbstractProviderMojo {

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {

    try {
      ContainerState state = ContainerStateHelper.readState();
      if (state == null) {
        getLog().warn("Unable to stop containers. Container state file not found " + ContainerStateHelper.stateFile());
      }
      DockerClient client = DockerClientFactory.lazyClient();
      if (state.getProviderContainerId() != null) {
        stopProvider(client, state);
      }
      if (state.getDatabaseContainerId() != null) {
        stopDatabase(client, state);
      }
      ContainerStateHelper.stateFile().delete();
      client.close();
    } catch (Exception e) {
      getLog().error("Error while stopping containers", e);
    }
  }

  private void stopDatabase(DockerClient client, ContainerState state) {
    try {
      getLog().info("Stopping postgreSQL container");
      try (StopContainerCmd cmd = client.stopContainerCmd(state.getDatabaseContainerId())) {
        cmd.exec();
      }
    } catch (Exception e) {
      getLog().error("Error while stopping database ", e);
    }
  }

  private void stopProvider(DockerClient client, ContainerState state) {
    try {
      getLog().info("Stopping database provider container");
      try (StopContainerCmd cmd = client.stopContainerCmd(state.getProviderContainerId())) {
        cmd.exec();
      }
    } catch (Exception e) {
      getLog().error("Error while stopping database provider", e);
    }
  }
}
