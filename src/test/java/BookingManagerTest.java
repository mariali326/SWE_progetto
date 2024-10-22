import flightPlanner.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BookingManagerTest {

    private static BookingManager bookingManager;

    @BeforeAll
    public static void setUp() throws IOException {
        FlightManager flightManager = new FlightManager();
        bookingManager = new BookingManager(flightManager, new PassengerManager(flightManager), new TicketManager(new LuggageManager()));
    }

    @Test
    @DisplayName("Test that checks loading booking from the CSV file")
    public void testLoadBookingFromCSV() {
        List<Booking> bookings = bookingManager.getAllBookings();

        assertNotNull(bookings);
        assertFalse(bookings.isEmpty());

        Booking booking = bookingManager.getBookingById("BK001");
        assertEquals("BK001", booking.getBookingId());
    }

    @Test
    @DisplayName("Test that checks if adding a booking works correctly and that the CSV file is updated after the addition")
    public void testAddBooking() throws IOException {
        List<Ticket> tickets = new ArrayList<>();
        Ticket ticket = new Ticket("T010", "BK010", "F011", "8A", 250.0,
                "Francesca", "Dattoli", new ArrayList<>());
        ticket.setDocumentType("ID CARD");
        ticket.setDocumentId("ID135C78H");
        tickets.add(ticket);
        Booking newBooking = new Booking("BK010", "cecciasuper", "F011", LocalDateTime.now(), tickets, 250.0);

        bookingManager.addBooking(newBooking);

        Booking addedBooking = bookingManager.getBookingById("BK010");
        assertNotNull(addedBooking);
        assertEquals("cecciasuper", addedBooking.getPassengerUsername());
        assertEquals("F011", addedBooking.getFlightNumber());
        assertEquals(250.0, addedBooking.getTotalAmount());
        assertTrue(addedBooking.getTickets().contains(ticket));
    }

    @Test
    @DisplayName("Test that checks it's impossible to add the same booking more than once")
    public void testAddDuplicateBooking() throws IOException {
        List<Ticket> tickets = new ArrayList<>();
        tickets.add(new Ticket("T015", "BK015", "F009", "20A", 350.0,
                "Stella", "Girailmondo", new ArrayList<>()));
        tickets.getFirst().setDocumentType("PASSPORT");
        tickets.getFirst().setDocumentId("PS123ST45");
        Booking newbooking = new Booking("BK015", "123stella", "F009", LocalDateTime.now(), tickets, 350.0);

        bookingManager.addBooking(newbooking);

        assertThrows(IllegalArgumentException.class, () -> bookingManager.addBooking(newbooking));
    }

    @Test
    @DisplayName("Test that checks removing a booking from the CSV file works correctly")
    public void testRemoveBooking() throws IOException {
        String bookingId = "BK002";

        bookingManager.removeBooking(bookingId);

        Booking removedBooking = bookingManager.getBookingById(bookingId);
        assertNull(removedBooking);
    }

    @Test
    @DisplayName("Test that checks updating a booking status works correctly")
    public void testUpdateBooking() throws IOException {
        Booking originalBooking = bookingManager.getBookingById("BK005");
        assertNotNull(originalBooking, "Original booking should exist");

        List<Ticket> originalTickets = new ArrayList<>(originalBooking.getTickets());
        assertEquals(2, originalTickets.size(), "Original booking should have 2 tickets");

        originalTickets.remove(1);
        assertEquals(1, originalTickets.size(), "Updated booking should have 1 ticket after removal");

        Booking updatedBooking = new Booking(
                originalBooking.getBookingId(),
                originalBooking.getPassengerUsername(),
                originalBooking.getFlightNumber(),
                originalBooking.getBookingDate(),
                originalTickets,
                originalBooking.getTotalAmount() - originalBooking.getTickets().get(1).getPrice()
        );

        bookingManager.updateBooking(updatedBooking);
        assertEquals("BK005", updatedBooking.getBookingId());
        assertEquals(originalTickets, updatedBooking.getTickets());
    }
}
