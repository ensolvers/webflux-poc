# Performance Tests for the WebFlux POC

## getById Test
```shell
mvn gatling:test \
  -Dgatling.simulationClass='webflux.WebFluxGetByIdSimulation' \
  -Denv='DEV' -Dusers=1000 -DrampUsersDuration=10
```

## getAll Test
The film table has 1000 records on it but I'm paging it (size = 50).
```shell
mvn gatling:test \
  -Dgatling.simulationClass='webflux.WebFluxGetAllSimulation' \
  -Denv='DEV' -Dusers=50 -DrampUsersDuration=5
```
