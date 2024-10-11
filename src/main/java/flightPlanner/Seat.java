package flightPlanner;

public class Seat {
    private String seatNumber; // es.13A
    private String classType; // es. "Economy", "Business", "First"
    private String flightNumber;
    private boolean isAvailable;

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
}
