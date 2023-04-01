# Test Database Provider - 0.1.2-SNAPSHOT

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
    <version>0.1.2-SNAPSHOT</version>
</plugin>
```


### Goals

| Goal                                                                                  | Description                                      | Default Lifecycle Phase |
| ------------------------------------------------------------------------------------- | ------------------------------------------------ | ----------------------- |
| [`testdb:start`](https://metaloom.github.io/testdatabase-provider/start-mojo.html)    | Create config and start containers               | initialize              |
| [`testdb:pool`](https://metaloom.github.io/testdatabase-provider/pool-mojo.html)      | Create new testdatabase pools                    | process-test-classes    |
| [`testdb:stop`](https://metaloom.github.io/testdatabase-provider/stop-mojo.html)      | Stop and destroy containers                      | post-integration-test   |
| [`testdb:clean`](https://metaloom.github.io/testdatabase-provider/clean-mojo.html)    | Stop and destroy containers                      | pre-clean               |


### Usage

The lifecyle order in this example:

* **pre-clean** - Stopping of any still running containers
* **initialize** - Startup of postgresql + provider container
* **generate-sources** - Flyway setup of database
* **process-test-classes** - Setup of a testdatabase pool
* **prepare-package** - Removal of started containers

Maven Commands:

```bash
# Start the containers using the test execution settings from the pom.xml
mvn testdb:start@start

# Setup the configured pool@pool using the pool execution settings from the pom.xml
mvn testdb:pool@pool

# Stop the containers
mvn testdb:stop
```

## [Minimal Example](examples/minimal)

This example shows how to use the provider with its bare minimum default configuration.

Example configuration:

```xml
<plugin>
  <groupId>io.metaloom.maven</groupId>
  <artifactId>testdb-maven-plugin</artifactId>
  <executions>
    <execution>
      <id>cleanup</id>
      <goals>
        <goal>clean</goal>
      </goals>
    </execution>
    <!-- Startup a postgreSQL container and the provider daemon -->
    <execution>
      <?m2e ignore?>
      <id>setup</id>
      <goals>
        <goal>start</goal>
      </goals>
    </execution>
    <!-- Setup a new testdatabase pool now that flyway has setup the
    tables -->
    <execution>
      <?m2e ignore?>
      <id>pool</id>
      <phase>process-test-classes</phase>
      <goals>
        <goal>pool</goal>
      </goals>
      <configuration>
        <pools>
          <pool>
            <id>dummy</id>
            <templateName>postgres</templateName>
            <limits>
              <minimum>10</minimum>
              <maximum>30</maximum>
              <increment>5</increment>
            </limits>
          </pool>
        </pools>
      </configuration>
    </execution>
    <execution>
      <id>stop</id>
      <goals>
        <goal>stop</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

## [Complex Example](examples/complex)

This example shows how to use the testdatabase provider in a multi-module maven project.

## [Dedicated Example](examples/dedicated)

This example demonstrates how the testprovider can be used without automatically starting containers. Instead an external running PostgreSQL and testcontainer server will be queried to provide the pooled testdatabases.

```bash
# Startup the standalone database + provider
cd examples/dedicated
docker-compose  up -d

# Run tests - a pool will be configured and tests executed
mvn clean package
```

The `start` goal of the plugin will create the `target/testdatabase-provider.json` file with the provided settings and enable the JUnit plugin to communicate with the provider server.

The `configuration` section contains thus all needed information to setup the testdatabase pool.

```xml
<configuration>
  <provider>
    <startContainer>false</startContainer>
    <host>localhost</host>
    <port>7543</port>
  </provider>
  <postgresql>
    <startContainer>false</startContainer>
    <username>sa</username>
    <password>sa</password>
    <database>test</database>
    <host>localhost</host>
    <port>15432</port>
    <internalHost>postgresql</internalHost>
    <internalPort>5432</internalPort>
  </postgresql>
</configuration>
```

