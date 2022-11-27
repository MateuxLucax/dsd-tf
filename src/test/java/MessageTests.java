import com.google.gson.reflect.TypeToken;
import infra.request.MessageCodeBody;
import infra.request.MsgCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import user.FriendshipStatus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MessageTests {
    private UserData user;
    private UserData friend;
    private UserData notFriend;

    @BeforeEach
    public void setup() {
        try {
            user = new UserData(1, "dude", "123");
            friend = new UserData(2, "bro", "7890");
            notFriend = new UserData(3, "friendless", "abcdefg");

            TestUtils.clearTestsDatabase();
            TestUtils.runSql(
                    "INSERT INTO users (id, username, fullname, password) " +
                            "VALUES (1, 'dude', 'some dude', '123')"
            );
            TestUtils.runSql(
                    "INSERT INTO users (id, username, fullname, password) " +
                            "VALUES (2, 'bro', 'brother', '7890')"
            );
            TestUtils.runSql(
                    "INSERT INTO users (id, username, fullname, password) " +
                            "VALUES (3, 'friendless', 'third guy', 'abcdefg')"
            );
            TestUtils.runSql("INSERT INTO friends (your_id, their_id) VALUES (1, 2)");
            TestUtils.runSql("INSERT INTO friends (your_id, their_id) VALUES (2, 1)");

            TestUtils.runSql(
                    "INSERT INTO messages (sender_id, receiver_id, id, sent_at, text_contents, file_reference) " +
                            "VALUES (1, 2, 1, current_timestamp, 'Hello bro!', null)," +
                            "       (2, 1, 2, current_timestamp, 'Hi Dude', null)," +
                            "       (1, 2, 3, current_timestamp, 'What up?', null)," +
                            "       (2, 1, 4, current_timestamp, 'Just testing hehe', null)"
            );
        } catch (Exception e) {
            System.err.println(e);
            fail();
        }
    }

    @AfterEach
    public void teardown() {
        try {
            TestUtils.clearTestsDatabase();
        } catch (Exception e) {
            System.err.println(e);
            fail();
        }
    }

    private record UserData(
            int id,
            String username,
            String password
    ) {}
    private record GetMessagesRequest(
            int friendId,
            int before,
            int limit
    ) {}

    private record SendMessageRequest(
            int to,
            String textContents,
            String fileReference
    ) {}
    private record Message(
            int id,
            int userId,
            String sentAt,
            String textContents,
            String fileReference
    ) {}

    private record UploadedFile(
            String filename
    ) {}

    @Test
    public void cannotGetMessagesWithoutToken() {
        // arrange
        GetMessagesRequest payload = new GetMessagesRequest(friend.id(), 0, 10);
        TestResponse response = null;
        MessageCodeBody json = null;

        // act
        try {
            response = TestUtils.jsonRequest("get-messages", payload);
            json = response.json(MessageCodeBody.class);
        } catch (Exception e) {
            fail();
        }

        // assert
        assertNotNull(response);
        assertNotNull(json);
        assertEquals(MsgCode.MALFORMED_REQUEST.name(), json.messageCode());
    }

    @Test
    public void cannotGetMessagesFromNotFriend() {
        // arrange
        GetMessagesRequest payload = new GetMessagesRequest(notFriend.id(), 0, 10);
        TestResponse response = null;
        MessageCodeBody json = null;

        // act
        try {
            String token = TestUtils.loginGetToken(user.username(), user.password());
            response = TestUtils.jsonRequest("get-messages", payload, new String[]{ "token " + token });
            json = response.json(MessageCodeBody.class);
        } catch (Exception e) {
            fail();
        }

        // assert
        assertNotNull(response);
        assertNotNull(json);
        assertEquals(MsgCode.NOT_FRIENDS.name(), json.messageCode());
    }

    @Test
    public void canGetMessagesFromFriend() {
        // arrange
        GetMessagesRequest payload = new GetMessagesRequest(friend.id(), 0, 100);
        TestResponse response = null;
        Message[] json = null;

        // act
        try {
            String token = TestUtils.loginGetToken(user.username(), user.password());
            response = TestUtils.jsonRequest("get-messages", payload, new String[]{ "token " + token });
            json = response.json(Message[].class);
        } catch (Exception e) {
            fail();
        }

        // assert
        assertNotNull(response);
        assertNotNull(json);
        assertEquals(json.length, 4);
    }

    @Test
    public void canGetMessagesPaginate() {
        // arrange
        GetMessagesRequest payload = new GetMessagesRequest(friend.id(), 3, 2);
        TestResponse response = null;
        Message[] json = null;

        // act
        try {
            String token = TestUtils.loginGetToken(user.username(), user.password());
            response = TestUtils.jsonRequest("get-messages", payload, new String[]{ "token " + token });
            json = response.json(Message[].class);
        } catch (Exception e) {
            fail();
        }
        // assert
        assertNotNull(response);
        assertNotNull(json);
        assertEquals(json[0].id, 3);
        assertEquals(json.length, 2);
    }

    @Test
    public void cannotSendMessageToNotFriend() {
        // arrange
        SendMessageRequest payload = new SendMessageRequest(notFriend.id(), "Would you like to be my friend?", null);
        TestResponse response = null;
        MessageCodeBody json = null;

        // act
        try {
            String token = TestUtils.loginGetToken(user.username(), user.password());
            response = TestUtils.jsonRequest("send-message", payload, new String[]{ "token " + token });
            json = response.json(MessageCodeBody.class);
        } catch (Exception e) {
            fail();
        }

        // assert
        assertNotNull(response);
        assertNotNull(json);
        assertEquals(MsgCode.NOT_FRIENDS.name(), json.messageCode());
    }

    @Test
    public void cannotSendMessageWithoutTextOrFile() {
        // arrange
        SendMessageRequest payload = new SendMessageRequest(friend.id(), null, null);
        TestResponse response = null;
        MessageCodeBody json = null;

        // act
        try {
            String token = TestUtils.loginGetToken(user.username(), user.password());
            response = TestUtils.jsonRequest("send-message", payload, new String[]{ "token " + token });
            json = response.json(MessageCodeBody.class);
        } catch (Exception e) {
            fail();
        }

        // assert
        assertNotNull(response);
        assertNotNull(json);
        assertEquals(MsgCode.MALFORMED_MESSAGE.name(), json.messageCode());
    }

    @Test
    public void canSendMessageTextToFriend() {
        // arrange
        SendMessageRequest payload = new SendMessageRequest(friend.id(), "Awesome", null);
        TestResponse response = null;
        Message json = null;

        // act
        try {
            String token = TestUtils.loginGetToken(user.username(), user.password());
            response = TestUtils.jsonRequest("send-message", payload, new String[]{ "token " + token });
            json = response.json(Message.class);
        } catch (Exception e) {
            fail();
        }

        // assert
        assertNotNull(response);
        assertNotNull(json);
        assertEquals(json.id(), 5);
    }

    @Test
    public void canSendMessageFileToFriend() {
        // arrange
        File file = new File("file.png");
        SendMessageRequest payload = new SendMessageRequest(friend.id(), null, file.getName());
        TestResponse response = null;
        Message json = null;

        // act
        try {
            String token = TestUtils.loginGetToken(user.username(), user.password());
            response = TestUtils.jsonRequest("send-message", payload, new String[]{ "token " + token });
            json = response.json(Message.class);
        } catch (Exception e) {
            fail();
        }

        // assert
        assertNotNull(response);
        assertNotNull(json);
        assertEquals(json.id(), 5);
    }

    @Test
    public void cannotPutFileWithoutExtension() {
        // arrange
        TestResponse response = null;
        MessageCodeBody json = null;
        File file = new File("uploads/file.png");
        byte[] fileData = null;
        try {
            fileData = Files.readAllBytes(file.toPath());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        // act
        try {
            String token = TestUtils.loginGetToken(user.username(), user.password());
            response = TestUtils.jsonRequest("put-file", fileData, new String[]{ "token " + token });
            json = response.json(MessageCodeBody.class);
        } catch (Exception e) {
            fail();
        }

        // assert
        assertNotNull(response);
        assertNotNull(json);
        assertEquals(MsgCode.MISSING_FILE_EXTENSION_HEADER.name(), json.messageCode());
    }

    @Test
    public void canPutFile() {
        // arrange
        TestResponse response = null;
        UploadedFile json = null;
        File file = new File("uploads/file.png");
        File uploadedFile = null;
        byte[] fileData = null;
        try {
            fileData = Files.readAllBytes(file.toPath());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        // act
        try {
            String token = TestUtils.loginGetToken(user.username(), user.password());
            response = TestUtils.jsonRequest("put-file", fileData, new String[]{ "token " + token }, "png");
            json = response.json(UploadedFile.class);
            uploadedFile = new File("uploads/" + json.filename());
        } catch (Exception e) {
            fail();
        }

        // assert
        assertNotNull(response);
        assertNotNull(json);
        assertTrue(!json.filename.isEmpty());
        assertTrue(uploadedFile.exists());

        if (uploadedFile.exists())
            uploadedFile.delete();
    }

    @Test
    public void cannotGetFileThatNotExists() {
        // arrange
        TestResponse response = null;
        MessageCodeBody json = null;
        UploadedFile fileData = new UploadedFile("notExist.png");

        // act
        try {
            String token = TestUtils.loginGetToken(user.username(), user.password());
            response = TestUtils.jsonRequest("get-file", fileData, new String[]{ "token " + token }, "png");
            json = response.json(MessageCodeBody.class);
        } catch (Exception e) {
            fail();
        }

        // assert
        assertNotNull(response);
        assertNotNull(json);
        assertEquals(MsgCode.FILE_DOES_NOT_EXISTS.name(), json.messageCode());
    }

    @Test
    public void canGetFile() {
        // arrange
        TestResponse response = null;
        String binaryContent = null;
        UploadedFile fileData = new UploadedFile("file.png");

        // act
        try {
            String token = TestUtils.loginGetToken(user.username(), user.password());
            response = TestUtils.jsonRequest("get-file", fileData, new String[]{ "token " + token }, "png");
            binaryContent = response.strbody();
        } catch (Exception e) {
            fail();
        }

        // assert
        assertNotNull(response);
        assertNotNull(binaryContent);
        assertTrue(!binaryContent.isEmpty());
    }
}