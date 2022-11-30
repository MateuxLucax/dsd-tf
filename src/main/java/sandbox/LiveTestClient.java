package sandbox;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class LiveTestClient {

    private static class ListenLiveSocket extends Thread {

        private String user;
        private Socket socket;
        private InputStream in;

        public ListenLiveSocket(String user, Socket socket) throws IOException {
            this.user = user;
            this.socket = socket;
            this.in = socket.getInputStream();
        }

        public void run() {
            try {
                var c = -1;
                var buf = new byte[1024];
                var off = 0;

                var size = 0;
                var readingSize = true;

                while (!isInterrupted() && (c = in.read()) != -1) {
                    if (readingSize) {
                        if (c == ' ') {
                            var sizeString = new String(buf, 0, off);
                            size = Integer.parseInt(sizeString);
                            readingSize = false;
                            off = 0;
                        } else buf[off++] = (byte) c;
                    }
                    else { // reading message body
                        buf[off++] = (byte) c;
                        if (off == size) {
                            var message = new String(buf, 0, size);
                            System.out.println("Live message to " + user + ": " + message);

                            readingSize = true;
                            off = 0;
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("ListenLiveSocket exception!");
                System.err.println(e);
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        {
            var body = "{\"username\": \"admin\", \"fullname\": \"Administrator\", \"password\": \"abc\"}";
            TestClient.makeRequest("create-user", body, null);

            var token = TestClient.loginGetToken("admin", "abc");

            var liveSocket = new Socket("localhost", 8080);
            TestClient.makeRequestWith(liveSocket, "go-online", "", token, new String[]{});

            // In the client we need to make sure that we only start reading from
            // the live socket after we've read the whole response

            // Also, we need to be very careful in the usual response reading code
            // to guarantee that it does not read any more characters than it needs

            // Otherwise when we get a message the first character (part of the
            // message size) could be missing


            var thread = new ListenLiveSocket("admin", liveSocket);
            thread.start();
        }

        Thread.sleep(2000);

        {
            var body = "{\"username\": \"dude\", \"fullname\": \"some dude\", \"password\": \"password\"}";
            TestClient.makeRequest("create-user", body, null);

            var token = TestClient.loginGetToken("dude", "password");

            var liveSocket = new Socket("localhost", 8080);
            TestClient.makeRequestWith(liveSocket, "go-online", "", token, new String[]{});

            new ListenLiveSocket("dude", liveSocket).start();
        }

    }

}
