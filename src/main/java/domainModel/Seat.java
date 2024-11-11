package domainModel;

public class Seat {
    private final String seatNumber; // es.13A
    private final String flightNumber;
    private final String classType; // es. "Economy", "Business", "First"
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

    public String getFlightNumber() {
        return flightNumber;
    }

    public void releaseSeat() {
        this.isAvailable = true;
    }

    @Override
    public String toString() {
        return "The seat " + seatNumber + " - " + classType + " on the flight " + flightNumber + " is available: " + isAvailable;
    }
}
