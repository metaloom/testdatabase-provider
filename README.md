
```java
@Test
public void testDB2() throws InterruptedException {
	Thread.sleep(2000);
	System.out.println(provider.db());
	Thread.sleep(2000);
}
```

```xml
    <!-- Test -->
    <dependency>
      <groupId>io.metaloom.test</groupId>
      <artifactId>testdatabase-provider-junit5</artifactId>
      <version>${testdatabase-provider.version}</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>ch.qos.logback</groupId>
      <artifactId>logback-classic</artifactId>
      <version>1.2.10</version>
      <scope>test</scope>
    </dependency>
    <!-- // @@ xml_snippet -->
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-maven-plugin</artifactId>
        <version>9.12.0</version>
        <executions>
          <execution>
            <?m2e ignore?>
            <phase>generate-sources</phase>
            <goals>
              <goal>migrate</goal>
            </goals>
          </execution>
        </executions>
        <configuration>
          <url>${maven.testdatabase-provider.postgresql.jdbcurl}</url>
          <user>${maven.testdatabase-provider.postgresql.username}</user>
          <password>${maven.testdatabase-provider.postgresql.password}</password>
          <locations>
            <location>filesystem:src/main/flyway</location>
          </locations>
        </configuration>
        <dependencies>
          <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <version>42.2.2</version>
          </dependency>
        </dependencies>
      </plugin>

      <plugin>
        <groupId>io.metaloom.maven</groupId>
        <artifactId>testdb-maven-plugin</artifactId>
        <version>${testdatabase-provider.version}</version>
        <executions>
          <execution>
            <goals>
              <goal>clean</goal>
            </goals>
          </execution>
          <!-- Startup a postgreSQL container and the provider daemon -->
          <execution>
            <id>start</id>
            <phase>initialize</phase>
            <goals>
              <goal>start</goal>
            </goals>
            <configuration>
              <skip>false</skip>
              <defaultLimits>
                <minimum>10</minimum>
                <maximum>20</maximum>
                <increment>5</increment>
              </defaultLimits>
              <postgresql>
                <containerImage>postgres:13.2</containerImage>
                <startContainer>true</startContainer>
                <tmpfsSizeMB>256</tmpfsSizeMB>
                <username>sa</username>
                <password>sa</password>
                <database>test</database>
                <!--
                Port and host are only used when providing
                an external database
                <port>5432</port>
                <host>localhost</host>
                -->
              </postgresql>
              <createPool>false</createPool>
              <startProvider>true</startProvider>
              <reuseContainers>true</reuseContainers>
            </configuration>
          </execution>
          <!-- Setup a new testdatabase pool now that flyway has setup the
          tables -->
          <execution>
            <id>pool</id>
            <phase>process-test-classes</phase>
            <goals>
              <goal>pool</goal>
            </goals>

            <configuration>
              <pools>
                <pool>
                  <id>dummy</id>
                  <host>${maven.testdatabase-provider.postgresql.host}</host>
                  <port>${maven.testdatabase-provider.postgresql.port}</port>
                  <username>${maven.testdatabase-provider.postgresql.username}</username>
                  <password>${maven.testdatabase-provider.postgresql.password}</password>
                  <database>${maven.testdatabase-provider.postgresql.database}</database>
                  <templateName>test</templateName>
                  <limits>
                    <minimum>10</minimum>
                    <maximum>30</maximum>
                    <increment>5</increment>
                  </limits>
                </pool>
              </pools>
            </configuration>
          </execution>

          <!-- Stop the previously started containers -->
          <execution>
            <id>stop</id>
            <phase>post-integration-test</phase>
            <goals>
              <goal>stop</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.11.0</version>
        <configuration>
          <release>17</release>
        </configuration>
      </plugin>
    </plugins>
  </build>
</project>
  
```


# Test Database Provider - 0.1.0-SNAPSHOT

This project provides tools to quickly allocate test databases for Java based projects.
Depending on the test database size and complexity it may be much faster to not have to prepare a new database for every testcase.

The goal of this project is to provide throwaway databases for unit tests. A test can modify the database and no cleanup actions (Recreate DB, Truncate Tables, Setup testfixture) needs to be performed.
Instead the provider can be queried to allocate a fresh database.

A dedicated provider daemon will constantly maintain a specified level of free databases. Databases which have been consumed by tests will be dropped and fresh ones will be created.

