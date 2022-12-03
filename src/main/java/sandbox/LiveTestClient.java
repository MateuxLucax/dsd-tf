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
                            System.out.println("----- LIVE MESSAGE TO " + user + " -----\n" + message);

                            readingSize = true;
                            off = 0;
                        }
                    }
                }

                System.out.println(" >>> Live socket "+user+" closed ");
            } catch (Exception e) {
                System.err.println("ListenLiveSocket exception!");
                System.err.println(e);
            }
        }
    }

    public static void test0() throws IOException, InterruptedException {

        var adminToken = "";
        var anotherAdminToken = "";
        var dudeToken = "";

        var sleepBetween = 5000;

        {
            var body = "{\"username\": \"admin\", \"fullname\": \"Administrator\", \"password\": \"abc\"}";
            TestClient.makeRequest("create-user", body, null);

            adminToken = TestClient.loginGetToken("admin", "abc");

            var liveSocket = new Socket("localhost", 8080);
            TestClient.makeRequestWith(liveSocket, "go-online", "", adminToken, new String[]{});

            // In the client we need to make sure that we only start reading from
            // the live socket after we've read the whole response

            // Also, we need to be very careful in the usual response reading code
            // to guarantee that it does not read any more characters than it needs

            // Otherwise when we get a message the first character (part of the
            // message size) could be missing


            var thread = new ListenLiveSocket("admin", liveSocket);
            thread.start();
        }

        Thread.sleep(sleepBetween/2);
        System.out.println("Now dude is about to go online...");
        Thread.sleep(sleepBetween/2);

        {
            var body = "{\"username\": \"dude\", \"fullname\": \"some dude\", \"password\": \"password\"}";
            TestClient.makeRequest("create-user", body, null);

            dudeToken = TestClient.loginGetToken("dude", "password");

            var liveSocket = new Socket("localhost", 8080);
            TestClient.makeRequestWith(liveSocket, "go-online", "", dudeToken, new String[]{});

            new ListenLiveSocket("dude", liveSocket).start();
        }

        Thread.sleep(sleepBetween/2);
        System.out.println("Admin will login and go online on another device. 'dude' shouldn't get the user-logged-in message again");
        Thread.sleep(sleepBetween/2);

        {
            anotherAdminToken = TestClient.loginGetToken("admin", "abc");
            var liveSocket = new Socket("localhost", 8080);
            TestClient.makeRequestWith(liveSocket, "go-online", "", anotherAdminToken, new String[]{});

            new ListenLiveSocket("admin (ON ANOTHER DEVICE)", liveSocket).start();
        }

        Thread.sleep(sleepBetween/2);
        System.out.println("Now admin is about to go offline");
        Thread.sleep(sleepBetween/2);

        {
            TestClient.makeRequest("go-offline", "", adminToken);
        }

        Thread.sleep(sleepBetween/2);
        System.out.println("Now admin on another device is about to go offline, so 'dude' should get a 'user-offline' message");
        Thread.sleep(sleepBetween/2);

        {
            TestClient.makeRequest("go-offline", "", anotherAdminToken);
        }
    }

    public static void test1() throws IOException {

        var adminToken = TestClient.loginGetToken("admin", "123456789");

        var liveSocket = new Socket("localhost", 8080);
        TestClient.makeRequestWith(liveSocket, "go-online", "", adminToken, new String[]{});

        var liveThread = new ListenLiveSocket("admin", liveSocket);
        liveThread.start();
    }

    public static void main(String[] args) throws IOException {
        test1();
    }

}
