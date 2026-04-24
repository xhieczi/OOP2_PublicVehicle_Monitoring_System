package OOP2Project;

class BunchingAlert extends Alert {
    public BunchingAlert(String message) {
        super("WARN", message);
    }
    @Override public String type() { return "BUNCHING"; }
}

/* 13) OffRouteAlert */
class OffRouteAlert extends Alert {
    public OffRouteAlert(String message) {
        super("WARN", message);
    }
    @Override public String type() { return "OFF_ROUTE"; }
}
