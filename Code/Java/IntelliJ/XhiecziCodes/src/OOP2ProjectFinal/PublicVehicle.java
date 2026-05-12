package OOP2ProjectFinal;
import java.util.*;


abstract class PublicVehicle {
    private final String vehicleId;
    private final String plateNumber;
    private final int capacity;

    private String routeId; // assigned route
    private Double lat;     // nullable = no data yet
    private Double lon;
    private double speedKmh;
    private int passengerCount;

    protected PublicVehicle(String vehicleId, String plateNumber, int capacity) {
        this.vehicleId = Objects.requireNonNull(vehicleId);
        this.plateNumber = Objects.requireNonNull(plateNumber);
        this.capacity = capacity;
    }

    public String vehicleId() { return vehicleId; }
    public String plateNumber() { return plateNumber; }
    public int capacity() { return capacity; }

    public String routeId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }

    public boolean hasLocation() { return lat != null && lon != null; }
    public double lat() { return lat != null ? lat : 0.0; }
    public double lon() { return lon != null ? lon : 0.0; }

    public double speedKmh() { return speedKmh; }
    public int passengerCount() { return passengerCount; }

    public void updateFromPing(VehiclePing ping) {
        this.lat = ping.lat();
        this.lon = ping.lon();
        this.speedKmh = ping.speedKmh();
        this.passengerCount = ping.passengerCount();
    }

    public abstract String getVehicleType();
}
