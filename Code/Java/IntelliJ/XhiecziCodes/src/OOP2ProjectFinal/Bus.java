package OOP2ProjectFinal;

public class Bus extends PublicVehicle {
    
    public Bus(String vehicleId, String plateNumber, int capacity) {
        super(vehicleId, plateNumber, capacity);
    }
    
    public String getVehicleType() {
        return "Bus";
    }
    
    // Remove @Override if getSpeedLimit doesn't exist in parent
    public double getSpeedLimit() {
        return 60;
    }
}