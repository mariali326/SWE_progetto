import flightPlanner.Booking;
import flightPlanner.Ticket;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BookingTest {

    @Test
    @DisplayName("Test that checks a booking creation")
    public void testBookingCreation() {
        String bookingId = "BK007";
        String passengerUsername = "happyPassenger";
        String flightNumber = "F008";
        LocalDateTime bookingDate = LocalDateTime.now();
        List<Ticket> tickets = new ArrayList<>();
        tickets.add(new Ticket("T007", bookingId, flightNumber, "18A", 350.0,
                "Alfonso", "Valdimare"));
        tickets.add(new Ticket("T008", bookingId, flightNumber, "18B", 350.0, "Geltrude",
                "Bevilacqua"));

        double totalAmount = 0.0;
        for (Ticket ticket : tickets) {
            totalAmount += ticket.getPrice();
        }

        Booking booking = new Booking(bookingId, passengerUsername, flightNumber, bookingDate, tickets, totalAmount);

        assertEquals(bookingId, booking.getBookingId());
        assertEquals(flightNumber, booking.getFlightNumber());
        assertEquals(bookingDate, booking.getBookingDate());
        assertEquals(tickets, booking.getTickets());
        assertEquals(totalAmount, booking.getTotalAmount());
        assertEquals(passengerUsername, booking.getPassengerUsername());
    }

    @Test
    @DisplayName("Test that checks if the method toString() returns the right string format")
    public void testToString() {
        Ticket ticket = new Ticket("T001", "BK001", "F001", "1A", 500.0, "John", "Doe");
        List<Ticket> tickets = new ArrayList<>();
        tickets.add(ticket);

        Booking booking = new Booking("BK001", "jdoe", "F001", LocalDateTime.now(), tickets, 500.0);

        String expected = "Booking ID: BK001, Flight number: F001, Date: " + booking.getBookingDate()
                + ", Total Amount: 500.0, Tickets: [" + ticket + "]";

        assertEquals(expected, booking.toString());
    }
}
