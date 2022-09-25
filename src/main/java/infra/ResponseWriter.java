package infra;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

// status header is mandatory
// but implicitly has "ok" value if no explicit value is given

public class ResponseWriter {

    private final OutputStream out;

    private boolean wroteStatus;
    private boolean writingBody;

    public ResponseWriter(OutputStream out) {
        this.out = out;
        this.wroteStatus = false;
        this.writingBody = false;
    }

    // Helper to wrap IOException in ResponseWriterException
    private void write(byte[] b) throws ResponseWriteException {
        try {
            out.write(b);
        } catch (IOException e) {
            throw new ResponseWriteException(e);
        }
    }

    private void write(String s) throws ResponseWriteException {
        write(s.getBytes());
    }

    public void writeHeader(String key, String value) throws ResponseWriteException {
        if (writingBody)
            throw new RuntimeException("Cannot write header after having started to write the body");
        if (key.isBlank())
            throw new RuntimeException("Cannot write header with empty key");
        if (value.isBlank())
            throw new RuntimeException("Cannot write header with empty value");

        key = key.trim().toLowerCase();
        value = value.trim();

        var header = key + " " + value + "\n";
        write(header);

        if (key.equals("status"))
            wroteStatus = true;
    }

    private void startBody() throws ResponseWriteException {
        if (!wroteStatus) writeHeader("status", "ok");
        write("\n");  // empty line separating headers from body
        writingBody = true;
    }

    public void writeToBody(String str) throws ResponseWriteException {
        if (!writingBody) startBody();
        write(str);
    }

    public void pipe(InputStream in) throws ResponseWriteException {
        if (!writingBody) startBody();

        var buf = new byte[1024];
        var n = 0;

        try {
            while ((n = in.read(buf)) > 0) {
                out.write(buf, 0, n);
            }
        } catch (IOException e) {
            throw new ResponseWriteException(e);
        }
    }

    public void writeError(String kind, String body) throws ResponseWriteException {
        writeHeader("status", "err:" + kind);
        writeToBody(body + '\n');
    }
}
