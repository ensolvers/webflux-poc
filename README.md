# WebFlux Proof of Concept

You need to have MySQL running with a db called `webflux_poc` after that you need to 
load the database with the data saved on `src/main/resources/db/`:
```shell
gunzip -c db-schema.sql.gz | mysql -u root --protocol=tcp webflux_poc
gunzip -c db-data.sql.gz | mysql -u root --protocol=tcp webflux_poc
```

## Reactive Streams

Dependencies:
 - Springboot webflux
 - R2DBC
 - R2DBC mysql driver

Documentation about reactive streams used for webflux: <br/>
- Project Reactor: https://projectreactor.io/docs/core/release/reference/