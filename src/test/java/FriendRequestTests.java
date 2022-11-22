import com.google.gson.reflect.TypeToken;
import friends.GetFriendRequests;
import org.junit.*;

import java.sql.SQLException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class FriendRequestTests {

  @Before
  public void setup() {
    try {
      TestUtils.clearTestsDatabase();
      TestUtils.runSql("INSERT INTO users (id, username, fullname, password) VALUES (1, 'dude', 'some dude', '123')");
      TestUtils.runSql("INSERT INTO users (id, username, fullname, password) VALUES (2, 'bro', 'brother', '8989')");
      TestUtils.runSql("INSERT INTO users (id, username, fullname, password) VALUES (3, 'admin', 'Administrator', '7890')");
      TestUtils.runSql("INSERT INTO users (id, username, fullname, password) VALUES (4, 'someone', 'asdf', '7890')");
      TestUtils.runSql("INSERT INTO friend_requests (sender_id, receiver_id) VALUES (1, 2)");
      TestUtils.runSql("INSERT INTO friend_requests (sender_id, receiver_id) VALUES (3, 2)");
    } catch (SQLException e) {
      System.err.println(e);
      fail();
    }
  }

  @After
  public void teardown() {
    try {
      TestUtils.clearTestsDatabase();
    } catch (SQLException e) {
      System.err.println(e);
      fail();
    }
  }

  //
  // get-friend-requests
  //

  private record UserData(
    int id,
    String username,
    String fullname
  ) {}

  private record FriendRequestData(
    UserData from,
    UserData to,
    String createdAt
  ) {}

  @Test
  public void getFriendRequestsShowsRequests() {
    try {
      // arrange
      var token = TestUtils.loginGetToken("bro", "8989");

      var dude  = new UserData(1, "dude", "some dude");
      var bro   = new UserData(2, "bro", "brother");
      var admin = new UserData(3, "admin", "Administrator");

      // won't compare createdAt
      var fstExpected = new FriendRequestData(dude, bro, "");
      var sndExpected = new FriendRequestData(admin, bro, "");

      // act
      var resp = TestUtils.makeRequest("get-friend-requests", new byte[]{}, new String[]{"token "+token});
      var typeToken = new TypeToken<ArrayList<FriendRequestData>>() {};
      var json = (ArrayList<FriendRequestData>) TestUtils.GSON.fromJson(new String(resp.body()), typeToken.getType());

      // assert
      assertEquals(2, json.size());

      assertEquals(fstExpected.from(), json.get(0).from());
      assertEquals(fstExpected.to(), json.get(0).to());

      assertEquals(sndExpected.from(), json.get(1).from());
      assertEquals(sndExpected.to(), json.get(1).to());

    } catch (Exception e) {
      System.err.println(e);
      fail();
    }
  }

  @Test
  public void getFriendRequestsEmpty() {
    try {
      // arrange
      var token = TestUtils.loginGetToken("someone", "7890");

      // act
      var resp = TestUtils.makeRequest("get-friend-requests", new byte[]{}, new String[]{"token "+token});
      var typeToken = new TypeToken<ArrayList<FriendRequestData>>() {};
      var json = (ArrayList<FriendRequestData>) TestUtils.GSON.fromJson(new String(resp.body()), typeToken.getType());

      // assert
      assertEquals(0, json.size());
    } catch (Exception e) {
      System.err.println(e);
      fail();
    }
  }

}
