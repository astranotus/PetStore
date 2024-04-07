import io.restassured.RestAssured;
import org.testng.annotations.BeforeTest;

import static constants.Endpoints.PET_URL;

public class BaseTest {
    @BeforeTest
    public void setup() {
        RestAssured.baseURI = PET_URL;
    }
}
