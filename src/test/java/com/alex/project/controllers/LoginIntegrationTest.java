package com.alex.project.controllers;

import com.alex.project.entiies.User;
import com.alex.project.repositories.UserRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LoginIntegrationTest {

    @Inject
    UserRepository userRepository;

    @Test
    void login_withValidCredentials_returnsJwtAndUserId() {
        String uniqueUser = "login-" + System.currentTimeMillis() + "@example.com";
        String requestBody = String.format("""
                {
                    "username": "%s",
                    "password": "ValidPass123!"
                }
                """, uniqueUser);

        // First register the user
        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/auth/signup/user")
                .then()
                .statusCode(200);

        // Then login
        String userId = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .cookie("JwtToken", notNullValue())
                .body(notNullValue())
                .extract()
                .body()
                .asString();

        assertNotNull(userId);
        assertNotEquals("", userId.trim());

        // Verify user exists in DB
        User user = userRepository.findByUsername(uniqueUser).orElse(null);
        assertNotNull(user, "User must be persisted in database");
    }

    @Test
    void login_withWrongPassword_returnsError() {
        String uniqueUser = "wrongpass-" + System.currentTimeMillis() + "@example.com";
        String registerBody = String.format("""
                {
                    "username": "%s",
                    "password": "CorrectPass1!"
                }
                """, uniqueUser);
        String loginBody = String.format("""
                {
                    "username": "%s",
                    "password": "WrongPassword1!"
                }
                """, uniqueUser);

        // Register first
        given()
                .contentType(ContentType.JSON)
                .body(registerBody)
                .when()
                .post("/auth/signup/user")
                .then()
                .statusCode(200);

        // Try wrong password
        given()
                .contentType(ContentType.JSON)
                .body(loginBody)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(not(200));
    }

    @Test
    void login_nonexistentUser_returnsError() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "username": "no-such-user@example.com",
                            "password": "SomePass123!"
                        }
                        """)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(not(200));
    }
}
