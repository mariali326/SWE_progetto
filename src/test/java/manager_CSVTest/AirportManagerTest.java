package manager_CSVTest;

import domainModel.Airport;
import manager_CSV.AirportManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AirportManagerTest {

    private static AirportManager airportManager;

    @BeforeAll
    public static void setUp() throws IOException {
        airportManager = new AirportManager();
    }

    @Test
    @DisplayName("Text that checks loading airports from the CSV file and verifies that it's possible to find an airport listed in the file")
    public void testLoadAirportsFromCSV() {
        List<Airport> airports = airportManager.getAllAirports();

        assertNotNull(airports);
        assertFalse(airports.isEmpty());

        Airport airport = airportManager.getAirportByCode("FLR");

        assertNotNull(airport);
        assertEquals("FLR", airport.getCode());
    }

    @Test
    @DisplayName("Test that checks if adding an airport works correctly and that the CSV file is updated after addition")
    public void testAddAirport() throws IOException {
        Airport newAirport = new Airport("WI", "Wonderland International", "Far Away", "Childhood");

        airportManager.addAirport(newAirport);

        Airport addedAirport = airportManager.getAirportByCode("WI");
        assertNotNull(addedAirport);
        assertEquals("Wonderland International", addedAirport.getName());
        assertEquals("Childhood", addedAirport.getCountry());
    }

    @Test
    @DisplayName("Test that checks it's impossible to add the same airport more than once")
    public void testAddDuplicatedAirport() throws IOException {
        Airport airport = new Airport("YY", "Life Philosophy", "So close", "EntireLife");

        airportManager.addAirport(airport);
        assertThrows(IllegalArgumentException.class, () -> airportManager.addAirport(airport));
    }

    @Test
    @DisplayName("Test that checks removing an airport from the CSV file works correctly")
    public void testRemoveAirport() throws IOException {
        String airportCode = "PVG";

        airportManager.removeAirport(airportCode);

        Airport removedAirport = airportManager.getAirportByCode(airportCode);
        assertNull(removedAirport);

    }

    @Test
    @DisplayName("Test that checks that updating an airport status works correctly")
    public void testUpdateAirport() throws IOException {

        Airport updatedAirport = new Airport("PVG", "Pudong", "Shanghai", "China");
        airportManager.updateAirport(updatedAirport);

        Airport updated = airportManager.getAirportByCode("PVG");

        assertEquals("Pudong", updated.getName());
    }
}
