package org.rusak.rtu.ditef.ai.tsq.hw3;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import io.restassured.config.LogConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrderTests {

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "https://petstore.swagger.io/v2";

         // Setting the Global Request and Response Logging Configuration
        RestAssured.config = RestAssured.config()
                .logConfig(LogConfig.logConfig()
                        .enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL)
                        .enablePrettyPrinting(true));
        // Enable global request and response logging filters
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());   
    }

    @AfterEach
    void tearDown() {
        System.out.println("Sleeping...");
        try { Thread.sleep(2500); } catch (InterruptedException ignore) {        }
    }

    @Test @Order(1)
    void placeOrder_Positive() {
        _placeOrder_Positive(131, 10);
    }

    long _placeOrder_Positive(long petId, int quantity) {
        String body = String.format("""
        {
          "petId": %d,
          "quantity": %d
        }
        """, petId, quantity);

        //parse json body
        RequestSpecification request = given()
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .body(body)
        ;
        Response response = request.post("/store/order");

        JsonPath jsonPath = response.jsonPath();
        long orderId = jsonPath.get("id");
        assert orderId > 0;

        int status = response.getStatusCode();
        assert status == 200;

        int _petId = jsonPath.get("petId");
        assert _petId == petId;

        int _quantity = jsonPath.get("quantity");
        assert _quantity == quantity;

        return orderId;
    }

    @Test @Order(2)
    void getOrder_Positive() {
        //we need to place order first, as it's automatically closed after a while
        long orderId = _placeOrder_Positive(131, 10);

        try { Thread.sleep(5000); } catch (InterruptedException ignore) {        }
        
        boolean repeat = false;
        int counter = 0;
        do{ //workaround for unstable api
            counter++;
            try{
                given()
                    .accept(ContentType.JSON)
                .when()
                    .get("/store/order/"+orderId)
                .then()
                    .statusCode(200)
                    //.body("status", anyOf(is("placed"), is("approved"), is("delivered")))
                    .body("complete", equalTo(false))
                ;

                repeat = false;
            } catch (AssertionError|Exception e) {
                if (!e.getLocalizedMessage().contains("Expected status code") || counter > 7){
                    throw e;
                }
                repeat = true;
            }
        } while (repeat);
    }

    @Test @Order(3)
    void getNonExistentOrder_Negative() {
        given()
            .accept(ContentType.JSON)
        .when()
            .get("/store/order/99999999999999999")
        .then()
            .statusCode(404)
            .body("message", equalTo("Order not found"))
        ;
    }

    @Test @Order(4)
    void getLargeIdOrder_Negative() {
        given()
            .accept(ContentType.JSON)
        .when()
        .get("/store/order/999999999999999999999")
        .then()
            .statusCode(is(404))
            .body("message", containsString("NumberFormatException"))            
        ;
    }


    @Test @Order(5)
    void deleteOrder_Positive() {
        long orderId = _placeOrder_Positive(131, 10);
        String api = String.format("/store/order/%d", orderId);

        boolean repeat = false;
        int counter = 0;
        do{ //workaround for unstable api
            counter++;
            try{
                given()
                    .accept(ContentType.JSON)
                .when()
                    .delete(api)
                .then()
                    .statusCode(is(200))
                    .body("message", equalTo(orderId+""))
                ;
                repeat = false;
            } catch (AssertionError|Exception e) {
                if (!e.getLocalizedMessage().contains("Expected status code") || counter > 7){
                    throw e;
                }
                repeat = true;
            }
            
        } while (repeat);
        
    }

    @Test @Order(6)
    void deleteOrder_Negative() {
        long orderId = 1234567;
        String api = String.format("/store/order/%d", orderId);

        given()
            .accept(ContentType.JSON)
        .when()
            .delete(api)
        .then()
            .statusCode(is(404))
            .body("message", equalTo("Order Not Found"))
        ;
    }

}
