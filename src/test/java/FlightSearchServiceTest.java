import flightPlanner.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class FlightSearchServiceTest {

    @Mock
    private FlightManager mockFlightManager;

    @Mock
    private AirportManager mockAirportManager;

    @InjectMocks// I mock vengono iniettati al suo interno
    private FlightSearchService flightSearchService;

    @BeforeEach
    public void setUp() throws Exception {
        try (var _ = MockitoAnnotations.openMocks(this)) {
            // Usato per inizializzare le annotazioni di Mockito durante il test, es.@Mock per creare oggetti mock
            // openMocks restituisce un oggetto che implementa l'interfaccia AutoCloseable che deve'essere chiuso per rilasciare correttamente le risorse

            List<Airport> airports = new ArrayList<>();
            airports.add(new Airport("JFK", "John F. Kennedy International Airport", "New York", "USA"));
            airports.add(new Airport("LAX", "Los Angeles International Airport", "Los Angeles", "USA"));

            List<Flight> flights = new ArrayList<>();
            LocalDateTime departureTime = LocalDate.now().atTime(10, 0);
            flights.add(new Flight("F001", "JFK", "LAX", departureTime,
                    departureTime.plusHours(5).plusMinutes(30), 240, 54, 10));
            flights.add(new Flight("F011", "LAX", "JFK", departureTime.plusHours(5),
                    departureTime.plusHours(10).plusMinutes(30), 250, 50, 12));

            // Simula il comportamento delle dipendenze, testa solo la logica all'interno di questa classe senza interagire con i reali manager
            when(mockFlightManager.getAllFlights()).thenReturn(flights);
            when(mockAirportManager.getAllAirports()).thenReturn(airports);
        }
    }

    @Test
    @DisplayName("Test that checks the validity of a search of flight based on departure and arrival(code or city)")
    public void testSearchFlightsValidDepartureAndArrival() {
        LocalDate searchDate = LocalDate.now();
        List<Flight> result = flightSearchService.searchFlights("New York", "LAX", searchDate);

        assertEquals(1, result.size());
        assertEquals("F001", result.getFirst().getFlightNumber());
    }

    @Test
    @DisplayName("Test that checks searching with no results works correctly")
    public void testSearchFlightsNoResults() {
        LocalDate searchDate = LocalDate.now();
        List<Flight> result = flightSearchService.searchFlights("JFK", "SFO", searchDate);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test that checks if a user enters a wrong date, the result should be empty")
    public void testSearchFlightsWithDifferentDate() {
        LocalDate searchDate = LocalDate.now().plusDays(1); // Data diversa
        List<Flight> result = flightSearchService.searchFlights("JFK", "LAX", searchDate);

        assertTrue(result.isEmpty());
    }
}
//è possibile fare test anche senza mock, basandosi sui dati nei file csv(più realistico)
//public class FlightSearchServiceTest {
//
//    private static FlightManager flightManager;
//    private static AirportManager airportManager;
//    private static FlightSearchService flightSearchService;
//
//    @BeforeAll // Eseguita una volta sola prima di tutte gli altri test
//    public static void setup() throws IOException {
//        // Vengono caricati i dati dal CSV
//        flightManager = new FlightManager();
//        airportManager = new AirportManager();
//        flightSearchService = new FlightSearchService(flightManager, airportManager);
//    }
//
//    @Test
//    public void testSearchFlightsWithDifferentDateFromCSV() {
//        LocalDate searchDate = LocalDate.now();
//        List<Flight> result = flightSearchService.searchFlights("JFK", "LAX", searchDate);
//
//        assertTrue(result.isEmpty());
//    }
// Altri test...
//}
