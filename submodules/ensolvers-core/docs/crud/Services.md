# Services

Now that we have a Repository, we come to the Services layer.

> Service is a type of domain object used to encapsulate domain logic that doesn't fit neatly into any entity or value object.   
<br>Services can act as coordinators, orchestrating interactions between entities or value objects to achieve a specific goal, such as enforcing business rules, coordinating transactions, or handling external integrations.  
<br>They allow developers to model complex, domain-specific behavior in a way that is both expressive and maintainable.

<sub> from _Domain-Driven Design: Tackling Complexity in the Heart of Software_ by Eric Evans

Much like [`Repositories`](./Repositories.md), Core provides two generic interfaces for services, made specifically for CRUD usage:


* [`CrudService`](../../modules/ensolvers-core-common/src/main/java/com/ensolvers/core/common/services/CrudService.java), for usage in conjunction of `GenericRepository` and those who inherit from it.
* [`PublicObjectCrudService`](../../modules/ensolvers-core-common/src/main/java/com/ensolvers/core/common/services/PublicObjectCrudService.java), which as the name implies, for usage in cojunction of `PublicObjectRepository`.

For usage with our implementation of `ClientRepository`, we will use `PublicObjectCrudService` as the base class:


```java
public class ClientService extends PublicObjectCrudService<Client, ClientRepository> {
    public ClientService(ClientRepository repository) {
        super(repository);
    }

    @Override
    protected void updateData(Client existingObject, Client updatedObject) {
        existingObject.setName(updatedObject.getName());
        existingObject.setAddress1(updatedObject.getAddress1());
        existingObject.setAddress2(updatedObject.getType());
        /* and so on... */
    }

    @Override
    protected Class<Client> getObjectClass() {
        return Client.class;
    }
}
```

The existence of these methods that we **must** implement is because:
* getObjectClass(): because of the way Java handles parametric objects we need to make explicit which object we are handling with this class, and also because of the way we convert them to DTOs in our controllers.
* updateData(): this is used so that we specify the way we handle the saving of already existing objects, we want to have absolute control of how we handle saving, since we cannot reliably predict how JPA will save our entities.


## Next: [`Testing`](./Testing.md)