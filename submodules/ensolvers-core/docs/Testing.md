# Creating REST CRUDs (WIP)

This section describes how to quickly build test cases for existing Services and other parts of the framework

## Getting CRUD tests just by extending a class

In order to automatically get CRUD tests for an existing CrudService, we just need to create
1. A new test class extending `AbstractCrudServiceTest<ServiceClass, ModelClass>`
2. Add the following annotations to ensure that the app context for the test is loaded properly, including any app-based
   context (in the example `CustomAppContext.class`)
```java
@ContextConfiguration(classes = { CustomAppContext.class, DevTestConfig.class })
@SpringBootTest
```

This will inherit tests for all generic CRUD methods - for more info check [AbstractCrudServiceTest](../modules/ensolvers-core-backend-api/src/test/java/com/ensolvers/core/common/services/AbstractCrudServiceTest.java)


## Running tests by using Testcontainers

When running tests that depend on other stack elements like caches, DBs, etc. we need to have everything ready so the
tests will not fails - or mock the integrations, which is time-comsuming.

One solution is to use [Testcontainers](https://www.testcontainers.org/) for that, which run all the dependencies via containers. 
Ensolvers Core includes support for several Testcontainers and takes care of replacing environment variables to point
to them, thus isolating from the running environment. This simplifies, for instance, running tests as a part of the development
pipelines. 

Below there is a list of properties that, if provided, will enable Testcontainers for setting up dependencies

```
# Starts a MySQL container and replaces DB_HOST, DB_NAME, and other properties required
testcontainers.db.engine=mysql

# Starts a Redis container, replacing the `redis.uri` property
testcontainers.redis.enabled=true
```

For more details, check the Testcontainer [folder/package](../modules/ensolvers-core-common/target/classes/com/ensolvers/core/common/configuration/testcontainers) 