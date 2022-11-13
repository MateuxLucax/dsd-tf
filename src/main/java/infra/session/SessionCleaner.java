package infra.session;

import java.util.ArrayList;

public class SessionCleaner extends Thread {

    private final SessionManager mgr;
    private final long sleepDuration;

    public SessionCleaner(SessionManager mgr, long sleepDuration) {
        this.mgr = mgr;
        this.sleepDuration = sleepDuration;
    }

    @Override
    public void run() {
        try {
            while (!isInterrupted()) {
                Thread.sleep(sleepDuration);
                this.mgr.cleanExpiredSessions();
            }
        } catch (InterruptedException e) {
            System.err.println("SessionCleaner interrupted!");
            e.printStackTrace();
        }
    }
}
