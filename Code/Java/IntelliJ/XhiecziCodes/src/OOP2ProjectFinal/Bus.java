package OOP2ProjectFinal;

public class Bus extends PublicVehicle {

    public Bus(String vehicleId, String plateNumber, int capacity) {
        super(vehicleId, plateNumber, capacity);
    }

    @Override
    public String getVehicleType() {
        return "Bus";
    }
}