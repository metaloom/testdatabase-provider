package io.metaloom.test.container.provider;

import java.sql.SQLException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.test.container.provider.common.ServerEnv;
import io.metaloom.test.container.provider.server.DatabaseProviderServer;
import io.metaloom.test.container.provider.server.ServerConfiguration;

@Singleton
public class BootstrapInitializer {

	private static final Logger log = LoggerFactory.getLogger(BootstrapInitializer.class);

	private final DatabasePoolManager manager;

	private final ServerConfiguration config;

	private final DatabaseProviderServer server;

	@Inject
	public BootstrapInitializer(DatabasePoolManager manager, ServerConfiguration config, DatabaseProviderServer server) {
		this.manager = manager;
		this.config = config;
		this.server = server;
	}

	public void start() {
		if (config.hasConnectionDetails()) {
			log.info("Searching for previously created pools and databases");
			try {
				manager.loadFromDB();
			} catch (SQLException e) {
				log.error("Error while loading databases", e);
			}
		} else {
			log.info("Skipping loading of existing databases because not all connection details have been provided.");
		}

		log.info("Starting server using port {}", config.httpPort());
		server.start();

		String templateDatabaseName = config.templateDatabaseName();
		if (templateDatabaseName != null) {
			log.info("Creating default pool for database " + config.host() + ":" + config.port() + "/" + templateDatabaseName + " using admin db "
				+ config.adminDB());
			DatabasePool pool = manager.createPool("default", config.host(), config.port(), config.host(), config.port(), config.username(),
				config.password(), config.adminDB(), templateDatabaseName);
			pool.setTemplateDatabaseName(templateDatabaseName);
			pool.start();
		} else {
			log.debug("Skipping creation of default pool since no template db was specified via "
				+ ServerEnv.TESTDATABASE_PROVIDER_DATABASE_TEMPLATE_DBNAME_KEY);
		}

	}

	public DatabaseProviderServer server() {
		return server;
	}

}
