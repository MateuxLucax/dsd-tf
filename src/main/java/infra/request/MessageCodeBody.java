package infra.request;

// To be converted into JSON: { messageCode: ... }

public record MessageCodeBody(String messageCode) {
    public static MessageCodeBody from(MsgCode code) {

        // TODO ensure toString() doesn't include class name?

        return new MessageCodeBody(code.toString());
    }
}
