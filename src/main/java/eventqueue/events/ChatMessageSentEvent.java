package eventqueue.events;

import java.time.Instant;

public record ChatMessageSentEvent(
    long id,
    long senderID,
    long receiverID,
    String textContents,
    String fileReference,
    Instant sentAt
) implements Event { }
