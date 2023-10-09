# Performance Tests for the WebFlux POC

## getById Test
```shell
mvn gatling:test \
  -Dgatling.simulationClass='webflux.WebFluxGetByIdSimulation' \
  -Denv='DEV' -Dusers=5000 -Diterations=10 -DrampUsersDuration=10
```