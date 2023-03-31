package io.metaloom.test.container.provider.common.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class ProviderConfigHelper {

	public static final Logger log = LoggerFactory.getLogger(ProviderConfigHelper.class);

	public static String PROVIDER_CONFIG_FILENAME = "testdatabase-provider.json";

	public static final String CONFIG_FOLDERNAME = "target";

	private ProviderConfigHelper() {
	}

	private static ObjectMapper mapper = new ObjectMapper();

	/**
	 * Searches for the config in all parent directory and overwrites the config if found. Otherwise the config will be written in the current working directory
	 * location for the config.
	 * 
	 * @param config
	 */
	public static void writeConfig(ProviderConfig config) {
		try {
			Path configPath = locateConfigPath();
			if (configPath == null) {
				configPath = currentConfigPath();
				ensureConfigFolder();
			}
			mapper.writerWithDefaultPrettyPrinter().writeValue(configPath.toFile(), config);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Config path which points to the location based on the current working dir.
	 * 
	 * @return
	 */
	public static Path currentConfigPath() {
		return Paths.get(CONFIG_FOLDERNAME, PROVIDER_CONFIG_FILENAME);
	}

	/**
	 * Locate the config and delete it.
	 * 
	 * @throws IOException
	 */
	public static void deleteConfig() throws IOException {
		Path config = locateConfigPath();
		if (Files.isRegularFile(config)) {
			Files.delete(config);
		} else {
			throw new RuntimeException("The config " + config + " is not a rgular file");
		}
	}

	/**
	 * Attempt to locate the config file by looking into parent directories if needed.
	 * 
	 * @return
	 */
	public static ProviderConfig readConfig() {
		try {
			Path configPath = locateConfigPath();
			if (configPath == null) {
				return null;
			} else {
				return readConfig(configPath);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static Path locateConfigPath() {
		Path folder = Paths.get("").toAbsolutePath();
		while (folder != null) {
			log.debug("Looking for config file in {}", folder);
			Path path = locateIn(folder);
			if (path != null) {
				return path;
			}
			folder = folder.getParent();
		}
		return null;
	}

	private static Path locateIn(Path folder) {
		Path path = folder.resolve(Paths.get("target", PROVIDER_CONFIG_FILENAME));
		if (Files.exists(path)) {
			log.debug("Found config in {}", path);
			return path;
		}
		Path path2 = folder.resolve(Paths.get(PROVIDER_CONFIG_FILENAME));
		if (Files.exists(path2)) {
			log.debug("Found config in {}", path2);
			return path2;
		}
		return null;
	}

	private static void ensureConfigFolder() {
		File folder = new File(CONFIG_FOLDERNAME);
		if (!folder.exists()) {
			folder.mkdirs();
		}
	}

	private static ProviderConfig readConfig(Path path) throws IOException {
		if (Files.exists(path)) {
			return mapper.readValue(path.toFile(), ProviderConfig.class);
		} else {
			return null;
		}
	}

}