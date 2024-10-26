import flightPlanner.Luggage;
import flightPlanner.LuggageManager;
import flightPlanner.Ticket;
import flightPlanner.TicketManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class TicketManagerTest {

    private static TicketManager ticketManager;

    @BeforeAll
    public static void setUp() throws IOException {
        LuggageManager luggageManager = new LuggageManager();
        ticketManager = new TicketManager(luggageManager);
    }

    @Test
    @DisplayName("Test that checks loading tickets from the CSV file and verifies that it's possible to find a ticket listed in the file")
    public void testLoadTicketsFromCSV() {
        List<Ticket> tickets = ticketManager.getAllTickets();

        assertNotNull(tickets);
        assertFalse(tickets.isEmpty());

        Ticket ticket = ticketManager.getTicketByNumber("T001");
        assertNotNull(ticket);
        assertEquals("T001", ticket.getTicketNumber());
    }

    @Test
    @DisplayName("Test that checks if adding a ticket works correctly and that the CSV file is updated after the addition")
    public void testAddTicket() throws IOException {
        Ticket ticket = new Ticket("T011", "BK011", "F012", "9A", 250.0,
                "Beatrice", "Van Gogh", new ArrayList<>());
        ticket.setDocumentType("ID CARD");
        ticket.setDocumentId("ID423V79B");

        ticketManager.addTicket(ticket);

        Ticket addedTicket = ticketManager.getTicketByNumber("T011");
        assertNotNull(addedTicket);
        assertEquals("Beatrice", addedTicket.getPassengerName());
        assertEquals("F012", addedTicket.getFlightNumber());
        assertEquals(250.0, addedTicket.getPrice());
        assertTrue(addedTicket.getLuggageList().isEmpty());
    }

    @Test
    @DisplayName("Test that checks it's impossible to add the same ticket more than once")
    public void testAddDuplicateTicket() throws IOException {
        Ticket newTicket = new Ticket("T045", "BK045", "F020", "24B", 350,
                "Sky", "Dreamer", new ArrayList<>());
        newTicket.setDocumentType("PASSPORT");
        newTicket.setDocumentId("PS468SKD9");

        ticketManager.addTicket(newTicket);

        assertThrows(IllegalArgumentException.class, () -> ticketManager.addTicket(newTicket));
    }

    @Test
    @DisplayName("Test that checks removing a ticket from the CSV file works correctly")
    public void testRemoveTicket() throws IOException {
        String ticketNumber = "T002";

        ticketManager.removeTicket(ticketNumber);

        Ticket removedTicket = ticketManager.getTicketByNumber(ticketNumber);
        assertNull(removedTicket);
    }

    @Test
    @DisplayName("Test that checks updating a ticket status works correctly")
    public void testUpdateTicket() throws IOException {
        Ticket updatedTicket = ticketManager.getTicketByNumber("T005");
        assertNotNull(updatedTicket, "Original ticket should exist");

        List<Luggage> originalLuggage = new ArrayList<>(updatedTicket.getLuggageList()); // Verifichiamo prima su una copia della lista senza modificare i dati originali
        assertEquals(3, originalLuggage.size(), "Original ticket should have 3 luggage");

        originalLuggage.remove(1);
        assertEquals(2, originalLuggage.size(), "Updated ticket should have 2 luggage after removal");

        double updatedPrice = updatedTicket.getPrice() - updatedTicket.getLuggageList().get(1).getCost();
        updatedTicket.getLuggageList().remove(1);

        ticketManager.updateTicket(updatedTicket.getTicketNumber());

        assertEquals("T005", updatedTicket.getTicketNumber());
        assertEquals(2, updatedTicket.getLuggageList().size());
        assertEquals(updatedPrice, updatedTicket.getPrice());
        assertEquals(originalLuggage, updatedTicket.getLuggageList());
    }
}
