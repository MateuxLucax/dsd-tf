import com.google.gson.reflect.TypeToken;
import infra.request.MessageCodeBody;
import infra.request.MsgCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class FriendRequestTests {

  @BeforeEach
  public void setup() {
    try {
      TestUtils.clearTestsDatabase();
      TestUtils.runSql("INSERT INTO users (id, username, fullname, password) VALUES (1, 'dude', 'some dude', '123')");
      TestUtils.runSql("INSERT INTO users (id, username, fullname, password) VALUES (2, 'bro', 'brother', '8989')");
      TestUtils.runSql("INSERT INTO users (id, username, fullname, password) VALUES (3, 'admin', 'Administrator', '7890')");
      TestUtils.runSql("INSERT INTO users (id, username, fullname, password) VALUES (4, 'lonely', 'asdf', '7890')");
      TestUtils.runSql("INSERT INTO friend_requests (sender_id, receiver_id) VALUES (1, 2)");
      TestUtils.runSql("INSERT INTO friend_requests (sender_id, receiver_id) VALUES (2, 3)");
    } catch (SQLException e) {
      System.err.println(e);
      fail();
    }
  }

  @AfterEach
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
      var sndExpected = new FriendRequestData(bro, admin, "");

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
      var token = TestUtils.loginGetToken("lonely", "7890");

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

  //
  // send-friend-request
  //

  private record SendFriendRequestData(
    long userId
  ) {}

  @Test
  public void sendFriendRequestCorrectly() {
    try {

      // test whether the send-friend-request operation works corretly
      // and also that after sending a request the receiver has the request in his get-friend-requests result

      // arrange
      var senderToken = TestUtils.loginGetToken("dude", "123");
      var senderBody = new SendFriendRequestData(4);

      var receiverToken = TestUtils.loginGetToken("lonely", "7890");
      var senderData = new UserData(1, "dude", "some dude");

      // act
      var sendResponse = TestUtils.jsonRequest("send-friend-request", senderBody, new String[]{"token "+senderToken});
      var sendJson = sendResponse.json(MessageCodeBody.class);

      var getResponse = TestUtils.makeRequest("get-friend-requests", new byte[]{}, new String[]{"token "+receiverToken});

      var getTypeToken = new TypeToken<List<FriendRequestData>>() {};
      var getJson = TestUtils.GSON.fromJson(new String(getResponse.body()), getTypeToken);

      // assert
      assertTrue(sendResponse.ok());
      assertEquals(MsgCode.SENT_FRIEND_REQUEST_SUCCESSFULLY.name(), sendJson.messageCode());

      assertEquals(1, getJson.size());
      assertEquals(senderData, getJson.get(0).from());

    } catch (Exception e) {
      System.err.println(e);
      fail();
    }
  }

  @Test
  public void sendFriendRequestWhenAlreadySent() {
    try {

      // arrange
      var token = TestUtils.loginGetToken("bro", "8989");
      var body = new SendFriendRequestData(1);

      // act
      var resp = TestUtils.jsonRequest("send-friend-request", body, new String[]{"token "+token});
      var json = resp.json(MessageCodeBody.class);

      // assert
      assertFalse(resp.ok());
      assertEquals(MsgCode.THEY_ALREADY_SENT_FRIEND_REQUEST.name(), json.messageCode());

    } catch (Exception e) {
      System.err.println(e);
      fail();
    }
  }

  //
  // finish-friend-request
  //

  private record FinishFriendRequestBody(
    long senderId,
    boolean accepted
  ) {}

  private record GetFriendsUserData(
    long id,
    String username,
    String fullname
  ) {}

  @Test
  public void finishFriendRequestCorrectlyAccepting() {
    try {

      // arrange
      var token = TestUtils.loginGetToken("bro", "8989");
      var finishBody = new FinishFriendRequestBody(1, true);

      // act
      var finishResp = TestUtils.jsonRequest("finish-friend-request", finishBody, new String[]{"token "+token});
      var finishJson = finishResp.json(MessageCodeBody.class);

      var friendsResp = TestUtils.makeRequest("get-friends", new byte[]{}, new String[]{"token "+token});
      var typeToken = new TypeToken<List<GetFriendsUserData>>(){};
      var friendsJson = (List<GetFriendsUserData>) TestUtils.GSON.fromJson(new String(friendsResp.body()), typeToken.getType());

      // assert
      assertTrue(finishResp.ok());
      assertEquals(
        MsgCode.FINISHED_FRIEND_REQUEST_SUCCESSFULLY.name(),
        finishJson.messageCode()
      );

      assertEquals(1, friendsJson.size());
      assertEquals(
        new GetFriendsUserData(1, "dude", "some dude"),
        friendsJson.get(0)
      );

    } catch (Exception e) {
      System.err.println(e);
      fail();
    }
  }

  @Test
  public void finishNonExistingFriendRequest() {
    try {
      // arrange
      var token = TestUtils.loginGetToken("dude", "123");
      var body = new FinishFriendRequestBody(4, true);

      // act
      var resp = TestUtils.jsonRequest("finish-friend-request", body, new String[]{"token "+token});
      var json = resp.json(MessageCodeBody.class);

      // assert
      assertFalse(resp.ok());
      assertEquals(MsgCode.FRIEND_REQUEST_NOT_FOUND.name(), json.messageCode());


    } catch (Exception e) {
      System.err.println(e);
      fail();
    }
  }

}
