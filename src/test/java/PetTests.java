import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.simple.JSONArray;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.json.simple.JSONObject;
import org.testng.asserts.SoftAssert;

import java.io.File;

import static constants.Endpoints.*;
import static constants.HttpStatusCods.*;
import static constants.PetSalaryStatus.*;
import static constants.Queries.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
public class PetTests extends BaseTest {
    private File imageFile = new File("src/main/resources/screen.PNG");
    private String petId;
    private static final String LAPA_NAME = "Lapa";
    private static final String REDJ_NAME = "Redji";

    @Test(priority = 1)
    public void testUploadImageById() {
        Response response = given()
                .log().all()
                .accept(ContentType.JSON)
                .header("Content-Type", "multipart/form-data")
                .multiPart("file", imageFile, "image/png")
                .when()
                .post(PET_EP + "/" + petId + "/uploadImage");

        response.then()
                .log().all()
                .statusCode(HTTP_OK);
    }

    @Test(priority = 0)
    public void testCreatePet() {
        JSONObject requestBody = new JSONObject();
        JSONObject category = new JSONObject();
        JSONObject tag = new JSONObject();

        category.put(ID, 0);
        category.put(NAME, "string");

        tag.put(ID, 0);
        tag.put(NAME, "string");

        requestBody.put(ID, 0);
        requestBody.put("category", category);
        requestBody.put(NAME, LAPA_NAME);

        JSONArray photoUrls = new JSONArray();
        photoUrls.add("string");
        requestBody.put("photoUrls", photoUrls);

        JSONArray tags = new JSONArray();
        tags.add(tag);
        requestBody.put("tags", tags);

        requestBody.put(STATUS, AVAILABLE);


        Response response = given()
                .log().all()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(PET_EP);

        SoftAssert softAssert = new SoftAssert();
        softAssert.assertEquals(response.getBody().jsonPath().get("category.id"), Integer.valueOf(0),
                "Содержание category.id не соответсвует ожидаемому");
        softAssert.assertEquals(response.getBody().jsonPath().get("category.name"), "string",
                "Содержание category.name не соответсвует ожидаемому");
        softAssert.assertEquals(response.path(NAME), LAPA_NAME, "Содержание " + NAME + " не соответсвует ожидаемому");
        softAssert.assertEquals(response.path(STATUS), AVAILABLE, "Содержание " + STATUS + " не соответсвует ожидаемому");
        softAssert.assertEquals(response.getBody().jsonPath().get("photoUrls[0]"), "string",
                "Содержание photoUrls не соответсвует ожидаемому");
        softAssert.assertEquals(response.getBody().jsonPath().get("tags[0].id"), Integer.valueOf(0),
                "Содержание tags.id не соответсвует ожидаемому");
        softAssert.assertEquals(response.getBody().jsonPath().get("tags[0].name"), "string",
                "Содержание tags.name не соответсвует ожидаемому");
        softAssert.assertAll();

        response.then()
                .log().all()
                .statusCode(HTTP_OK);

        petId = response.jsonPath().getString(ID);
    }
    @Test(priority = 2)
    public void negativeTestCreatePetWithoutBody() {
        Response response = given()
                .log().all()
                .contentType(ContentType.JSON)
                .when()
                .post(PET_EP);

        response.then()
                .log().all()
                .statusCode(HTTP_BAD_METHOD);
    }

    @Test(priority = 1)
    public void testUpdatePet() {
        JSONObject requestBody = new JSONObject();
        JSONObject category = new JSONObject();
        category.put(ID, 0);
        category.put(NAME, "string");
        requestBody.put(ID, petId);
        requestBody.put("category", category);
        requestBody.put(NAME, REDJ_NAME);

        JSONArray photoUrls = new JSONArray();
        photoUrls.add("string");
        requestBody.put("photoUrls", photoUrls);

        JSONObject tag = new JSONObject();
        tag.put(ID, 0);
        tag.put(NAME, "string");
        JSONArray tags = new JSONArray();
        tags.add(tag);
        requestBody.put("tags", tags);

        requestBody.put(STATUS, AVAILABLE);

        Response response = given()
                .log().all()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put(PET_EP);

        SoftAssert softAssert = new SoftAssert();
        softAssert.assertEquals(response.path(NAME), REDJ_NAME);
        softAssert.assertEquals(response.path(STATUS), AVAILABLE);
        softAssert.assertAll();

        response.then()
                .log().all()
                .statusCode(HTTP_OK);
    }

    @Test(priority = 2)
    public void negativeTestUpdateWerePetNotFound() {
        JSONObject requestBody = new JSONObject();
        JSONObject category = new JSONObject();
        category.put(ID, 0);
        category.put(NAME, "string");
        requestBody.put(ID, 999999996);
        requestBody.put("category", category);
        requestBody.put(NAME, REDJ_NAME);

        JSONArray photoUrls = new JSONArray();
        photoUrls.add("string");
        requestBody.put("photoUrls", photoUrls);

        JSONObject tag = new JSONObject();
        tag.put(ID, 0);
        tag.put(NAME, "string");
        JSONArray tags = new JSONArray();
        tags.add(tag);
        requestBody.put("tags", tags);

        requestBody.put(STATUS, AVAILABLE);

        given()
                .log().all()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put(PET_EP)
                .then()
                .log().all()
                .statusCode(HTTP_BAD_METHOD);
    }

    @Test(priority = 2)
    public void negativeTestUpdatePetWithoutBody() {
        given()
                .log().all()
                .contentType(ContentType.JSON)
                .when()
                .put(PET_EP)
                .then()
                .log().all()
                .statusCode(HTTP_BAD_METHOD);
    }


