package flightPlanner;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            // Inizializza FlightPlanner con i percorsi ai file CSV
            FlightPlanner planner = new FlightPlanner(
                    "csv/flights.csv",
                    "csv/routes.csv",
                    "csv/seats.csv",
                    "bookings.csv",
                    "csv/payments.csv"
            );


            Flight newFlight = new Flight("F009", "NYK", "LAX", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(3));
            planner.addFlight(newFlight);

            // Creamo passeggeri
            Passenger passenger = new Passenger("John", "Smith", "password123", "johnSmith");
            planner.registerPassenger(passenger);
            Passenger passenger1 = new Passenger("Alice","Bonfanti", "xyz123", "alice123");
            planner.registerPassenger(passenger1);
//            Passenger passenger2 = new Passenger("Bob", "Homes", "z8u6i5","bob456");
//            planner.registerPassenger(passenger2);
//            Passenger passenger3 = new Passenger("Charlie", "Potter", "a0q8x7", "charlie789");
//            planner.registerPassenger(passenger3);// Passeggero senza preferenze


            // Configuriamo le preferenze di notifica dei passeggeri
            // Alice preferisce notifiche di cambi di gate tramite Email
            passenger1.getPreferences().addPreference(NotificationType.GATE_CHANGE);
            passenger1.getPreferences().addChannel(new EmailNotification());

            // Bob preferisce notifiche di cambi di gate e ritardi tramite Email e SMS
//            passenger2.getPreferences().addPreference(NotificationType.GATE_CHANGE);
//            passenger2.getPreferences().addPreference(NotificationType.DELAY);
//            passenger2.getPreferences().addChannel(new EmailNotification());
//            passenger2.getPreferences().addChannel(new SMSNotification());

            // John e Charlie non hanno impostato preferenze di notifica

            // Login a passenger
            Passenger loggedInPassenger = planner.login("johnDoe", "password123");
            if (loggedInPassenger != null) {

                // Book the flight for the logged-in passenger
                List<Ticket> tickets = new ArrayList<>();
                Ticket ticket1 = new Ticket("T001", "B007", "F009", "13A", 100.0);
                tickets.add(ticket1);
                Booking newBooking = new Booking("B007", loggedInPassenger.getName()+" "+passenger.getSurname(), "F009", LocalDateTime.now().plusDays(1).plusHours(2), tickets,100.0 );
                planner.addBooking(newBooking);

                // Add a seat for the flight
                Seat seat = new Seat("13A", "Economy","F009",true);
                planner.addSeat("F009", seat);

                System.out.println("Booking successful: " + newBooking);

                // Visualizzare la prenotazione iniziale
                System.out.println("Prenotazione iniziale:");
                System.out.println(newBooking);

                // Simulazione di cancellazione della prenotazione
                planner.removeBooking("B001");
                System.out.println("\nPrenotazione cancellata per il passeggero John Doe.");

                // Simulazione di cancellazione del volo
                planner.removeFlight("F009");
                System.out.println("\nVolo F009 cancellato.");

                // Aggiornare lo stato di un volo
                LocalDateTime newDeparture = LocalDateTime.now().plusHours(6);
                LocalDateTime newArrival = LocalDateTime.now().plusHours(11);
                String updateMessage = "Il volo è stato ritardato di un'ora.";
                planner.updateFlightStatus("F009", newDeparture, newArrival, updateMessage, NotificationType.DELAY);

                System.out.println("\nStato del volo aggiornato: " + updateMessage);
            }


            System.out.println("Searching for flights...");
            planner.findFlights("AirportA", "AirportB").forEach(System.out::println);

            //FlightSearchService searchService = planner.getFlightSearchService();


//            // Simula aggiornamenti dei voli
//            // Aggiornamento su Gate Change
//            planner.updateFlightStatus("AA123", LocalDateTime.of(2024,12,12,8,30), LocalDateTime.of(2024,12,12,11,30), "Gate changed to B12", NotificationType.GATE_CHANGE);
//
//            // Aggiornamento su Delay
//            planner.updateFlightStatus("BA456", LocalDateTime.of(2024,5,30,9,0), LocalDateTime.of(2024,5,30,11,0), "Delayed by 2 hours", NotificationType.DELAY);
//
//            // Alice si cancella dal volo AA123
//            planner.removePassengerFromFlight("AA123", passenger1);
//
//            // Un altro aggiornamento per il volo AA123
//            planner.updateFlightStatus("AA123", LocalDateTime.of(2024,9,17,9,45), LocalDateTime.of(2024,9,16,11,45), "Flight delayed by 15 minutes", NotificationType.DELAY);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
