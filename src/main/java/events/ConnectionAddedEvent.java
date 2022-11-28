package events;

import java.net.Socket;

public record ConnectionAddedEvent(
        long userID,
        String token,
        Socket socket
) implements Event { }
