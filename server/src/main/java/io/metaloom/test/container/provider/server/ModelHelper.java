package io.metaloom.test.container.provider.server;

import io.metaloom.test.container.provider.DatabaseAllocation;
import io.metaloom.test.container.provider.DatabasePool;
import io.metaloom.test.container.provider.model.DatabaseAllocationResponse;
import io.metaloom.test.container.provider.model.DatabasePoolConnection;
import io.metaloom.test.container.provider.model.DatabasePoolResponse;
import io.metaloom.test.container.provider.model.DatabasePoolSettings;

public final class ModelHelper {

	private ModelHelper() {
	}

	public static DatabasePoolResponse toModel(DatabasePool pool) {
		DatabasePoolConnection connection = new DatabasePoolConnection();
		connection.setDatabase(pool.settings().adminDB());
		connection.setHost(pool.settings().host());
		connection.setPort(pool.settings().port());
		connection.setInternalHost(pool.settings().internalHost());
		connection.setInternalPort(pool.settings().internalPort());

		connection.setUsername(pool.settings().username());
		connection.setPassword(pool.settings().password());

		DatabasePoolSettings settings = new DatabasePoolSettings();
		settings.setIncrement(pool.getIncrement());
		settings.setMaximum(pool.getMaximum());
		settings.setMinimum(pool.getMinimum());

		DatabasePoolResponse response = new DatabasePoolResponse();
		response.setConnection(connection);
		response.setSettings(settings);
		response.setAllocationLevel(pool.allocationLevel());
		response.setLevel(pool.level());
		response.setTemplateDatabaseName(pool.getTemplateName());
		response.setStarted(pool.isStarted());
		return response;
	}

	public static DatabaseAllocationResponse toModel(DatabaseAllocation allocation) {
		DatabaseAllocationResponse response = new DatabaseAllocationResponse();
		response.setPoolId(allocation.getPool().id());
		response.setId(allocation.id());
		response.setHost(allocation.db().settings().host());
		response.setPort(allocation.db().settings().port());
		response.setJdbcUrl(allocation.db().jdbcUrl());
		response.setUsername(allocation.db().settings().username());
		response.setPassword(allocation.db().settings().password());
		response.setDatabaseName(allocation.db().name());
		return response;
	}
}
