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
            case "1":
                newUser = new Operator(userId, name, username, password);
                break;
            case "2":
                newUser = new Commuter(userId, name, username, password);
                break;
            default:
                System.out.println("Invalid role choice.");
                return;
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
                case "1":
                    addRoute(sc, monitoring);
                    break;
                case "2":
                    addStop(sc, monitoring);
                    break;
                case "3":
                    registerVehicle(sc, monitoring);
                    break;
                case "4":
                    assignVehicleToRoute(sc, monitoring);
                    break;
                case "5":
                    sendPing(sc, monitoring);
                    break;
                case "6":
                    viewDashboard(sc, monitoring);
                    break;
                case "7":
                    viewAlerts(monitoring);
                    break;
                case "8":
                    viewRouteStops(sc, monitoring);
                    break;
                case "9":
                    viewAllVehicles(monitoring);
                    break;
                case "10":
                    searchVehicle(sc, monitoring);
                    break;
                case "0":
                    System.out.println("Logged out successfully.");
                    return;
                default:
                    System.out.println("Invalid choice.");
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
                case "1":
                    viewDashboard(sc, monitoring);
                    break;
                case "2":
                    viewAlerts(monitoring);
                    break;
                case "3":
                    viewRouteStops(sc, monitoring);
                    break;
                case "4":
                    searchVehicle(sc, monitoring);
                    break;
                case "0":
                    System.out.println("Logged out successfully.");
                    return;
                default:
                    System.out.println("Invalid choice.");
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

        if (route == null) {
            return;
        }

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
            case "1":
                v = new Jeepney(vid, plate, cap);
                break;
            case "2":
                v = new ModernJeep(vid, plate, cap);
                break;
            case "3":
                v = new Bus(vid, plate, cap);
                break;
            default:
                System.out.println("Invalid vehicle type.");
                return;
        }

        monitoring.registerVehicle(v);
        System.out.println("Vehicle registered successfully.");
    }

    private static void assignVehicleToRoute(Scanner sc, MonitoringService monitoring) {
        System.out.print("Vehicle ID: ");
        String vehicleId = sc.nextLine().trim();

        System.out.print("Route ID: ");
        String routeId = sc.nextLine().trim();

        try {
            monitoring.assignVehicleToRoute(vehicleId, routeId);
            System.out.println("Vehicle assigned to route successfully.");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void sendPing(Scanner sc, MonitoringService monitoring) {
        System.out.print("Vehicle ID: ");
        String vehicleId = sc.nextLine().trim();

        if (!monitoring.vehicleExists(vehicleId)) {
            System.out.println("Vehicle not found.");
            return;
        }

        System.out.print("Latitude: ");
        double lat = readDouble(sc);

        System.out.print("Longitude: ");
        double lon = readDouble(sc);

        System.out.print("Speed km/h: ");
        double speed = readDouble(sc);

        System.out.print("Passenger count: ");
        int passengerCount = readPositiveInt(sc);

        VehiclePing ping = new VehiclePing(vehicleId, Instant.now(), lat, lon, speed, passengerCount);
        monitoring.receivePing(ping);

        System.out.println("Vehicle ping sent successfully.");
    }

    private static void viewDashboard(Scanner sc, MonitoringService monitoring) {
        Route route = pickRoute(sc, monitoring);

        if (route == null) {
            return;
        }

        List<PublicVehicle> vehicles = monitoring.getVehiclesByRoute(route.routeId());

        System.out.println("\n--- ROUTE DASHBOARD ---");
        System.out.println("Route: " + route.routeId() + " - " + route.routeName());

        if (vehicles.isEmpty()) {
            System.out.println("No vehicles assigned to this route.");
            return;
        }

        for (PublicVehicle v : vehicles) {
            Stop nearest = monitoring.getNearestStopOnRoute(v, route);
            double eta = monitoring.calculateEtaMinutes(v, nearest);

            System.out.println("--------------------------------");
            System.out.println("Vehicle ID: " + v.vehicleId());
            System.out.println("Type: " + v.getVehicleType());
            System.out.println("Plate: " + v.plateNumber());
            System.out.println("Status: " + monitoring.getVehicleStatus(v));
            System.out.println("Speed: " + (v.hasLocation() ? v.speedKmh() + " km/h" : "No data"));
            System.out.println("Passengers: " + (v.hasLocation() ? v.passengerCount() + "/" + v.capacity() : "No data"));
            System.out.println("Nearest Stop: " + (nearest != null ? nearest.stopName() : "N/A"));
            System.out.println("ETA: " + (eta >= 0 ? Math.round(eta * 10.0) / 10.0 + " minutes" : "N/A"));
        }
    }

    private static void viewAlerts(MonitoringService monitoring) {
        System.out.println("\n--- ALERTS LOG ---");

        List<Alert> alerts = monitoring.getAlerts();

        if (alerts.isEmpty()) {
            System.out.println("No alerts found.");
            return;
        }

        for (Alert alert : alerts) {
            System.out.println(alert.displayAlert());
        }
    }

    private static void viewRouteStops(Scanner sc, MonitoringService monitoring) {
        Route route = pickRoute(sc, monitoring);

        if (route == null) {
            return;
        }

        System.out.println("\n--- ROUTE STOPS ---");
        System.out.println("Route: " + route.routeId() + " - " + route.routeName());

        List<Stop> stops = route.stops();

        if (stops.isEmpty()) {
            System.out.println("No stops found.");
            return;
        }

        for (Stop stop : stops) {
            System.out.println(stop.stopId() + " - " + stop.stopName() +
                    " (" + stop.lat() + ", " + stop.lon() + ")");
        }
    }

    private static void viewAllVehicles(MonitoringService monitoring) {
        System.out.println("\n--- ALL VEHICLES ---");

        List<PublicVehicle> vehicles = monitoring.getAllVehicles();

        if (vehicles.isEmpty()) {
            System.out.println("No vehicles registered.");
            return;
        }

        for (PublicVehicle v : vehicles) {
            System.out.println("--------------------------------");
            System.out.println("Vehicle ID: " + v.vehicleId());
            System.out.println("Type: " + v.getVehicleType());
            System.out.println("Plate: " + v.plateNumber());
            System.out.println("Capacity: " + v.capacity());
            System.out.println("Route: " + (v.routeId() != null ? v.routeId() : "Unassigned"));
            System.out.println("Status: " + monitoring.getVehicleStatus(v));
        }
    }

    private static void searchVehicle(Scanner sc, MonitoringService monitoring) {
        System.out.print("Vehicle ID: ");
        String vehicleId = sc.nextLine().trim();

        PublicVehicle v = monitoring.findVehicleById(vehicleId);

        if (v == null) {
            System.out.println("Vehicle not found.");
            return;
        }

        System.out.println("\n--- VEHICLE DETAILS ---");
        System.out.println("Vehicle ID: " + v.vehicleId());
        System.out.println("Type: " + v.getVehicleType());
        System.out.println("Plate Number: " + v.plateNumber());
        System.out.println("Capacity: " + v.capacity());
        System.out.println("Assigned Route: " + (v.routeId() != null ? v.routeId() : "Unassigned"));
        System.out.println("Location: " + (v.hasLocation() ? "(" + v.lat() + ", " + v.lon() + ")" : "No GPS data yet"));
        System.out.println("Speed: " + (v.hasLocation() ? v.speedKmh() + " km/h" : "N/A"));
        System.out.println("Passengers: " + (v.hasLocation() ? v.passengerCount() + "/" + v.capacity() : "N/A"));
        System.out.println("Status: " + monitoring.getVehicleStatus(v));
    }

    private static Route pickRoute(Scanner sc, MonitoringService monitoring) {
        System.out.print("Route ID: ");
        String routeId = sc.nextLine().trim();

        List<Route> routes = monitoring.getRoutes();

        for (Route route : routes) {
            if (route.routeId().equalsIgnoreCase(routeId)) {
                return route;
            }
        }

        System.out.println("Route not found.");
        return null;
    }

    private static double readDouble(Scanner sc) {
        while (true) {
            try {
                return Double.parseDouble(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Invalid number. Enter again: ");
            }
        }
    }

    private static int readPositiveInt(Scanner sc) {
        while (true) {
            try {
                int value = Integer.parseInt(sc.nextLine().trim());

                if (value > 0) {
                    return value;
                }

                System.out.print("Enter a positive number: ");
            } catch (NumberFormatException e) {
                System.out.print("Invalid number. Enter again: ");
            }
        }
    }
}