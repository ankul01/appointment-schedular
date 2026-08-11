package scheduling.domain;

/** Reschedule keeps the same row as {@link #CONFIRMED}; there is no RESCHEDULED status. */
public enum AppointmentStatus {
    CONFIRMED,
    CANCELLED
}
