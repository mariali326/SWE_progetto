package flightPlanner;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        FlightPlannerApp.main(args);

        Flight flight = new Flight("AA123", "NYC", "LAX", LocalDateTime.now(), LocalDateTime.now().plusHours(5));


        Set<NotificationType> types = new HashSet<>();
        types.add(NotificationType.GATE_CHANGE);
        types.add(NotificationType.SPECIAL_OFFER);
        List<NotificationChannel> channels = new ArrayList<>();
        channels.add(new EmailNotification());
        channels.add(new SmsNotification());
        Passenger passenger = new Passenger("johnDoe", "John", "Doe", "john@example.com", "3333333", "password123", types, channels, PaymentMethod.BANK_TRANSFER);
        passenger.registerForFlight(flight);

        flight.updateFlightStatus(LocalDateTime.now().plusMinutes(10), LocalDateTime.now().plusHours(4).plusMinutes(30), "Flight delayed", NotificationType.GATE_CHANGE);
    }
}
