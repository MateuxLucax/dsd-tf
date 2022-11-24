import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class FriendTests {

    @BeforeEach
    public void setup() {
        try {

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

    //
    // get-friends
    //

    private record GetFriendsUserData(
        long id,
        String username,
        String fullname
    ) {}

    @Test
    public void getFriendsHavingFriends() {
        try {

            // arrange
            var token1 = TestUtils.loginGetToken("dude", "123");
            var expectedFriend1 = new GetFriendsUserData(2, "bro", "brother");

            var token2 = TestUtils.loginGetToken("bro", "7890");
            var expectedFriend2 = new GetFriendsUserData(1, "dude", "some dude");

            // act
            var resp1 = TestUtils.makeRequest("get-friends", new byte[]{}, new String[]{"token "+token1});
            var resp2 = TestUtils.makeRequest("get-friends", new byte[]{}, new String[]{"token "+token2});

            // assert
            var type = (new TypeToken<List<GetFriendsUserData>>() {}).getType();

            var json1 = (List<GetFriendsUserData>) TestUtils.GSON.fromJson(resp1.strbody(), type);
            var json2 = (List<GetFriendsUserData>) TestUtils.GSON.fromJson(resp2.strbody(), type);

            assertEquals(1, json1.size());
            assertEquals(expectedFriend1, json1.get(0));

            assertEquals(1, json2.size());
            assertEquals(expectedFriend2, json2.get(0));

        } catch (Exception e) {
            System.err.println(e);
            fail();
        }
    }

    @Test
    public void getFriendsHavingNoFriends() {
        try {

            // arrange
            var token = TestUtils.loginGetToken("friendless", "abcdefg");

            // act
            var resp = TestUtils.makeRequest("get-friends", new byte[]{}, new String[]{"token "+token});
            var json = resp.json(List.class);

            // assert
            assertEquals(0, json.size());

        } catch (Exception e) {
            System.err.println(e);
            fail();
        }
    }

    //
    // remove-friend
    //

    private record RemoveFriendData(
        long friendId
    ) {}

    @Test
    public void removeNonExistingFriend() {
        // if you weren't friends in the first place, technically it got removed

        try {

            // arrange
            var token = TestUtils.loginGetToken("dude", "123");
            var body = new RemoveFriendData(3);

            // act
            var resp = TestUtils.jsonRequest("remove-friend", body, new String[]{"token "+token});

            // assert
            assertTrue(resp.ok());

        } catch (Exception e) {
            System.err.println(e);
            fail();
        }
    }

    @Test
    public void removeExistingFriend() {

        try {

            // arrange
            var token1 = TestUtils.loginGetToken("dude", "123");
            var body1 = new RemoveFriendData(2);

            var token2 = TestUtils.loginGetToken("bro", "7890");

            // act
            var removeResp1 = TestUtils.jsonRequest("remove-friend", body1, new String[]{"token "+token1});

            var getResp1 = TestUtils.makeRequest("get-friends", new byte[]{}, new String[]{"token " + token1});
            var getResp2 = TestUtils.makeRequest("get-friends", new byte[]{}, new String[]{"token " + token2});

            var getJson1 = getResp1.json(List.class);
            var getJson2 = getResp2.json(List.class);

            // assert
            assertTrue(removeResp1.ok());

            assertEquals(0, getJson1.size());
            assertEquals(0, getJson2.size());

        } catch (Exception e) {
            System.err.println(e);
            fail();
        }
    }
}
