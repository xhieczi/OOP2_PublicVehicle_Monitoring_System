package OOP2Project;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;

class Route {
    private final String routeId;
    private final String routeName;
    private final List<Stop> stops = new ArrayList<>();

    public Route(String routeId, String routeName) {
        this.routeId = Objects.requireNonNull(routeId);
        this.routeName = Objects.requireNonNull(routeName);
    }

    public String routeId() {
        return routeId;
    }

    public String routeName() {
        return routeName;
    }

    public void addStop(Stop s) {
        stops.add(Objects.requireNonNull(s));
    }

    public List<Stop> stops() {
        return Collections.unmodifiableList(stops);
    }
}
