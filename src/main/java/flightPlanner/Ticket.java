package flightPlanner;

public class Ticket {
    private String ticketNumber;
    private String bookingId;
    private String flightNumber;
    private String seatNumber;
    private double price;

    public Ticket(String ticketNumber, String bookingId, String flightNumber, String seatNumber, double price) {
        this.ticketNumber = ticketNumber;
        this.bookingId = bookingId;
        this.flightNumber = flightNumber;
        this.seatNumber = seatNumber;
        this.price = price;
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

    @Override
    public String toString() {
        return "Ticket Number: " + ticketNumber + ", Flight Number: " + flightNumber + ", Seat: " + seatNumber;
    }
}
