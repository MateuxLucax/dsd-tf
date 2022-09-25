package infra;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Request {

    // bodySize and operation are also headers, but are mandatory headers, hence they're properties
    private final Map<String, String> headers;
    private       long bodySize;
    private       String operation;
    private final InputStream body;

    public Request(Map<String, String> headers, InputStream body) throws MalformedRequestException {
        this.headers = Collections.unmodifiableMap(headers);
        this.body = body;

        if (!headers.containsKey("body-size")) {
            throw MalformedRequestException.missingHeader("body-size");
        }
        if (!headers.containsKey("operation")) {
            throw MalformedRequestException.missingHeader("operation");
        }

        this.bodySize = Long.parseLong(headers.get("body-size"));
        this.operation = headers.get("operation");
    }

    public Map<String, String> headers() { return this.headers; }
    public long bodySize() { return this.bodySize; }
    public String operation() { return this.operation; }
    public InputStream body() { return body; }

    public String readBody() throws IOException, MalformedRequestException {

        var builder = new ByteArrayOutputStream(); // acts as a StringBuilder but for bytes
        var total = 0;

        var buf = new byte[1024];

        // TODO since it's request-response, the client will send the request and wait for the response, leaving the connection open
        //  which means our inputStream will remain open and .read() will block indefinitely after we've read everything
        //  the purpose of bodySize is precisely to avoid this: we stop when we have read bodySize bytes
        //  the problem is: if the body has less bytes than given at bodySize, .read() will block the thread indefinitely
        //  so we need to implement a timeout

        while (total < bodySize) {
            var n = body.read(buf);
            if (n < 0) throw MalformedRequestException.sizeMismatch();
            if (total + n > bodySize) throw MalformedRequestException.sizeMismatch();

            builder.write(buf, 0, n);
            total += n;
        }

        if (total < bodySize) throw MalformedRequestException.sizeMismatch();

        return builder.toString();
    }

    public static Request from(InputStream in) throws IOException, MalformedRequestException {

        final var MAX_LINE = 1024;

        var headers = new HashMap<String, String>();

        var line = new byte[MAX_LINE];
        var lineCount = 0;
        var lineLength = 0;

        while (true) {
            var c = in.read();

            if (c == -1 || c == '\n' || lineLength == MAX_LINE)
            {
                // Just ended reading a line

                var lineString = new String(line, 0, lineLength);

                lineCount++;

                // Black line, end of header section
                if (lineString.isBlank()) break;

                var splitPoint = lineString.indexOf(" ");
                if (splitPoint == -1) throw MalformedRequestException.invalidHeaderFormat(lineCount);

                var key = lineString.substring(0, splitPoint).trim().toLowerCase();
                var value = lineString.substring(splitPoint+1).trim();

                if (key.isBlank()) throw MalformedRequestException.invalidHeaderFormat(lineCount);
                if (value.isBlank()) throw MalformedRequestException.invalidHeaderFormat(lineCount);

                headers.put(key, value);

                // End of request
                if (c == -1) break;

                // Start reading next line
                lineLength = 0;
            }
            else
            {
                // Still reading a line
                line[lineLength++] = (byte) c;
            }
        }

        // At this point in.read() will read from the body of the request, so we can just use the same stream
        return new Request(headers, in);
    }
}
