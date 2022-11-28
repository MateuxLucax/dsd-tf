package events;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

// TODO to prevent idle sockets, periodically ping the sockets here and remove and close the ones
//  from which we get no response after some time

// another thread can enqueue the ping event, then the ping'ing is actually done by the event queue
// although it would block the event loop
// we'll actually need some concurrency control anyway

// many threads can ping many clients
// so we'll need a lock for each socket
// because we can't have the ping thread and the event loop both writing to the socket at the same time

// whenever the event loop tries to acquire a socket but it's held by some ping thread
// it can just enqueue the message again for later instead of blocking until the ping is completed


// Map<Long, Map<String, LiveSocket>>
// LiveSocket = socket, userid, token, lock, ...?
// also could store the input and output stream in variables because the get methods for these throw exceptions
// (deal with the exceptions just in the constructor or something, not every other method needs a 'throws' at the end too)

public class OnlineUsers {

    // map(userId -> map(token -> socket))
    // We use the token to identify the device
    // So we know which socket to remove from our list when the user goes offline on a given device
    private final Map<Long, Map<String, Socket>> data;

    public OnlineUsers() {
        data = new HashMap<>();
    }

    public void addConnection(long userId, String token, Socket socket) throws IOException {
        var sockets = data.computeIfAbsent(userId, id -> new HashMap<String, Socket>());

        // Prevent idle socket
        // This shouldn't happen, if it happens it's an error in the client
        var oldSocket = sockets.get(token);
        if (oldSocket != null && !oldSocket.isClosed()) {
            oldSocket.close();
        }

        sockets.put(token, socket);
    }

    public void removeConnection(long userId, String token) throws IOException {
        var sockets = data.get(userId);
        if (!sockets.isEmpty()) {
            var socket = sockets.remove(token);
            socket.close();
        }
    }

}
