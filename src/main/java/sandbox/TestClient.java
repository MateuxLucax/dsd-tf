package sandbox;

// Pacote pra escrever coisas só pra testar

import com.google.gson.JsonParser;
import infra.MsgCode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class TestClient {

    public static void main(String[] args) throws IOException {

        try (var socket = new Socket("localhost", 80)) {
            var body = "{\"username\": \"admin\", \"password\": \"123\", \"fullname\": \"joao jose da silva\"}";

            var req = String.join("\n", new String[]{
                "OPERATION create-user",
                "Body-Size " + body.getBytes().length,
                "TOKEN sdjasodjasiojdaosjd",
                "TEST something",
                "",
                body
            });
            requestThenPrintResponse(socket, req);
        }

        var token = "";

        try (var socket = new Socket("localhost", 80)) {
            var body = "{\"username\": \"admin\", \"password\": \"123\"}";

            var req = String.join("\n", new String[]{
                "body-Size " + body.getBytes().length,
                "operation create-session",
                "",
                body
            });

            var response = requestThenPrintResponse(socket, req);
            var responseBody = response.split("\n\n")[1];

            token = JsonParser.parseString(responseBody).getAsJsonObject().getAsJsonPrimitive("token").getAsString();
        }

        try (var socket = new Socket("localhost", 80)) {
            // body always necessary even if empty

            var req = String.join("\n", new String[]{
                "body-size 0",
                "operation whoami",
                "token " + token,
                "",
                ""
            });

            requestThenPrintResponse(socket, req);
        }

        try (var socket = new Socket("localhost", 80)) {
            var body = "{\"search\": \"\", \"page\": 1}";
            var req = String.join("\n", new String[]{
                "body-size " + body.getBytes().length,
                "operation search-users",
                "token " + token,
                "",
                body
            });

            requestThenPrintResponse(socket, req);
        }

        try (var socket = new Socket("localhost", 80)) {
            var body = "{\"userId\": 9}";
            var req = String.join("\n", new String[] {
                "Body-Size "+body.getBytes().length,
                "TOKEN "+token,
                "Operation send-friend-request",
                "",
                body
            });

            requestThenPrintResponse(socket, req);
        }

        try (var socket = new Socket("localhost", 80)) {
            var req = String.join("\n", new String[]{
                "body-size 0",
                "token "+token,
                "operation get-friend-requests",
                "",
                ""
            });

            requestThenPrintResponse(socket, req);
        }

        try (var socket = new Socket("localhost", 80)) {
            var req = String.join("\n", new String[]{
                "operation get-all-error-codes",
                "body-size 0",
                "",
                ""
            });
            requestThenPrintResponse(socket, req);
        }

        try (var socket = new Socket("localhost", 80)) {
            var req = String.join("\n", new String[]{
                "operation get-index",
                "body-size 0",
                "",
                ""
            });
            requestThenPrintResponse(socket, req);
        }

        var op1 = MsgCode.from("jiajdoas");
        System.out.println(op1.isPresent() ? op1.get() : "no");

        var op2 = MsgCode.from("NO_USER_WITH_GIVEN_ID");
        System.out.println(op2.isPresent() ? op2.get() : "no");
    }

    private static String requestThenPrintResponse(Socket socket, String req) throws IOException {
        System.out.println("---- REQUEST ----");
        System.out.println(req);

        var out = socket.getOutputStream();
        out.write(req.getBytes());

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
