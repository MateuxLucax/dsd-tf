// TODO distinguish between internal errors with "internal": true of "internal": false
// but that will require a different record for {status: ok, message} (we don't want {status: ok, internal, message}
public record MessageResponse(String status, String message) {
    public static MessageResponse ok(String message) {
        return new MessageResponse("ok", message);
    }
    public static MessageResponse error(String message) {
        return new MessageResponse("error", message);
    }
}
