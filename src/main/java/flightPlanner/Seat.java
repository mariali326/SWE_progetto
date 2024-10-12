package flightPlanner;

public class Seat {
    private String seatNumber; // es.13A
    private String classType; // es. "Economy", "Business", "First"
    private String flightNumber;
    private boolean isAvailable;
    private Passenger passenger;
    private String passengerUsername;

    public Seat(String seatNumber, String classType, String flightNumber, boolean isAvailable) {
        this.seatNumber = seatNumber;
        this.classType =classType;
        this.flightNumber = flightNumber;
        this.isAvailable = isAvailable;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public String getClassType() {
        return classType;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public Passenger getPassenger() {
        return passenger;
    }

    public void setPassenger(Passenger passenger) {
        if (this.isAvailable) {
            this.passenger = passenger;
            this.passengerUsername = passenger.getUsername();
            this.isAvailable = false;  // Il posto non è più disponibile
        } else {
            System.out.println("Seat " + seatNumber + " is already assigned.");
        }
    }

    public void setPassengerUsername(String passengerUsername) {
        this.passengerUsername = passengerUsername;
    }

    public String getPassengerUsername() {
        return passengerUsername;
    }

    public void releaseSeat() {
        this.passenger = null;
        this.passengerUsername = null;
        this.isAvailable = true;
    }
}
