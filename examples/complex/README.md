# Complex Example

This example shows how to use the `testdb-maven-plugin` in a multi-module maven build.

It assumes two sub modules (`moduleA`, `moduleB`)

## moduleA

This module contains the flyway migration resources. It prepares the provided database and initializes the testdb pool. Once the pool has been setup the provider daemon can hand out databases for tests.

## moduleB

This module contains the tests for the project which make use of the created pool.


## Parent

The parent project `testdatabase-provider-complex-example` contains the invocation of the `start` goal in order to setup the database container so that the flyway container can setup the db. The startup of the containers should be located in the parent module for all modules that want to access the database. Otherwise the configuration can't be located by tests. To prevent `start` from being invoked in submodules it is important to add the `<inherited>false</inherited>` tag to the execution.