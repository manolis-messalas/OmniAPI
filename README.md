
docker-compose up -d
 
 ./mvnw clean install

 #mvn spring-boot:run -Ph2
 #mvn spring-boot:run -Ppostgres

 ./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=h2"
 ./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=postgres"
 ./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=sqlite"
