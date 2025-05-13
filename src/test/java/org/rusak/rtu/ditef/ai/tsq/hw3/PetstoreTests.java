package org.rusak.rtu.ditef.ai.tsq.hw3;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.rusak.rtu.ditef.ai.tsq.hw3.models.PetVO;

import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import io.restassured.config.LogConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PetstoreTests {

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
        try { Thread.sleep(1000); } catch (InterruptedException ignore) {        }
    }

    @Test @Order(1)
    void getAvailablePets_Positive() {            
        List<PetVO> pets = getAvailablePets();
        
        assertThat(pets, not(empty()));
    }

    List<PetVO> getAvailablePets() {
        Response response = given()
            .accept(ContentType.JSON)
        .when()
            .get("/pet/findByStatus?status=available");
        
        response.then()
            .statusCode(200)
            .contentType(ContentType.JSON);
            
        return response.jsonPath().getList("", PetVO.class);
    }

    @Test @Order(2)
    void getPetById_Positive() { //unstable, may fail unexpectedly
        List<PetVO> pets = getAvailablePets();
        long petId = pets.get(0).getId();

        try { Thread.sleep(1000); } catch (InterruptedException ignore) {        }
        PetVO pet = getPetById(petId);

        assertThat(pet.getId(), equalTo((long)petId));
    }

    PetVO getPetById(long petId) {
        Response response = given()
            .header("api_key", "special-key")
            .accept(ContentType.JSON)
        .when()
            .get("/pet/"+petId);
        
        response.then()
            .statusCode(200)
            .contentType(ContentType.JSON);
            
        return response.jsonPath().getObject("", PetVO.class);
    }


    @Test @Order(3)
    void updatePetWithValidStatus_Positive() {
        boolean repeat = false;
        int counter = 0;
        do{ //workaround unstable api endpoint
            counter++;
            try { 

                given()
                    .accept(ContentType.JSON)
                    .contentType("application/x-www-form-urlencoded")
                    .formParam("name", "Great doggie")
                    .formParam("status", "pending")
                .when()
                    .post("/pet/131")
                .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                ;
                repeat = false;
            } catch (AssertionError|Exception e) {
                if (!e.getLocalizedMessage().contains("Expected status code") || counter > 7){
                    throw e;
                }
                repeat = true;
            }
            try { Thread.sleep(5000); } catch (InterruptedException ignore) {        }
        }while (repeat);

        //try { Thread.sleep(5000); } catch (InterruptedException ignore) {        }

        PetVO pet = new PetVO();
        counter = 0;
        while(!pet.hasId() && counter < 15){ //workaround for unstable api
            counter++;
            try {
                pet = getPetById(131);            
            } catch (AssertionError |Exception ignore) { }
            try { Thread.sleep(2000); } catch (InterruptedException ignore) {        }
        }
        assertThat(pet.getStatus(), equalTo("pending"));
    }


    @Test @Order(4)
    void updatePetWithInvalidStatus_FalsePositive() { //Server side doesnt check status validity
        long petId = 131;
        PetVO pet = new PetVO();
        int counter = 0;
        while(!pet.hasId() && counter < 15){ //waiting for data to populate
            counter++;
            try {
                pet = getPetById(petId);  
                if(pet.getStatus().equals("incredible")) {pet.setId(null);}
                        
            } catch (AssertionError |Exception ignore) { }
            try { Thread.sleep(2000); } catch (InterruptedException ignore) {        }
        }

        assertThat(pet.getStatus(), not(equalTo("incredible")));

        try { Thread.sleep(5000); } catch (InterruptedException ignore) {        }

        boolean repeat = false;
        counter = 0;
        do{ //workaround for unstable api
            counter++;
            try{
                given()
                    .accept(ContentType.JSON)
                    .contentType("application/x-www-form-urlencoded")
                    .formParam("name", "Great doggie")
                    .formParam("status", "incredible")
                .when()
                    .post("/pet/"+pet.getId())
                .then()
                    .statusCode(200) //should be invalid
                    .contentType(ContentType.JSON)
                ;

                repeat = false;
            } catch (AssertionError|Exception e) {
                if (!e.getLocalizedMessage().contains("Expected status code") || counter > 7){
                    throw e;
                }
                repeat = true;
            }
        } while (repeat);        
        
        //try { Thread.sleep(5000); } catch (InterruptedException ignore) {        }

        //pet = getPetById(131);
        //assertThat(pet.getStatus(), not(equalTo("incredible"))); //shouldnt fail
    }

    @Test @Order(5)
    void createPet_Positive() {
        createPet();
        
    }
    
    long createPet(){
        String body = """
        {
          "name": "delete me",
          "tags": [
            {"name": "test"},
            {"name":"delete"}
          ],
          "status": "pending"
        }
        """;

        Response response = given()
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/pet")
        ;
        response.then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("name", equalTo("delete me"))
            .body("status", equalTo("pending"))
        ;

        return response.jsonPath().getLong("id");
    }

    @Test @Order(6)
    void deletePet_Positive() {
        long petId = createPet();
        
        try { Thread.sleep(5000); } catch (InterruptedException ignore) {        }

        PetVO pet = new PetVO();
        int counter = 0;
        while(!pet.hasId() && counter < 15){ //waiting for data to populate
            counter++;
            try {
                pet = getPetById(petId);            
            } catch (AssertionError |Exception ignore) { }
            try { Thread.sleep(2000); } catch (InterruptedException ignore) {        }
        }
        
        boolean repeat = false;
        counter = 0;
        do{ //workaround for unstable api
            counter++;
            try{
                given()
                    .accept(ContentType.JSON)
                .when()
                    .delete("/pet/"+petId)
                .then()
                    .statusCode(is(200))
                    .contentType(ContentType.JSON)
                    .body("message", equalTo(petId+""))
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

    @Test @Order(7)
    void deletePet_Negative() {
        given()
            .accept(ContentType.JSON)
        .when()
            .delete("/pet/9999999999")
        .then()
            .statusCode(is(404))
            .body(equalTo(""))
        ;
        
    }
}
