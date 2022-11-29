package events.messages;

public abstract class EventMessage {

    private final String type;
    public EventMessage(String type) {
        this.type = type;
    }
    public String type() { return type; }
}
