package OOP2Project;

class OffRouteAlert extends Alert {

    public OffRouteAlert(String message) {
        super("WARN", message);
    }

    @Override
    public String type() {
        return "OFF_ROUTE";
    }
}
