package eventqueue.events;

import eventqueue.events.ConnectionAddedEvent;

public sealed interface Event
permits ChatMessageSentEvent, ConnectionAddedEvent, ConnectionRemovedEvent
{
}
