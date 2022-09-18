# NSS - RoomRes

Semestral project created by Matouš Najman, Vojtěch Luňák, Pavel Sušický.

# 1. Installation

# 1.1 Prerequisites

- **[docker](https://www.docker.com/get-started/)** installed
- **[docker-compose](https://docs.docker.com/compose/install/)** installed (it should be automatically installed
  with `docker for desktop`)
- **java sdk** installed
- **IntelliJ IDEA** installed

# 1.2 Environment variables

See `.env.example` file. Create `.env` from it.

| VARIABLE_NAME  | Required | Description |  Example |
| ------------- | ------------- | ------------- | ------------- |
| LOG_DIRECTORY | Yes | Absolute path to local folder, where logs will be stored and pulled by Logstash. | C:/Projects/nss/logs/ or unix-like equivalent
| KAFKA_BROKER_COUNT | No | Used by docker for Kafka scaling, currently unused variable. | 1 |


# 1.3 Installation

Open the project in Intellij IDEA and hope that it will do all its magic, and you will see that everything is working.

# 2. Applications

| Service name  | Port |
| ------------- | ------------- |
| Reservation Service | 8081 |
| User Service | 8083 |
| Room Service | 8082 |
| API Gateway | 9999 |

# 2.1 Technologies used
- [x] Choose ideal technology and programming language => **SpringBoot + Java**
- [x] **Readme** file in git with instructions and description
- [x] Common DB => **PostgreSQL**
- [x] Cache usage => **Hazelcast cluster cache** (Reservation Service)
- [x] Messaging principle used => **Kafka**
- [x] Security => **JWT authentication** (User Service)
- [x] Interceptors => **GlobalInterceptor** (ApiGateway), **AuthTokenFilter** (Reservation and Room services in package security - Jwt check)
- [x] **REST**
- [x] Production deployment on **Heroku** - standalone User microservice on https://nss-reservation-system.herokuapp.com (branch heroku_deploy)
- [x] Choose ideal architecture => **Microservices**
- [ ] Installation and usage instructions => **Readme**
- [x] Usage of elasticsearch => **ELK stack implemented**
- [x] At least 5 design patterns =>
  - **Facade** - ReservationController has more services to aggregate data effectively.
  - **Interceptor** - see Interceptors checkbox above
  - **Builder** - Multiple Spring configs creations are done by builder pattern
  - **Factory** - Kafka consumer and producer defines Factory for creating specific producers and consumers
  - **Dependency Injection** - SpringBoot controlled autowiring with annotations (@Autowired).
  - **DAO** - In all microservices in dao packages.

# 3. Resources used

- [Multi module project bootstrapping](https://www.baeldung.com/maven-multi-module)
