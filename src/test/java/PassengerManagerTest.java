import flightPlanner.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class PassengerManagerTest {

    private static PassengerManager passengerManager;

    @BeforeAll
    public static void setUp() throws IOException {
        FlightManager flightManager = new FlightManager();
        passengerManager = new PassengerManager(flightManager);
    }

    @Test
    @DisplayName("Test that checks loading passengers from the CSV file and verifies that it's possible to find a passenger listed in the file")
    public void testLoadPassengersFromCSV() {
        List<Passenger> passengers = passengerManager.getAllPassengers();

        assertNotNull(passengers);
        assertFalse(passengers.isEmpty());

        Passenger passenger = passengerManager.getPassengerByUsername("jdoe");
        assertEquals("jdoe", passenger.getUsername());
    }

    @Test
    @DisplayName("Test that checks if registering a passenger works correctly and that the CSV file is updated after the addition")
    public void testRegisterPassenger() throws IOException {
        Passenger newPassenger = new Passenger("newPassenger", "Gideon", "Sun", "gideon.sun@example.com", null,
                "segretissimo", null, null, PaymentMethod.PAYPAL, "PASSPORT", "PS37GS000");

        passengerManager.registerPassenger(newPassenger);

        Passenger registeredPassenger = passengerManager.getPassengerByUsername("newPassenger");
        assertNotNull(registeredPassenger);
        assertEquals("PS37GS000", registeredPassenger.getDocumentId());
        assertEquals("Gideon", registeredPassenger.getName());
    }

    @Test
    @DisplayName("Test that checks it's impossible to register the same passenger more than once")
    public void testRegisterDuplicatePassenger() throws IOException {
        Passenger passenger = new Passenger("realityNoFiction", "Truman", "Earth", "truth@example.com", null,
                "topSecret", null, null, PaymentMethod.BANK_TRANSFER, "ID CARD", "ID89TE888");

        passengerManager.registerPassenger(passenger);

        assertThrows(IllegalArgumentException.class, () -> passengerManager.registerPassenger(passenger));
    }

    @Test
    @DisplayName("Test that checks unregistering a passenger from the CSV file works correctly")
    public void testUnregisterPassenger() throws IOException {
        String username = "bbrown";
        Passenger passenger = passengerManager.getPassengerByUsername(username);
        assertNotNull(passenger);

        passengerManager.unregisterPassenger(passenger);

        Passenger unregisteredPassenger = passengerManager.getPassengerByUsername(username);
        assertNull(unregisteredPassenger);
    }

    @Test
    @DisplayName("Test that checks updating a passenger status works correctly")
    public void testUpdatePassenger() throws IOException {

        Passenger updatedPassenger = new Passenger("not defined", "Andrea", "Morini", "smorini@example.com", null,
                null, null, null, null, "ID CARD", "ID367AM77");

        passengerManager.updatePassenger(updatedPassenger);

        Passenger updated = passengerManager.getPassengerByFullNameAndDocument("Andrea", "Morini", "ID CARD", "ID367AM77");
        assertEquals("ID CARD", updated.getDocumentType());
        assertEquals("ID367AM77", updated.getDocumentId());
    }

    @Test
    @DisplayName("Test that checks updating a passenger's notification preference work correctly")
    public void testUpdateNotificationPreferences() throws IOException {
        Passenger passenger = passengerManager.getPassengerByUsername("rroe");
        assertNotNull(passenger);

        Set<NotificationType> newNotificationTypes = Set.of(NotificationType.CANCELLATION, NotificationType.DELAY);
        List<NotificationChannel> newChannels = List.of(new SmsNotification(), new EmailNotification());
        passenger.setPhoneNumber("+44 0123456789");
        passengerManager.updateNotificationPreferences(passenger, newNotificationTypes, newChannels);

        Passenger updatedPassenger = passengerManager.getPassengerByUsername("rroe");
        assertNotNull(updatedPassenger);
        assertTrue(updatedPassenger.getPreferredTypes().contains(NotificationType.CANCELLATION));
        assertTrue(updatedPassenger.getPreferences().getPreferredTypes().contains(NotificationType.DELAY));
        assertTrue(updatedPassenger.getChannels().stream().anyMatch(channel -> channel instanceof SmsNotification));
        assertInstanceOf(EmailNotification.class, updatedPassenger.getChannels().getLast());
    }
}