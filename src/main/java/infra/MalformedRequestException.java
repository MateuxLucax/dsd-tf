package infra;

public class MalformedRequestException extends Exception {

    public MalformedRequestException(String message) {
        super(message);
    }

    public static MalformedRequestException invalidHeader(int lineNumber) {
        return new MalformedRequestException("Invalid header at line " + lineNumber);
    }

    public static MalformedRequestException sizeMismatch() {
        return new MalformedRequestException("Body size in header does not match actual body size");
    }

    public static MalformedRequestException missingHeader(String header) {
        return new MalformedRequestException("Request is missing mandatory header '"+header+"'");
    }

}