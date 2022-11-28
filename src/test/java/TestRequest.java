import infra.request.MalformedRequestException;
import infra.request.Request;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

// Se algum teste falhar, não corrigir, porque relatar os erros faz parte da próxima entrega

public class TestRequest {

    // Mock input stream that returns a text
    public static InputStream inputStreamFrom(String s) {
        return new InputStream() {
            private byte[] bytes = s.getBytes();
            private int off = 0;
            public int read() throws IOException {
                if (off < bytes.length) {
                    return bytes[off++];
                }
                return -1;
            }
        };
    }

    public static void assertMalformed(String s) {
        var in = inputStreamFrom(s);
        assertThrows(MalformedRequestException.class, () -> Request.from(in));
    }

    @Test
    public void incorrectHeadersShouldThrow() {
        assertMalformed("header-sem-espaco");
    }

    @Test
    public void missingHeadersShouldThrow() {
        var req =
            "operation create-session\n" +
            "\n" +
            "{\"username\": \"abc\", \"password\": \"123\"}";
        assertMalformed(req);
    }

    @Test
    public void nonIntegerBodySizeShouldThrow() {
        var req =
            "operation create-session\n" +
            "body-size abcd\n" +
            "\n";
        assertMalformed(req);
    }

    @Test
    public void noEmptyLineAfterHeadersShouldThrow() {
        var req =
            "operation create-session\n" +
            "body-size 0\n";
        assertMalformed(req);
    }

    @Test
    public void tokenShortherThan64ShouldThrow() {
        var req =
            "operation get-friends\n" +
            "body-size 0\n" +
            "token abcdefgh\n" +
            "\n";
        assertMalformed(req);
    }

    @Test
    public void tokenWithInvalidCharacterShouldThrow() {
        // 64 chars but has a !
        var req =
            "operation get-friends\n" +
            "body-size 0\n" +
            "token !bcdefghi0abcdefghi0abcdefghi0abcdefghi0abcdefghi0abcdefghi01234\n";
        assertMalformed(req);
    }

    @Test
    public void parsedCorrectly() {
        var operation = "foo-bar";
        var body = "hello world";
        var bodySize = body.getBytes().length;

        var txt =
            "operation "+operation+"\n" +
            "body-size "+bodySize+"\n" +
            "\n" +
            body;

        try {
            var req = Request.from(inputStreamFrom(txt));
            assertEquals(operation, req.headers().get("operation"));
            assertEquals(bodySize, Integer.parseInt(req.headers().get("body-size")));
            assertEquals(2, req.headers().size());
            assertEquals(body, new String(req.body()));
        } catch (Exception e) {
            System.err.println(e);
            fail();
        }
    }
}
