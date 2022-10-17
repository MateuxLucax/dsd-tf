package infra;

import java.time.Instant;
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
        System.out.println(Instant.now().toString() + ") Starting SessionCleaner...");
        try {
            while (!isInterrupted()) {
                Thread.sleep(sleepDuration);
                System.out.println(Instant.now().toString() + ") SessionCleaner running...");
                cleanSessions();

                // debug
                System.out.println("Remaining sessions");
                for (var session : mgr.allSessions()) {
                    System.out.printf(
                        "(token: %s, id: %d, createdAt: %s)\n",
                        session.getToken(),
                        session.getUserId(),
                        session.createdAt().toString()
                    );
                }
            }
        } catch (InterruptedException e) {
            System.err.println("SessionCleaner interrupted!");
            e.printStackTrace();
        }
    }
}
