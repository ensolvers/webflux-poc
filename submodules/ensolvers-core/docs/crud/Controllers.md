## Controllers


Much in the same vein as before, we should base our controllers from examples that already exist in our project.

For CRUDs in particular we have two base clases:

* [`CrudControllerV2`](../../modules/ensolvers-core-backend-api/src/main/java/com/ensolvers/core/api/controller/CrudControllerV2.java), which is the one you want to use.
* [`CrudController`](../../modules/ensolvers-core-backend-api/src/main/java/com/ensolvers/core/api/controller/CrudController.java), which **is deprecated**, although, still used in some places.

First of all, we need to create a a DTO to comunicate the relevant/important data.

You should just use whatever your IDE provides for creating this, but most importantly, you must extend from [`PublicDTO`](../../modules/ensolvers-core-common/src/main/java/com/ensolvers/core/common/model/PublicDTO.java) which extends from [`DTO`](../../modules/ensolvers-core-common/src/main/java/com/ensolvers/core/common/model/DTO.java).

A proper explanation of this is in [`DTOs`](../DTOs.md) in the base documentation.

So, we'll just create the DTO here:

```java
@Getter
@Setter
public class ClientDTO extends PublicDTO<Client> {
    private String name;
    private String address1;
    private String address2;
    private String city;
    private String idDocument;
    private ClientType type;
}
```

Once done, we can now go an create the Controller itself

```java
@Controller
@RequestMapping("/api/v1/client")
public class ClientController extends CrudControllerV2<Client, ClientDTO, ClientService> {
    @Override
    protected Class<Client> getModelClass() {
        return Client.class;
    }

    @Override
    protected Class<ClientDTO> getDTOClass() {
        return ClientDTO.class;
    }
}
```

The same explanation for the need to create these overrides here as in [`Services`](./Services.md)

And that is it, you may now start adding the required functionality to your controller (and other layers).


## Next: [`Custom Filters`](./Custom_Filters.md)