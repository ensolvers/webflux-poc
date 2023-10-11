# Base entities

Part of Core provides various models to use and base our entities on, some of these are

## [`GenericModel`](../../modules/ensolvers-core-common/src/main/java/com/ensolvers/core/common/model/GenericModel.java) 

 serves as a foundational model that sets a standard for other entities to follow. It defines a set of characteristics that all entities will have in common, which will then be stored in our database. This promotes consistency and coherence across different types of entities.

## [`PublicModel`](../../modules/ensolvers-core-common/src/main/java/com/ensolvers/core/common/model/PublicModel.java)

PublicModel extends GenericModel and includes an externalId that is based on UUID. This enables it to be uniquely identified externally, irrespective of the underlying internal implementation of the entity that we are creating.


## [`AuditableModel`](../../modules/ensolvers-core-common/src/main/java/com/ensolvers/core/common/model/AuditableModel.java)

By extending PublicModel, AuditableModel provides the ability to audit our class, meaning that each time a specific entity is created or updated, its corresponding creation or update timestamp will also be updated. Additionally, AuditableModel enables us to exercise control over whether or not and entity can be updated.


## Next: [Database Migrations](./Database_Migrations.md)
