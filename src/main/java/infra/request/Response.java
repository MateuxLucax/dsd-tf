package infra.request;

import java.io.IOException;
import java.io.OutputStream;

public class Response {

    private final boolean ok;
    private final String errKind;
    private final byte[] body;

    public Response(boolean ok, String errKind, byte[] body)  {
        this.ok = ok;
        this.errKind = errKind;
        this.body = body;
    }

    public void writeTo(OutputStream out) throws ResponseWriteException {
        try {
            var status = ok ? "ok" : String.format("err:%s", errKind);
            var statusHeader = String.format("status %s", status);
            var bodySizeHeader = String.format("body-size %d", body.length);

            out.write((statusHeader+'\n').getBytes());
            out.write((bodySizeHeader+'\n').getBytes());
            out.write('\n');
            out.write(body);
        } catch (IOException ex) {
            throw new ResponseWriteException(ex);
        }
    }
}
