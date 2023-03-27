package io.metaloom.test.container.provider;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class SQLUtils {

  public static final String SELECT_DATABASES = "SELECT datname FROM pg_database";

  private SQLUtils() {
  }

  public static void dropDatabase(Database db) throws SQLException {
    DatabaseSettings settings = db.settings();
    try (Connection connection = DriverManager.getConnection(db.settings().internalAdminJdbcUrl(), settings.username(), settings.password())) {
      PreparedStatement statement = connection.prepareStatement("DROP DATABASE " + db.name());
      statement.executeUpdate();
    }
  }

  public static Database copyDatabase(DatabaseSettings settings, String sourceName, String targetName) throws SQLException {
    try (Connection connection = DriverManager.getConnection(settings.internalJdbcUrl()+ sourceName, settings.username(), settings.password())) {
      PreparedStatement statement = connection
        .prepareStatement("CREATE DATABASE " + targetName + " WITH TEMPLATE " + sourceName + " OWNER " + settings.username());
      statement.executeUpdate();
      return new Database(settings, targetName);
    }
  }

  public static List<String> listDatabases(DatabaseSettings settings) throws SQLException {
    List<String> databaseNames = new ArrayList<>();
    try (Connection connection = DriverManager.getConnection(settings.internalAdminJdbcUrl(), settings.username(), settings.password())) {
      PreparedStatement statement = connection.prepareStatement(SELECT_DATABASES);
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        String name = rs.getString(1);
        databaseNames.add(name);
      }
    }
    return databaseNames;
  }
}
