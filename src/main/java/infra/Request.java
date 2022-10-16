package infra;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Request {

    private final Map<String, String> headers;
    private final byte[] body;

    private Request(Map<String, String> headers, byte[] body) {
        this.headers = Collections.unmodifiableMap(headers);
        this.body = body;
    }

    public Map<String, String> headers() {
        return this.headers;
    }

    public byte[] body() {
        return body;
    }

    private static void readHeaders(Map<String,String> headers, InputStream in) throws IOException, MalformedRequestException {
        var buf = new byte[1024];
        var len = 0;
        var count = 0;

        while (true) {
            var c = in.read();

            if (c != -1 && c != '\n' && len < buf.length) {
                buf[len++] = (byte) c;
            }
            else {
                var line = new String(buf, 0, len);
                count++;

                // Blank like marking the end of the header section
                if (line.isBlank()) break;

                var split = line.indexOf(" ");
                if (split == -1) throw MalformedRequestException.invalidHeaderFormat(count);

                var key = line.substring(0, split).trim().toLowerCase();
                var val = line.substring(split+1).trim();

                if (key.isBlank()) throw MalformedRequestException.invalidHeaderFormat(count);
                if (val.isBlank()) throw MalformedRequestException.invalidHeaderFormat(count);

                // TODO also detect invalid characters in header key
                //  only alphanumeric [a-zA-Z0-9] and dash -

                headers.put(key, val);

                // End of request or prepare for next line
                if (c == -1) break;
                len = 0;
            }
        }

        for (var required : new String[]{"body-size", "operation"})
            if (!headers.containsKey(required))
                throw MalformedRequestException.missingHeader(required);
    }

    public static Request from(InputStream in) throws IOException, MalformedRequestException {
        var headers = new HashMap<String, String>();
        readHeaders(headers, in);

        try {
            var size = Integer.parseInt(headers.get("body-size"));
            if (size < 0) throw MalformedRequestException.invalidHeaderValue("body-size");

            // could use readNBytes, but this makes debugging easier
            var body = new byte[size];
            var off = 0;
            while (off < size) {
                var c = in.read();
                body[off++] = (byte) c;
            }

            return new Request(headers, body);
        } catch (NumberFormatException e) {
            throw MalformedRequestException.invalidHeaderValue("body-size");
        }

    }
}
