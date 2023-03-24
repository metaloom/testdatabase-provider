package io.metaloom.maven.provider;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.jupiter.api.Test;

import io.metaloom.test.container.provider.client.DatabaseProviderClient;
import io.metaloom.test.container.provider.common.ContainerState;
import io.metaloom.test.container.provider.common.ContainerStateHelper;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class ProviderStartMojoTest {

  @Test
  public void testStart() throws MojoExecutionException, MojoFailureException, IOException, InterruptedException {
    new ProviderStartMojo().execute();
    ContainerState state = ContainerStateHelper.readState();
    assertNotNull(state);
    Thread.sleep(2000);
    System.out.println(state.toString());
    DatabaseProviderClient client = new DatabaseProviderClient(Vertx.vertx(), state.getProviderHost(), state.getProviderPort());
    try {
      JsonObject stat = client.listPools().toCompletionStage().toCompletableFuture().get();
      System.out.println("Stat:\n" + stat.encodePrettily());
    } catch (Exception e) {
      e.printStackTrace();
    }
    new ProviderStopMojo().execute();
  }
}
