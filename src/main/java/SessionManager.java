import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.IntPredicate;

public enum SessionManager {
    INSTANCE;

    private static final int TOKLEN = 64;

    private final Random random;
    private final Map<String, Long> tokenToUserId;

    SessionManager() {
        tokenToUserId = new HashMap<>();
        random = new Random();
    }

    public long getUserId(String token) {
        return tokenToUserId.getOrDefault(token, 0L);
    }

    public boolean outsideInvalidRanges(int i) {
        return (i <= 57 || i >= 65) && (i <= 90 || i >= 97);
    }

    public boolean validateTokenSyntax(String token) {
        return token.length() == 64
            && token.chars().allMatch(i -> i >= '0' && i <= 'z' && outsideInvalidRanges(i));
    }

    public String createTokenFor(long userId) {
        var token = random.ints('0', 'z'+1)
            .filter(this::outsideInvalidRanges)
            .limit(TOKLEN)
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();
        tokenToUserId.put(token, userId);
        return token;
    }
}
