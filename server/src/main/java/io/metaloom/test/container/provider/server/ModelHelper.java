package io.metaloom.test.container.provider.server;

import io.metaloom.test.container.provider.DatabasePool;
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
    response.setTemplateName(pool.getTemplateName());
    response.setStarted(pool.isStarted());
    return response;
  }
}
