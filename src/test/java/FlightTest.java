import flightPlanner.Flight;
import flightPlanner.NotificationType;
import flightPlanner.Observer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class FlightTest {

    @Test
    @DisplayName("Test that checks a flight creation")
    public void testFlightCreation() {
        String flightNumber = "F009";
        String departureAirportCode = "JFK";
        String arrivalAirportCode = "LAX";
        LocalDateTime departureTime = LocalDateTime.now();
        LocalDateTime arrivalTime = LocalDateTime.now().plusHours(6).plusMinutes(15);

        Flight flight = new Flight(flightNumber, departureAirportCode, arrivalAirportCode, departureTime, arrivalTime);

        assertEquals(flightNumber, flight.getFlightNumber());
        assertEquals(departureAirportCode, flight.getDepartureAirportCode());
        assertEquals(arrivalAirportCode, flight.getArrivalAirportCode());
        assertEquals(departureTime, flight.getDepartureTime());
        assertEquals(arrivalTime, flight.getArrivalTime());
    }

    @Test
    @DisplayName("Test that checks if updating new departure and new arrival time works well")
    public void testUpdateDepartureAndArrivalTime() {
        LocalDateTime departure = LocalDateTime.of(2024, 10, 8, 10, 30);
        LocalDateTime arrival = LocalDateTime.of(2024, 10, 8, 23, 25);
        Flight flight = new Flight("F007", "FLR", "PVG", departure, arrival);

        LocalDateTime newDeparture = LocalDateTime.of(2024, 10, 8, 11, 0);
        LocalDateTime newArrival = LocalDateTime.of(2024, 10, 8, 23, 55);

        flight.setDepartureTime(newDeparture);
        flight.setArrivalTime(newArrival);

        assertEquals(newDeparture, flight.getDepartureTime());
        assertEquals(newArrival, flight.getArrivalTime());
    }

    @Test
    @DisplayName("Test that checks if subscription and unsubscription of an observer works correctly")
    public void testSubscribeAndUnsubscribeObserver() {
        Flight flight = new Flight("F009", "JFK", "LAX", LocalDateTime.now(), LocalDateTime.now().plusHours(6).plusMinutes(15));
        Observer observer = mock(Observer.class);

        flight.subscribe(observer);
        assertTrue(flight.getObservers().contains(observer));

        flight.unsubscribe(observer);
        assertFalse(flight.getObservers().contains(observer));
    }

    @Test
    @DisplayName("Test that verifies when a flight status changes, all the observer subscribed to this flight will be notified")
    public void testNotifyObservers() {
        Flight flight = new Flight("F009", "JFK", "LAX", LocalDateTime.now(), LocalDateTime.now().plusHours(6).plusMinutes(15));
        Observer observer1 = mock(Observer.class);// Implementazione fittizia che ci permette di vedere come viene chiamato il metodo update
        Observer observer2 = mock(Observer.class);// Non serve un'implementazione della classe Observer in modo reale

        flight.subscribe(observer1);
        flight.subscribe(observer2);

        flight.updateFlightStatus(LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(7), "Flight Delayed", NotificationType.DELAY);

        verify(observer1).update("Flight Delayed", NotificationType.DELAY);// Verificare se il metodo viene chiamato con i parametri corretti
        verify(observer2).update("Flight Delayed", NotificationType.DELAY);
    }

    @Test
    @DisplayName("Test that checks the method toString() returns the right format of a string")
    public void testToString() {
        LocalDateTime departure = LocalDateTime.of(2024, 10, 8, 10, 30);
        LocalDateTime arrival = LocalDateTime.of(2024, 10, 8, 16, 45);
        Flight flight = new Flight("F009", "JFK", "LAX", departure, arrival);

        String expectedString = "F009 - JFK to LAX | Departure: " + departure + " | Arrival: " + arrival;
        assertEquals(expectedString, flight.toString());
    }

    @Test
    @DisplayName("Test that checks the same observer can't be subscribed more than once")
    public void testNoDuplicateObservers() {
        Flight flight = new Flight("F009", "JFK", "LAX", LocalDateTime.now(), LocalDateTime.now().plusHours(6).plusMinutes(15));
        Observer observer = mock(Observer.class);

        flight.subscribe(observer);
        flight.subscribe(observer);

        assertEquals(1, flight.getObservers().size());
    }
}


