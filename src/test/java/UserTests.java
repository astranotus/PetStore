import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Map;

import static constants.Endpoints.*;
import static constants.HttpStatusCods.*;
import static constants.Queries.ID;
import static constants.UserData.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

public class UserTests extends BaseTest {
    private Map<String, String> cookies;
    @Test(priority = 0)
    public void testCreateUser() {
        JSONObject requestBody = createUserObject(USER_NAME);

        Response response = given()
                .log().all()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(USER);

        response.then()
                .log().all()
                .statusCode(HTTP_OK);

    }

    @Test(priority = 1)
    public void testCreateListOfUsers() {
        JSONArray requestBody = new JSONArray();
        JSONObject user = createUserObject(USER_NAME);
        requestBody.add(user);

        Response response = given()
                .log().all()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(USER + CREATE_WITH_LIST);

        response.then()
                .log().all()
                .statusCode(HTTP_OK);
    }

    @Test(priority = 1)
    public void testFindUserByUserName() {
        Response response = given()
                .log().all()
                .accept(ContentType.JSON)
                .when()
                .get(USER + "/" + USER_NAME);

        response.then()
                .log().all()
                .statusCode(HTTP_OK);
    }

    @Test(priority = 3)
    public void negativeTestFindUserByUsernameNotFound() {
        Response response = given()
                .log().all()
                .accept(ContentType.JSON)
                .when()
                .get(USER + "/1234" );

        response.then()
                .log().all()
                .statusCode(HTTP_NOT_FOUND);
    }

    @Test(priority = 3)
    public void negativeTestUpdateUserByUsernameWithoutAuthorization() {
        Response response = given()
                .log().all()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(createUserObject(ANOTHER_USER_NAME))
                .when()
                .put(USER + "/" + USER_NAME);

        response.then()
                .log().all()
                .statusCode(HTTP_OK);

        given()
                .log().all()
                .accept(ContentType.JSON)
                .when()
                .get(USER + "/" + ANOTHER_USER_NAME)
        .then()
                .log().all()
                .statusCode(HTTP_OK);
        response.path("username",ANOTHER_USER_NAME);
    }

    @Test(priority = 3)
    public void negativeTestUpdateUserByWrongUsername() {
        Response response = given()
                .log().all()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(createUserObject(ANOTHER_USER_NAME))
                .when()
                .put(USER + "/999999999999999999999999999");

        response.then()
                .log().all()
                .statusCode(HTTP_NOT_FOUND);
    }

    @Test(priority = 3)
    public void negativeTestUpdateUserWithWrongUsernameInputData() {
        JSONObject user = new JSONObject();
        user.put(ID, USER_ID);
        user.put("username", 123456233);
        user.put("firstName", USER_FIRST_NAME);
        user.put("lastName", USER_LAST_NAME);
        user.put("email", USER_EMAIL);
        user.put("password", USER_PASSWORD);
        user.put("phone", USER_PHONE);
        user.put("userStatus", USER_STATUS);
        Response response = given()
                .log().all()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(user)
                .when()
                .put(USER + "/" + USER_NAME);

        response.then()
                .log().all()
                .statusCode(HTTP_BAD_REQUEST);
    }

    @Test(priority = 1)
    public void testAuthorization() {
        Response response = given()
                .log().all()
                .queryParam("username", USER_NAME)
                .queryParam("password", USER_PASSWORD)
                .accept(ContentType.JSON)
                .when()
                .get(USER + LOGIN);

        cookies = response.getCookies();

        response.then()
                .log().all()
                .body("message", containsString("logged in user session"))
                .statusCode(HTTP_OK);
    }

    @Test(priority = 2)
    public void testUpdateUserByUsernameWithAuthorization() {
        Response response = given()
                .log().all()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(createUserObject(ANOTHER_USER_NAME))
                .when()
                .put(USER + "/" + USER_NAME);

        response.then()
                .log().all()
                .header("x-expires-after", not(""))
                .header("x-rate-limit", not(""))
                .statusCode(HTTP_OK);

        given()
                .log().all()
                .cookies(cookies)
                .accept(ContentType.JSON)
                .when()
                .get(USER + "/" + ANOTHER_USER_NAME)
                .then()
                .log().all()
                .statusCode(HTTP_OK);
        response.path("username", ANOTHER_USER_NAME);
    }

    @Test(priority = 3)
    public void negativTestAuthorizationWithWrongPassword() {
        Response response = given()
                .log().all()
                .queryParam("username", USER_NAME)
                .queryParam("password", "password")
                .accept(ContentType.JSON)
                .when()
                .get(USER + LOGIN);

        response.then()
                .log().all()
                .statusCode(HTTP_BAD_REQUEST);
    }

    @Test(priority = 1)
    public void testLogout() {
        Response response = given()
                .log().all()
                .queryParam("username", USER_NAME)
                .queryParam("password", USER_PASSWORD)
                .accept(ContentType.JSON)
                .when()
                .get(USER + LOGIN);

        response.then()
                .log().all()
                .body("message", containsString("logged in user session"))
                .statusCode(HTTP_OK);

        Response logoutResponse = given()
                .log().all()
                .accept(ContentType.JSON)
                .when()
                .get(USER + LOGOUT);

        logoutResponse.then()
                .log().all()
                .body("message", containsString("ok"))
                .statusCode(HTTP_OK);
    }

    public JSONObject createUserObject(String username) {
        JSONObject user = new JSONObject();
        user.put(ID, USER_ID);
        user.put("username", username);
        user.put("firstName", USER_FIRST_NAME);
        user.put("lastName", USER_LAST_NAME);
        user.put("email", USER_EMAIL);
        user.put("password", USER_PASSWORD);
        user.put("phone", USER_PHONE);
        user.put("userStatus", USER_STATUS);
        return user;
    }
}
