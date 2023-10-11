# Repositories

Since we have both our entity and our table structure, we can proceed to our Repository.

> A Repository is a collection-like object that provides more advanced querying capabilities. It manages objects of a specific type, adding and removing them as necessary from the database.  
<br>Clients use query methods to request objects from the Repository based on specific criteria, such as attribute values.  
<br>The Repository handles the querying and metadata mapping, and can implement a range of queries to meet client requirements. 

<sub> from _Domain-Driven Design: Tackling Complexity in the Heart of Software_ by Eric Evans

Core provides generic interfaces for the following Base entities:

* [`GenericRepository`](../../modules/ensolvers-core-common/src/main/java/com/ensolvers/core/common/repositories/GenericRepository.java) for the `GenericModel` class and those who inherit from it.
* [`PublicObjectRepository`](../../modules/ensolvers-core-common/src/main/java/com/ensolvers/core/common/repositories/PublicObjectRepository.java) for the `PublicModel` class and those who inherit from it.


These implement some of the generic queries/methods that we will use most often when working with entities that inherit from the Base entities. You may use `GenericRepository` on `PublicModel` entities, but you cannot do the opposite, since `GenericModel` lacks the required attributes for `PublicObjectRepository`.

**Note**: For `AuditableModel`, you can use either `GenericRepository` or `PublicObjectRepository`.


As an example of a basic repository, we'll use the [`Client`](./README.md#code-considerations) class we defined before.


```java
public interface ClientRepository extends PublicObjectRepository<Client, Long> {
    Page<Client> findByName(String name, PageRequest of);
    Page<Client> findByAddress1(String address1, PageRequest of);
    Page<Cleint> findByType(ClientType type, PageRequest of);
    Page<Client> findByIdDocument(String idDocument, PageRequest of);
    /* and so on...*/
}
```

Here there is some magic implemented by Spring Data which allow us to specify what are we querying for just by looking at the method name, of course, if required, we may use the `@Query` annotation for querying for more specific values. See [Spring Data Documentation on @Query](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods.at-query) for more info

We don't really need to explain much else since this is simply a interface that exposes our database to upper layers. 

## Next: [`Services`](./Services.md)