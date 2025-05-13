package org.rusak.rtu.ditef.ai.tsq.hw3;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserTests {

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "https://petstore.swagger.io/v2";
    }

    @AfterEach
    void tearDown() {
        System.out.println("Sleeping...");
        try { Thread.sleep(5000); } catch (InterruptedException ignore) {        }
    }

    @Test @Order(1)
    void createUser_Positive() { // actually it shouldnt allow duplicate users...
        String body = """
        {
          "username": "tets01",
          "password": "superpass1234"
        }
        """;

        given()
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/user")
        .then()
            .log().all()
            .statusCode(200)
            .body("message", notNullValue())
        ;
    }

    @Test @Order(2)
    void createUser_Negative() {
        String body = """
        {
            "id": 01,
          "username": "tets01",
          "password": "superpass1234"
        }
        """;

        given()
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/user")
        .then()
            .log().all()
            .statusCode(400)
            .body("message", equalTo("bad input"))
        ;
    }

    @Test @Order(3)
    void userLogin_Positive() {
        given()
            .accept(ContentType.JSON)
        .when()
            .get("/user/login?username=tets01&password=superpass1234")
        .then()
            .log().all()
            .statusCode(200)
            .body("message", containsString("logged in user session"));
    }

    @Test @Order(4)
    void userLogin_FalsePositive() { //actually it should fail with any 4xx error
        String login = "";//Utils.randomString(10);
        String pass = "";//Utils.randomPassword(10);

        String api = String.format("/user/login?username=%s&password=%s", login, pass);

        given()
            .accept(ContentType.JSON)
        .when()
            .get(api)
        .then()
            .log().all()
            .statusCode(200)
            .body("message", containsString("logged in user session"));
    }

    @Test @Order(5)
    void userLogout_Positive() { //not usefull - always succeeds
        given()
            .accept(ContentType.JSON)
        .when()
            .get("/user/logout")
        .then()
            .log().all()
            .statusCode(200)
            .body("message", equalTo("ok"));
    }
}
