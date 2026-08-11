package scheduling.domain;

public enum RequiredResourceMode {
    BAY_ONLY,
    TECHNICIAN_ONLY,
    /** Bay and technician must both be free for the same window. */
    BAY_AND_TECHNICIAN
}
