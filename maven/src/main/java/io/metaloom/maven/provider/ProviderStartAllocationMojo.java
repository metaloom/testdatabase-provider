package io.metaloom.maven.provider;

import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import io.metaloom.test.container.provider.client.DatabaseProviderClient;
import io.metaloom.test.container.provider.common.ContainerState;
import io.metaloom.test.container.provider.common.ContainerStateHelper;
import io.vertx.core.Vertx;

@Mojo(name = "start-allocation", defaultPhase = LifecyclePhase.PROCESS_TEST_CLASSES)
public class ProviderStartAllocationMojo extends AbstractProviderMojo {

  @Parameter(property = "pools")
  private List<PoolConfiguration> pools;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    try {
      ContainerState state = ContainerStateHelper.readState();
      if (state == null) {
        getLog().warn("Unable to stop containers. Container state file not found " + ContainerStateHelper.stateFile());
      } else {
        String host = state.getProviderHost();
        int port = state.getProviderPort();
        DatabaseProviderClient client = new DatabaseProviderClient(vertx, host, port);
        for (PoolConfiguration pool : pools) {
          client.createPool(pool.getId(), pool.getTemplateName());
        }
      }
    } catch (Exception e) {
      getLog().error("Error while invoking start of test database allocation.", e);
    }
  }

}
