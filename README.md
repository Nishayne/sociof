# HuSparkMasterAssigment

### Imp: on linux prefix docker with sudo since docker needs a special ac to run so instead sudo is better

```bash

sudo docker ps  # Check running containers
sudo docker container stop xy123 # stop container xyz123, for example and stop all containers
sudo docker system prune -a
# Create a Docker network for docker containers to communicate
sudo docker network create my-network

# Run the MySQL container
sudo docker run --name mysql_container --network my-network -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=socio -e MYSQL_TCP_BIND_ADDRESS=0.0.0.0 -p 3306:3306 -d mysql:8.0
OR
sudo docker start mysql_container  # Start MySQL if stopped

mvn wrapper:wrapper #download latest maven wrapper
chmod +x mvnw #on linux

./mvnw clean package
OR
mvn clean install

mvn clean compile && mvn spring-boot:run

# run from docker container
sudo docker build -t socio:0.0.1 .
# sudo docker run -p 127.0.0.1:8000:8000 socio:0.0.1
# Run your Java application container
sudo docker run --name socio --network my-network -p 8080:8080 socio:0.0.1

# spring.profiles.active=local # in application.properties and that's the default file
# else for docker can change to production to use application-production.properties 
# instead of application-local.properties that uses mySQL, but ensure postgres is running 
# 
# Database connection properties
# if using application-local.properties, ensure DB_HOST=mysql_container 
# DB_HOST=localhost for local testing, for docker use DB_HOST=mysql_container, provided both containers on same host network

```
