package infra;

public class MalformedRequestException extends Exception {

    public MalformedRequestException(String message) {
        super(message);
    }

    // TODO rework this to work with error code returns
    //  (currently the extra information in the exception message is thrown away, we just return ErrCode.MALFORMED_REQUEST anyway)

    public static MalformedRequestException invalidHeaderFormat(int lineNumber) {
        return new MalformedRequestException("Invalid header format at line " + lineNumber);
    }

    public static MalformedRequestException invalidHeaderValue(String header) {
        return new MalformedRequestException("Invalid value for header " + header);
    }

    public static MalformedRequestException sizeMismatch() {
        return new MalformedRequestException("Body size in header does not match actual body size");
    }

    public static MalformedRequestException missingHeader(String header) {
        return new MalformedRequestException("Request is missing mandatory header '"+header+"'");
    }

}