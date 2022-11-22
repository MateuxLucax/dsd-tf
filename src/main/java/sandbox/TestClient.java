package sandbox;

// Pacote pra escrever coisas só pra testar

import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class TestClient {

    public static String makeRequest(String operation, String body, String token, String ...additionalHeaders) throws IOException {
        try (var socket = new Socket("localhost", 8080)) {
            var request = "";
            request += "operation " + operation + '\n';
            request += "body-size " + body.getBytes().length + '\n';
            if (token != null) {
                request += "token " + token + '\n';
            }
            for (var header : additionalHeaders) {
                request += header + '\n';
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

    public static String getResponseBody(String wholeResponse) {
        return wholeResponse.split("\n\n")[1];
    }

    public static String loginGetToken(String username, String password) throws IOException {
        var body = "{\"username\": \""+username+"\", \"password\": \""+password+"\"}";
        var response = makeRequest("create-session", body, null);
        var responseBody = getResponseBody(response);
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

        final var searchBody = "{\"query\": \"\", \"page\": 1}";

        // dude send friend request to bro
        body = "{\"userId\": "+broId+"}";
        makeRequest("send-friend-request", body, dudeToken);

        // list friend requests for both
        makeRequest("get-friend-requests", "", dudeToken);
        makeRequest("get-friend-requests", "", broToken);

        // sends again, should return an error
        body = "{\"userId\": "+broId+"}";
        makeRequest("send-friend-request", body, dudeToken);

        // also check search-users, it needs to show that a friend request has been sent
        makeRequest("search-users", searchBody, dudeToken);
        makeRequest("search-users", searchBody, broToken);

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

        // also check search-users, it needs to show that they're friends
        makeRequest("search-users", searchBody, dudeToken);
        makeRequest("search-users", searchBody, broToken);

        // remove friend
        body = "{\"friendId\": "+broId+"}";
        makeRequest("remove-friend", body, dudeToken);

        // verify that get-friends is empty
        makeRequest("get-friends", "", dudeToken);

        // again, check search-users
        makeRequest("search-users", searchBody, dudeToken);
        makeRequest("search-users", searchBody, broToken);
    }

    public static void testFiles() throws IOException {
        var token = loginGetToken("bro", "123");

        var fileContents = "hello\nthis is a text file\ngoodbye\n:)";
        var response = makeRequest("put-file", fileContents, token, "file-extension txt");

        var responseBody = getResponseBody(response);
        var json = JsonParser.parseString(responseBody);
        var filename = json.getAsJsonObject().getAsJsonPrimitive("filename").getAsString();

        System.out.println("-- Created file filename: " + filename);

        makeRequest("get-file", "{\"filename\": \""+ filename +"\"}", token);
    }

    public static void testTokenExpired() throws IOException {
        String body;
        var token = loginGetToken("bro", "123");
        makeRequest("search-users", "{\"query\": \"\", \"page\": 1}", token);
        makeRequest("end-session", "", token);
        // token expired
        makeRequest("search-users", "{\"query\": \"\", \"page\": 1}", token);
    }

    public static String txtmsg(int to, String text) {
        return String.format("{\"to\": %d, \"textContents\": \"%s\"}", to, text);
    }
    public static String filemsg(int to, String ref) {
        return String.format("{\"to\": %d, \"fileReference\": \"%s\"}", to, ref);
    }

    public static void makeConversation() throws IOException {
        var tokbro = loginGetToken("bro", "444");
        var tokdud = loginGetToken("dude", "123");

        var idbro = 2;
        var iddud = 1;

        makeRequest("send-message", txtmsg(idbro, "hey"), tokdud);
        makeRequest("send-message", txtmsg(idbro, "HEY"), tokdud);
        makeRequest("send-message", txtmsg(iddud, "what"), tokbro);
        makeRequest("send-message", txtmsg(idbro, "how much is 2+2"), tokdud);
        makeRequest("send-message", txtmsg(iddud, "idk math is rard"), tokbro);
        makeRequest("send-message", txtmsg(idbro, "english too it seems"), tokdud);
        makeRequest("send-message", txtmsg(iddud, "idk wha t yo'rue talking about"), tokbro);
        makeRequest("send-message", txtmsg(idbro, "forget it"), tokdud);
    }

    public static String getmsg(int userId, int before, int limit) {
        return String.format("{\"friendId\": %d, \"before\": %d, \"limit\": %d}", userId, before, limit);
    }
    public static void testGetMessages() throws IOException {
        var tok = loginGetToken("bro", "444");
        makeRequest("get-messages", getmsg(1, 0, 3), tok);
        makeRequest("get-messages", getmsg(1, 17, 3), tok);
        makeRequest("get-messages", getmsg(1, 14, 3), tok);
    }

    public static void main(String[] args) throws IOException {

        //testFriendRequests();
        //testFiles();
        //testSendMessage();
        //makeConversation();
        testGetMessages();

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
