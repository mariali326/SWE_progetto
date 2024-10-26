import flightPlanner.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class PassengerTest {

    @Test
    @DisplayName("Test that checks the creation of a passenger")
    public void testPassengerCreation() {
        String username = "mystery";
        String name = "Mimo";
        String surname = "Silence";
        String email = "mystery@example.com";
        String phoneNumber = "+39 3278888888";
        String password = "nobodyKnow";
        Set<NotificationType> notificationTypes = new HashSet<>();
        List<NotificationChannel> notificationChannels = new ArrayList<>();
        PaymentMethod paymentMethod = PaymentMethod.BANK_TRANSFER;
        String documentType = "PASSPORT";
        String documentId = "PS37MS899";

        Passenger passenger = new Passenger(username, name, surname, email, phoneNumber, password, notificationTypes, notificationChannels,
                paymentMethod, documentType, documentId);

        assertEquals(username, passenger.getUsername());
        assertEquals(name, passenger.getName());
        assertEquals(surname, passenger.getSurname());
        assertEquals(email, passenger.getEmail());
        assertEquals(phoneNumber, passenger.getPhoneNumber());
        assertNotNull(passenger.getPreferences());
        assertEquals(2, passenger.getPreferredTypes().size());
        assertTrue(passenger.getPreferredTypes().contains(NotificationType.CANCELLATION));
        assertTrue(passenger.getPreferredTypes().contains(NotificationType.GATE_CHANGE));
        assertEquals(1, passenger.getChannels().size());
        assertInstanceOf(EmailNotification.class, passenger.getChannels().getFirst());
        assertEquals(password, passenger.getPassword());
        assertEquals(paymentMethod, passenger.getPaymentMethod());
        assertEquals(documentType, passenger.getDocumentType());
        assertEquals(documentId, passenger.getDocumentId());
        assertTrue(passenger.getRegisteredFlights().isEmpty());
    }

    @Test
    @DisplayName("Test that checks the passenger's subscription and unsubscription for a flight")
    public void testRegisteAndUnregisterForFlight() {
        Passenger passenger = new Passenger("username123", "Joshua", "Darker", "joshua.darker@example.com", "+1 1234567890", "password123",
                null, null, PaymentMethod.CREDIT_CARD, "PASSPORT", "PS12NH888");
        Flight flight = new Flight("F024", "FLR", "PVG", LocalDateTime.now(), LocalDateTime.now().plusHours(13), 260, 50, 10);
        passenger.registerForFlight(flight);
        assertTrue(passenger.isRegisteredForFlight(flight));

        passenger.unregisterFromFlight(flight);
        assertFalse(passenger.isRegisteredForFlight(flight));
    }

    @Test
    @DisplayName("Test that checks updating passenger's preferences through updatePreferences() method and setter works both correctly")
    public void testNotificationPreferencesUpdate() {
        Passenger passenger = new Passenger("gameLover", "Jenny", "Brighter", "jenny.brighter@example.com", "+1 0987654321", "password09",
                null, null, PaymentMethod.CREDIT_CARD, "ID CARD", "ID88JB123");
        Set<NotificationType> newTypes = Set.of(NotificationType.DELAY);
        List<NotificationChannel> newChannels = List.of(new EmailNotification());

        passenger.updatePreferences(newTypes, newChannels);
        assertTrue(passenger.getPreferredTypes().contains(NotificationType.DELAY));
        assertInstanceOf(EmailNotification.class, passenger.getChannels().getFirst());
    }

    @Test
    @DisplayName("Test that checks the passenger can receive correctly the update message through mocking")
    public void testUpdateWithMockNotificationChannel() {
        NotificationChannel mockChannel = Mockito.mock(NotificationChannel.class);

        List<NotificationChannel> channels = List.of(mockChannel);

        Passenger passenger = new Passenger("salute123", "Silvestro", "Alvino", "salvino@example.com", null, "segreto",
                null, channels, PaymentMethod.DEBIT_CARD, "ID CARD", "ID3456SA9");

        String message = "Your gate has changed!";
        passenger.update(message, NotificationType.GATE_CHANGE);

        Mockito.verify(mockChannel).sendNotification(message, passenger);

        passenger.getPreferences().addPreference(NotificationType.DELAY);
        String anotherMessage = "Your flight has delayed!";
        passenger.update(anotherMessage, NotificationType.DELAY);

        Mockito.verify(mockChannel).sendNotification(anotherMessage, passenger);

    }

    @Test
    @DisplayName("Test that checks the passenger without preferences can correctly receive the update message through the getNotification() method")
    public void testUpdateWithoutPreferences() {
        Passenger passenger = new Passenger("dune55", "Dune", "Smith", "dune.smith@example.com", null, "secret",
                null, null, PaymentMethod.BANK_TRANSFER, "ID CARD", "ID654DS12");

        String message = "Your flight has been canceled!";
        passenger.update(message, NotificationType.CANCELLATION);

        assertTrue(passenger.getNotifications().contains(message));
    }


    @Test
    @DisplayName("Test that checks the toString() method returns the correct string format")
    public void testToString() {
        Set<NotificationType> notificationTypes = Set.of(NotificationType.GATE_CHANGE);
        List<NotificationChannel> channels = List.of(new EmailNotification());
        String phoneNumber = "+39 3289999999";
        String documentId = "PS23BM679";
        Passenger passenger = new Passenger("loveForever", "Beethoven", "Musician", "beethoven.musician@example.com", phoneNumber, "passionMusic",
                notificationTypes, channels, PaymentMethod.DEBIT_CARD, "PASSPORT", documentId);

        String expectedString = "loveForever, Beethoven Musician, email: beethoven.musician@example.com, phoneNumber: " + phoneNumber
                + ", password: passionMusic, preferredNotificationTypes: " + notificationTypes + ", preferredChannels: Email Notification, paymentMethod: " + PaymentMethod.DEBIT_CARD;
        assertEquals(expectedString, passenger.toString());
    }

}
