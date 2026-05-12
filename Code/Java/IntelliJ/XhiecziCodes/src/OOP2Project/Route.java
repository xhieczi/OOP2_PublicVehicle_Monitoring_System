package OOP2Project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a public vehicle route in Cebu City.
 * Example: "R-04L" = IT Park → Ayala → Fuente → Colon
 */
public class Route {

    private final String routeId;
    private final String routeName;
    private final List<Stop> stops;

    public Route(String routeId, String routeName) {
        this.routeId = Objects.requireNonNull(routeId, "Route ID cannot be null");
        this.routeName = Objects.requireNonNull(routeName, "Route name cannot be null");
        this.stops = new ArrayList<>();
    }

    public String routeId() { return routeId; }
    public String routeName() { return routeName; }

    /**
     * Returns an unmodifiable view of stops to prevent external modification.
     */
    public List<Stop> stops() {
        return Collections.unmodifiableList(stops);
    }

    /**
     * Adds a stop to this route in order.
     */
    public void addStop(Stop stop) {
        if (stop == null) throw new IllegalArgumentException("Stop cannot be null");
        stops.add(stop);
    }

    /**
     * Returns number of stops on this route.
     */
    public int stopCount() {
        return stops.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Route[").append(routeId).append(": ").append(routeName).append(", Stops: ");
        for (Stop s : stops) {
            sb.append(s.stopName()).append(" -> ");
        }
        if (!stops.isEmpty()) sb.setLength(sb.length() - 4);
        sb.append("]");
        return sb.toString();
    }
}