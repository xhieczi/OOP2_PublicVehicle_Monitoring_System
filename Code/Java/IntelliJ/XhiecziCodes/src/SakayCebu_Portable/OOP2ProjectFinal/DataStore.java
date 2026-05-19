package OOP2ProjectFinal;

import java.io.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

class DataStore {

    private static final String DATA_FOLDER = "data";
    private static final String USERS_FILE = DATA_FOLDER + "/users.csv";
    private static final String ROUTES_FILE = DATA_FOLDER + "/routes.csv";
    private static final String STOPS_FILE = DATA_FOLDER + "/stops.csv";
    private static final String VEHICLES_FILE = DATA_FOLDER + "/vehicles.csv";

    public static void ensureDataFolder() {
        File folder = new File(DATA_FOLDER);

        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    public static boolean dataExists() {
        return new File(USERS_FILE).exists()
                || new File(ROUTES_FILE).exists()
                || new File(STOPS_FILE).exists()
                || new File(VEHICLES_FILE).exists();
    }

    public static void saveAll(AuthenticationService auth,
                               MonitoringService monitoring,
                               List<SavedUser> savedUsers) {
        ensureDataFolder();
        saveUsers(savedUsers);
        saveRoutes(monitoring);
        saveStops(monitoring);
        saveVehicles(monitoring);
    }

    public static void loadAll(AuthenticationService auth,
                               MonitoringService monitoring,
                               List<SavedUser> savedUsers) {
        ensureDataFolder();
        loadUsers(auth, savedUsers);
        loadRoutes(monitoring);
        loadStops(monitoring);
        loadVehicles(monitoring);
    }

    private static void saveUsers(List<SavedUser> users) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE))) {
            for (SavedUser user : users) {
                writer.println(
                        safe(user.userId()) + "," +
                                safe(user.name()) + "," +
                                safe(user.username()) + "," +
                                safe(user.password()) + "," +
                                safe(user.role())
                );
            }
        } catch (IOException e) {
            System.out.println("Error saving users: " + e.getMessage());
        }
    }

    private static void loadUsers(AuthenticationService auth, List<SavedUser> savedUsers) {
        File file = new File(USERS_FILE);

        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);

                if (parts.length < 5) continue;

                String userId = unsafe(parts[0]);
                String name = unsafe(parts[1]);
                String username = unsafe(parts[2]);
                String password = unsafe(parts[3]);
                String role = unsafe(parts[4]);

                SavedUser savedUser = new SavedUser(userId, name, username, password, role);
                savedUsers.add(savedUser);

                if (role.equalsIgnoreCase("Operator")) {
                    auth.addUser(new Operator(userId, name, username, password));
                } else {
                    auth.addUser(new Commuter(userId, name, username, password));
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading users: " + e.getMessage());
        }
    }

    private static void saveRoutes(MonitoringService monitoring) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(ROUTES_FILE))) {
            for (Route route : monitoring.getRoutes()) {
                writer.println(
                        safe(route.routeId()) + "," +
                                safe(route.routeName())
                );
            }
        } catch (IOException e) {
            System.out.println("Error saving routes: " + e.getMessage());
        }
    }

    private static void loadRoutes(MonitoringService monitoring) {
        File file = new File(ROUTES_FILE);

        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);

                if (parts.length < 2) continue;

                String routeId = unsafe(parts[0]);
                String routeName = unsafe(parts[1]);

                if (!monitoring.routeExists(routeId)) {
                    monitoring.addRoute(new Route(routeId, routeName));
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading routes: " + e.getMessage());
        }
    }

    private static void saveStops(MonitoringService monitoring) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(STOPS_FILE))) {
            for (Route route : monitoring.getRoutes()) {
                for (Stop stop : route.stops()) {
                    writer.println(
                            safe(route.routeId()) + "," +
                                    safe(stop.stopId()) + "," +
                                    safe(stop.stopName()) + "," +
                                    stop.lat() + "," +
                                    stop.lon()
                    );
                }
            }
        } catch (IOException e) {
            System.out.println("Error saving stops: " + e.getMessage());
        }
    }

    private static void loadStops(MonitoringService monitoring) {
        File file = new File(STOPS_FILE);

        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);

                if (parts.length < 5) continue;

                String routeId = unsafe(parts[0]);
                String stopId = unsafe(parts[1]);
                String stopName = unsafe(parts[2]);
                double lat = Double.parseDouble(parts[3]);
                double lon = Double.parseDouble(parts[4]);

                Route route = findRoute(monitoring, routeId);

                if (route != null && !route.stopExists(stopId)) {
                    route.addStop(new Stop(stopId, stopName, lat, lon));
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading stops: " + e.getMessage());
        }
    }

    private static void saveVehicles(MonitoringService monitoring) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(VEHICLES_FILE))) {
            for (PublicVehicle vehicle : monitoring.getAllVehicles()) {
                writer.println(
                        safe(vehicle.vehicleId()) + "," +
                                safe(vehicle.getVehicleType()) + "," +
                                safe(vehicle.plateNumber()) + "," +
                                vehicle.capacity() + "," +
                                safe(vehicle.routeId() != null ? vehicle.routeId() : "") + "," +
                                vehicle.hasLocation() + "," +
                                vehicle.lat() + "," +
                                vehicle.lon() + "," +
                                vehicle.speedKmh() + "," +
                                vehicle.passengerCount()
                );
            }
        } catch (IOException e) {
            System.out.println("Error saving vehicles: " + e.getMessage());
        }
    }

    private static void loadVehicles(MonitoringService monitoring) {
        File file = new File(VEHICLES_FILE);

        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);

                if (parts.length < 10) continue;

                String vehicleId = unsafe(parts[0]);
                String type = unsafe(parts[1]);
                String plate = unsafe(parts[2]);
                int capacity = Integer.parseInt(parts[3]);
                String routeId = unsafe(parts[4]);
                boolean hasLocation = Boolean.parseBoolean(parts[5]);
                double lat = Double.parseDouble(parts[6]);
                double lon = Double.parseDouble(parts[7]);
                double speed = Double.parseDouble(parts[8]);
                int passengers = Integer.parseInt(parts[9]);

                if (monitoring.vehicleExists(vehicleId)) continue;

                PublicVehicle vehicle;

                if (type.equalsIgnoreCase("Jeepney")) {
                    vehicle = new Jeepney(vehicleId, plate, capacity);
                } else if (type.equalsIgnoreCase("Modern Jeep")) {
                    vehicle = new ModernJeep(vehicleId, plate, capacity);
                } else {
                    vehicle = new Bus(vehicleId, plate, capacity);
                }

                monitoring.registerVehicle(vehicle);

                if (!routeId.isEmpty() && monitoring.routeExists(routeId)) {
                    monitoring.assignVehicleToRoute(vehicleId, routeId);
                }

                if (hasLocation) {
                    VehiclePing ping = new VehiclePing(vehicleId, Instant.now(), lat, lon, speed, passengers);
                    vehicle.updateFromPing(ping);
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading vehicles: " + e.getMessage());
        }
    }

    private static Route findRoute(MonitoringService monitoring, String routeId) {
        for (Route route : monitoring.getRoutes()) {
            if (route.routeId().equals(routeId)) {
                return route;
            }
        }

        return null;
    }

    private static String safe(String value) {
        if (value == null) return "";
        return value.replace(",", "{comma}");
    }

    private static String unsafe(String value) {
        return value.replace("{comma}", ",");
    }

    public record SavedUser(
            String userId,
            String name,
            String username,
            String password,
            String role
    ) {}
}
