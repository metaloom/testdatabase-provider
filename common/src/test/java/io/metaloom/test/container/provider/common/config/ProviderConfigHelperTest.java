package io.metaloom.test.container.provider.common.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;

public class ProviderConfigHelperTest {

	@Test
	public void testReadFromParent() throws IOException {
		File testFile = writeConfigIntoParentFolder();
		ProviderConfig state = ProviderConfigHelper.readConfig();
		assertNotNull(state);
		assertEquals("test1234", state.getPostgresql().getHost());
		testFile.delete();
	}

	@Test
	public void testWriteCurrent() {
		File config = new File(ProviderConfigHelper.CONFIG_FOLDERNAME, ProviderConfigHelper.PROVIDER_CONFIG_FILENAME);
		if (config.exists()) {
			config.delete();
		}
		ProviderConfigHelper.writeConfig(new ProviderConfig());
		assertTrue(config.exists());
		config.delete();
	}

	@Test
	public void testWriteParent() throws IOException {
		writeConfigIntoParentFolder();
		ProviderConfigHelper.writeConfig(new ProviderConfig().setProviderContainerId("enemenemuh"));
		assertEquals("enemenemuh", ProviderConfigHelper.readConfig().getProviderContainerId());
	}

	private File writeConfigIntoParentFolder() throws IOException {
		ProviderConfigHelper.PROVIDER_CONFIG_FILENAME = "test-testdb-provider.json";
		File testFile = new File("../target/", ProviderConfigHelper.PROVIDER_CONFIG_FILENAME);
		if (testFile.exists()) {
			testFile.delete();
		}
		String json = """
			{
				"postgresql": {
					"host": "test1234"
				}
			
			}
			""";
		FileUtils.writeStringToFile(testFile, json, Charset.defaultCharset());
		return testFile;
	}
}
