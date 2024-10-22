import flightPlanner.Airport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AirportTest {

    @Test
    @DisplayName("Test that checks the creation of an airport")
    public void AirportCreation() {
        String code = "WI";
        String name = "Wonderland International";
        String city = "Far Away";
        String country = "Childhood";

        Airport airport = new Airport(code, name, city, country);

        assertEquals(code, airport.getCode());
        assertEquals(name, airport.getName());
        assertEquals(city, airport.getCity());
        assertEquals(country, airport.getCountry());
    }

    @Test
    @DisplayName("Test that checks the toString() method returns the correct string format")
    public void TestToString() {
        String code = "WI";
        String name = "Wonderland International";

        Airport airport = new Airport(code, name, "Far Away", "Childhood");

        String expectedString = code + " - " + name + ", Far Away, Childhood";
        assertEquals(expectedString, airport.toString());
    }
}
