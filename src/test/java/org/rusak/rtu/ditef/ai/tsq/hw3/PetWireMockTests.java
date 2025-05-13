package org.rusak.rtu.ditef.ai.tsq.hw3;

import static org.hamcrest.Matchers.equalTo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import com.github.tomakehurst.wiremock.WireMockServer;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import io.restassured.config.LogConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.parsing.Parser;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PetWireMockTests {

    private WireMockServer wireMockServer;

    @BeforeAll
    void setup() {
        // Setting the Global Request and Response Logging Configuration
        RestAssured.config = RestAssured.config()
                .logConfig(LogConfig.logConfig()
                        .enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL)
                        .enablePrettyPrinting(true));
        // Enable global request and response logging filters
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
        RestAssured.defaultParser = Parser.JSON;
        
        RestAssured.baseURI = "http://localhost:8089";
        
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(8089));
        wireMockServer.start();
        configureFor("localhost", 8089);
    }

    @AfterAll
    void teardown() {
        wireMockServer.stop();
    }

    @Test
    void mockGetPetById() {
        stubFor(get(urlEqualTo("/pet/131"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"id\":131,\"name\":\"Mock Wire\",\"status\":\"available\"}")));

        given()
            .contentType("application/json")
        .when()
            .get("/pet/131")
        .then()
            .statusCode(200)
            .body("name", equalTo("Mock Wire"))
            .body("status", equalTo("available"))
        ;
    }

    @Test
    void mockGetNotFound() {
        stubFor(get(urlEqualTo("/pet/999"))
            .willReturn(aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"message\":\"Pet not found\"}")));

        given()
            .contentType("application/json")
        .when()
            .get("/pet/999")
        .then()
            .statusCode(404)
            .body("message", equalTo("Pet not found"));
    }

    @Test
    void mockPostCreatePet() {
        stubFor(post(urlEqualTo("/pet"))
            .willReturn(aResponse()
                .withStatus(201)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"id\":101,\"name\":\"Created Pet\",\"status\":\"pending\"}")));

        given()
            .contentType("application/json")
            .body("{\"name\":\"Created Pet\", \"status\":\"pending\"}")
        .when()
            .post("/pet")
        .then()
            .statusCode(201)
            .body("id", equalTo(101));
    }

    @Test
    void mockPostCreatePet_InvalidData() {
        stubFor(post(urlEqualTo("/pet"))
            .willReturn(aResponse()
                .withStatus(400)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"message\":\"Invalid pet data\"}")));

        given()
            .contentType("application/json")
            .body("{\"invalid\":\"data\"}")
        .when()
            .post("/pet")
        .then()
            .statusCode(400)
            .body("message", equalTo("Invalid pet data"));
    }
    
    @Test
    void mockDeletePet() {
        stubFor(delete(urlEqualTo("/pet/101"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"message\":\"Pet deleted successfully\"}")));

        given()
            .contentType("application/json")
        .when()
            .delete("/pet/101")
        .then()
            .statusCode(200)
            .body("message", equalTo("Pet deleted successfully"));
    }

    @Test
    void mockDeletePet_NotFound() {
        stubFor(delete(urlEqualTo("/pet/404"))
            .willReturn(aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"message\":\"Pet not found\"}")));

        given()
            .contentType("application/json")
        .when()
            .delete("/pet/404")
        .then()
            .statusCode(404)
            .body("message", equalTo("Pet not found"));
    }
}
