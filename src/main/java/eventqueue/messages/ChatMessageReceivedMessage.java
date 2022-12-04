package eventqueue.messages;

import eventqueue.events.ChatMessageSentEvent;

public class ChatMessageReceivedMessage extends LiveMessage {

    private final long from;
    private final String textContents;
    private final String fileReference;
    private final String sentAt;

    public ChatMessageReceivedMessage(long from, String textContents, String fileReference, String sentAt) {
        super("chat-message-received");
        this.from = from;
        this.textContents = textContents;
        this.fileReference = fileReference;
        this.sentAt = sentAt;
    }

    public static ChatMessageReceivedMessage from(ChatMessageSentEvent chat) {
        return new ChatMessageReceivedMessage(chat.senderID(),chat.textContents(), chat.fileReference(), chat.sentAt().toString());
    }
}
