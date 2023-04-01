# Dedicated Example

This example shows how the needed database and provider services can be setup in a dedicated way which is not managed by the `testdb-maven-plugin`. This way one DB + daemon can be setup to be utilized by multiple projects or in a different hosting environment.

The `testdb-maven-plugin` will in this case only be used to provide the needed configuration for the unit tests. It will write the configuration file in the `target` folder.

Additionally it can be used to setup the test database pools.
