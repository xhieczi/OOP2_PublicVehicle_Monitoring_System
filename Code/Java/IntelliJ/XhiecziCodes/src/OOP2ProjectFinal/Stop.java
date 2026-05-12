package OOP2ProjectFinal;


import java.util.*;

class Stop {
    private final String stopId;
    private final String stopName;
    private final double lat;
    private final double lon;

    public Stop(String stopId, String stopName, double lat, double lon) {
        this.stopId = Objects.requireNonNull(stopId);
        this.stopName = Objects.requireNonNull(stopName);
        this.lat = lat;
        this.lon = lon;
    }

    public String stopId() { return stopId; }
    public String stopName() { return stopName; }
    public double lat() { return lat; }
    public double lon() { return lon; }
}
