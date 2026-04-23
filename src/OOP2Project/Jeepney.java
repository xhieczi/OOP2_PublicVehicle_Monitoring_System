package OOP2Project;

class Jeepney extends PublicVehicle {

    public Jeepney(String vehicleId, String plateNumber, int seatingCapacity,
                   String vehicleColor, String engineType) {

        super(vehicleId, plateNumber, seatingCapacity, vehicleColor, engineType);
    }

    @Override
    public String getVehicleType() {
        return "Jeepney";
    }
}