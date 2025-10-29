# Smart E-Commerce Platform with Chatbot and Recommendation System - Backend

This repository contains **only the backend** of the **Smart E-Commerce Platform with Chatbot and Recommendation System**, an intelligent e-commerce solution built with **Spring Boot**.  
The backend provides all the server-side logic, data processing, and RESTful APIs that power the main platform — enabling secure authentication, product and order management, AI-based recommendations, and an integrated chatbot.

The platform targets the **beauty and cosmetics industry**, delivering personalized shopping experiences powered by machine learning and conversational AI.

---

## Overview

The backend system is built on a **microservices architecture** to ensure modularity, scalability, and maintainability.  
Each service is responsible for a specific business domain (users, products, orders, chatbot, recommendations, etc.), and all communication between services is handled through REST APIs.

---

## Core Features

- Microservices-based architecture for modularity and scalability  
- Secure authentication and authorization with JWT  
- Complete CRUD operations for users, products, orders, and reviews  
- Automated email service for verification, notifications, and order confirmations  
- Hybrid AI recommendation engine (content-based + collaborative filtering)  
- Conversational chatbot for customer support and dynamic interaction  
- RESTful APIs for communication between all services  
- Comprehensive testing using Postman and JUnit  

---

## Backend Modules

- **User Service** – Handles registration, authentication, profile management, and password reset  
- **Product Service** – Manages products, categories, brands, discounts, and stock  
- **Order Service** – Checkout, payment, invoice generation (PDF), and delivery tracking  
- **Review & Reaction Service** – Manages reviews, ratings, reactions (like/dislike), and notifications  
- **Recommendation Service** – Provides personalized product suggestions based on user behavior  
- **Chatbot Service** – Conversational assistant for FAQs and guided shopping  
- **Admin Service** – Administrative operations: user management, order control, product monitoring, and statistics  

---

## Non-Functional Requirements

- **Scalability** – Efficient handling of large datasets and concurrent users  
- **Reliability** – Independent microservices ensure fault tolerance  
- **Security** – JWT-based authentication, encrypted passwords, and role-based access control  
- **Maintainability** – Modular structure and clean separation of concerns (DDD principles)  
- **Testability** – Each endpoint tested with Postman and JUnit  
- **Extensibility** – Easily integrable with external APIs or AI components  

---

## Technologies Used

- Java 17  
- Spring Boot 3  
- Spring Security  
- Spring Data JPA / Hibernate  
- MySQL  
- JWT Authentication  
- Maven  
- Lombok  
- ModelMapper  
- JUnit / Postman (Testing)  
- Docker (optional, for containerization)  

---


---

## Installation and Setup

### 1. Clone the repository
```bash
git clone https://github.com/your-username/Smart-E-Commerce-Backend.git
cd Smart-E-Commerce-Backend
2. Configure the database

Create a MySQL database (e.g., smart_ecommerce) and update your credentials in
src/main/resources/application.properties:

spring.datasource.url=jdbc:mysql://localhost:3306/smart_ecommerce
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

3. Build the project
mvn clean install

4. Run the application
mvn spring-boot:run

5. Access the backend
http://localhost:8080

Project Goal

The purpose of this backend is to provide a robust, secure, and intelligent infrastructure for a next-generation e-commerce platform that merges the efficiency of online shopping with AI-powered personalization and conversational interaction.

Future Enhancements

Integration with advanced AI models (e.g., OpenAI API) for dynamic chatbot responses

Virtual mirror for product visualization using computer vision

Skin tone detection for personalized makeup recommendations

Integration with courier and payment APIs for real-time delivery tracking

Scalable cloud deployment with Docker and Kubernetes

License

This project is developed for educational and research purposes.
All rights reserved © 2025 by Oana-Elena Ilies.
