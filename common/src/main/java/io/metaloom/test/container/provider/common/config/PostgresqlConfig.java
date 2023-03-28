package io.metaloom.test.container.provider.common.config;

public class PostgresqlConfig extends AbstractDatabaseConfig {

	@Override
	public String adminJdbcUrl() {
		return jdbcUrl(getDatabaseName());
	}

	@Override
	public String jdbcUrl(String databaseName) {
		return ("jdbc:postgresql://" +
			getHost() +
			":" +
			getPort() +
			"/" +
			databaseName);
	}

}
