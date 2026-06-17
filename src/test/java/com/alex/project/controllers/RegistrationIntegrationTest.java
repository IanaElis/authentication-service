package com.alex.project.controllers;

import com.alex.project.entiies.Role;
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
class RegistrationIntegrationTest {

    @Inject
    UserRepository userRepository;

    private static final String TEST_USERNAME = "integration-test@example.com";
    private static final String TEST_PASSWORD = "StrongPass123!";
    private static final long RUN_ID = System.currentTimeMillis();
    private String currentUsername;

    @Test
    @Order(1)
    void registerUser_shouldCreateDatabaseRecord() {
        currentUsername = TEST_USERNAME.replace("@", "+" + RUN_ID + "@");
        String requestBody = String.format("""
                {
                    "username": "%s",
                    "password": "%s"
                }
                """, currentUsername, TEST_PASSWORD);

        String response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/auth/signup/user")
                .then()
                .statusCode(200)
                .cookie("JwtToken", notNullValue())
                .extract()
                .body()
                .asString();

        assertNotNull(response, "Response body should contain user ID");
        Long userId = Long.parseLong(response);
        assertTrue(userId > 0, "User ID should be positive");

        // Verify DB record
        User user = userRepository.findById(userId);
        assertNotNull(user, "User must exist in database");
        assertEquals(currentUsername, user.getUsername(), "Username must match");
        assertEquals(Role.USER, user.getRole(), "Role must be USER");
        assertNotNull(user.getPassword(), "Password hash must not be null");
        assertNotEquals(TEST_PASSWORD, user.getPassword(), "Password must be hashed");
    }

    @Test
    @Order(2)
    void registerDuplicateUser_shouldReturnServerError() {
        String requestBody = String.format("""
                {
                    "username": "%s",
                    "password": "%s"
                }
                """, currentUsername, "AnotherPass1!");

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/auth/signup/user")
                .then()
                .statusCode(500);
    }

    @Test
    @Order(3)
    void registerUser_invalidData_shouldReturnError() {
        String requestBody = """
                {
                    "username": "",
                    "password": "short"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/auth/signup/user")
                .then()
                .statusCode(not(200));
    }
}
