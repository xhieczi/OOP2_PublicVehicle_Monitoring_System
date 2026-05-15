package OOP2ProjectFinal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Route {

    private final String routeId;
    private final String routeName;
    private final List<Stop> stops;

    public Route(String routeId, String routeName) {
        this.routeId = Objects.requireNonNull(routeId, "Route ID cannot be null");
        this.routeName = Objects.requireNonNull(routeName, "Route name cannot be null");
        this.stops = new ArrayList<>();
    }

    public String routeId() {
        return routeId;
    }

    public String routeName() {
        return routeName;
    }

    public List<Stop> stops() {
        return Collections.unmodifiableList(stops);
    }

    public void addStop(Stop stop) {
        if (stop == null) {
            throw new IllegalArgumentException("Stop cannot be null");
        }

        stops.add(stop);
    }

    public int stopCount() {
        return stops.size();
    }

    public boolean stopExists(String stopId) {
        for (Stop stop : stops) {
            if (stop.stopId().equalsIgnoreCase(stopId)) {
                return true;
            }
        }

        return false;
    }

    public boolean removeStop(String stopId) {
        return stops.removeIf(stop -> stop.stopId().equalsIgnoreCase(stopId));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(routeId)
                .append(" - ")
                .append(routeName);

        if (!stops.isEmpty()) {
            sb.append(" | Stops: ");

            for (Stop stop : stops) {
                sb.append(stop.stopName()).append(" -> ");
            }

            sb.setLength(sb.length() - 4);
        }

        return sb.toString();
    }
}