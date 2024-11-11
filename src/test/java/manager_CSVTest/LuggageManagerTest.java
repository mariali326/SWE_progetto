package manager_CSVTest;

import domainModel.Luggage;
import manager_CSV.LuggageManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class LuggageManagerTest {

    private static LuggageManager luggageManager;

    @BeforeAll
    public static void setUp() throws IOException {
        luggageManager = new LuggageManager();
    }

    @Test
    @DisplayName("Text that checks loading luggage from the CSV file and verifies that it's possible to find a luggage listed in the file")
    public void testLoadLuggageFromCSV() {
        List<Luggage> luggageList = luggageManager.getAllLuggage();

        assertNotNull(luggageList);
        assertFalse(luggageList.isEmpty());

        Luggage luggage = luggageManager.getLuggageById("L001");
        assertNotNull(luggage);
        assertEquals("L001", luggage.getLuggageId());
    }

    @Test
    @DisplayName("Test that checks if adding a luggage works correctly and that the CSV file is updated after addition")
    public void testAddLuggage() throws IOException {
        Luggage newluggage = new Luggage("L026", 20, "hold", 0, 50, 40, 20, "T026");

        luggageManager.addLuggage(newluggage);

        Luggage addedLuggage = luggageManager.getLuggageById("L026");
        assertNotNull(addedLuggage);
        assertEquals("L026", addedLuggage.getLuggageId());
        assertEquals("T026", addedLuggage.getTicketNumber());
        assertEquals("hold", addedLuggage.getType());
    }

    @Test
    @DisplayName("Test that checks it's impossible to add the same luggage more than once")
    public void testAddDuplicatedLuggage() throws IOException {
        Luggage luggage = new Luggage("L027", 5, "cabin", 0, 20, 20, 20, "T027");

        luggageManager.addLuggage(luggage);
        assertThrows(IllegalArgumentException.class, () -> luggageManager.addLuggage(luggage));
    }

    @Test
    @DisplayName("Test that checks removing a luggage from the CSV file works correctly")
    public void testRemoveLuggage() throws IOException {
        String luggageId = "L001";

        luggageManager.removeLuggage(luggageId);
        Luggage removedLuggage = luggageManager.getLuggageById(luggageId);

        assertNull(removedLuggage);
    }
}
