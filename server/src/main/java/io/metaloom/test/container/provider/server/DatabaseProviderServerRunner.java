package io.metaloom.test.container.provider.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.test.container.provider.common.version.Version;
import io.metaloom.test.container.provider.server.dagger.DaggerServerComponent;
import io.metaloom.test.container.provider.server.dagger.ServerComponent;

public class DatabaseProviderServerRunner {

	private static final Logger log = LoggerFactory.getLogger(DatabaseProviderServerRunner.class);

	public static void main(String[] args) {
		log.info("Starting Test Database Provider Server [" + Version.getPlainVersion() + "] - [" + Version.getBuildInfo().getBuildtimestamp() + "]");
		ServerConfiguration config = ServerConfigurationLoader.load();
		ServerComponent component = DaggerServerComponent.builder().configuration(config).build();
		component.boot().start();
	}

}
