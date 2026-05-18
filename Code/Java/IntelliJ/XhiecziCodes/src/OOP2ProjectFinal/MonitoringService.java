package OOP2ProjectFinal;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MonitoringService {

    private final List<Route> routes = new ArrayList<>();
    private final List<PublicVehicle> vehicles = new ArrayList<>();
    private final List<Alert> alerts = new ArrayList<>();

    // Routes
    public void addRoute(Route route) {
        if (route != null) routes.add(route);
    }

    public List<Route> getRoutes() {
        return new ArrayList<>(routes);
    }

    public boolean routeExists(String routeId) {
        return routes.stream().anyMatch(r -> r.routeId().equalsIgnoreCase(routeId));
    }

    public Route getRouteById(String routeId) {
        return routes.stream()
                .filter(r -> r.routeId().equalsIgnoreCase(routeId))
                .findFirst()
                .orElse(null);
    }

    public boolean removeRoute(String routeId) {
        return routes.removeIf(r -> r.routeId().equalsIgnoreCase(routeId));
    }

    public boolean removeStopFromRoute(String routeId, String stopId) {
        Route route = getRouteById(routeId);
        return route != null && route.removeStop(stopId);
    }

    // Vehicles
    public void registerVehicle(PublicVehicle vehicle) {
        if (vehicle != null) vehicles.add(vehicle);
    }

    public List<PublicVehicle> getAllVehicles() {
        return new ArrayList<>(vehicles);
    }

    public boolean vehicleExists(String vehicleId) {
        return vehicles.stream().anyMatch(v -> v.vehicleId().equalsIgnoreCase(vehicleId));
    }

    public boolean removeVehicle(String vehicleId) {
        return vehicles.removeIf(v -> v.vehicleId().equalsIgnoreCase(vehicleId));
    }

    public void assignVehicleToRoute(String vehicleId, String routeId) {
        PublicVehicle v = findVehicleById(vehicleId);
        if (v != null) v.setRouteId(routeId);
    }

    // Used by MainFX.java
    public List<PublicVehicle> getVehiclesOnRoute(String routeId) {
        return vehicles.stream()
                .filter(v -> routeId != null && routeId.equalsIgnoreCase(v.routeId()))
                .collect(Collectors.toList());
    }

    // Used by Main.java
    public List<PublicVehicle> getVehiclesByRoute(String routeId) {
        return getVehiclesOnRoute(routeId);   // alias
    }

    public PublicVehicle findVehicleById(String vehicleId) {
        return vehicles.stream()
                .filter(v -> v.vehicleId().equalsIgnoreCase(vehicleId))
                .findFirst()
                .orElse(null);
    }

    // Used by Main.java
    public Stop getNearestStopOnRoute(PublicVehicle v, Route route) {
        if (v == null || !v.hasLocation() || route == null) return null;
        return route.getNearestStop(v.lat(), v.lon());
    }

    public double calculateEtaMinutes(PublicVehicle v, Stop stop) {
        if (v == null || stop == null || !v.hasLocation()) return -1;
        double distance = haversine(v.lat(), v.lon(), stop.lat(), stop.lon());
        double speed = v.speedKmh() > 0 ? v.speedKmh() : 20.0;
        return (distance / speed) * 60;
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon/2) * Math.sin(dLon/2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    // Ping
    public void receivePing(VehiclePing ping) {
        PublicVehicle v = findVehicleById(ping.vehicleId());
        if (v != null) {
            v.updateFromPing(ping);
        }
    }

    public String getVehicleStatus(PublicVehicle v) {
        if (v == null || !v.hasLocation()) return "No Signal";
        int pct = v.capacity() > 0 ? (v.passengerCount() * 100 / v.capacity()) : 0;
        if (pct >= 90) return "FULL";
        if (pct >= 60) return "BUSY";
        return "ON TIME";
    }

    // Alerts
    public List<Alert> getAlerts() {
        return new ArrayList<>(alerts);
    }

    public List<Alert> getAlertsBySeverity(String severity) {
        return alerts.stream()
                .filter(a -> a.severity().equalsIgnoreCase(severity))
                .collect(Collectors.toList());
    }
}