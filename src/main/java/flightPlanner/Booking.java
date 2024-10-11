package flightPlanner;

import java.time.LocalDateTime;
import java.util.List;

public class Booking {
    private String bookingId;
    private String passengerUsername;
    private String flightNumber;
    private LocalDateTime bookingDate;
    private List<Ticket> tickets; // un insieme di biglietti acquistati da un passeggero per un volo specifico
    private double totalAmount;

    public Booking(String bookingId, String passengerUsername, String flightNumber, LocalDateTime bookingDate, List<Ticket> tickets, double totalAmount) {
        this.bookingId = bookingId;
        this.passengerUsername = passengerUsername;
        this.flightNumber = flightNumber;
        this.bookingDate = bookingDate;
        this.tickets = tickets;
        this.totalAmount = totalAmount;
    }

    public String getBookingId() {
        return bookingId;
    }

    public String getPassengerUsername() {
        return passengerUsername;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public LocalDateTime getBookingDate() {
        return bookingDate;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    @Override
    public String toString() {
        return "Booking ID: " + bookingId + ", Flight number: " + getFlightNumber() + ", Date: " + bookingDate;
    }

    public void removeTicket(String ticketId) {
        tickets.removeIf(ticket -> ticket.getTicketNumber().equalsIgnoreCase(ticketId));
    }

    public void addTicket(Ticket ticket) {
        tickets.add(ticket);
    }
}
