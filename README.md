# HuSparkMasterAssigment

docker ps  # Check running containers
docker start mysql_container  # Start MySQL if stopped
OR
docker run --name mysql_container -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=your_db_name -p 3306:3306 -d mysql:8

./mvnw clean package