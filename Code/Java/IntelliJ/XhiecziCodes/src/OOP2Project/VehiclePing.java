package OOP2Project;
import java.time.Instant;
import java.util.*;


class VehiclePing {
    private final String vehicleId;
    private final Instant timestamp;
    private final double lat;
    private final double lon;
    private final double speedKmh;
    private final int passengerCount;

    public VehiclePing(String vehicleId, Instant timestamp, double lat, double lon, double speedKmh, int passengerCount) {
        this.vehicleId = Objects.requireNonNull(vehicleId);
        this.timestamp = Objects.requireNonNull(timestamp);
        this.lat = lat;
        this.lon = lon;
        this.speedKmh = speedKmh;
        this.passengerCount = passengerCount;
    }

    public String vehicleId() { return vehicleId; }
    public Instant timestamp() { return timestamp; }
    public double lat() { return lat; }
    public double lon() { return lon; }
    public double speedKmh() { return speedKmh; }
    public int passengerCount() { return passengerCount; }
}
