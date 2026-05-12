package OOP2ProjectFinal;

/**
 * Represents an Operator user — someone who manages routes, vehicles, and alerts.
 * Extends User (inheritance).
 */
public class Operator extends User {

    /**
     * Creates a new operator account.
     *
     * @param userId   Unique ID like "OP-001"
     * @param name     Full name like "Juan Dela Cruz"
     * @param username Login username
     * @param password Login password
     */
    public Operator(String userId, String name, String username, String password) {
        super(userId, name, username, password);
    }

    /**
     * Returns the role of this user.
     * Required because User declares this as abstract.
     */
    @Override
    public String role() {
        return "OPERATOR";
    }

    /**
     * Registers a new vehicle in the monitoring system.
     *
     * @param service The monitoring service
     * @param vehicle The vehicle to register
     */
    public void registerVehicle(MonitoringService service, PublicVehicle vehicle) {
        System.out.println("Operator " + name() + " is registering vehicle: " + vehicle.vehicleId());
        service.registerVehicle(vehicle);
    }

    /**
     * Assigns a vehicle to a specific route.
     *
     * @param service  The monitoring service
     * @param vehicleId The vehicle ID to assign
     * @param routeId  The route ID to assign to
     */
    public void assignVehicleToRoute(MonitoringService service, String vehicleId, String routeId) {
        System.out.println("Operator " + name() + " is assigning " + vehicleId + " to route " + routeId);
        service.assignVehicleToRoute(vehicleId, routeId);
    }

    /**
     * Adds a new route to the monitoring system.
     *
     * @param service The monitoring service
     * @param route   The route to add
     */
    public void addRoute(MonitoringService service, Route route) {
        System.out.println("Operator " + name() + " is adding route: " + route.routeId());
        service.addRoute(route);
    }

    @Override
    public String toString() {
        return String.format("Operator[%s: %s]", userId(), name());
    }
}