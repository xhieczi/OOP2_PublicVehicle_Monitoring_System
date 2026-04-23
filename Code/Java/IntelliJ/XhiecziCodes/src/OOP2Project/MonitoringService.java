package OOP2Project;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import OOP2Project.*;

import java.util.List;

class MonitoringService {
    // Thresholds (tune as you like)
    private static final double OFF_ROUTE_THRESHOLD_METERS = 700.0;
    private static final double BUNCHING_THRESHOLD_METERS = 250.0;
    private static final double MIN_SPEED_KMH_FOR_ETA = 5.0;

    private final Map<String, Route> routes = new LinkedHashMap<>();
    private final Map<String, PublicVehicle> vehicles = new LinkedHashMap<>();
    private final List<Alert> alerts = new ArrayList<>();

    public void addRoute(Route r) {
        routes.put(r.routeId(), r);
    }

    public List<Route> getRoutes() {
        return new ArrayList<>(routes.values());
    }

    public void registerVehicle(PublicVehicle v) {
        vehicles.put(v.vehicleId(), v);
    }

    public List<PublicVehicle> getAllVehicles() {
        return new ArrayList<>(vehicles.values());
    }

    public void assignVehicleToRoute(String vehicleId, String routeId) {
        PublicVehicle v = vehicles.get(vehicleId);
        Route r = routes.get(routeId);
        if (v == null) throw new IllegalArgumentException("Vehicle not found: " + vehicleId);
        if (r == null) throw new IllegalArgumentException("Route not found: " + routeId);
        v.setRouteId(routeId);
    }

    public void receivePing(VehiclePing ping) {
        PublicVehicle v = vehicles.get(ping.vehicleId());
        if (v == null) {
            // ignore unknown vehicle for prototype
            alerts.add(new OffRouteAlert("Ping from unknown vehicle: " + ping.vehicleId()));
            return;
        }

        v.updateFromPing(ping);

        // Only analyze if assigned to route
        if (v.routeId() != null && routes.containsKey(v.routeId())) {
            Route r = routes.get(v.routeId());

            // Off-route check
            if (isOffRoute(v, r)) {
                alerts.add(new OffRouteAlert(
                        "Vehicle " + v.vehicleId() + " (" + v.getVehicleType() + ") seems off-route on " + r.routeId()
                ));
            }

            // Bunching check (route-wide)
            detectBunching(r.routeId());
        }
    }

    public List<Alert> getAlerts() {
        // latest first
        List<Alert> copy = new ArrayList<>(alerts);
        copy.sort((a, b) -> b.timestamp().compareTo(a.timestamp()));
        return copy;
    }

    public List<PublicVehicle> getVehiclesByRoute(String routeId) {
        List<PublicVehicle> list = new ArrayList<>();
        for (PublicVehicle v : vehicles.values()) {
            if (routeId.equals(v.routeId())) list.add(v);
        }
        return list;
    }

    public Stop getNearestStopOnRoute(PublicVehicle v, Route r) {
        if (!v.hasLocation()) return null;
        Stop best = null;
        double bestMeters = Double.MAX_VALUE;
        for (Stop s : r.stops()) {
            double d = haversineMeters(v.lat(), v.lon(), s.lat(), s.lon());
            if (d < bestMeters) {
                bestMeters = d;
                best = s;
            }
        }
        return best;
    }

    public double calculateEtaMinutes(PublicVehicle v, Stop targetStop) {
        if (targetStop == null || !v.hasLocation()) return -1.0;
        double meters = haversineMeters(v.lat(), v.lon(), targetStop.lat(), targetStop.lon());
        double km = meters / 1000.0;
        double speed = Math.max(v.speedKmh(), MIN_SPEED_KMH_FOR_ETA);
        return (km / speed) * 60.0;
    }

    private boolean isOffRoute(PublicVehicle v, Route r) {
        Stop nearest = getNearestStopOnRoute(v, r);
        if (nearest == null) return false;
        double d = haversineMeters(v.lat(), v.lon(), nearest.lat(), nearest.lon());
        return d > OFF_ROUTE_THRESHOLD_METERS;
    }

    private void detectBunching(String routeId) {
        List<PublicVehicle> list = getVehiclesByRoute(routeId);
        // only consider vehicles with location
        List<PublicVehicle> active = new ArrayList<>();
        for (PublicVehicle v : list) if (v.hasLocation()) active.add(v);

        for (int i = 0; i < active.size(); i++) {
            for (int j = i + 1; j < active.size(); j++) {
                PublicVehicle a = active.get(i);
                PublicVehicle b = active.get(j);
                double d = haversineMeters(a.lat(), a.lon(), b.lat(), b.lon());
                if (d < BUNCHING_THRESHOLD_METERS) {
                    alerts.add(new BunchingAlert(
                            "Vehicles " + a.vehicleId() + " and " + b.vehicleId() + " are bunching (~" + (int)d + "m apart) on route " + routeId
                    ));
                }
            }
        }
    }

    // Haversine distance in meters (no extra helper class)
    private static double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371000.0; // meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.min(1.0, Math.sqrt(a)));
        return R * c;
    }
}

