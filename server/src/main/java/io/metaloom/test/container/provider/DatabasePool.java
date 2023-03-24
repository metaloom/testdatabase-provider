package io.metaloom.test.container.provider;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.test.container.provider.common.ServerEnv;
import io.vertx.core.Vertx;

public class DatabasePool {

  public static final Logger log = LoggerFactory.getLogger(DatabasePool.class);

  private Stack<Database> databases = new Stack<>();

  private Map<String, Database> allocations = new HashMap<>();

  private int minimum;

  private int maximum;

  private AtomicLong databaseIdCounter = new AtomicLong();

  private Vertx vertx;

  private Long maintainPoolTimerId;

  private String templateName;

  private int increment;

  private DatabaseSettings settings;

  /**
   * Create a new database pool with the specified levels.
   * 
   * @param vertx
   * @param minimum
   *          Minimum amount of databases to allocate
   * @param maximum
   *          Maximum amount of databases to allocate
   * @param increment
   *          Incremental for new database being allocated at once
   * @param host
   * @param port
   * @param username
   * @param password
   */
  public DatabasePool(Vertx vertx, int minimum, int maximum, int increment, String host, int port, String username, String password) {
    this.vertx = vertx;
    this.minimum = minimum;
    this.maximum = maximum;
    this.increment = increment;
    this.settings = new DatabaseSettings(host, port, username, password);
  }

  public DatabasePool(Vertx vertx) {
    this(vertx, ServerEnv.getPoolMinimum(), ServerEnv.getPoolMaximum(), ServerEnv.getPoolIncrement(), ServerEnv.getDatabaseHost(),
      ServerEnv.getDatabasePort(), ServerEnv.getDatabaseUsername(), ServerEnv.getDatabasePassword());
  }

  public void start() {
    if (isStarted()) {
      log.error("Pool already started. Ignoring start request.");
      return;
    }
    this.maintainPoolTimerId = vertx.setPeriodic(1000, th -> {
      try {
        preAllocate();
      } catch (SQLException e) {
        
        System.out.println(settings());
        log.error("Error while preallocating database", e);
      }
    });
  }

  public void stop() {
    if (maintainPoolTimerId != null) {
      log.info("Stopping pre-allocation process");
      vertx.cancelTimer(maintainPoolTimerId);
      maintainPoolTimerId = null;
    }
  }

  public DatabaseAllocation allocate(String name) throws SQLException {
    if (databases.isEmpty()) {
      preAllocate();
    }
    if (!databases.isEmpty()) {
      Database database = databases.pop();
      allocations.put(name, database);
      return new DatabaseAllocation(name, database);
    }
    return null;
  }

  public void preAllocate() throws SQLException {
    int size = databases.size();
    if (templateName == null) {
      log.warn("Unable to preallocate database. No template name set.");
    }
    if (size < minimum && !(size > maximum) && templateName != null) {
      log.info("Need more databases. Got {} but need {} / {}", size, minimum, maximum);
      for (int i = 0; i < increment; i++) {
        try (Connection connection = DriverManager.getConnection(settings.jdbcUrl(), settings.username(), settings.password())) {
          String newName = "test_db_" + databaseIdCounter.incrementAndGet();
          PreparedStatement statement = connection
            .prepareStatement("CREATE DATABASE " + newName + " WITH TEMPLATE " + templateName + " OWNER " + settings.username());
          statement.executeUpdate();
          databases.push(new Database(newName, settings.username(), settings.password()));
        }
      }
    }
  }

  public void release(DatabaseAllocation allocation) throws SQLException {
    // Delete the db and re-allocate
    String name = allocation.getName();
    Database alloction = allocations.remove(name);
    try (Connection connection = DriverManager.getConnection(settings.jdbcUrl(), settings.username(), settings.password())) {
      PreparedStatement statement = connection.prepareStatement("DROP DATABASE " + alloction.name());
      statement.executeUpdate();
    }
  }

  /**
   * Return the count of database that the pool is ready to provide.
   * 
   * @return
   */
  public int level() {
    return databases.size();
  }

  public int allocationLevel() {
    return allocations.size();
  }

  public String getTemplateName() {
    return templateName;
  }

  public void setTemplateName(String databaseName) {
    this.templateName = databaseName;
  }

  public DatabaseSettings settings() {
    return settings;
  }

  public boolean isStarted() {
    return maintainPoolTimerId != null;
  }
}
