import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.simple.JSONObject;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import static constants.Endpoints.*;
import static constants.HttpStatusCods.HTTP_BAD_REQUEST;
import static constants.HttpStatusCods.HTTP_OK;
import static constants.Queries.*;
import static io.restassured.RestAssured.given;
import static org.testng.Assert.assertTrue;

public class PetStoreTests extends BaseTest {
    @Test(priority = 1)
    public void testStoreInventory() {
        Response response = given()
                .log().all()
                .accept(ContentType.JSON)
                .when()
                .get(STORE + INVENTORY );

        assertTrue(response.getBody().asString() != null && !response.getBody().asString().isEmpty(),
                "Респонс пуст");

        response.then()
                .log().all()
                .statusCode(HTTP_OK);
    }

    @Test(priority = 0)
    public void testCreateOrder() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("id", 9);
        requestBody.put("petId", 0);
        requestBody.put("quantity", 0);
        requestBody.put("shipDate", "2024-04-06T19:13:49.713Z");
        requestBody.put("status", "placed");
        requestBody.put("complete", true);

        Response response = given()
                .log().all()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(requestBody.toJSONString())
                .when()
                .post(STORE + ORDER);

        response.then()
                .log().all()
                .statusCode(HTTP_OK);
    }

    @Test(priority = 2)
    public void negativeCreateOrderWithoutBody() {
        Response response = given()
                .log().all()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post(STORE + ORDER);

        response.then()
                .log().all()
                .statusCode(HTTP_BAD_REQUEST);
    }

    @Test(priority = 1)
    public void testFindOrderById() {
        Response response = given()
                .log().all()
                .accept(ContentType.JSON)
                .when()
                .get(STORE + ORDER + "/10" );

        SoftAssert softAssert = new SoftAssert();
        softAssert.assertEquals(response.path(ID), Integer.valueOf(10), "Содержание " + ID + " не соответсвует ожидаемому");

        response.then()
                .log().all()
                .statusCode(HTTP_OK);
    }

    @Test(priority = 2)
    public void negativeTestFindOrderByIdWhereIdMoreThenTen() {
        Response response = given()
                .log().all()
                .accept(ContentType.JSON)
                .when()
                .get(STORE + ORDER + "/11" );

        response.then()
                .log().all()
                .statusCode(HTTP_BAD_REQUEST);
    }

    @Test(priority = 2)
    public void negativeTestFindOrderByIdWhereIdLessThenOne() {
        Response response = given()
                .log().all()
                .accept(ContentType.JSON)
                .when()
                .get(STORE + ORDER + "/0" );

        response.then()
                .log().all()
                .statusCode(HTTP_BAD_REQUEST);
    }

    @Test(priority = 3)
    public void testDeleteOrder() {
        Response response = given()
                .log().all()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .delete(STORE + ORDER + "/9");

        response.then()
                .log().all()
                .statusCode(HTTP_OK);
    }

    @Test(priority = 3)
    public void negativeTestDeleteOrder() {
        Response response = given()
                .log().all()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .delete(STORE + ORDER + "/9999999");

        response.then()
                .log().all()
                .statusCode(HTTP_BAD_REQUEST);
    }
}
