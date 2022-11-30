package eventqueue.messages;

public abstract class EventMessage {

    protected final String type;
    public EventMessage(String type) {
        this.type = type;
    }
}
