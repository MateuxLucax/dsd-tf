import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class TestResponse {

    private final Map<String, String> headers;
    private final byte[] body;

    private final boolean ok;
    private final String err;

    public TestResponse(Map<String, String> headers, byte[] body) {
        this.headers = headers;
        this.body = body;

        var status = headers.get("status").split(":");
        this.ok = status[0].equals("ok");
        this.err = this.ok ? null : status[1];
    }

    public Map<String, String> headers() { return headers; }
    public byte[] body() { return body; }
    public boolean ok() { return ok; }
    public String err() { return err; }

    public <T> T json(Class<T> c) {
        return TestUtils.GSON.fromJson(new String(this.body), c);
    }

    public String headersToString() {
        var s = "";
        for (var header : headers.entrySet()) {
            var k = header.getKey();
            var v = header.getValue();
            s += k + " " + v + "\n";
        }
        return s;
    }

    public String toString() {
        var s = "-- RESPONSE --";
        s += headersToString();
        s += new String(body);
        s += "\n";
        s += "----";
        return s;
    }
}
