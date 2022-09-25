package infra;

// TODO make thread-safe (Random already is thread-safe)

// TODO thread for removing sessions that haven't been accessed in some time (like 2 hours, could be configurable)
//  this means each time we authorize a user, it should updated the session 'lastAccess' attribute (which we don't have yet)
//  -> keep sessiondata in (concurrent) linked list (easy to append, easy to delete, easy to move
//  -> keep it in order of last access time, descending (so we can stop when now - curr.lastAccessTime < 2 hours)
//  -> for the access(token) methdd that updates last access time, make a map of token -> linked list node containing session data
//    maybe the SessionData could itself be the node, but this should be invisible to the outside
//    -> implement SessionDataNode inside SessionManager + SessionData interface for the outside, or something
//       (yeah, let's implement our own concurrent linked list; doing so efficiently (lock-free?) could give us some extra points)

import java.util.*;

public class SessionManager {

    private static final int TOKEN_LENGTH = 64;
    private final Random random;
    private final Map<String, SessionData> tokenToSession;
    private final Map<Long, List<String>> idToTokenList;

    public SessionManager() {
        random = new Random();
        tokenToSession = new HashMap<>();
        idToTokenList = new HashMap<>();
    }

    public static boolean charOutsideInvalidRanges(int i) {
        return (i <= 57 || i >= 65) && (i <= 90 || i >= 97);
    }

    public static boolean tokenSyntaxValid(String tok) {
        return tok.length() == TOKEN_LENGTH
            && tok.chars().allMatch(i -> i >= '0' && i <= 'z' && charOutsideInvalidRanges(i));
    }

    public String makeToken() {
        return random.ints('0', 'z'+1)
            .filter(SessionManager::charOutsideInvalidRanges)
            .limit(TOKEN_LENGTH)
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();
    }

    public String createSession(long id) {
        var token = "";
        do {
            token = makeToken();
        } while (tokenToSession.get(token) != null);

        var data = new SessionData(token, id);
        tokenToSession.put(token, data);

        idToTokenList
            .computeIfAbsent(id, k -> new ArrayList<String>())
            .add(token);

        return token;
    }

    public boolean hasSession(String token) {
        return tokenToSession.containsKey(token);
    }

    public SessionData getSessionData(String token) {
        return tokenToSession.get(token);
    }
}
