package OOP2Project;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a GPS ping from a vehicle.
 * Immutable data class.
 */
public final class VehiclePing {

    private final String vehicleId;
    private final Instant timestamp;
    private final double lat;
    private final double lon;
    private final double speedKmh;
    private final int passengerCount;

    public VehiclePing(String vehicleId, Instant timestamp,
                       double lat, double lon,
                       double speedKmh, int passengerCount) {
        this.vehicleId = Objects.requireNonNull(vehicleId, "Vehicle ID cannot be null");
        this.timestamp = Objects.requireNonNull(timestamp, "Timestamp cannot be null");
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

    @Override
    public String toString() {
        return String.format(
            "Ping[%s at (%.5f, %.5f), speed=%.1f km/h, passengers=%d]",
            vehicleId, lat, lon, speedKmh, passengerCount
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VehiclePing that = (VehiclePing) o;
        return vehicleId.equals(that.vehicleId) && timestamp.equals(that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vehicleId, timestamp);
    }
}