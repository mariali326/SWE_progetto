package flightPlanner;

public class Seat {
    private final String seatNumber; // es.13A
    private String classType; // es. "Economy", "Business", "First"
    private final String flightNumber;
    private boolean isAvailable;

    public Seat(String seatNumber, String classType, String flightNumber, boolean isAvailable) {
        this.seatNumber = seatNumber;
        this.classType = classType;
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

    public void setClassType(String classType) {
        this.classType = classType;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void releaseSeat() {
        this.isAvailable = true;
    }
}
