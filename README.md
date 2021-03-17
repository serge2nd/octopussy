# octopussy [![JDK8](https://github.com/serge2nd/octopussy/workflows/JDK8/badge.svg)](https://github.com/serge2nd/octopussy/actions?query=workflow%3A%22JDK8%22) [![JDK11](https://github.com/serge2nd/octopussy/workflows/JDK11/badge.svg)](https://github.com/serge2nd/octopussy/actions?query=workflow%3A%22JDK11%22) [![JDK15](https://github.com/serge2nd/octopussy/workflows/JDK15/badge.svg)](https://github.com/serge2nd/octopussy/actions?query=workflow%3A%22JDK15%22) [![Coverage Status](https://coveralls.io/repos/github/serge2nd/octopussy/badge.svg?branch=develop)](https://coveralls.io/github/serge2nd/octopussy?branch=develop)
**Note:** *This project is not maintained anymore. The further progress is made in [octopussy-v2][10].*

Interfaces and components to interact with arbitrary hot-pluggable data sources (not necessary JDBC/JPA).
In this project context the objects representing these data sources are called *data kits*.

Modules:
- [`octopussy-core`][20]  
  SPI and some basic implementations.  
  See the [`ru.serge2nd.octopussy.spi`][50] package,
  the [`DataKit`][60] class and its descendants for better insight);
  

- [`octopussy-service-webmvc`][30]  
  A REST application exposing endpoints to manage data kits and to query/update via some data kit.
  See the [contracts][70] and the [tests][80] for examples.
  

- [`octopussy-cdc`][40]  
  Shared application contracts such as REST paths and [consumer-driven contracts][90] for tests.
  
## Build
First install the [`octopussy-cdc`][40] package to the local repository:
```
./mvnw -f octopussy-cdc/pom.xml clean install
```

Then you will be able to build all the modules:
```
./mvnw clean package
```

Then you can build the Docker image running the Maven goal
```
./mvnw -f octopussy-service-webmvc/pom.xml dockerfile:build
```
or manually
```
docker build -t octopussy octopussy-service-webmvc/target
```

[10]: https://github.com/serge2nd/octopussy-v2
[20]: ./octopussy-core
[30]: ./octopussy-service-webmvc
[40]: ./octopussy-cdc
[50]: ./octopussy-core/src/main/java/ru/serge2nd/octopussy/spi
[60]: ./octopussy-core/src/main/java/ru/serge2nd/octopussy/spi/DataKit.java
[70]: ./octopussy-cdc/src/test/resources/contracts
[80]: ./octopussy-service-webmvc/src/test/java/ru/serge2nd/octopussy
[90]: https://spring.io/projects/spring-cloud-contract