## Supported Databases

> Currently **only** PostgreSQL is supported.

## Maven

[Plugin Documentation](https://metaloom.github.io/testdatabase-provider/)

The dedicated `testdb-maven-plugin` can be used to startup a postgreSQL and provider server container. The provider server can be queried by tests to provide a database.

```xml
<plugin>
    <groupId>io.metaloom.maven</groupId>
    <artifactId>testdb-maven-plugin</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</plugin>
```


### Goals

| Goal                                                                                  | Description                                      | Default Lifecycle Phase |
| ------------------------------------------------------------------------------------- | ------------------------------------------------ | ----------------------- |
| [`testdb:start`](https://metaloom.github.io/testdatabase-provider/start-mojo.html)    | Create config and start containers               | initialize              |
| [`testdb:pool`](https://metaloom.github.io/testdatabase-provider/pool-mojo.html)      | Create new testdatabase pools                    | process-test-classes    |
| [`testdb:stop`](https://metaloom.github.io/testdatabase-provider/stop-mojo.html)      | Stop and destroy containers                      | post-integration-test   |
| [`testdb:clean`](https://metaloom.github.io/testdatabase-provider/clean-mojo.html)    | Stop and destroy containers                      | clean                   |


### Usage

The lifecyle order in this example:

* **initialize** - Startup of postgresql + provider container
* **generate-sources** - Flyway setup of database
* **process-test-classes** - Setup of a testdatabase pool
* **post-integration-test** - Removal of started containers

Maven Commands:

```bash
# Start the containers using the test execution settings from the pom.xml
mvn testdb:start@start

# Setup the configured pool@pool using the pool execution settings from the pom.xml
mvn testdb:pool@pool

# Stop the containers
mvn testdb:stop
```

### Example

Example configuration:

```xml
…
<plugin>
<groupId>org.flywaydb</groupId>
<artifactId>flyway-maven-plugin</artifactId>
<version>9.12.0</version>
<executions>
    <execution>
    <?m2e ignore?>
    <phase>generate-sources</phase>
    <goals>
        <goal>migrate</goal>
    </goals>
    </execution>
</executions>
<configuration>
    <!-- The flyway plugin uses the properties which have been provided by the testdatabase--plugin. -->
    <url>${maven.testdatabase-provider.postgresql.jdbcurl}</url>
    <user>${maven.testdatabase-provider.postgresql.username}</user>
    <password>${maven.testdatabase-provider.postgresql.password}</password>
    <locations>
    <location>filesystem:src/main/flyway</location>
    </locations>
</configuration>
<dependencies>
    <dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.2.2</version>
    </dependency>
</dependencies>
</plugin>

<plugin>
<groupId>io.metaloom.maven</groupId>
<artifactId>testdb-maven-plugin</artifactId>
<version>0.0.1-SNAPSHOT</version>
<executions>
    <!-- Start the provider daemon and needed database containers -->
    <execution>
        <id>start</id>
        <phase>initialize</phase>
        <goals>
            <goal>start</goal>
            <configuration>
                <skip>false</skip>
                <defaultLimits>
                <minimum>10</minimum>
                <maximum>20</maximum>
                <increment>5</increment>
                </defaultLimits>
                <postgresql>
                    <containerImage>postgres:13.2</containerImage>
                    <startContainer>true</startContainer>
                    <tmpfsSizeMB>256</tmpfsSizeMB>
                    <username>sa</username>
                    <password>sa</password>
                    <database>test</database>
                    <!--
                    Port and host are only used when providing
                    an external database
                    <port>5432</port>
                    <host>localhost</host>
                    -->
                </postgresql>
                <createPool>false</createPool>
                <startProvider>true</startProvider>
                <reuseContainers>true</reuseContainers>
            </configuration>
        </goals>
    </execution>
    <!-- Now flyway has populated the database and we can setup our test database pool -->
    <execution>
        <id>pool</id>
        <phase>process-test-classes</phase>
        <goals>
            <goal>pool</goal>
        </goals>
        <configuration>
            <pools>
                <pool>
                    <id>dummy</id>
                    <!-- The connection details can be omitted when the start goal provides a database container -->
                    <!--
                    <host>${maven.testdatabase-provider.postgresql.host}</host>
                    <port>${maven.testdatabase-provider.postgresql.port}</port>
                    <username>${maven.testdatabase-provider.postgresql.username}</username>
                    <password>${maven.testdatabase-provider.postgresql.password}</password>
                    <database>${maven.testdatabase-provider.postgresql.database}</database>
                    -->
                    <templateName>test</templateName>
                    <limits>
                    <minimum>10</minimum>
                    <maximum>30</maximum>
                    <increment>5</increment>
                    </limits>
                </pool>
            </pools>
        </configuration>
    </execution>

    <!-- Finally we stop the started containers -->
    <execution>
        <id>stop</id>
        <phase>post-integration-test</phase>
        <goals>
            <goal>stop</goal>
        </goals>
    </execution>
</executions>
</plugin>
…
```

## Pitfalls

The `reuseContainers` setting will ensure that the started containers are not being removed once the maven process terminates. This is especially useful when providing test databases for your IDE test execution.

This feature requires the file `~/.testcontainers.properties` to contain the line `testcontainers.reuse.enable=true`. See [reusable containers](https://www.testcontainers.org/features/reuse/) for more information.

## Standalone

The provider server container can also be setup as a standlone container.

```bash
docker run --rm \
  metaloom/testdatabase-provider:0.1.0-SNAPSHOT
```

## Provider Server Environment variables

Various variables may be specified during startup that reference the testdatabase being used. When the `TESTDATABASE_PROVIDER_POOL_TEMPLATE_NAME` variable has been specified a `default`database pool will be setup during startup of the provider container. Please note that this is not mandatory as the REST API can be called after startup to CRUD new test database pools.

* `TESTDATABASE_PROVIDER_DATABASE_HOST` -  Host setting which will be passed along to tests that requested a database.
* `TESTDATABASE_PROVIDER_DATABASE_PORT` - Port setting which will be passed along to tests that requested a database.
* `TESTDATABASE_PROVIDER_DATABASE_INTERNAL_HOST` - Host setting which will be used by the provider server to manage the pooled databases.
* `TESTDATABASE_PROVIDER_DATABASE_INTERNAL_PORT` - Port setting which will be used by the provider server to managed the pooled databases.
* `TESTDATABASE_PROVIDER_DATABASE_USERNAME` - Connection details for database.
* `TESTDATABASE_PROVIDER_DATABASE_PASSWORD` - Connection details for database.
* `TESTDATABASE_PROVIDER_DATABASE_DBNAME_KEY` - Name of the database which will be selected when invoking admin operations (e.g. DROP DATABASE, CREATE DATABASE)
* `TESTDATABASE_PROVIDER_POOL_TEMPLATE_NAME` - Name of the database which should be used by the `default` pool. When omitted no `default` pool will be created.

* `TESTDATABASE_PROVIDER_POOL_MINIMUM` - Default minimum for newly created pools.
* `TESTDATABASE_PROVIDER_POOL_MAXIMUM` - Default maximum for newly created pools.
* `TESTDATABASE_PROVIDER_POOL_INCREMENT` - Default increment for newly created pools. The server will create new databases whenever the minium threshold is hot. The increment setting can be used to create multiple new databases in one step.


## Test - Junit 5

```xml
<dependency>
  <groupId>io.metaloom.test</groupId>
  <artifactId>testdatabase-provider-junit5</artifactId>
  <version>0.1.0-SNAPSHOT</version>
  <scope>test</scope>
</dependency>
```


A test can acquire a database using the `DatabaseProviderExtension` extension.

```java
@RegisterExtension
static DatabaseProviderExtension provider = new DatabaseProviderExtension();

@Test
public void testDB() {
    System.out.println(provider.db());
}
```

## Test - Junit 4 

```xml
<dependency>
  <groupId>io.metaloom.test</groupId>
  <artifactId>testdatabase-provider-junit4</artifactId>
  <version>0.1.0-SNAPSHOT</version>
  <scope>test</scope>
</dependency>
```

```java
@Rule
public DatabaseProviderRule provider = new DatabaseProviderRule("localhost", server.getPort());

@Test
public void testDB() {
    System.out.println(provider.db());
}
```

## Releasing 

```bash
# Update version
mvn versions:set -DgenerateBackupPoms=false

# Update readme
mvn post-clean

# Invoke release
mvn clean deploy -Drelease

# Update gh-pages branch
# TODO
```
