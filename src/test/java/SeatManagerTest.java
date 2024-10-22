import flightPlanner.Seat;
import flightPlanner.SeatManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SeatManagerTest {

    private static SeatManager seatManager;

    @BeforeAll
    public static void setUp() throws IOException {
        seatManager = new SeatManager();
    }

    @Test
    @DisplayName("Test that checks loading seats from the CSV file")
    public void testLoadSeatsFromCSV() {
        List<Seat> seats = seatManager.getAllSeats();

        assertNotNull(seats);
        assertFalse(seats.isEmpty());

        Seat seat = seatManager.getSeatByNumber("1A", "F001");
        assertNotNull(seat);
        assertEquals("1A", seat.getSeatNumber());
        assertEquals("F001", seat.getFlightNumber());

        List<Seat> seatsOfFlight = seatManager.getSeatsByFlightNumber("F001");
        assertNotNull(seatsOfFlight);
        assertEquals(4, seatsOfFlight.size());
    }

    @Test
    @DisplayName("Test that checks if adding a new seat works correctly and that the CSV file is updated after addition")
    public void testAddSeat() throws IOException {
        Seat newSeat = new Seat("1A", "Economy", "F002", true);

        seatManager.addSeat(newSeat);

        Seat addedSeat = seatManager.getSeatByNumber("1A", "F002");

        assertNotNull(addedSeat);
        assertEquals("Economy", addedSeat.getClassType());
        assertTrue(addedSeat.isAvailable());
    }

    @Test
    @DisplayName("Test that checks it's impossible to add the same seat more than once")
    public void testAddDuplicateSeat() throws IOException {
        Seat seat = new Seat("14A", "Business", "F003", true);
        seatManager.addSeat(seat);
        assertThrows(IllegalArgumentException.class, () -> seatManager.addSeat(seat));
    }

    @Test
    @DisplayName("Test that checks removing a seat works correctly")
    public void testRemoveSeat() throws IOException {
        Seat seat = seatManager.getSeatByNumber("1A", "F002");
        seatManager.removeSeats(seat);

        Seat removedSeat = seatManager.getSeatByNumber("1A", "F002");
        assertNull(removedSeat);
    }

    @Test
    @DisplayName("Test that checks updating a seat's availability works correctly")
    public void testUpdateSeatAvailability() throws IOException {
        Seat seat = new Seat("12A", "Economy", "F008", true);
        seatManager.addSeat(seat);

        seatManager.updateSeatAvailability("12A", "F008", false);

        Seat updatedSeat = seatManager.getSeatByNumber("12A", "F008");
        assertFalse(updatedSeat.isAvailable());
    }

    @Test
    @DisplayName("Test that checks the booking process for a seat")
    public void testBookSeat() throws IOException {
        Seat seat = new Seat("12B", "Economy", "F008", true);
        seatManager.addSeat(seat);

        seatManager.bookSeat("F008", "12B");

        Seat bookedSeat = seatManager.getSeatByNumber("12B", "F008");
        assertFalse(bookedSeat.isAvailable());
    }

    @Test
    @DisplayName("Test that checks the release of a seat ")
    public void testReleaseSeat() throws IOException {
        seatManager.releaseSeat("12B", "F008");

        Seat releasedSeat = seatManager.getSeatByNumber("12B", "F008");
        assertTrue(releasedSeat.isAvailable());
    }

    @Test
    @DisplayName("Test that checks the search for available seats on a specific flight")
    public void testFindAvailableSeats() {
        List<Seat> availableSeats = seatManager.findAvailableSeats("F001");
        assertEquals(2, availableSeats.size());
        assertEquals("6B", availableSeats.get(0).getSeatNumber());
    }

    @Test
    @DisplayName("Test that checks the available seats along with their class types ")
    public void testGetAvailableSeatsWithClassType() {
        Map<String, String> availableSeatsMap = seatManager.getAvailableSeatsWithClassType("F001");
        assertEquals(2, availableSeatsMap.size());
        assertEquals("Business", availableSeatsMap.get("9C"));
        assertEquals("First", availableSeatsMap.get("6B"));
    }
}
