package io.metaloom.test.container.server;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.testcontainers.containers.PostgreSQLContainer;

public final class TestHelper {

  private TestHelper() {
  }

  private static final String CREATE_TABLE = """
    CREATE TABLE users
    (id INT PRIMARY KEY, name TEXT)
    """;

  private static final String INSERT_USER = "INSERT INTO users (id, name) VALUES (?, ?)";

  private static final String SELECT_USERS = "SELECT id, name from users";

  private static final String DELETE_USERS = "DELETE FROM users";

  public static final int USER_COUNT = 50_000;

  private static final String CREATE_DB = "CREATE DATABASE test_template";

  public static String setupTable(String jdbcUrl, String username, String password) throws SQLException {
    String dbName = "test_template";
    try (Connection connection = DriverManager.getConnection(jdbcUrl + "postgres", username, password)) {
      Statement statement1 = connection.createStatement();
      statement1.execute(CREATE_DB);
    }
    try (Connection connection = DriverManager.getConnection(jdbcUrl + dbName, username, password)) {
      Statement statement2 = connection.createStatement();
      statement2.execute(CREATE_TABLE);
    }
    return dbName;
  }

  public static void insertUsers(PostgreSQLContainer db, int id, String name) throws SQLException {
    try (Connection connection = DriverManager.getConnection(db.getJdbcUrl(), db.getUsername(), db.getPassword())) {
      for (int i = 1; i <= USER_COUNT; i++) {
        PreparedStatement statement = connection.prepareStatement(INSERT_USER);
        statement.setInt(1, id + i);
        statement.setString(2, name + "_" + i);
        assertEquals(1, statement.executeUpdate());
      }
    }
  }

  public static void deleteUsers(PostgreSQLContainer db) throws SQLException {
    try (Connection connection = DriverManager.getConnection(db.getJdbcUrl(), db.getUsername(), db.getPassword())) {
      PreparedStatement statement = connection.prepareStatement(DELETE_USERS);
      statement.executeUpdate();
    }

  }

}
