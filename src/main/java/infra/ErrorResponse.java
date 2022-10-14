package infra;

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

    private String kind;
    private String message;

    public ErrorResponse(String kind, String message) {
        this.kind = kind;
        this.message = message;
    }

    public String getKind() { return kind; }
    public String getMessage() { return message; }

    public MessageBody toBody() { return new MessageBody(message); }
}
