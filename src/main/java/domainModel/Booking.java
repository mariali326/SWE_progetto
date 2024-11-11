package domainModel;

import java.time.LocalDateTime;
import java.util.List;

public class Booking {
    private final String bookingId;
    private final String passengerUsername;
    private final String flightNumber;
    private final LocalDateTime bookingDate;
    private final List<Ticket> tickets; // Un insieme di biglietti acquistati da un passeggero per un volo specifico
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

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Booking ID: ").append(bookingId)
                .append(", Flight number: ").append(flightNumber)
                .append(", Date: ").append(bookingDate)
                .append(", Total Amount: ").append(totalAmount)
                .append(", Tickets: [");

        for (int i = 0; i < tickets.size(); i++) {
            sb.append(tickets.get(i).toString());
            if (i < tickets.size() - 1) {
                sb.append(", "); // Si usa la virgola per la separazione
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
