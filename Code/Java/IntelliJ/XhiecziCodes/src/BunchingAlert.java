package OOP2Project;

class BunchingAlert extends Alert {
    public BunchingAlert(String message) {
        super("WARN", message);
    }

    @Override
    public String type() {
        return "BUNCHING";
    }
}



class OffRouteAlert extends Alert {
    public OffRouteAlert(String message) {
        super("WARN", message);
    }

    @Override
    public String type() {
        return "OFF_ROUTE";
    }
}



class OvercapacityAlert extends Alert {
    public OvercapacityAlert(String message) {
        super("CRITICAL", message);
    }

    @Override
    public String type() {
        return "OVERCAPACITY";
    }
}



class OverspeedAlert extends Alert {
    public OverspeedAlert(String message) {
        super("WARN", message);
    }

    @Override
    public String type() {
        return "OVERSPEED";
    }
}



class TrafficAlert extends Alert {
    public TrafficAlert(String message) {
        super("INFO", message);
    }

    @Override
    public String type() {
        return "TRAFFIC";
    }
}



class IdleVehicleAlert extends Alert {
    public IdleVehicleAlert(String message) {
        super("INFO", message);
    }

    @Override
    public String type() {
        return "IDLE_VEHICLE";
    }
}
