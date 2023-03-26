# Test Database Provider

This project provides tools to quickly allocate test databases for Java based projects.
Depending on the test database size and complexity it may be much faster to not have to prepare a new database for every testcase.

The goal of this project is to provide throwaway databases for unit tests. A test can modify the database and no cleanup actions (Recreate DB, Truncate Tables, Setup testfixture) needs to be performed.
Instead the provider can be queried to allocate a fresh database.

A dedicated provider daemon will constantly maintain a specified level of free databases. Databases which have been consumed by tests will be dropped and fresh ones will be created.


## Maven

The dedicated `postgresql-testdatabase-provider-maven-plugin` can be used to startup a postgreSQL and provider server container. The provider server can be queried by tests to provide a database.

The lifecyle order in this example:

* **initialize** - Startup of postgresql + provider container
* **generate-sources** - Flyway setup of database
* **process-test-classes** - Setup of a testdatabase pool
* **post-integration-test** - Removal of started containers

Maven Commands:

```bash
# Start the containers
mvn testdatabase-provider:start

# Setup the configured pool
mvn testdatabase-provider:pool

# Stop the containers
mvn testdatabase-provider:stop
```

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
    <!-- The flyway plugin uses the properties which have been provided by the provider-maven-plugin. -->
    <url>${maven.provider.db.url}</url>
    <user>${maven.provider.db.username}</user>
    <password>${maven.provider.db.password}</password>
    <locations>
    <location>filesystem:src/main/flyway</location>
    </locations>
</configuration>
<dependencies>
    <dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>${postgres.driver.version}</version>
    </dependency>
</dependencies>
</plugin>

<plugin>
<groupId>io.metaloom.test</groupId>
<artifactId>postgresql-testdatabase-provider-maven-plugin</artifactId>
<version>0.0.1-SNAPSHOT</version>
<executions>
    <!-- Start the provider daemon and needed database containers -->
    <execution>
    <id>start</id>
    <phase>initialize</phase>
    <goals>
        <goal>start</goal>
    </goals>
    </execution>
    <!-- Now flyway has populated the database and we can setup our test database pool -->
    <execution>
    <id>setup-pool</id>
    <phase>process-test-classes</phase>
    <goals>
        <goal>pool</goal>
    </goals>
    <configuration>
        <pools>
        <pool>
            <id>dummy</id>
            <templateName>postgres</templateName>
            <minimum>10</minimum>
            <maximum>20</maximum>
            <increment>5</increment>
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

## Standalone

The provider server container can also be setup as a standlone container.

```bash
docker run --rm \
 --env "TESTDATABASE_PROVIDER_DATABASE_HOST=localhost" \
 --env "TESTDATABASE_PROVIDER_DATABASE_PORT=5432" \
 --env "TESTDATABASE_PROVIDER_DATABASE_USERNAME=sa" \
 --env "TESTDATABASE_PROVIDER_DATABASE_PASSWORD=sa" \
 metaloom/postgresql-testdatabase-provider:0.0.1-SNAPSHOT
```

## Test - Junit 5

A test can acquire a database using the `DatabaseProviderExtension` extension.

```java
@RegisterExtension
static DatabaseProviderExtension provider = new DatabaseProviderExtension();

@Test
public void testDB() {
    System.out.println(provider.db());
}
```
