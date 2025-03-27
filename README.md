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
sudo docker run --name socio --network my-network -e DB_HOST=mysql_container -p 8080:8080 socio:0.0.1

# spring.profiles.active=local # in application.properties and that's the default file
# else for docker can change to production to use application-production.properties 
# instead of application-local.properties that uses mySQL, but ensure postgres is running 
# 
# Database connection properties
# if using application-local.properties, ensure DB_HOST=mysql_container 
# DB_HOST=localhost for local testing, for docker use DB_HOST=mysql_container, provided both containers on same host network

```
# SOCIO - Social Network Project

## Project Overview
**SOCIO** is a Java-based social networking application with a MySQL backend, featuring a robust authentication system, user profiles, posts, comments, likes, friend requests, and real-time messaging. The system will include a RESTful API with Spring Boot and integrate Spring Security for authentication.

## Technology Stack
- **Backend:** Java 17, Spring Boot, Spring Security, Spring Data JPA
- **Database:** MySQL
- **Frontend:** React.js (or Angular)
- **Real-time Communication:** WebSockets with Spring Boot
- **Authentication:** JWT (JSON Web Token)
- **Storage:** Amazon S3 for media uploads
- **Logging & Monitoring:** ELK Stack (Elasticsearch, Logstash, Kibana)
- **CI/CD:** Docker, Jenkins, Kubernetes

## ER Diagram
(Placeholder: To be designed and refined based on requirements)

## Features
### 1. **User Authentication & Authorization**
   - Registration & Login
   - JWT-based authentication
   - Role-based access control (User, Admin, Moderator)
   
### 2. **User Profile Management**
   - Update profile details (bio, profile picture, contact info)
   - Privacy settings
   
### 3. **Posts & Media Uploads**
   - Text, images, videos support
   - Amazon S3 integration for media storage
   
### 4. **Like, Comment, and Share System**
   - Users can like, comment on, and share posts
   
### 5. **Friend Requests & Follow System**
   - Users can send, accept, or decline friend requests
   - Follow/unfollow feature
   
### 6. **Real-time Chat & Notifications**
   - WebSockets for live chat
   - Notification system for likes, comments, messages
   
### 7. **Admin & Moderation Panel**
   - User management
   - Content moderation (reporting system)
   
### 8. **Search & Discover**
   - Search users, posts, and hashtags
   - Trending topics

## API Endpoints (Draft)
### **Authentication**
- `POST /auth/register` - User registration
- `POST /auth/login` - User login
- `POST /auth/logout` - User logout

### **User Management**
- `GET /users/{id}` - Get user profile
- `PUT /users/{id}` - Update user profile

### **Posts**
- `POST /posts` - Create a post
- `GET /posts/{id}` - Retrieve a post
- `DELETE /posts/{id}` - Delete a post

### **Friend Requests**
- `POST /friends/request/{id}` - Send friend request
- `POST /friends/accept/{id}` - Accept friend request
- `DELETE /friends/reject/{id}` - Reject friend request

### **Messages (WebSocket-based)**
- `POST /messages/send` - Send a message
- `GET /messages/conversation/{id}` - Fetch conversation

## Milestones
1. **Milestone 1:** Spring Boot Setup, MySQL Configuration, ERD
2. **Milestone 2:** JWT Authentication, Role-Based Security
3. **Milestone 3:** User Profiles, CRUD Operations
4. **Milestone 4:** Post Management, Media Uploads (S3 Integration)
5. **Milestone 5:** Friend Requests & Follow System
6. **Milestone 6:** Real-time Chat & Notifications (WebSockets)
7. **Milestone 7:** Admin Panel & Content Moderation
8. **Milestone 8:** Frontend Integration & Testing
9. **Milestone 9:** Deployment (Docker, Kubernetes, CI/CD)

## Next Steps
- Implement the basic Spring Boot structure with JWT Authentication
- Design & refine ER Diagram
- Develop API endpoints based on the above plan
- Integrate real-time WebSocket communication

## Deliverables
- Full source code with README instructions
- Database schema
- API documentation (Swagger)
- CI/CD pipeline configuration

This document serves as a blueprint for the SOCIO project, ensuring smooth development and integration at each milestone.

