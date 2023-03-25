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
import io.metaloom.test.container.provider.model.DatabasePoolRequest;

/**
 * The pool operation will setup a new test database pool. After this step the provider daemon will automatically populate the database with copies from the
 * template database and allow tests to allocate databases.
 */
@Mojo(name = "pool", defaultPhase = LifecyclePhase.PROCESS_TEST_CLASSES)
public class ProviderPoolMojo extends AbstractProviderMojo {

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
          DatabasePoolRequest request = new DatabasePoolRequest();
          request.setTemplateName(pool.getTemplateName());
          request.setIncrement(pool.getIncrement());
          request.setMinimum(pool.getMinimum());
          request.setMaximum(pool.getMaximum());
          //TODO add db settings
          client.createPool(pool.getId(), request);
        }
      }
    } catch (Exception e) {
      getLog().error("Error while invoking start of test database allocation.", e);
    }
  }

}
