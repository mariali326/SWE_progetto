import flightPlanner.Flight;
import flightPlanner.FlightManager;
import flightPlanner.NotificationType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FlightManagerTest {

    private static FlightManager flightManager;

    @BeforeAll
    public static void setUp() throws IOException {
        flightManager = new FlightManager();
    }

    @Test
    @DisplayName("Text that checks loading flights from the file CSV and verifies it's possible find a flight written on the file")
    public void testLoadFlightsFromCSV() {
        List<Flight> flights = flightManager.getAllFlights();

        // Il file CSV contiene almeno un volo
        assertNotNull(flights);
        assertFalse(flights.isEmpty());

        Flight flight = flightManager.getFlightByNumber("F001");
        assertEquals("F001", flight.getFlightNumber());
    }

    @Test
    @DisplayName("Test that checks adding a flight works correctly and after addition the file CSV is updated")
    public void testAddFlight() throws IOException {
        Flight newFlight = new Flight("F010", "PVG", "FLR", LocalDateTime.now(), LocalDateTime.now().plusHours(12).plusMinutes(55));

        flightManager.addFlight(newFlight);

        Flight addedFlight = flightManager.getFlightByNumber("F010");
        assertNotNull(addedFlight);
        assertEquals("PVG", addedFlight.getDepartureAirportCode());
        assertEquals("FLR", addedFlight.getArrivalAirportCode());
    }

    @Test
    @DisplayName("Test that checks it's impossible to add the same flight more than once")
    public void testAddDuplicateFlight() throws IOException {
        Flight newFlight = new Flight("F011", "JFK", "LHR", LocalDateTime.now(), LocalDateTime.now().plusHours(7).plusMinutes(30));

        flightManager.addFlight(newFlight);
        assertThrows(IllegalArgumentException.class, () -> flightManager.addFlight(newFlight));
    }

    @Test
    @DisplayName("Test that checks removing a flight from the file CSV works correctly")
    public void testRemoveFlight() throws IOException {
        String flightNumber = "F001";

        flightManager.removeFlight(flightNumber);

        Flight removedFlight = flightManager.getFlightByNumber(flightNumber);
        assertNull(removedFlight);
    }

    @Test
    @DisplayName("Test that checks that updating a flight status works correctly")
    public void testUpdateFlightStatus() throws IOException {
        String flightNumber = "F001";
        LocalDateTime newDepartureTime = LocalDateTime.now().plusHours(1);
        LocalDateTime newArrivalTime = LocalDateTime.now().plusHours(7).plusMinutes(15);

        flightManager.updateFlightStatus(flightNumber, newDepartureTime, newArrivalTime, "Flight delayed", NotificationType.DELAY);

        Flight updatedFlight = flightManager.getFlightByNumber(flightNumber);
        assertEquals(newDepartureTime, updatedFlight.getDepartureTime());
        assertEquals(newArrivalTime, updatedFlight.getArrivalTime());
    }
}
