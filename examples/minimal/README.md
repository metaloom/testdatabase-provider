# Minimal Example

The minimal example only contains the bare minimum maven configuration.

The maven configuration does:

1. Clean the environment from previously running instances
2. Startup the needed db + provider containers
3. Invoke flyway to run the db migration
4. Setup the testdb pool for the prepared database
5. Run the tests
6. Stop the containers

This example also shows how a pooled database can be updated via the dedicated `PoolSetupAction` class. It recreates the db and runs flyway to setup the tables. Afterwards it re-created the testdatabase pool.
