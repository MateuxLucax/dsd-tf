package infra.session;


// Cannot create sessions while expired sessions are being cleaned
// Inefficient, scales badly, but it sure works correctly

import java.time.Duration;
import java.util.*;
import java.util.concurrent.Semaphore;

public class SessionManager {

    public static final Duration SESSION_DURATION = Duration.ofHours(1);
    public static final Duration THREAD_SLEEP = Duration.ofMinutes(30); // less than session duration

    private static final Random random = new Random();
    private static final int TOKEN_LENGTH = 64;

    public static boolean charOutsideInvalidRanges(int i) {
        return (i <= 57 || i >= 65) && (i <= 90 || i >= 97);
    }

    public static boolean tokenSyntaxValid(String tok) {
        return tok.length() == TOKEN_LENGTH
            && tok.chars().allMatch(i -> i >= '0' && i <= 'z' && charOutsideInvalidRanges(i));
    }

    public static String makeToken() {
        return random.ints('0', 'z'+1)
            .filter(SessionManager::charOutsideInvalidRanges)
            .limit(TOKEN_LENGTH)
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();
    }

    private final Semaphore sema;

    private final Map<String, SessionData> tokenToSession;
    private final Map<Long, List<String>> idToTokenList;

    public SessionManager() {
        var fair = true;
        sema = new Semaphore(1, fair);

        tokenToSession = new HashMap<>();
        idToTokenList = new HashMap<>();
    }

    public void cleanExpiredSessions() throws InterruptedException {
        sema.acquire();
        var toRemove = new ArrayList<SessionData>();
        for (var session : tokenToSession.values()) {
            if (session.isExpired()) {
                toRemove.add(session);
            }
        }
        for (var session : toRemove) {
            removeSessionImpl(session);
        }
        sema.release();
    }

    public String createSession(long id) throws InterruptedException {
        sema.acquire();
        var token = "";
        do {
            token = makeToken();
        } while (tokenToSession.get(token) != null);

        var data = new SessionData(token, id);
        tokenToSession.put(token, data);

        idToTokenList
        .computeIfAbsent(id, k -> new ArrayList<String>())
        .add(token);

        sema.release();

        return token;
    }

    public boolean hasSession(String token) throws InterruptedException {
        sema.acquire();
        var has = tokenToSession.containsKey(token);
        sema.release();
        return has;
    }

    public SessionData getSessionData(String token) throws InterruptedException {
        sema.acquire();
        var data = tokenToSession.get(token);
        sema.release();
        return data;
    }

    private void removeSessionImpl(SessionData session) {
        var token = session.getToken();
        tokenToSession.remove(token);
        var userId = session.getUserId();
        var tokenList = idToTokenList.get(userId);
        if (tokenList != null) {
            tokenList.remove(token);
        }
    }

    public void removeSession(SessionData session) throws InterruptedException {
        sema.acquire();
        removeSessionImpl(session);
        sema.release();
    }
}
