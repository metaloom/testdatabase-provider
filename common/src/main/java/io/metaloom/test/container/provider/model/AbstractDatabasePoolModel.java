package io.metaloom.test.container.provider.model;

public abstract class AbstractDatabasePoolModel {

	private DatabasePoolConnection connection;

	private DatabasePoolSettings settings;

	private String templateDatabaseName;

	public DatabasePoolConnection getConnection() {
		return connection;
	}

	public AbstractDatabasePoolModel setConnection(DatabasePoolConnection connection) {
		this.connection = connection;
		return this;
	}

	public String getTemplateDatabaseName() {
		return templateDatabaseName;
	}

	public AbstractDatabasePoolModel setTemplateDatabaseName(String templateDatabaseName) {
		this.templateDatabaseName = templateDatabaseName;
		return this;
	}

	public DatabasePoolSettings getSettings() {
		return settings;
	}

	public AbstractDatabasePoolModel setSettings(DatabasePoolSettings settings) {
		this.settings = settings;
		return this;
	}
}
