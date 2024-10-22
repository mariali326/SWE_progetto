import flightPlanner.Route;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RouteTest {
    @Test
    @DisplayName("Test that check the creation of a route")
    public void testRouteCreation() {
        String routeId = "R007";
        String departureAirportCode = "PVG";
        String arrivalAirportCode = "FLR";
        double distance = 14047;
        Duration flightDuration = Route.parseDuration("12h55m");

        Route route = new Route(routeId, departureAirportCode, arrivalAirportCode, distance, flightDuration);

        assertEquals(routeId, route.getRouteId());
        assertEquals(departureAirportCode, route.getDepartureAirportCode());
        assertEquals(arrivalAirportCode, route.getArrivalAirportCode());
        assertEquals(distance, route.getDistance());
        assertEquals(flightDuration, route.getFlightDuration());
    }

    @Test
    @DisplayName("Test that checks the toString() method returns the right string format")
    public void TestToString() {
        Duration flightDuration = Duration.ofHours(1).plusMinutes(30);
        Route route = new Route("R008", "MXP", "CDG", 801, flightDuration);

        String expectedString = "R008: MXP - CDG the distance is " + route.getDistance() + "km and the flight duration is "
                + route.getFormattedDuration();
        assertEquals(expectedString, route.toString());
        assertEquals("1h30m", route.getFormattedDuration());
    }

    @Test
    @DisplayName("Test that checks the conversion from String to Duration")
    public void testStringToDurationConversion() {
        String flightDuration = "2h45";
        Duration testDuration = Route.parseDuration(flightDuration);

        assertEquals(testDuration, Duration.ofHours(2).plusMinutes(45));
    }
}
