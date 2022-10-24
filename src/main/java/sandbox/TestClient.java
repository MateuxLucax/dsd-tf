package sandbox;

// Pacote pra escrever coisas só pra testar

import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class TestClient {

    public static String makeRequest(String operation, String body, String token) throws IOException {
        try (var socket = new Socket("localhost", 1234)) {
            var request = "";
            request += "operation " + operation + '\n';
            request += "body-size " + body.getBytes().length + '\n';
            if (token != null) {
                request += "token " + token + '\n';
            }
            request += "\n";
            request += body;

            System.out.println("--- REQUEST ---");
            System.out.println(request);

            var out = socket.getOutputStream();
            out.write(request.getBytes());

            var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            var line = "";

            var sb = new StringBuilder();
            System.out.println("---- RESPONSE ----");
            while ((line = in.readLine()) != null) {
                System.out.println(line);
                sb.append(line).append('\n');
            }
            return sb.toString();
        }
    }

    public static String loginGetToken(String username, String password) throws IOException {
        var body = "{\"username\": \""+username+"\", \"password\": \""+password+"\"}";
        var response = makeRequest("create-session", body, null);
        var responseBody = response.split("\n")[3];
        var token = JsonParser.parseString(responseBody).getAsJsonObject().getAsJsonPrimitive("token").getAsString();
        return token;
    }

    public static void testFriendRequests() throws IOException {
        String body;

        //body = "{\"username\": \"dude\", \"fullname\": \"john dude\", \"password\": \"123\"}";
        //makeRequest("create-user", body, null);
        var dudeToken = loginGetToken("dude", "123");

        //body = "{\"username\": \"bro\", \"fullname\": \"bro smith\", \"password\": \"123\"}";
        //makeRequest("create-user", body, null);
        var broToken = loginGetToken("bro", "123");

        final var dudeId = 1;
        final var broId = 2;

        // dude send friend request to bro
        body = "{\"userId\": "+broId+"}";
        makeRequest("send-friend-request", body, dudeToken);

        // list friend requests for both
        makeRequest("get-friend-requests", "", dudeToken);
        makeRequest("get-friend-requests", "", broToken);

        // sends again, should return an error
        body = "{\"userId\": "+broId+"}";
        makeRequest("send-friend-request", body, dudeToken);

        // bro accepts the request
        body = "{\"senderId\": "+dudeId+", \"accepted\": true}";
        makeRequest("finish-friend-request", body, broToken);

        // dude can't accept his own request, should return an error
        body = "{\"senderId\": "+dudeId+", \"accepted\": true}";
        makeRequest("finish-friend-request", body, dudeToken);

        // list friend requests for both, should be empty
        makeRequest("get-friend-requests", "", dudeToken);
        makeRequest("get-friend-requests", "", broToken);

        makeRequest("get-friends", "", dudeToken);
        makeRequest("get-friends", "", broToken);

        // remove friend
        body = "{\"friendId\": "+broId+"}";
        makeRequest("remove-friend", body, dudeToken);

        // verify that get-friends is empty
        makeRequest("get-friends", "", dudeToken);
    }

    public static void main(String[] args) throws IOException {

        testFriendRequests();


        /*
        String body;

        body = "{\"username\": \"admin\", \"password\": \"123\", \"fullname\": \"joao jose da silva\"}";
        makeRequest("create-user", body, null);

        var token = loginGetToken("admin", "123");

        makeRequest("whoami", "", token);

        body = "{\"search\": \"\", \"page\": 1}";
        makeRequest("search-users", body, token);

        body = "{\"userId\": 9}";
        makeRequest("send-friend-request", body, token);

        makeRequest("get-friend-requests", "", token);

        makeRequest("get-all-error-codes", "", token);

        makeRequest("get-index", "", token);
         */
    }
}
