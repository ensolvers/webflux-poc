# WebFlux Archetype

Clean project that integrates fox to develop WebFlux backend solutions.

**Reactive Programming is better suited for cases where you have a notable amount of users getting the same content**
(for example a get call to home content), it changes the blocking nature of normal procedural code for non-blocking code. 
The advantages of this are the resources you utilize, instead of having a thread allocated for one task for the entirety 
of it you allocate a small pool that cycles through tasks reacting to events. **You have to be careful when programming 
in a non-blocking manner because any blocking code can cause the whole program to slow-down in a non-blocking context 
like WebFlux**.

**IMPORTANT**: You need to have a MySQL instance running on port 3306 with a database named
`base_project`(by default, can be changed) created.

## Info about Reactive Streams

Documentation about reactive streams used for WebFlux primary two Objects, Mono and Flux: <br/>
- Project Reactor: https://projectreactor.io/docs/core/release/reference/
- Spring WebFlux: https://docs.spring.io/spring-framework/reference/web/webflux.html

## Dependencies
- Springboot WebFlux
- R2DBC
- R2DBC MySQL Driver
