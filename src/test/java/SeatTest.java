import flightPlanner.Seat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SeatTest {
    @Test
    @DisplayName("Test that checks the creation of a seat")
    public void testSeatCreation() {
        String seatNumber = "13C";
        String classType = "Economy";
        String flightNumber = "F014";
        boolean isAvailable = false;

        Seat seat = new Seat(seatNumber, classType, flightNumber, isAvailable);

        assertEquals(seatNumber, seat.getSeatNumber());
        assertEquals(classType, seat.getClassType());
        assertEquals(flightNumber, seat.getFlightNumber());
        assertFalse(seat.isAvailable());

        seat.releaseSeat();
        assertTrue(seat.isAvailable());
    }

    @Test
    @DisplayName("Test that checks the toString() method returns the right format of a string")
    public void testToString() {
        Seat seat = new Seat("13D", "Economy", "F014", false);

        String expectedString = "The seat 13D - " + seat.getClassType() + " on the flight F014 is available: " + seat.isAvailable();

        assertEquals(expectedString, seat.toString());
    }
}
