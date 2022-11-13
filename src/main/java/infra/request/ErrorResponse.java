package infra.request;

/**
 * This class is intended to be used in request handlers performing database transactions when you need to roll back.
 * Without this, we'd need to use the Exception class, but there are some kinds of exceptions we actually want to keep propagating up.
 *
 * var conn = Database.getConnection()
 * conn.setAutoCommit(false);
 *
 * try {
 *
 *     if (somethingWrong)
 *         throw new ErrorResponse("badRequest", "That is not allowed");
 *
 *     try {
 *         ... some IO operation ...
 *     } catch (IOException e) {
 *         throw new ErrorResponse("internal", "Couldn't open the file");
 *     }
 *
 * } catch (ErrorResponse e) {
 *     response.writeErrorResponse(e);
 *     conn.rollback();
 * }
 */

public class ErrorResponse extends Throwable {

    private final String kind; // like http status codes
    private final MsgCode code; // replaces what would be a 'message'
    // client is responsible for translating the code into the appropriate human-readable message
    // possibly in different languages

    public ErrorResponse(String kind, MsgCode code) {
        this.kind = kind;
        this.code = code;
    }

    public String getKind() { return kind; }
    public MsgCode getCode() { return code; }

    public MessageCodeBody toResponseBody() { return new MessageCodeBody(code.toString()); }
}
