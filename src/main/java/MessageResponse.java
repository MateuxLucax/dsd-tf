public record MessageResponse(String status, String message) {
    public static MessageResponse ok(String message) {
        return new MessageResponse("ok", message);
    }
    public static MessageResponse error(String message) {
        return new MessageResponse("error", message);
    }
}
