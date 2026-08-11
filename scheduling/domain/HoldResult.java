package scheduling.domain;

import java.util.Objects;

/**
 * Result of a soft hold. On success, client must echo {@link #holdToken()} on book.
 */
public final class HoldResult {
    private final boolean success;
    private final String holdToken;
    private final long newVersion;

    private HoldResult(boolean success, String holdToken, long newVersion) {
        this.success = success;
        this.holdToken = holdToken;
        this.newVersion = newVersion;
    }

    public static HoldResult success(String holdToken, long newVersion) {
        return new HoldResult(true, Objects.requireNonNull(holdToken, "holdToken"), newVersion);
    }

    public static HoldResult failure() {
        return new HoldResult(false, null, 0L);
    }

    public boolean success() {
        return success;
    }

    /** Present when {@link #success()} is true; otherwise {@code null}. */
    public String holdToken() {
        return holdToken;
    }

    /** Present when {@link #success()} is true; otherwise {@code 0}. */
    public long newVersion() {
        return newVersion;
    }
}
