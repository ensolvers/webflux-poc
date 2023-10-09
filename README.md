# WebFlux Proof of Concept

**IMPORTANT**: You need to have a MySQL instance running on port 3306 with a database named
`webflux_poc` created, it loads the `core_dynamic_properties` table with one test value.

## Reactive Streams

Dependencies:
 - Springboot webflux
 - R2DBC
 - R2DBC mysql driver

Documentation about reactive streams used for webflux: <br/>
- Project Reactor: https://projectreactor.io/docs/core/release/reference/