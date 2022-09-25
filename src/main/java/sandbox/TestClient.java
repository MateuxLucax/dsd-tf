package sandbox;

// Pacote pra escrever coisas só pra testar

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class TestClient {

    public static void main(String[] args) throws IOException {

        var body = "{\"username\": \"àdmïn\", \"password\": \"admin\"}";

        var req = String.join("\n", new String[]{
            "OPERATION create-user",
            "Body-Size "+body.getBytes().length,
            "TOKEN sdjasodjasiojdaosjd",
            "TEST something",
            "",
            body
        });

        try (var socket = new Socket("localhost", 80)) {

            var out = socket.getOutputStream();
            out.write(req.getBytes());

            var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            var line = "";

            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }


        }
    }
}
