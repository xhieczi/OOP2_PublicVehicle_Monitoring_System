package OOP2ProjectFinal;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;


abstract class Alert {
    private final String alertId;
    private final Instant timestamp;
    private final String severity;  // keep simple (no extra enum class)
    private final String message;

    protected Alert(String severity, String message) {
        this.alertId = UUID.randomUUID().toString();
        this.timestamp = Instant.now();
        this.severity = severity;
        this.message = message;
    }

    public Instant timestamp() { return timestamp; }
    public String severity() { return severity; }
    public String message() { return message; }

    public abstract String type();

    public String displayAlert() {
        String time = DateTimeFormatter.ISO_INSTANT.format(timestamp);
        return "[" + time + "] " + severity + " " + type() + " - " + message;
    }

    public String alertId() { return alertId; }
}
