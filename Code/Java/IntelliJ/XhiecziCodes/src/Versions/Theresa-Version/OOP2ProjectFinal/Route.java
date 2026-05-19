package OOP2ProjectFinal;

import java.util.ArrayList;
import java.util.List;

public class Route {

    private final String routeId;
    private final String routeName;
    private final List<Stop> stops = new ArrayList<>();

    public Route(String routeId, String routeName) {
        this.routeId = routeId;
        this.routeName = routeName;
    }

    public String routeId() { return routeId; }
    public String routeName() { return routeName; }

    public List<Stop> stops() { return new ArrayList<>(stops); }

    public void addStop(Stop stop) {
        if (stop != null) stops.add(stop);
    }

    public boolean stopExists(String stopId) {
        return stops.stream().anyMatch(s -> s.stopId().equalsIgnoreCase(stopId));
    }

    public boolean removeStop(String stopId) {
        return stops.removeIf(s -> s.stopId().equalsIgnoreCase(stopId));
    }

    // Used by MonitoringService
    public Stop getNearestStop(double lat, double lon) {
        if (stops.isEmpty()) return null;

        Stop nearest = null;
        double minDist = Double.MAX_VALUE;

        for (Stop s : stops) {
            double dist = haversine(lat, lon, s.lat(), s.lon());
            if (dist < minDist) {
                minDist = dist;
                nearest = s;
            }
        }
        return nearest;
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon/2) * Math.sin(dLon/2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    @Override
    public String toString() {
        return routeId + " - " + routeName;
    }
}