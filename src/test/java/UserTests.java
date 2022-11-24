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

    private record CreateSessionResponse(
        String token
    ) {}

    private record CreateSessionData(
        String username,
        String password
    ) {}

    @Test
    public void canCreateAValidSession() {
        // arrange
        var user = new CreateUserData("test", "123", "Testimus III");
        TestResponse resp = null;
        CreateSessionResponse json = null;

        // act
        try {
            TestUtils.jsonRequest("create-user", user);
            var body = new CreateSessionData(user.username, user.password);
            resp = TestUtils.jsonRequest("create-session", body);
            json = resp.json(CreateSessionResponse.class);
        } catch (Exception e) {
            fail();
        }

        // assert
        assertNotNull(resp);
        assertFalse(json.token.isEmpty());
    }

    @Test
    public void cannotCreateAValidSessionWhenPasswordInvalid() {
        // arrange
        var user = new CreateUserData("test", "123", "Testimus III");
        TestResponse resp = null;
        MessageCodeBody json = null;

        // act
        try {
            TestUtils.jsonRequest("create-user", user);
            var body = new CreateSessionData(user.username, "wrong-password");
            resp = TestUtils.jsonRequest("create-session", body);
            json = resp.json(MessageCodeBody.class);
        } catch (Exception e) {
            fail();
        }

        // assert
        assertNotNull(resp);
        assertEquals(MsgCode.INCORRECT_CREDENTIALS.name(), json.messageCode());
    }

    @Test
    public void canEndSession() {
        // arrange
        var user = new CreateUserData("test", "123", "Testimus III");
        TestResponse resp = null;

        // act
        try {
            TestUtils.jsonRequest("create-user", user);
            var token = TestUtils.loginGetToken(user.username, user.password);
            resp = TestUtils.jsonRequest("end-session", null, new String[] { "token " + token });
        } catch (Exception e) {
            fail();
        }

        // assert
        assertNotNull(resp);
        assertTrue(resp.toString().contains("ok"));
    }

    @Test
    public void cannotEndSessionWhithoutToken() {
        // arrange
        TestResponse resp = null;
        MessageCodeBody json = null;

        // act
        try {
            resp = TestUtils.jsonRequest("end-session", null, new String[] { "token " });
            json = resp.json(MessageCodeBody.class);
        } catch (Exception e) {
            fail();
        }

        // assert
        assertNotNull(resp);
        assertEquals(MsgCode.MALFORMED_REQUEST.name(), json.messageCode());
    }

    @Test
    public void canRetrieveUserInfo() {
        // arrange
        var user = new CreateUserData("test", "123", "Testimus III");
        TestResponse resp = null;
        WhoamiUserResponse json = null;

        // act
        try {
            TestUtils.jsonRequest("create-user", user);
            var token = TestUtils.loginGetToken(user.username, user.password);
            resp = TestUtils.jsonRequest("whoami", null, new String[]{ "token " + token });
            json = resp.json(WhoamiUserResponse.class);
        } catch (Exception e) {
            fail();
        }

        // assert
        assertNotNull(resp);
        assertNotNull(json);
        assertEquals(json.fullname, user.fullname);
        assertEquals(json.username, user.username);
    }

    @Test
    public void cannotRetrieveUserInfoWithoutToken() {
        // arrange
        TestResponse respEdited = null;
        MessageCodeBody json = null;

        // act
        try {
            respEdited = TestUtils.jsonRequest("edit-user", null, new String[]{ "token " });
            json = respEdited.json(MessageCodeBody.class);
        } catch (Exception e) {
            fail();
        }

        // assert
        assertNotNull(respEdited);
        assertEquals(MsgCode.MALFORMED_REQUEST.name(), json.messageCode());
    }

}
