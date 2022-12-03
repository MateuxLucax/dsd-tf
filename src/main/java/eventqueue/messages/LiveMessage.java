package eventqueue.messages;

public abstract class LiveMessage {

    protected final String type;
    public LiveMessage(String type) {
        this.type = type;
    }
}
