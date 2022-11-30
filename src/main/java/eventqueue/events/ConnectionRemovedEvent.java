package eventqueue.events;

import eventqueue.LiveSocket;

public record ConnectionRemovedEvent(
    long userID,
    String token
) implements Event { }
