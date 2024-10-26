import flightPlanner.Luggage;
import flightPlanner.Ticket;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TicketTest {

    @Test
    @DisplayName("Test that checks the creation of a ticket")
    public void testTicketCreation() {
        String ticketNumber = "T020";
        String bookingId = "BK020";
        String flightNumber = "F001";
        String seatNumber = "15B";
        double price = 500;
        String passengerName = "Laura";
        String passengerSurname = "Felice";
        List<Luggage> luggageList = new ArrayList<>();
        luggageList.add(new Luggage("L025", 5, "cabin", 0, 20, 20, 20, ticketNumber));

        Ticket ticket = new Ticket(ticketNumber, bookingId, flightNumber, seatNumber, price, passengerName, passengerSurname, luggageList);
        ticket.setDocumentType("ID CARD");
        ticket.setDocumentId("ID135678909");

        assertEquals("T020", ticket.getTicketNumber());
        assertEquals("BK020", ticket.getBookingId());
        assertEquals("F001", ticket.getFlightNumber());
        assertEquals("15B", ticket.getSeatNumber());
        assertEquals(500, ticket.getPrice());
        assertEquals("Laura", ticket.getPassengerName());
        assertEquals("Felice", ticket.getPassengerSurname());
        assertEquals(luggageList, ticket.getLuggageList());
        assertEquals("ID CARD", ticket.getDocumentType());
        assertEquals("ID135678909", ticket.getDocumentId());
    }

    @Test
    @DisplayName("Test that checks the toString() method returns the right string format")
    public void testToString() {
        String ticketNumber = "T021";
        String bookingId = "BK021";
        String flightNumber = "F002";
        List<Luggage> luggageList = new ArrayList<>();
        luggageList.add(new Luggage("L026", 7, "cabin", 0, 20, 20, 20, ticketNumber));
        Ticket ticket = new Ticket(ticketNumber, bookingId, flightNumber, "17A", 250, "Lillo", "Savoia", luggageList);
        ticket.setDocumentType("PASSPORT");
        ticket.setDocumentId("PS146789098");

        String expectedString = "Ticket Number: " + ticketNumber + ", Flight Number: " + flightNumber + ", Seat: 17A, Passenger: Lillo Savoia, Luggage List: " + luggageList;
        assertEquals(expectedString, ticket.toString());

    }
}
