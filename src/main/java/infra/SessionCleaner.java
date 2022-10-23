package infra;

import java.util.ArrayList;

public class SessionCleaner extends Thread {

    private final SessionManager mgr;
    private final long sleepDuration;

    public SessionCleaner(SessionManager mgr, long sleepDuration) {
        this.mgr = mgr;
        this.sleepDuration = sleepDuration;
    }

    public void cleanSessions() {
        var toRemove = new ArrayList<SessionData>();
        for (var session : mgr.allSessions())
            if (session.isExpired())
                toRemove.add(session);
        toRemove.forEach(mgr::removeSession);
    }

    @Override
    public void run() {
        try {
            while (!isInterrupted()) {
                Thread.sleep(sleepDuration);
                cleanSessions();
            }
        } catch (InterruptedException e) {
            System.err.println("SessionCleaner interrupted!");
            e.printStackTrace();
        }
    }
}
