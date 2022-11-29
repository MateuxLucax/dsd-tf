package events;

public record ConnectionAddedEvent(
    LiveSocket socket
) implements Event { }
