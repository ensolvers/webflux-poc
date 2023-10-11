## Testing

While we could just go into implementing the controller next, we should actually test most of what we've written so far.

We can just give some hints to our code by using JUnit and some helper classes included in core, namely [`AbstractCrudServiceTest`](modules/ensolvers-core-backend-api/src/test/java/com/ensolvers/core/common/services/AbstractCrudServiceTest.java)


As a sample, we can create a test for the service we have created in [Service](./Services.md), `ClientService`:


```java
@ContextConfiguration(classes = {TestConfig.class})
@SpringBootTest
class ClientServiceTest extends AbstractCrudServiceTest<ClientService, Client> {

    @Override
    protected Client newSample() {
        return new Client("John Doe", "Fake St 123", "", "Sydney", ClientType.NORMAL);
    }

    @Override
    protected Client updateModel(Client model) {
        model.setName("Jane Doe");
        return model;
    }
}
```

A few notes here:

`@ContextConfiguration` will ensure that our Spring context will be set correctly for our test, this will vary depending on if we have more than one possible configuration for our possible context.

We also have 2 methods that we must implement:

* `newSample`: which creates a new instance of the base model entity our service uses that we need to test consistency of.
* `updateModel`: which allow us to update a instance of our base model entity our service uses to ensure that changes made to them are persisted correctly into our database.

Other than that, there isn't any particularity of the way we implement tests, these will be run according to the `@ContextConfiguration` we have given to them as part of our test suite automatically.



## Next: [`Controllers`](./Controllers.md)