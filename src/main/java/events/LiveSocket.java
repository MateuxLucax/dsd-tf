package events;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.Semaphore;

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

    public InputStream inputStream() {
        return this.sin;
    }

    public OutputStream outputStream() {
        return this.sout;
    }

    //
    // Semaphore methods
    //

    private final static boolean DEBUG = true;

    private void debug(String msg) {
        if (!DEBUG) return;
        var tid = Thread.currentThread().getId();
        var pre = "LiveSocket: Thread " + tid + ": ";
        System.out.println(pre + msg);
    }

    public boolean ioTryAcquire() {
        debug("trying to acquire");
        var ok = ioSema.tryAcquire();
        debug(ok ? "acquired successfully" : "failed to acquire");
        return ok;
    }

    public void ioAcquire() {
        debug("acquiring");
        ioSema.acquireUninterruptibly();
        debug("acquired");
    }

    public void ioRelease() {
        debug("releasing");
        ioSema.release();
    }


}
