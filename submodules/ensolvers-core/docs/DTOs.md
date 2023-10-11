# DTO management and conversion

Ensolvers Core uses Data Transfer Objects ([DTOs](https://martinfowler.com/eaaCatalog/dataTransferObject.html)) to transfer outside and inside Controllers. 

In 80% of the cases, DTOs are very similar to their model counterparts, except that they are more "plain" in the sense
that they MUST avoid deeper relationship with other objects and also sensitive information (like IPs, internal IDs, etc.)

Ensolvers Core already uses ModelMapper to automate Model <> DTO conversions. However, in some cases this conversion might 
need to be tuned with custom code (for instance, by adding computing properties into de DTO and so on)

As a general guide we should use 

```
DTOMapper.get().map(model, DTO.class)
DTOMapper.get().map(dto, Model.class)
```

for converting model objects into DTOs and vice-versa. Ideally, all DTO objects should inherit from `DTO<ModelCounterpart>` so,
if the need of refining post-conversion actions both for Model -> DTO and DTO -> Model can be easily specified. 

For more detail, check [DTOMapperTest](../modules/ensolvers-core-backend-api/src/test/java/com/ensolvers/core/common/dto/utils/DTOMapperTest.java)
in which a simple DTO <> Model conversion (adding/removing a String prefix) is implemented