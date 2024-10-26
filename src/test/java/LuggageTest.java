import flightPlanner.Luggage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LuggageTest {

    @Test
    @DisplayName("Test that checks the creation of a luggage")
    public void testLuggageCreation() {
        String luggageId = "L024";
        double weight = 8;
        String luggageType = "cabin";
        double cost = 0;
        double length = 20;
        double width = 25;
        double height = 20;
        String ticketNumber = "T024";

        Luggage luggage = new Luggage(luggageId, weight, luggageType, cost, length, width, height, ticketNumber);

        assertEquals(luggageId, luggage.getLuggageId());
        assertEquals(ticketNumber, luggage.getTicketNumber());
        assertEquals(weight, luggage.getWeight());
        assertEquals(length, luggage.getLength());
        assertEquals(width, luggage.getWidth());
        assertEquals(height, luggage.getHeight());
        assertEquals(luggageType, luggage.getType());
        assertEquals(cost, luggage.getCost());
    }

    @Test
    @DisplayName("Test that checks the getter method of dimensions and weight based on class types works correctly")
    public void testGetDimensionsAndWeight() {
        String classType = "Economy";
        double maxLengthHold = Luggage.getMaxLengthHold(classType);
        double maxWidthHold = Luggage.getMaxWidthHold(classType);
        double maxHeightHold = Luggage.getMaxHeightHold(classType);
        double maxWeightHold = Luggage.getMaxWeightHold(classType);

        assertEquals(50, maxLengthHold);
        assertEquals(40, maxWidthHold);
        assertEquals(20, maxHeightHold);
        assertEquals(23, maxWeightHold);
    }

    @Test
    @DisplayName("Test that checks the toString() method returns the correct string format")
    public void testToString() {
        String luggageType = "cabin";

        Luggage luggage = new Luggage("L025", 9, luggageType, 50, 30, 25, 20, "T025");

        String expectedString = luggageType + " luggage: 30.0x25.0x20.0, 9.0kg, Cost: 50.0 EUR";
        assertEquals(expectedString, luggage.toString());
    }
}
