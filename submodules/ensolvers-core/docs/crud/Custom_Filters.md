## Custom Filters


A very basic need we may find very early on is the need to find a specific instance in our database based on data we've received from our Controller.

In a very simple example, we are receiving a `idDocument` to look for.

You may recall we already put something like this in our [Repository](./Repositories.md), a method called `findByIdDocument` which works for us perfectly here, because of how Spring works, we do not need a implementation for this class as it will be given by Spring for us based on the method's name.

So, on our service class we'll give a simple implementation for our service layer method, which we'll call `findPageForIdDocument`:

```java
public class ClientService extends PublicObjectCrudService<Client, ClientRepository> {
    /* Earlier definition of service omitted for brevity */

    Page<Client> findPageForIdDocument(String idDocument, Integer page, Integer pageSize) {
        return repository.findByEventId(idDocument, PageRequest.of(page, pageSize));
    }
}
```

And then the corresponding method for our controller layer, of the same name as before, `findPageForIdDocument`:

```java
public class ClientController extends CrudControllerV2<Client, ClientDTO, ClientService> {
    /* Earlier definition of controller omitted for brevity */
    @PreAuthorize("hasAnyRole('ROLE_SUPERADMIN')")
    @GetMapping
    public ResponseEntity<ApiResultDTO<Page<ClientDTO>>> read(
        @RequestParam(defaultValue = "0") Integer page, 
        @RequestParam(defaultValue = "10") Integer pageSize,
        @RequestParam String idDocument) {
            return this.pageResult(this.service.findPageForIdDocument(idDocument, page, pageSize));
    }
}
```

In here, [ApiResult](../../modules/ensolvers-core-common/src/main/java/com/ensolvers/core/common/dto/ApiResultDTO.java) will generalize our paged result that we have obtained from our service.

And that is it, we have our custom filter, where we'll filter by our Client's id document.