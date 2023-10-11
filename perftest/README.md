# Performance Tests for the WebFlux POC

## getById Test
```shell
mvn gatling:test \
  -Dgatling.simulationClass='webflux.WebFluxGetByIdSimulation' \
  -Denv='DEV' -Dusers=5000 -Diterations=100 -DrampUsersDuration=10
```

## getById cached with sleep
```shell
mvn gatling:test \
  -Dgatling.simulationClass='webflux.WebFluxGetByIdSimulationWithSleep' \
  -Denv='DEV' -Dusers=5000 -Diterations=500 -DrampUsersDuration=10
```