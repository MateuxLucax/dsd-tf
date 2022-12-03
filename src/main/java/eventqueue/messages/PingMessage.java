package eventqueue.messages;

public class PingMessage extends LiveMessage {
    public PingMessage() {
        super("ping");
    }
}
