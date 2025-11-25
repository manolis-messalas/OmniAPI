
docker-compose up -d
 
 ./mvnw clean install

 mvn spring-boot:run -Ph2
 mvn spring-boot:run -Ppostgres
