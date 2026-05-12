package OOP2ProjectFinal;

import java.time.Instant;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        AuthenticationService auth = new AuthenticationService();
        MonitoringService monitoring = new MonitoringService();

        auth.addUser(new Operator("U-001", "Default Operator", "operator", "1234"));
        auth.addUser(new Commuter("U-002", "Default Commuter", "commuter", "1234"));

        Route r = new Route("R-04L", "IT Park - Ayala - Fuente - Colon");
        r.addStop(new Stop("S-001", "IT Park", 10.3270, 123.9063));
        r.addStop(new Stop("S-002", "Ayala Center Cebu", 10.3187, 123.9056));
        r.addStop(new Stop("S-003", "Fuente Osmeña", 10.3090, 123.8929));
        r.addStop(new Stop("S-004", "Colon", 10.2965, 123.8988));
        monitoring.addRoute(r);

        while (true) {
            System.out.println("\n=== CEBU PUBLIC VEHICLE MONITORING SYSTEM ===");
            System.out.println("Default Operator: operator / 1234");
            System.out.println("Default Commuter: commuter / 1234");
            System.out.println("1) Login");
            System.out.println("2) Register New Account");
            System.out.println("3) Exit");
            System.out.print("Choose: ");

            String mainChoice = sc.nextLine().trim();

            switch (mainChoice) {
                case "1":
                    loginUser(sc, auth, monitoring);
                    break;
                case "2":
                    registerUser(sc, auth);
                    break;
                case "3":
                    System.out.println("Thank you for using the system. Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void registerUser(Scanner sc, AuthenticationService auth) {
        System.out.println("\n--- REGISTER NEW ACCOUNT ---");

        System.out.print("Enter User ID: ");
        String userId = sc.nextLine().trim();

        if (auth.userIdExists(userId)) {
            System.out.println("User ID already exists.");
            return;
        }

        System.out.print("Enter Full Name: ");
        String name = sc.nextLine().trim();

        System.out.print("Enter Username: ");
        String username = sc.nextLine().trim();

        if (auth.usernameExists(username)) {
            System.out.println("Username already exists.");
            return;
        }

        System.out.print("Enter Password: ");
        String password = sc.nextLine().trim();

        if (password.length() < 4) {
            System.out.println("Password must be at least 4 characters.");
            return;
        }

        System.out.print("Confirm Password: ");
        String confirmPassword = sc.nextLine().trim();

        if (!password.equals(confirmPassword)) {
            System.out.println("Passwords do not match.");
            return;
        }

        System.out.println("Select Role:");
        System.out.println("1) Operator");
        System.out.println("2) Commuter");
        System.out.print("Choose role: ");

        String roleChoice = sc.nextLine().trim();

        User newUser;

        switch (roleChoice) {
            case "1" -> newUser = new Operator(userId, name, username, password);
            case "2" -> newUser = new Commuter(userId, name, username, password);
            default -> {
                System.out.println("Invalid role choice.");
                return;
            }
        }

        auth.addUser(newUser);
        System.out.println(newUser.role() + " account created successfully!");
    }

    private static void loginUser(Scanner sc, AuthenticationService auth, MonitoringService monitoring) {
        System.out.println("\n--- LOGIN ---");

        System.out.print("Username: ");
        String username = sc.nextLine().trim();

        System.out.print("Password: ");
        String password = sc.nextLine().trim();

        User user = auth.login(username, password);

        if (user == null) {
            System.out.println("Login failed. Invalid username or password.");
            return;
        }

        System.out.println("Welcome, " + user.name() + " (" + user.role() + ")");

        if (user instanceof Operator) {
            operatorMenu(sc, monitoring);
        } else {
            commuterMenu(sc, monitoring);
        }

        auth.logout();
    }

    private static void operatorMenu(Scanner sc, MonitoringService monitoring) {
        while (true) {
            System.out.println("\n--- OPERATOR MENU ---");
            System.out.println("1) Add Route");
            System.out.println("2) Add Stop to Route");
            System.out.println("3) Register Vehicle");
            System.out.println("4) Assign Vehicle to Route");
            System.out.println("5) Send Vehicle Ping");
            System.out.println("6) View Route Dashboard");
            System.out.println("7) View Alerts Log");
            System.out.println("8) View Route Stops");
            System.out.println("9) View All Vehicles");
            System.out.println("10) Search Vehicle by ID");
            System.out.println("0) Logout");
            System.out.print("Choose: ");

            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> addRoute(sc, monitoring);
                case "2" -> addStop(sc, monitoring);
                case "3" -> registerVehicle(sc, monitoring);
                case "4" -> assignVehicleToRoute(sc, monitoring);
                case "5" -> sendPing(sc, monitoring);
                case "6" -> viewDashboard(sc, monitoring);
                case "7" -> viewAlerts(monitoring);
                case "8" -> viewRouteStops(sc, monitoring);
                case "9" -> viewAllVehicles(monitoring);
                case "10" -> searchVehicle(sc, monitoring);
                case "0" -> {
                    System.out.println("Logged out successfully.");
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void commuterMenu(Scanner sc, MonitoringService monitoring) {
        while (true) {
            System.out.println("\n--- COMMUTER MENU ---");
            System.out.println("1) View Route Status");
            System.out.println("2) View Recent Alerts");
            System.out.println("3) View Route Stops");
            System.out.println("4) Search Vehicle by ID");
            System.out.println("0) Logout");
            System.out.print("Choose: ");

            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> viewDashboard(sc, monitoring);
                case "2" -> viewAlerts(monitoring);
                case "3" -> viewRouteStops(sc, monitoring);
                case "4" -> searchVehicle(sc, monitoring);
                case "0" -> {
                    System.out.println("Logged out successfully.");
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void addRoute(Scanner sc, MonitoringService monitoring) {
        System.out.print("Route ID: ");
        String id = sc.nextLine().trim();

        if (monitoring.routeExists(id)) {
            System.out.println("Route ID already exists.");
            return;
        }

        System.out.print("Route name: ");
        String name = sc.nextLine().trim();

        monitoring.addRoute(new Route(id, name));
        System.out.println("Route added successfully.");
    }

    private static void addStop(Scanner sc, MonitoringService monitoring) {
        Route route = pickRoute(sc, monitoring);
        if (route == null) return;

        System.out.print("Stop ID: ");
        String sid = sc.nextLine().trim();

        if (route.stopExists(sid)) {
            System.out.println("Stop ID already exists in this route.");
            return;
        }

        System.out.print("Stop name: ");
        String sname = sc.nextLine().trim();

        System.out.print("Latitude: ");
        double lat = readDouble(sc);

        System.out.print("Longitude: ");
        double lon = readDouble(sc);

        route.addStop(new Stop(sid, sname, lat, lon));
        System.out.println("Stop added successfully.");
    }

    private static void registerVehicle(Scanner sc, MonitoringService monitoring) {
        System.out.println("Vehicle type:");
        System.out.println("1) Jeepney");
        System.out.println("2) Modern Jeep");
        System.out.println("3) Bus");
        System.out.print("Choose type: ");

        String t = sc.nextLine().trim();

        System.out.print("Vehicle ID: ");
        String vid = sc.nextLine().trim();

        if (monitoring.vehicleExists(vid)) {
            System.out.println("Vehicle ID already exists.");
            return;
        }

        System.out.print("Plate number: ");
        String plate = sc.nextLine().trim();

        System.out.print("Capacity: ");
        int cap = readPositiveInt(sc);

        PublicVehicle v;

        switch (t) {
            case "1" -> v = new Jeepney(vid, plate, cap);
            case "2" -> v = new ModernJeep(vid, plate, cap);
            case "3" -> v = new Bus(vid, plate, cap);
            default -> {
                System.out.println("Invalid vehicle type.");
                return;
            }
        }

        monitoring.registerVehicle(v);
        System.out.println("Vehicle registered successfully.");
    }

    private static void assignVehicleToRoute(Scanner sc, MonitoringService monitoring) {
        PublicVehicle v = pickVehicle(sc, monitoring);
        if (v == null) return;

        Route r = pickRoute(sc, monitoring);
        if (r == null) return;

        monitoring.assignVehicleToRoute(v.vehicleId(), r.routeId());
        System.out.println("Vehicle assigned successfully.");
    }

    private static void sendPing(Scanner sc, MonitoringService monitoring) {
        PublicVehicle v = pickVehicle(sc, monitoring);
        if (v == null) return;

        System.out.print("Latitude: ");
        double lat = readDouble(sc);

        System.out.print("Longitude: ");
        double lon = readDouble(sc);

        System.out.print("Speed (km/h): ");
        double speed = readNonNegativeDouble(sc);

        System.out.print("Passenger count: ");
        int pax = readNonNegativeInt(sc);

        VehiclePing ping = new VehiclePing(v.vehicleId(), Instant.now(), lat, lon, speed, pax);
        monitoring.receivePing(ping);

        System.out.println("Ping received successfully.");
    }

    private static void viewDashboard(Scanner sc, MonitoringService monitoring) {
        Route r = pickRoute(sc, monitoring);
        if (r == null) return;

        System.out.println("\n=== ROUTE DASHBOARD ===");
        System.out.println("Route: " + r.routeId() + " - " + r.routeName());

        List<PublicVehicle> vehicles = monitoring.getVehiclesByRoute(r.routeId());

        if (vehicles.isEmpty()) {
            System.out.println("No vehicles assigned to this route.");
            return;
        }

        System.out.printf("%-12s %-13s %-12s %-14s %-12s %-15s %-25s %-10s%n",
                "Vehicle ID", "Type", "Plate", "Passengers", "Speed", "Status", "Nearest Stop", "ETA");

        for (PublicVehicle v : vehicles) {
            if (!v.hasLocation()) {
                System.out.printf("%-12s %-13s %-12s %-14s %-12s %-15s %-25s %-10s%n",
                        v.vehicleId(), v.getVehicleType(), v.plateNumber(), "N/A", "N/A", "NO DATA", "No data", "N/A");
                continue;
            }

            Stop nearest = monitoring.getNearestStopOnRoute(v, r);
            double etaMin = monitoring.calculateEtaMinutes(v, nearest);

            System.out.printf("%-12s %-13s %-12s %d/%-12d %-12.1f %-15s %-25s %-10.1f%n",
                    v.vehicleId(),
                    v.getVehicleType(),
                    v.plateNumber(),
                    v.passengerCount(),
                    v.capacity(),
                    v.speedKmh(),
                    monitoring.getVehicleStatus(v),
                    nearest != null ? nearest.stopName() : "N/A",
                    etaMin);
        }
    }

    private static void viewAlerts(MonitoringService monitoring) {
        System.out.println("\n=== ALERTS LOG ===");

        List<Alert> alerts = monitoring.getAlerts();

        if (alerts.isEmpty()) {
            System.out.println("No alerts.");
            return;
        }

        int shown = 0;

        for (Alert a : alerts) {
            System.out.println(a.displayAlert());
            shown++;

            if (shown >= 20) break;
        }
    }

    private static void viewRouteStops(Scanner sc, MonitoringService monitoring) {
        Route r = pickRoute(sc, monitoring);
        if (r == null) return;

        System.out.println("\n=== ROUTE STOPS ===");
        System.out.println(r.routeId() + " - " + r.routeName());

        if (r.stops().isEmpty()) {
            System.out.println("No stops added yet.");
            return;
        }

        for (Stop s : r.stops()) {
            System.out.println(s.stopId() + " - " + s.stopName()
                    + " (" + s.lat() + ", " + s.lon() + ")");
        }
    }

    private static void viewAllVehicles(MonitoringService monitoring) {
        System.out.println("\n=== ALL REGISTERED VEHICLES ===");

        List<PublicVehicle> vehicles = monitoring.getAllVehicles();

        if (vehicles.isEmpty()) {
            System.out.println("No vehicles registered.");
            return;
        }

        for (PublicVehicle v : vehicles) {
            System.out.println(v.vehicleId()
                    + " | " + v.getVehicleType()
                    + " | Plate: " + v.plateNumber()
                    + " | Capacity: " + v.capacity()
                    + " | Route: " + (v.routeId() != null ? v.routeId() : "Unassigned"));
        }
    }

    private static void searchVehicle(Scanner sc, MonitoringService monitoring) {
        System.out.print("Enter Vehicle ID: ");
        String id = sc.nextLine().trim();

        PublicVehicle v = monitoring.findVehicleById(id);

        if (v == null) {
            System.out.println("Vehicle not found.");
            return;
        }

        System.out.println("\n=== VEHICLE DETAILS ===");
        System.out.println("Vehicle ID: " + v.vehicleId());
        System.out.println("Type: " + v.getVehicleType());
        System.out.println("Plate Number: " + v.plateNumber());
        System.out.println("Capacity: " + v.capacity());
        System.out.println("Assigned Route: " + (v.routeId() != null ? v.routeId() : "Unassigned"));

        if (v.hasLocation()) {
            System.out.println("Current Location: (" + v.lat() + ", " + v.lon() + ")");
            System.out.println("Speed: " + v.speedKmh() + " km/h");
            System.out.println("Passengers: " + v.passengerCount() + "/" + v.capacity());
            System.out.println("Status: " + monitoring.getVehicleStatus(v));
        } else {
            System.out.println("Location: No GPS data yet.");
        }
    }

    private static Route pickRoute(Scanner sc, MonitoringService monitoring) {
        List<Route> routes = monitoring.getRoutes();

        if (routes.isEmpty()) {
            System.out.println("No routes available.");
            return null;
        }

        System.out.println("\nRoutes:");

        for (int i = 0; i < routes.size(); i++) {
            Route r = routes.get(i);
            System.out.println((i + 1) + ") " + r.routeId() + " - " + r.routeName());
        }

        System.out.print("Choose route #: ");
        int idx = readInt(sc) - 1;

        if (idx < 0 || idx >= routes.size()) {
            System.out.println("Invalid route.");
            return null;
        }

        return routes.get(idx);
    }

    private static PublicVehicle pickVehicle(Scanner sc, MonitoringService monitoring) {
        List<PublicVehicle> vehicles = monitoring.getAllVehicles();

        if (vehicles.isEmpty()) {
            System.out.println("No vehicles registered.");
            return null;
        }

        System.out.println("\nVehicles:");

        for (int i = 0; i < vehicles.size(); i++) {
            PublicVehicle v = vehicles.get(i);
            System.out.println((i + 1) + ") " + v.vehicleId()
                    + " (" + v.getVehicleType() + ") plate=" + v.plateNumber());
        }

        System.out.print("Choose vehicle #: ");
        int idx = readInt(sc) - 1;

        if (idx < 0 || idx >= vehicles.size()) {
            System.out.println("Invalid vehicle.");
            return null;
        }

        return vehicles.get(idx);
    }

    private static int readInt(Scanner sc) {
        while (true) {
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (Exception e) {
                System.out.print("Enter a valid integer: ");
            }
        }
    }

    private static int readPositiveInt(Scanner sc) {
        while (true) {
            int value = readInt(sc);

            if (value > 0) return value;

            System.out.print("Enter a positive integer: ");
        }
    }

    private static int readNonNegativeInt(Scanner sc) {
        while (true) {
            int value = readInt(sc);

            if (value >= 0) return value;

            System.out.print("Enter a non-negative integer: ");
        }
    }

    private static double readDouble(Scanner sc) {
        while (true) {
            try {
                return Double.parseDouble(sc.nextLine().trim());
            } catch (Exception e) {
                System.out.print("Enter a valid number: ");
            }
        }
    }

    private static double readNonNegativeDouble(Scanner sc) {
        while (true) {
            double value = readDouble(sc);

            if (value >= 0) return value;

            System.out.print("Enter a non-negative number: ");
        }
    }
}
