package eventqueue.events;

import eventqueue.LiveSocket;

public record ConnectionAddedEvent(
    LiveSocket socket
) implements Event { }
