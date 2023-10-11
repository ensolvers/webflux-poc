# Properties and secrets management

Core offers a general way of dealing with environment properties via the class [`EnvironmentPropertiesService`](../modules/ensolvers-core-common/src/main/java/com/ensolvers/core/common/configuration/EnvironmentPropertiesService.java). In this class, common ways of accessing different kind
of properties are provided.

First of all, two big different types of properties are supported
- **non sensitive:**  they can be seen by any developer or team member, so it is safe to place them in the `application.properties` file directly.
- **sensitive:** those that need to be preserved as hidden as possible and accessed only by a subset of team members. Those properties need to be stored securely (Core uses AWS Secret Manager for that). Locally, in general those properties are mocked, but in production, an ARN that point to application.properties file in local environment and from AWS Secret Manager in the rest of the environments.

## General guidelines
It's a good practice to create a subclass from [`EnvironmentPropertiesService`](../modules/ensolvers-core-common/src/main/java/com/ensolvers/core/common/configuration/EnvironmentPropertiesService.java) to retrieve properties from your Application.

## Adding new non-sensitive properties

When adding a new property you must add it in 2 property files:
  - `modules/ensolvers-core-common/src/main/resources/application.properties`.
  - `modules/ensolvers-core-backend-api/src/test/resources/application.properties`.

To retrieve information there are defined methods that allow to extract properties and cast it to different types (like boolean). Just
look at the `property` and similar methods

Examples of non-sensitive properties are 
  - A public URL that an integration might use
  - Attribute to enable/disable a specific functionality

## Adding sensitive properties

Sensitive properties cannot be stored in application.properties files directly due to their nature. However, we can store mock or sandbox values there to simplify the development process.

All sensitive properties need to be accessed via `sensitiveProperty` method which, if no secret manager configuration has been provided
just defaults to `property`. 

By default, we use AWS Secret Manager as our de-facto secret manager. If a AWS Secret Manager is provided via the `aws.secret.manager.secret.arn` property
(the ARN of the Secret needs to be provided), [`EnvironmentPropertiesService`](../modules/ensolvers-core-common/src/main/java/com/ensolvers/core/common/configuration/EnvironmentPropertiesService.java) will
obtain all properties required via `sensitiveProperty` through it.

Examples of sensitive information can be found on [`EnvironmentPropertiesService`](../modules/ensolvers-core-common/src/main/java/com/ensolvers/core/common/configuration/EnvironmentPropertiesService.java) itself, including:
- Google Client Id
- Google Client Secret
- Google Map Client Secret

## Dynamic properties

Core gives the possibility to store and retrieve properties in the DB.

There is a service to manage it: [`DynamicPropertiesService`](../modules/ensolvers-core-common/src/main/java/com/ensolvers/core/common/services/DynamicPropertyService.java)

The information is saved in the table `core_dynamic_table` and there are several methods to create/retrieve properties directly provided 
by this service.

