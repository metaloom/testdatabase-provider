# Test Database Provider - ${project.version}

This project provides tools to quickly allocate test databases for Java based projects.
Depending on the test database size and complexity it may be much faster to not have to prepare a new database for every testcase.

The goal of this project is to provide throwaway databases for unit tests. A test can modify the database and no cleanup actions (Recreate DB, Truncate Tables, Setup testfixture) needs to be performed.
Instead the provider can be queried to allocate a fresh database.

A dedicated provider daemon will constantly maintain a specified level of free databases. Databases which have been consumed by tests will be dropped and fresh ones will be created.

## Supported Databases

> Currently **only** PostgreSQL is supported.

## Recommendation

This testdb provider is aims to solve test performance issues in larger projects. For smaller projects it makes more sense to just use a testcontainer to provide a database for your tests. It is easier to setup and less complicated. For larger projects it may be suitable to setup a tmpfs backed database that can very quickly allocate database copies.

## Maven

[Plugin Documentation](https://metaloom.github.io/testdatabase-provider/)

The dedicated `testdb-maven-plugin` can be used to startup a postgreSQL and provider server container. The provider server can be queried by tests to provide a database.

```xml
<plugin>
    <groupId>io.metaloom.maven</groupId>
    <artifactId>testdb-maven-plugin</artifactId>
    <version>${project.version}</version>
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
%{snippet|id=plugin-section|file=./examples/minimal/pom.xml}
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
%{snippet|id=config-section|file=./examples/dedicated/pom.xml}
```

The use of this setup may also be suitable when running tests in a test environment which can be configured to allow access to additional containerized services (e.g. Jenkins CI Worker using `PodTemplate` in a K8S worker setup).

## [Dedicated (Without Maven Plugin) Example](examples/dedicated-no-maven-plugin)

It is also possible to setup a dedicated provider and database and reference the service without the use of the maven plugin.
In this case the provider connection details must be specified in code. The `TestDatabaseProvider#localConfig` method can be used to set a provider configuration which supersedes any configuration file or environment variables.

```java
%{snippet|id=localconfig|file=./examples/dedicated-no-maven-plugin/src/test/java/io/metaloom/example/PoolSetupAction.java}
```

Additionally the JUnit rules/extensions can reference the server as well.

```java
// JUnit 4
%{snippet|id=provider|file=./examples/dedicated-no-maven-plugin/src/test/java/io/metaloom/example/ExampleJunit4Test.java}

// JUnit 5
%{snippet|id=provider|file=./examples/dedicated-no-maven-plugin/src/test/java/io/metaloom/example/ExampleJunit5Test.java}
```

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
  metaloom/testdatabase-provider:${project.version}
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
  <version>${project.version}</version>
  <scope>test</scope>
</dependency>
```

A test can acquire a database from the pool via the `DatabaseProviderExtension` extension.

```java
%{snippet|id=test_snippet|file=./examples/minimal/src/test/java/io/metaloom/example/ExampleJunit5Test.java}
```

## Test - JUnit 4 

```xml
<dependency>
  <groupId>io.metaloom.test</groupId>
  <artifactId>testdatabase-provider-junit4</artifactId>
  <version>${project.version}</version>
  <scope>test</scope>
</dependency>
```

With JUnit 4 the pool can be queried using the `DatabaseProviderRule` test rule.

```java
%{snippet|id=test_snippet|file=./examples/minimal/src/test/java/io/metaloom/example/ExampleJunit4Test.java}
```

## Database Changes

Database Pools can be updated/recreated by updating the pool definition via REST. The `TestDatabaseProvider` provides easy access to common tasks like re-creating a database in order to update the template for the pool.

*examples/minimal/src/test/java/io/metaloom/example/PoolSetupAction.java*
```java
%{snippet|id=pool_setup|file=./examples/minimal/src/test/java/io/metaloom/example/PoolSetupAction.java}
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
