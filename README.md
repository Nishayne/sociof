# HuSparkMasterAssigment

### Imp: on linux prefix docker with sudo since docker needs a special ac to run so instead sudo is better

```bash
docker ps  # Check running containers
docker start mysql_container  # Start MySQL if stopped
OR
docker run --name mysql_container -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=socio -p 3306:3306 -d mysql:8

./mvnw clean package
OR
mvn clean install
```
