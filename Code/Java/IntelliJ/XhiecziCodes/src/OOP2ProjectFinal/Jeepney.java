package OOP2ProjectFinal;

class Jeepney extends PublicVehicle {
    public Jeepney(String vehicleId, String plateNumber, int capacity) {
        super(vehicleId, plateNumber, capacity);
    }
    @Override public String getVehicleType() { return "Jeepney"; 
    }
}
