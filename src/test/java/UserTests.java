import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class UserTests {

    @Before
    public void setup() {
        try {
            TestUtils.clearTestsDatabase();
        } catch (Exception e) {
            System.err.println(e);
            fail();
        }
    }

    private record CreateUserData(
        String username,
        String password,
        String fullname
    ) {}

    private record CreateUserResponse(long id) {}

    @Test
    public void createUserLoginWhoami() {
        // arrange
        var user = new CreateUserData("test", "123", "Testimus III");

        // act
        TestResponse resp = null;
        CreateUserResponse json = null;
        try {
            resp = TestUtils.jsonRequest("create-user", user);
            json = resp.json(CreateUserResponse.class);
        } catch (Exception e) {
            fail();
        }

        // assert
        assertNotNull(resp);
        assertNotNull(json);
        assertEquals(1, json.id);
    }

}
