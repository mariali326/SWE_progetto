package flightPlanner;

public class Ticket {
    private final String ticketNumber;
    private final String bookingId;
    private final String flightNumber;
    private final String seatNumber;
    private final double price;
    private final String passengerName;
    private final String passengerSurname;

    public Ticket(String ticketNumber, String bookingId, String flightNumber, String seatNumber, double price, String passengerName, String passengerSurname) {
        this.ticketNumber = ticketNumber;
        this.bookingId = bookingId;
        this.flightNumber = flightNumber;
        this.seatNumber = seatNumber;
        this.price = price;
        this.passengerName = passengerName;
        this.passengerSurname = passengerSurname;
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

    public String getPassengerName() {
        return passengerName;
    }

    public String getPassengerSurname() {
        return passengerSurname;
    }

    @Override
    public String toString() {
        return "Ticket Number: " + ticketNumber + ", Flight Number: " + flightNumber + ", Seat: " + seatNumber +
                " Passenger: " + passengerName + " " + passengerSurname;
    }
}
