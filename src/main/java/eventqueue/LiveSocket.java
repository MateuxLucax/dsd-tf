package eventqueue;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.Semaphore;

// Maybe this should be just 'extends Socket'

public class LiveSocket implements Closeable {

    private final long userID;
    private final String token;

    private final Socket sock;
    private final InputStream sin;
    private final OutputStream sout;

    // semaphore controlling access to the input and output streams
    private final Semaphore ioSema;

    public LiveSocket(long userID, String userToken, Socket sock) throws IOException {
        this.userID = userID;
        this.token = userToken;
        this.sock = sock;
        sin = sock.getInputStream();
        sout = sock.getOutputStream();

        this.ioSema = new Semaphore(1, true);
    }

    public long userID() { return userID; }
    public String userToken() { return token; }
    public Socket socket() { return sock; }

    //
    // Socket methods
    //

    public void close() throws IOException {
        sock.close();
    }

    public boolean isClosed() {
        return sock.isClosed();
    }

    public OutputStream out() {
        return sout;
    }

    public InputStream in() {
        return sin;
    }

    public static byte[] prepareMessage(String msg) {
        var messageBytes = msg.getBytes();
        var sizeBytes = Integer.toString(messageBytes.length).getBytes();

        var buf = new byte[sizeBytes.length + 1 + messageBytes.length];
        var off = 0;

        for (var b : sizeBytes) buf[off++] = b;
        buf[off++] = ' ';
        for (var b : messageBytes) buf[off++] = b;

        return buf;
    }

    public void writeMessage(String msg) throws IOException {
        var prepared = prepareMessage(msg);
        sout.write(prepared);
    }

    // Shortcut
    public boolean tryWrite(byte[] b) throws IOException {
        var ok = ioSema.tryAcquire();
        debug("tryWrite " + (ok ? "ok" : "failed"));
        if (ok) {
            sout.write(b);
            ioSema.release();
        }
        return ok;
    }

    public void setSoTimeout(int timeout) throws SocketException {
        sock.setSoTimeout(timeout);
    }

    //
    // Semaphore methods
    //

    private final static boolean DEBUG = false;

    private void debug(String msg) {
        if (!DEBUG) return;
        var tid = Thread.currentThread().getId();
        var pre = "LiveSocket: Thread " + tid + ": ";
        System.out.println(pre + msg);
    }

    public void ioAcquire() {
        ioSema.acquireUninterruptibly();
    }

    public void ioRelease() {
        ioSema.release();
    }

}
