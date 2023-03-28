package io.metaloom.test.container.provider.common.config;

public class PostgresqlConfig extends AbstractDatabaseConfig {

	@Override
	public String jdbcUrl() {
		return ("jdbc:postgresql://" +
			getHost() +
			":" +
			getPort() +
			"/" +
			getDatabaseName());
	}

}
