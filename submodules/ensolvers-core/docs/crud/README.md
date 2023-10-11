### Building a CRUD with Core

This section of the documentation is used to demostrate how to build a mid-complexity CRUD from scratch using Ensolvers Core

The aim is to:

* Get familiar with the different components, layers, and classes that make up the framework.

* Learn about best practices and how the framework facilitates their implementation. For example, by providing generic methods and classes for controllers, services, test cases, etc.



Not only that, this initial readme file is also intended to provide general good practices/tips outside the scope of Core itself:

* Always base your solution on existing classes/implementations - use search intensively to navigate the code

* Using the IDE assistance to solve code issues and create methods, classes, etc. helping/speeding up development a lot

* Use search and IDE to find usage examples of the components/classes/methods illustrated here and of other possible approaches

* Provide a brief description of entities and/or methods that you write

* Commits should be brief but descriptive, we can compare entire branches so there is no need to get everything in a single big commit.


## Code considerations

Throughout these documents we'll use an example entity called `Client` which will be defined (initally) as:

<a name="generic-client-class"></a>

```java
@Entity
@Getter
@Setter
public class Client extends PublicModel {
    private String name;
    private String address1;
    private String address2;
    private String city;
    
    @Enumerated(EnumType.STRING)
    private ClientType type;
}
```

And the corresponding `ClientType` enumeration:

```java
public enum ClientType {
    NORMAL,
    PREMIUM,
    VIP,
    OTHER;
}
```

These are not meant to be an exhaustive example of a proper entity that we would use on a project, nor this is an exhaustive example on how to use Core, but instead, we use these to give a general idea of how we can create or adapt a project to use Core.


### Table of contents:

[`Base Entities`](./Base_Entities.md)

[`Database Migrations`](./Database_Migrations.md)

[`Repositories`](./Repositories.md)

[`Services`](./Services.md)

[`Testing`](./Testing.md)

[`Controllers`](./Controllers.md)

[`Custom Filters`](./Custom_Filters.md)