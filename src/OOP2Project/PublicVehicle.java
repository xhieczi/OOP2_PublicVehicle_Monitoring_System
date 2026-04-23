package OOP2Project;

import java.util.Objects;

abstract class PublicVehicle {

    private final String vehicleId;
    private final String plateNumber;
    private final int seatingCapacity;
    private final String vehicleColor;
    private final String engineType;

    private String routeCode;

    private Double lat;
    private Double lon;
    private double speedKmh;
    private int passengerCount;

    protected PublicVehicle(String vehicleId, String plateNumber,
                            int seatingCapacity, String vehicleColor,
                            String engineType) {

        this.vehicleId = Objects.requireNonNull(vehicleId);
        this.plateNumber = Objects.requireNonNull(plateNumber);
        this.seatingCapacity = seatingCapacity;
        this.vehicleColor = vehicleColor;
        this.engineType = engineType;
    }

    // Getters
    public String getVehicleId() { return vehicleId; }

    public String getPlateNumber() { return plateNumber; }

    public int getSeatingCapacity() { return seatingCapacity; }

    public String getVehicleColor() { return vehicleColor; }

    public String getEngineType() { return engineType; }

    public String getRouteCode() { return routeCode; }

    public void setRouteCode(String routeCode) { this.routeCode = routeCode; }

    public boolean hasLocation() {
        return lat != null && lon != null;
    }

    public double getLat() { return lat != null ? lat : 0.0; }

    public double getLon() { return lon != null ? lon : 0.0; }

    public double getSpeedKmh() { return speedKmh; }

    public int getPassengerCount() { return passengerCount; }

    public void updateLocation(double lat, double lon, double speed, int passengerCount) {
        this.lat = lat;
        this.lon = lon;
        this.speedKmh = speed;
        this.passengerCount = passengerCount;
    }

    public abstract String getVehicleType();
}