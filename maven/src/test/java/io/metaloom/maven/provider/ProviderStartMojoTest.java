package io.metaloom.maven.provider;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import io.metaloom.test.container.provider.client.ProviderClient;
import io.metaloom.test.container.provider.common.config.ProviderConfig;
import io.metaloom.test.container.provider.common.config.ProviderConfigHelper;
import io.metaloom.test.container.provider.model.DatabasePoolListResponse;

public class ProviderStartMojoTest {

	@Test
	public void testStart() throws Exception {
		new ProviderStartMojo().execute();
		ProviderConfig config = ProviderConfigHelper.readConfig();
		assertNotNull(config);
		Thread.sleep(2000);
		System.out.println(config.toString());
		ProviderClient client = new ProviderClient(config.getProviderHost(), config.getProviderPort());
		DatabasePoolListResponse response = client.listPools().get();
		assertNotNull(response);
		assertNotNull(response.getList());
		new ProviderStopMojo().execute();
	}
}
