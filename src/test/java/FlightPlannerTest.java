import flightPlanner.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class FlightPlannerTest {

    private static FlightPlanner flightPlanner;

    @BeforeAll
    public static void setUp() throws IOException {
        flightPlanner = new FlightPlanner();
    }

    @Test
    @DisplayName("Test that checks if removing a flight works correctly, ensuring that the related seats and bookings are also removed.")
    public void testRemoveFlight() throws IOException {

        Flight flight = flightPlanner.findFlight("F001");
        int seatEconomyCounts = flightPlanner.getSeatClassCountsForFlight(flight.getFlightNumber(), "Economy");
        int seatBusinessCounts = flightPlanner.getSeatClassCountsForFlight(flight.getFlightNumber(), "Business");
        int seatFirstCounts = flightPlanner.getSeatClassCountsForFlight(flight.getFlightNumber(), "First");
        int totalSeatsOnCSV = seatEconomyCounts + seatBusinessCounts + seatFirstCounts;
        List<Booking> bookingsForFlight = flightPlanner.getAllBookings().stream()
                .filter(b -> b.getFlightNumber().equalsIgnoreCase(flight.getFlightNumber()))
                .toList();
        List<Passenger> passengersForFlight = flightPlanner.getPassengers().stream()
                .filter(p -> p.isRegisteredForFlight(flight))
                .toList();
        assertNotNull(flight);
        assertEquals(4, totalSeatsOnCSV);
        assertEquals(2, bookingsForFlight.size());
        assertEquals(2, passengersForFlight.size());

        flightPlanner.removeFlight(flight.getFlightNumber());

        int seatEconomyCountsAfterRemoval = flightPlanner.getSeatClassCountsForFlight(flight.getFlightNumber(), "Economy");
        int seatBusinessCountsAfterRemoval = flightPlanner.getSeatClassCountsForFlight(flight.getFlightNumber(), "Business");
        int seatFirstCountsAfterRemoval = flightPlanner.getSeatClassCountsForFlight(flight.getFlightNumber(), "First");
        int totalSeatsOnCsvAfterRemoval = seatEconomyCountsAfterRemoval + seatBusinessCountsAfterRemoval + seatFirstCountsAfterRemoval;
        List<Booking> bookingsForFlightAfterRemoval = flightPlanner.getAllBookings().stream()
                .filter(b -> b.getFlightNumber().equalsIgnoreCase(flight.getFlightNumber()))
                .toList();
        List<Passenger> passengersForFlightAfterRemoval = flightPlanner.getPassengers().stream()
                .filter(p -> p.isRegisteredForFlight(flight))
                .toList();

        assertFalse(flightPlanner.getAllFlights().contains(flight));
        assertEquals(0, totalSeatsOnCsvAfterRemoval);
        assertTrue(bookingsForFlightAfterRemoval.isEmpty());
        assertTrue(passengersForFlightAfterRemoval.isEmpty());
    }

    @Test
    @DisplayName("Test that checks whether passengers who selected CANCELLATION as a notification preference correctly receive the update message")
    public void testPassengerNotificationForCancellationPreference() throws IOException {
        Flight flight = new Flight("F029", "JFK", "LAX", LocalDateTime.now(), LocalDateTime.now().plusHours(5).plusMinutes(15), 250, 50, 10);

        // Creazione del primo passeggero con la preferenza di notifica per le cancellazioni
        Passenger passengerWithPref = new Passenger("bobby_doe", "Bobby", "Doe", "john@example.com", "123456789", "password",
                Set.of(NotificationType.CANCELLATION), List.of(new EmailNotification()), null, "PASSPORT", "PS1212121");

        // Creazione del secondo passeggero senza la preferenza di notifica per le cancellazioni
        Passenger passengerWithoutPref = new Passenger("diva_doe", "Diva", "Doe", "jane@example.com", "987654321", "password",
                Set.of(NotificationType.SPECIAL_OFFER), List.of(new EmailNotification()), null, "ID CARD", "ID1212121");

        flight.subscribe(passengerWithPref);
        flight.subscribe(passengerWithoutPref);

        List<Ticket> tickets = new ArrayList<>();
        tickets.add(new Ticket("T029", "BK029", "F029", "13D", 550, "John", "Doe", new ArrayList<>()));
        Booking bookingForPassengerWithPref = new Booking("BK029", "bobby_doe", "F029", LocalDateTime.now().minusDays(7), tickets, 550);
        List<Ticket> ticketList = new ArrayList<>();
        ticketList.add(new Ticket("T030", "BK030", "F029", "13C", 550, "Diva", "Doe", new ArrayList<>()));
        Booking bookingForPassengerWithoutPref = new Booking("BK030", "diva_doe", "F029", LocalDateTime.now().minusDays(7), ticketList, 550);

        List<Booking> bookings = flightPlanner.getAllBookings();
        bookings.add(bookingForPassengerWithPref);
        bookings.add(bookingForPassengerWithoutPref);
        flightPlanner.addFlight(flight);

        flightPlanner.notifyCancellationToPassengers("F029");

        assertTrue(passengerWithPref.getNotifications().contains("Sorry for the inconvenience, the flight F029 is cancelled. Your refund of 550.0 EUR including luggage cost for the booking BK029 is processed automatically."));
        assertFalse(passengerWithoutPref.getNotifications().contains("Sorry for the inconvenience, the flight F029 is cancelled. the flight F029 is cancelled. Your refund of 550.0 EUR including luggage cost for the booking BK030 is processed automatically."));
    }

    @Test
    @DisplayName("Test that verifies the flight status update works correctly, ensuring that the related route is also updated.")
    public void testUpdateFlightStatus() throws IOException {
        Flight flight = flightPlanner.findFlight("F002");
        LocalDateTime originalDepartureTime = flight.getDepartureTime();
        LocalDateTime originalArrivalTime = flight.getArrivalTime();

        Route route = flightPlanner.getRouteByAirportsCode(flight.getDepartureAirportCode(), flight.getArrivalAirportCode());
        Duration originalDuration = route.getFlightDuration();

        Passenger passenger = flightPlanner.getPassengers().stream()
                .filter(p -> p.isRegisteredForFlight(flight))
                .findFirst()
                .orElse(null);

        LocalDateTime newDepartureTime = LocalDateTime.of(2024, 12, 26, 11, 0);
        LocalDateTime newArrivalTime = LocalDateTime.of(2024, 12, 26, 15, 30);
        String updateMsg = "Your flight has delayed!";
        flightPlanner.updateFlightStatus("F002", newDepartureTime, newArrivalTime, updateMsg, NotificationType.DELAY);

        assertNotEquals(originalDepartureTime, flight.getDepartureTime());
        assertNotEquals(originalArrivalTime, flight.getArrivalTime());
        assertNotEquals(originalDuration, route.getFlightDuration());
        assertNotNull(passenger);// Non ha scelto come notifica di preferenza il ritardo
        assertFalse(passenger.getNotifications().contains(updateMsg));
    }

    @Test
    @DisplayName("Test that checks if removing an airport works correctly, ensuring that the related routes are also removed.")
    public void testRemoveAirport() throws IOException {
        Airport airport = flightPlanner.getAllAirports().stream()
                .filter(a -> a.getCode().equalsIgnoreCase("CDG"))
                .findFirst()
                .orElse(null);
        assertNotNull(airport);

        List<Route> routes = flightPlanner.getAllRoutes().stream()
                .filter(r -> r.getDepartureAirportCode().equalsIgnoreCase(airport.getCode()) || r.getArrivalAirportCode().equalsIgnoreCase(airport.getCode()))
                .toList();
        assertFalse(routes.isEmpty());

        flightPlanner.removeAirport(airport.getCode());

        Airport removedAirport = flightPlanner.getAllAirports().stream()
                .filter(a -> a.getCode().equalsIgnoreCase("CDG"))
                .findFirst()
                .orElse(null);
        assertNull(removedAirport);

        List<Route> removedRoutes = flightPlanner.getAllRoutes().stream()
                .filter(r -> r.getDepartureAirportCode().equalsIgnoreCase(airport.getCode()) || r.getArrivalAirportCode().equalsIgnoreCase(airport.getCode()))
                .toList();
        assertTrue(removedRoutes.isEmpty());
    }

    @Test
    @DisplayName("Test that check the booking flight process of a passenger works correctly")
    public void testBookingFlightForPassenger() throws IOException {
        Passenger mainPassenger = flightPlanner.getPassenger("jdoe");
        Passenger additionalPassenger = new Passenger("not defined", "Pippo", "Baudo", mainPassenger.getEmail(),
                null, null, null, null, null, "ID CARD", "ID167PB99");
        List<Passenger> additionalPassengers = List.of(additionalPassenger);

        String bookingId = "BK057";
        double price = flightPlanner.getPrice("F002", "Economy");
        Ticket ticket1 = new Ticket("T057", bookingId, "F002", "10D", price, "John", "Doe", new ArrayList<>());
        Ticket ticket2 = new Ticket("T058", bookingId, "F002", "18A", price, "Pippo", "Baudo", new ArrayList<>());
        List<Ticket> tickets = List.of(ticket1, ticket2);

        Flight flight = flightPlanner.findFlight("F002");

        assertNull(flightPlanner.findBooking(bookingId));
        assertFalse(mainPassenger.isRegisteredForFlight(flight));
        assertFalse(additionalPassenger.isRegisteredForFlight(flight));

        flightPlanner.bookFlightForPassenger("F002", mainPassenger, additionalPassengers, tickets, bookingId);

        assertNotNull(flightPlanner.findBooking(bookingId));
        assertTrue(mainPassenger.isRegisteredForFlight(flight));
        assertTrue(additionalPassenger.isRegisteredForFlight(flight));
    }

    @Test
    @DisplayName("Test that checks the cancellation process of a booking works correctly")
    public void testCancelBooking() throws IOException {
        String bookingId = "BK005";
        Booking booking = flightPlanner.findBooking(bookingId);
        assertNotNull(booking);

        Passenger passenger = flightPlanner.getPassenger(booking.getPassengerUsername());
        Flight flight = flightPlanner.findFlight(booking.getFlightNumber());
        assertTrue(passenger.isRegisteredForFlight(flight));

        List<Ticket> tickets = booking.getTickets();
        assertEquals(2, tickets.size());
        Ticket ticket = tickets.getLast();

        Passenger additionalPassenger = flightPlanner.getPassengerByFullNameAndDocument(ticket.getPassengerName(), ticket.getPassengerSurname(), ticket.getDocumentType(), ticket.getDocumentId());
        assertTrue(additionalPassenger.isRegisteredForFlight(flight));

        List<Luggage> luggageList = new ArrayList<>();
        for (Ticket t : tickets) {
            luggageList.addAll(t.getLuggageList());
        }
        assertFalse(luggageList.isEmpty());

        flightPlanner.cancelBooking(bookingId, passenger);

        Booking canceledBooking = flightPlanner.findBooking(bookingId);
        assertNull(canceledBooking);
        assertNull(flightPlanner.findTicket(bookingId, tickets.getFirst().getTicketNumber()));
        assertNull(flightPlanner.findTicket(bookingId, tickets.getLast().getTicketNumber()));

        assertFalse(passenger.isRegisteredForFlight(flight));
        assertFalse(additionalPassenger.isRegisteredForFlight(flight));

        List<Luggage> luggageListAfterRemoval = flightPlanner.getAllLuggage().stream()
                .filter(l -> l.getTicketNumber().equalsIgnoreCase(tickets.getFirst().getTicketNumber()) ||
                        l.getTicketNumber().equalsIgnoreCase(tickets.getLast().getTicketNumber()))
                .toList();
        assertTrue(luggageListAfterRemoval.isEmpty());
    }

    @Test
    @DisplayName("Test that checks if removing a route works correctly, ensuring that the related flights are also removed.")
    public void testRemoveRoute() throws IOException {
        Route route = flightPlanner.getAllRoutes().stream()
                .filter(r -> r.getRouteId().equalsIgnoreCase("R006"))
                .findFirst()
                .orElse(null);
        assertNotNull(route);
        Flight flight = flightPlanner.getAllFlights().stream()
                .filter(f -> f.getDepartureAirportCode().equalsIgnoreCase(route.getDepartureAirportCode()) &&
                        f.getArrivalAirportCode().equalsIgnoreCase(route.getArrivalAirportCode()))
                .findFirst()
                .orElse(null);
        assertNotNull(flight);
        String flightNumber = flight.getFlightNumber();

        Seat seat = new Seat("20A", "Economy", flightNumber, true);
        flightPlanner.addSeatToFlight(flightNumber, seat);

        int economySeatsCounts = flightPlanner.getSeatClassCountsForFlight(flightNumber, "Economy");
        assertEquals(1, economySeatsCounts);

        flightPlanner.removeRoute(route.getRouteId());

        Route removedRoute = flightPlanner.getAllRoutes().stream()
                .filter(r -> r.getRouteId().equalsIgnoreCase("R006"))
                .findFirst()
                .orElse(null);

        assertNull(removedRoute);
        assertNull(flightPlanner.findFlight(flightNumber));

        int economySeatsCountsAfterRemoval = flightPlanner.getSeatClassCountsForFlight(flightNumber, "Economy");
        assertEquals(0, economySeatsCountsAfterRemoval);
    }

    @Test
    @DisplayName("Test that checks the cancellation process of a ticket works correctly")
    public void testCancelTicket() throws IOException {
        Passenger passenger = flightPlanner.getPassenger("jdoe");
        String ticketNumber = "T078";
        String bookingId = "BK078";
        Luggage luggage = new Luggage("L078", 6, "cabin", 0, 20, 20, 20, ticketNumber);
        Ticket ticket = new Ticket(ticketNumber, bookingId, "F002", "10D", 250, passenger.getName(), passenger.getSurname(),
                List.of(luggage));

        flightPlanner.bookFlightForPassenger("F002", passenger, new ArrayList<>(), List.of(ticket), bookingId);
        Booking booking = flightPlanner.findBooking(bookingId);
        assertEquals(1, booking.getTickets().size());
        assertEquals(1, booking.getTickets().getFirst().getLuggageList().size());

        flightPlanner.cancelTicket(bookingId, ticket);

        Ticket removedTicket = flightPlanner.findTicket(bookingId, ticketNumber);
        assertNull(removedTicket);
        Booking removedBooking = flightPlanner.findBooking(bookingId);
        assertNull(removedBooking);

        List<Luggage> luggageListAfterRemoval = flightPlanner.getAllLuggage().stream()
                .filter(l -> l.getTicketNumber().equalsIgnoreCase(booking.getTickets().getFirst().getTicketNumber()))
                .toList();
        assertTrue(luggageListAfterRemoval.isEmpty());
    }

    @Test
    @DisplayName("Test that verifies the ticket price is updated correctly when changing the seat or adding new luggage")
    public void testUpdateTicket() throws IOException {
        // Aggiornamento nel caso in cui si sceglie di cambiare con un posto della stessa classe
        Ticket ticket = flightPlanner.findTicket("BK002", "T002");
        String currentSeatNumber = ticket.getSeatNumber();
        String currentSeatClass = flightPlanner.getSeatClass(currentSeatNumber, ticket.getFlightNumber());
        double currentPrice = ticket.getPrice();
        Booking booking = flightPlanner.findBooking(ticket.getBookingId());
        double bookingCurrentAmount = booking.getTotalAmount();

        flightPlanner.updateTicketSeatPrice(ticket.getTicketNumber(), "18A", flightPlanner.getPrice(ticket.getFlightNumber(), "Economy"));

        String newSeatClass = flightPlanner.getSeatClass("18A", ticket.getFlightNumber());
        String newSeatNumber = ticket.getSeatNumber();
        double newPrice = ticket.getPrice();

        assertNotEquals(currentSeatNumber, newSeatNumber);
        assertEquals(currentSeatClass, newSeatClass);
        assertEquals(currentPrice, newPrice);
        assertEquals(bookingCurrentAmount, booking.getTotalAmount());

        // Aggiornamento nel caso in cui si aggiunge un nuovo bagaglio
        List<Luggage> luggageList = ticket.getLuggageList();
        assertEquals(1, luggageList.size());

        double luggagePrice = flightPlanner.calculateLuggageCost(newSeatClass, "F002", "cabin", 20, 20, 20, 10, ticket.getCabinLuggageCount() + 1, 0);
        luggageList.add(new Luggage("L036", 10, "cabin", luggagePrice, 20, 20, 20, ticket.getTicketNumber()));
        assertEquals(2, luggageList.size());

        double beforeUpdatePrice = ticket.getPrice();
        double newTotalPrice = ticket.getPrice() + luggagePrice;

        flightPlanner.updateTicketPrice(ticket.getTicketNumber(), newTotalPrice);

        double bookingNewTotalAmount = booking.getTotalAmount();

        assertNotEquals(beforeUpdatePrice, newTotalPrice);
        assertNotEquals(bookingCurrentAmount, bookingNewTotalAmount);
    }

    @Test
    @DisplayName("Test checks if there is a duplicate passenger who is not registered in the app; they will be merged into one after their registration")
    public void testRemoveDuplicatePassenger() throws IOException {
        String name = "Andrea";
        String surname = "Morini";
        String documentType = "PASSPORT";
        String documentId = "PS1357900";
        Passenger newPassenger = new Passenger("amorini", name, surname, "amorini@example.com",
                null, "segreto", null, null, null, documentType, documentId);
        flightPlanner.registerPassenger(newPassenger);

        assertTrue(flightPlanner.checkDuplicatePassenger(name, surname, documentType, documentId));

        Passenger duplicatePassenger = flightPlanner.findDuplicatePassenger(name, surname, documentType, documentId);
        assertNotNull(duplicatePassenger);

        List<Flight> registeredFlight = duplicatePassenger.getRegisteredFlights();
        assertTrue(registeredFlight.contains(flightPlanner.findFlight("F005")));
        assertFalse(newPassenger.getRegisteredFlights().contains(flightPlanner.findFlight("F005")));

        flightPlanner.removeDuplicatePassenger(newPassenger.getUsername(), name, surname, documentType, documentId);

        Passenger removedDuplicatePassenger = flightPlanner.findDuplicatePassenger(name, surname, documentType, documentId);
        assertNull(removedDuplicatePassenger);
        assertTrue(newPassenger.getRegisteredFlights().contains(flightPlanner.findFlight("F005")));

    }

    @Test
    @DisplayName("Test")
    public void testCalculateLuggageCost() {
        String classType = "Economy";
        String flightNumber = "F005";
        double cabinLength = 44;
        double cabinWidth = 30;
        double cabinHeight = 20;
        double cabinWeight = 9;

        double cabinCost = flightPlanner.calculateLuggageCost(classType, flightNumber, "cabin", cabinLength, cabinWidth,
                cabinHeight, cabinWeight, 1, 0);
        assertEquals(100, cabinCost);

        double holdLength1 = 50;
        double holdWidth1 = 40;
        double holdHeight1 = 20;
        double holdWeight1 = 23;

        double holdCost1 = flightPlanner.calculateLuggageCost(classType, flightNumber, "hold", holdLength1, holdWidth1,
                holdHeight1, holdWeight1, 1, 1);
        assertEquals(0, holdCost1);

        double holdLength2 = 50;
        double holdWidth2 = 40;
        double holdHeight2 = 21; // Dimensione superato
        double holdWeight2 = 24; // Peso superato di 1 kg

        double holdCost2 = flightPlanner.calculateLuggageCost(classType, flightNumber, "hold", holdLength2, holdWidth2,
                holdHeight2, holdWeight2, 1, 2); // Numero di bagagli da stiva gratis superato per voli > 6 ore
        assertEquals(200, holdCost2);
    }

    @Test
    @DisplayName("Test that checks adding new flight-class prices works correctly")
    public void testAddFlightClassPrices() {
        String flightNumber = "F100";
        double economyPrice = 100.0;
        double businessPrice = 200.0;
        double firstClassPrice = 300.0;

        flightPlanner.addFlightClassPrices(flightNumber, economyPrice, businessPrice, firstClassPrice);

        Map<String, Map<String, Double>> flightClassPrices = flightPlanner.getFlightClassPrices();
        assertTrue(flightClassPrices.containsKey(flightNumber));

        Map<String, Double> prices = flightClassPrices.get(flightNumber);
        assertEquals(economyPrice, prices.get("Economy"));
        assertEquals(businessPrice, prices.get("Business"));
        assertEquals(firstClassPrice, prices.get("First"));
        assertEquals(100.0, flightPlanner.getPrice(flightNumber, "Economy"));
        assertEquals(200.0, flightPlanner.getPrice(flightNumber, "Business"));
        assertEquals(300.0, flightPlanner.getPrice(flightNumber, "First"));

        assertThrows(IllegalArgumentException.class, () -> flightPlanner.getPrice(flightNumber, "Premium"));
    }

    @Test
    @DisplayName("Test that checks adding flight-class price works correctly")
    public void testAddFlightClassPrice() {
        String flightNumber = "F200";
        flightPlanner.addFlightClassPrice(flightNumber, "Economy", 150.0);
        flightPlanner.addFlightClassPrice(flightNumber, "Business", 250.0);
        flightPlanner.addFlightClassPrice(flightNumber, "First", 350.0);

        Map<String, Map<String, Double>> flightClassPrices = flightPlanner.getFlightClassPrices();
        assertTrue(flightClassPrices.containsKey(flightNumber));

        Map<String, Double> prices = flightClassPrices.get(flightNumber);
        assertEquals(150.0, prices.get("Economy"));
        assertEquals(250.0, prices.get("Business"));
        assertEquals(350.0, prices.get("First"));
    }

    @Test
    @DisplayName("Test that checks updating an existing flight-class prices works correctly")
    public void testUpdateFlightClassPrices() {
        String flightNumber = "F300";
        flightPlanner.addFlightClassPrices(flightNumber, 120.0, 220.0, 320.0);

        flightPlanner.updateFlightClassPrices(flightNumber, "Business", 230.0);

        Map<String, Double> prices = flightPlanner.getFlightClassPrices().get(flightNumber);
        assertEquals(230.0, prices.get("Business"));

        assertThrows(IllegalArgumentException.class, () -> flightPlanner.updateFlightClassPrices("F400", "Economy", 150.0));

        assertThrows(IllegalArgumentException.class, () -> flightPlanner.updateFlightClassPrices(flightNumber, "Premium", 180.0));
    }

}
