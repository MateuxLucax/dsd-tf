import infra.request.MessageCodeBody;
import infra.request.MsgCode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;

import static org.junit.Assert.*;

public class UserTests {

    @Before
    public void setup() {
        try {
            TestUtils.clearTestsDatabase();

            try (
              var conn = TestUtils.testConnection();
              var stmt = conn.createStatement()
            ) {
                var sql = "INSERT INTO users (username, fullname, password) VALUES ('admin', 'O Administrador', '1234')";
                stmt.execute(sql);
            }
        } catch (Exception e) {
            System.err.println(e);
            fail();
        }
    }

    @After
    public void teardown() {
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
    public void canCreateUser() {
        // arrange
        var user = new CreateUserData("test", "123", "Testimus III");
        TestResponse resp = null;
        CreateUserResponse json = null;

        // act
        try {
            resp = TestUtils.jsonRequest("create-user", user);
            json = resp.json(CreateUserResponse.class);
        } catch (Exception e) {
            fail();
        }

        // assert
        assertNotNull(resp);
        assertNotNull(json);
        assertEquals(2, json.id);
    }

    @Test
    public void cannotCreateUserWithExistingName() {
        // arrange
        var user = new CreateUserData("admin", "9193", "The Admin");
        TestResponse resp = null;
        MessageCodeBody json = null;

        // act
        try {
            resp = TestUtils.jsonRequest("create-user", user);
            json = resp.json(MessageCodeBody.class);
        } catch (Exception e) {
            fail();
        }

        // assert
        assertNotNull(resp);
        assertNotNull(json);
        assertEquals(MsgCode.USERNAME_IN_USE.name(), json.messageCode());
    }

    private record EditUserData(
        String newFullname,
        String newPassword
    ) {}

    private record WhoamiUserResponse(String username, String fullname, Timestamp createdAt, Timestamp updatedAt) {}

    @Test
    public void canEditAValidUser() {
        // arrange
        var user = new CreateUserData("test", "123", "Testimus III");
        var editedUser = new EditUserData("test-edited", "1234");
        TestResponse respEdited = null;
        TestResponse respWhoami;
        WhoamiUserResponse json = null;

        // act
        try {
            TestUtils.jsonRequest("create-user", user);
            var token = TestUtils.loginGetToken(user.username, user.password);
            respEdited = TestUtils.jsonRequest("edit-user", editedUser, new String[]{ "token " + token });
            respWhoami = TestUtils.jsonRequest("whoami", null, new String[]{"token " + token});
            json = respWhoami.json(WhoamiUserResponse.class);
        } catch (Exception e) {
            fail();
        }

        // assert
        assertNotNull(respEdited);
        assertNotNull(json);
        assertEquals(editedUser.newFullname, json.fullname);
    }

    @Test
    public void cannotEditUserWithoutToken() {
        // arrange
        var user = new CreateUserData("test", "123", "Testimus III");
        var editedUser = new EditUserData("test-edited", "1234");
        TestResponse respEdited = null;
        MessageCodeBody json = null;

        // act
        try {
            TestUtils.jsonRequest("create-user", user);
            respEdited = TestUtils.jsonRequest("edit-user", editedUser, new String[]{ "token " });
            json = respEdited.json(MessageCodeBody.class);
        } catch (Exception e) {
            fail();
        }

        // assert
        assertNotNull(respEdited);
        assertEquals(MsgCode.MALFORMED_REQUEST.name(), json.messageCode());
    }
}