The use of this setup may also be suitable when running tests in a test environment which can be configured to allow access to additional containerized services (e.g. Jenkins CI Worker using `PodTemplate` in a K8S worker setup).

## Pitfalls

The `reuseContainers` setting will ensure that the started containers are not being removed once the maven process terminates. This is especially useful when providing test databases for your IDE test execution.

This feature requires the file `~/.testcontainers.properties` to contain the `testcontainers.reuse.enable` setting. See [reusable containers](https://www.testcontainers.org/features/reuse/) for more information.

.testcontainers.properties
```bash
testcontainers.reuse.enable=true
```

## Standalone

The provider server container can also be setup as a standlone container.

```bash
docker run --rm \
  metaloom/testdatabase-provider:0.1.2-SNAPSHOT
```

## Provider Server Environment variables

Various variables may be specified during startup that reference the testdatabase being used. When the `TESTDATABASE_PROVIDER_POOL_TEMPLATE_NAME` variable has been specified a `default` database pool will be setup during startup of the provider container. Please note that this is not mandatory as the REST API can be called after startup to CRUD new test database pools.

* `TESTDATABASE_PROVIDER_DATABASE_HOST` -  Host setting which will be passed along to tests that requested a database.
* `TESTDATABASE_PROVIDER_DATABASE_PORT` - Port setting which will be passed along to tests that requested a database.
* `TESTDATABASE_PROVIDER_DATABASE_INTERNAL_HOST` - Host setting which will be used by the provider server to manage the pooled databases.
* `TESTDATABASE_PROVIDER_DATABASE_INTERNAL_PORT` - Port setting which will be used by the provider server to managed the pooled databases.
* `TESTDATABASE_PROVIDER_DATABASE_USERNAME` - Connection details for database.
* `TESTDATABASE_PROVIDER_DATABASE_PASSWORD` - Connection details for database.
* `TESTDATABASE_PROVIDER_DATABASE_DBNAME_KEY` - Name of the database which will be selected when invoking admin operations (e.g. DROP DATABASE, CREATE DATABASE). This DB will not be used as a template for new databases.
* `TESTDATABASE_PROVIDER_POOL_TEMPLATE_NAME` - Name of the database which should be used by the `default` pool. When omitted no `default` pool will be created.

* `TESTDATABASE_PROVIDER_POOL_MINIMUM` - Default minimum for newly created pools.
* `TESTDATABASE_PROVIDER_POOL_MAXIMUM` - Default maximum for newly created pools.
* `TESTDATABASE_PROVIDER_POOL_INCREMENT` - Default increment for newly created pools. The server will create new databases whenever the minium threshold is hot. The increment setting can be used to create multiple new databases in one step.

## Test - JUnit 5


```xml
<dependency>
  <groupId>io.metaloom.test</groupId>
  <artifactId>testdatabase-provider-junit5</artifactId>
  <version>0.1.2-SNAPSHOT</version>
  <scope>test</scope>
</dependency>
```

A test can acquire a database from the pool via the `DatabaseProviderExtension` extension.

```java
@RegisterExtension
public static ProviderExtension ext = ProviderExtension.create("dummy");

@Test
public void testDB() throws Exception {
	System.out.println(ext.db());
}
```

## Test - JUnit 4 

```xml
<dependency>
  <groupId>io.metaloom.test</groupId>
  <artifactId>testdatabase-provider-junit4</artifactId>
  <version>0.1.2-SNAPSHOT</version>
  <scope>test</scope>
</dependency>
```

With JUnit 4 the pool can be queried using the `DatabaseProviderRule` test rule.

```java
@Rule
public DatabaseProviderRule provider = DatabaseProviderRule.create("dummy");

@Test
public void testDB() throws Exception {
	System.out.println(provider.db());
}
```

## Releasing 

```bash
# Update version
mvn versions:set -DgenerateBackupPoms=false

# Update readme
mvn clean

# Invoke release
mvn clean deploy -Drelease

# Update gh-pages branch
mvn clean site
cd ../testdatabase-provider-gh-pages
./update.sh
# Commit + push
```
