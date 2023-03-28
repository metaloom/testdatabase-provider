package io.metaloom.maven.provider;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.jupiter.api.Test;

import io.metaloom.test.container.provider.client.ProviderClient;
import io.metaloom.test.container.provider.common.config.ProviderConfig;
import io.metaloom.test.container.provider.common.config.ProviderConfigHelper;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class ProviderStartMojoTest {

  @Test
  public void testStart() throws MojoExecutionException, MojoFailureException, IOException, InterruptedException {
    new ProviderStartMojo().execute();
    ProviderConfig config = ProviderConfigHelper.readConfig();
    assertNotNull(config);
    Thread.sleep(2000);
    System.out.println(config.toString());
    ProviderClient client = new ProviderClient(Vertx.vertx(), config.getProviderHost(), config.getProviderPort());
    try {
      JsonObject stat = client.listPools().toCompletionStage().toCompletableFuture().get();
      System.out.println("Stat:\n" + stat.encodePrettily());
    } catch (Exception e) {
      e.printStackTrace();
    }
    new ProviderStopMojo().execute();
  }
}