    @DataProvider(name = "statusProvider")
    public Object[][] statusProvider() {
        return new Object[][] {
                {AVAILABLE},
                {PENDING},
                {SOLD}
        };
    }
    @Test(priority = 1, dataProvider = "statusProvider")
    public void testFindByStatusPet(String status) {
        Response response = given()
                .log().all()
                .accept(ContentType.JSON)
                .param(STATUS, status)
                .when()
                .get( PET_EP+FIND_BY_STATUS);

        response.then()
                .log().all()
                .statusCode(HTTP_OK)
                .body(STATUS, hasItem(status));
    }

    @Test(priority = 2)
    public void negativeTestFindByWrongStatusPet() {
        given()
                .log().all()
                .accept(ContentType.JSON)
                .param(STATUS, REDJ_NAME)
                .when()
                .get(PET_EP + FIND_BY_STATUS)
                .then()
                .log().all()
                .statusCode(HTTP_BAD_REQUEST);
    }

    @Test(priority = 1)
    public void testFindByIdPet() {
        Response response = given()
                .log().all()
                .accept(ContentType.JSON)
                .when()
                .get(PET_EP + "/" + petId);

        SoftAssert softAssert = new SoftAssert();
        softAssert.assertEquals(response.getBody().jsonPath().get("category.id"), Integer.valueOf(0),
                "Содержание category.id не соответсвует ожидаемому");
        softAssert.assertEquals(response.getBody().jsonPath().get("category.name"), "string",
                "Содержание category.name не соответсвует ожидаемому");
        softAssert.assertEquals(response.path(NAME), LAPA_NAME, "Содержание " + NAME + " не соответсвует ожидаемому");
        softAssert.assertEquals(response.path(STATUS), AVAILABLE, "Содержание " + STATUS + " не соответсвует ожидаемому");
        softAssert.assertEquals(response.getBody().jsonPath().get("photoUrls[0]"), "string",
                "Содержание photoUrls не соответсвует ожидаемому");
        softAssert.assertEquals(response.getBody().jsonPath().get("tags[0].id"), Integer.valueOf(0),
                "Содержание tags.id не соответсвует ожидаемому");
        softAssert.assertEquals(response.getBody().jsonPath().get("tags[0].name"), "string",
                "Содержание tags.name не соответсвует ожидаемому");
        softAssert.assertAll();

        response.then()
                .log().all()
                .statusCode(HTTP_OK);
    }

    @Test(priority = 2)
    public void negativeTestFindByIdPetWherePetNotFound() {
        given()
                .log().all()
                .accept(ContentType.JSON)
                .when()
                .get(PET_EP + "/" + 999999998)
                .then()
                .log().all()
                .statusCode(HTTP_NOT_FOUND);
    }

    @Test(priority = 2)
    public void negativeTestFindByStringIdPet() {
        given()
                .log().all()
                .accept(ContentType.JSON)
                .when()
                .get(PET_EP + "/"  + REDJ_NAME)
                .then()
                .log().all()
                .statusCode(HTTP_BAD_REQUEST);
    }

    @Test(priority = 1)
    public void testUpdatePetWithFormData() {
        JSONObject requestBody = new JSONObject();
        requestBody.put(ID, petId);
        requestBody.put(NAME, LAPA_NAME);
        requestBody.put(STATUS, SOLD);

        Response response = given()
                .log().all()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(PET_EP);

        SoftAssert softAssert = new SoftAssert();
        softAssert.assertEquals(response.path(NAME), LAPA_NAME);
        softAssert.assertEquals(response.path(STATUS), SOLD);
        softAssert.assertAll();

        response.then()
                .log().all()
                .statusCode(HTTP_OK);
    }

    @Test(priority = 2)
    public void negativeTestUpdatePetWithFormDataWithoutId() {
        JSONObject requestBody = new JSONObject();
        requestBody.put(NAME, REDJ_NAME);
        requestBody.put(STATUS, SOLD);

        Response response = given()
                .log().all()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(PET_EP);

        response.then()
                .log().all()
                .statusCode(HTTP_BAD_METHOD);
    }

    @Test(priority = 2)
    public void negativeTestUpdatePetWithFormDataWithIntName() {
        JSONObject requestBody = new JSONObject();
        requestBody.put(ID, petId);
        requestBody.put(NAME, 1234);

        Response response = given()
                .log().all()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(PET_EP);

        response.then()
                .log().all()
                .statusCode(HTTP_BAD_METHOD);
    }

    @Test(priority = 2)
    public void negativeTestUpdatePetWithFormDataWithIntStatus() {
        JSONObject requestBody = new JSONObject();
        requestBody.put(ID, petId);
        requestBody.put(STATUS, SOLD);

        Response response = given()
                .log().all()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(PET_EP);

        response.then()
                .log().all()
                .statusCode(HTTP_BAD_METHOD);
    }

    @Test(priority = 3)
    public void testDeletePetById() {
        given()
                .log().all()
                .accept(ContentType.JSON)
                .when()
                .delete(PET_EP +"/" + petId)
                .then()
                .log().all()
                .statusCode(HTTP_OK);
    }

    @Test(priority = 3)
    public void negativeTestDeletePetByIdWerePetNotFound() {
        given()
                .log().all()
                .accept(ContentType.JSON)
                .when()
                .delete(PET_EP +"/" + 999999995)
                .then()
                .log().all()
                .statusCode(HTTP_NOT_FOUND);
    }

    @Test(priority = 3)
    public void negativeTestDeletePetByIdWithStringId() {
        given()
                .log().all()
                .accept(ContentType.JSON)
                .when()
                .delete(PET_EP +"/"+ REDJ_NAME)
                .then()
                .log().all()
                .statusCode(HTTP_BAD_REQUEST);
    }
}

