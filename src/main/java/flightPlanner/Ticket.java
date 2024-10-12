package flightPlanner;

public class Ticket {
    private String ticketNumber;
    private String bookingId;
    private String flightNumber;
    private String seatNumber;
    private double price;
    private String passengerUsername;

    public Ticket(String ticketNumber, String bookingId, String flightNumber, String seatNumber, double price, String passengerUsername) {
        this.ticketNumber = ticketNumber;
        this.bookingId = bookingId;
        this.flightNumber = flightNumber;
        this.seatNumber = seatNumber;
        this.price = price;
        this.passengerUsername = passengerUsername;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public String getBookingId() {
        return bookingId;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public double getPrice() {
        return price;
    }

    public String getPassengerUsername() {
        return passengerUsername;
    }

    @Override
    public String toString() {
        return "Ticket Number: " + ticketNumber + ", Flight Number: " + flightNumber + ", Seat: " + seatNumber +
                " Passenger Username: " + passengerUsername;
    }
}
