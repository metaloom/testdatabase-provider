package io.metaloom.test.container.provider.model;

public abstract class AbstractDatabasePoolModel {

  private DatabasePoolConnection connection;

  private DatabasePoolSettings settings;

  public DatabasePoolConnection getConnection() {
    return connection;
  }

  public AbstractDatabasePoolModel setConnection(DatabasePoolConnection connection) {
    this.connection = connection;
    return this;
  }

  private String templateName;

  public String getTemplateName() {
    return templateName;
  }

  public AbstractDatabasePoolModel setTemplateName(String templateName) {
    this.templateName = templateName;
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
