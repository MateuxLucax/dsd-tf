package infra.request;

import java.io.IOException;

public class ResponseWriteException extends Exception {

    private final IOException ex;

    public ResponseWriteException(IOException underlyingException) {
        this.ex = underlyingException;
    }

    public IOException getIOException() {
        return ex;
    }
}
