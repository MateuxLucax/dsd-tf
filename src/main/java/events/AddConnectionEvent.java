package events;

import java.net.Socket;

public record AddConnectionEvent(
        long userID,
        String token,
        Socket socket
) implements Event { }
