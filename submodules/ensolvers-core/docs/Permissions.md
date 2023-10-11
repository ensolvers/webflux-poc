# Permissions

App permissions are customisable and represented in the DB by the following entity class: 

```java
/**
 * Represents a specific permission that is related to a user through the {@link ObjectAccess} entity The permission is
 * identified using a unique universal id (UUID). For example: 'Administration:Users:Delete'. Category and subCategory
 * properties are used to classify, group and order permissions. These are used in the ui that is shown to the user
 * (along with the name and description), on the backend side the uuid is used
 */
@Table(name = "core_permission")
public class Permission extends AuditableModel<ObjectAccess> {
    private String category;
    private String subCategory;
    private String name;
    private String description;
    private String uuid;
```

To give permissions to a user on a specific object we use the following class:

```java
@Entity
@Getter
@Setter
@Table(name = "core_object_access")
public class ObjectAccess extends AuditableModel<ObjectAccess> {
    private String objectExternalId;
    private Long objectInternalId;
    private String objectType;
    /**
     * @deprecated Use permission instead
     */
    @Deprecated(since = "2022-10-04")
    private AccessType accessType;

    @OneToOne
    @JoinColumn(name = "permission_id")
    private Permission permission;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
```

Note that accessType is deprecated and should not be used.

So, you should specify the user, the permission and the object.

To specify the object you can use the internalId or the externalId depending on whether it is public or not.
Also, the objectType (class name of the object) should be specified. We do it because the externalId / objectInternalId are not unique by themselves.

## Some examples of permission management
You can use endpoints listed en PermissionController (enable his usage adding property `core.controllers.permission.enabled=true` to app.properties)

```java
// Create a permission
permissionService.save(
        new Permission(
                "App", 
                "Release", 
                "View app releases", 
                "The user can view app releases (read only)", 
                "App:Release:View"
        )
)
```

```java
// Find permissions by categories
List<Permission> permissions = permissionService.findAllByCategories(List.of("App", "Purchase"));
```

```java
// Find permissions by uuid
List<Permission> permissions = permissionService.getByUUIDs(List.of("App:Release:View"));
```

```java
//Grant access to public object
objectAccessService.grantAccessToPublicObject(<User>, <Object>, <List<Permissions>>);
```

```java
// Retrieve list of object permissions of the userId
List<ObjectAccess> acl = this.objectAccessService.getAccessControlListOfPublicObject(<userId>, <PublicModel>);
```

```java
// Check if permission is present
boolean permissionPresent = objectAccessService.permissionIsPresent(<userId>, <objectExternalId>, <Object.Class>, <permissionUuid>);
```