package flightPlanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class PassengerManager {
    private static final Log log = LogFactory.getLog(PassengerManager.class);
    private List<Passenger> passengers;
    private FlightManager flightManager;
    private CSVManager csvManager;
    private String csvFilePath = "csv/passengers.csv";

    public PassengerManager(FlightManager flightManager) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(csvFilePath);
        if (inputStream == null) {
            throw new FileNotFoundException("File not found in resources: " + csvFilePath);
        }
        this.csvManager = new CSVManager(new InputStreamReader(inputStream));
        this.passengers = loadPassengers();
        this.flightManager = flightManager;
    }

    private List<Passenger> loadPassengers() throws IOException {
        List<String[]> records = csvManager.readAll();
        List<Passenger> passengers = new ArrayList<>();
        // Si salta l'header
        for (int i = 1; i < records.size(); i++) {
            String[] record = records.get(i);
            Passenger passenger = new Passenger(
                    !record[0].isEmpty() ? record[0] : "not defined",
                    record[1],
                    record[2],
                    record[3],
                    !record[4].isEmpty() ? record[4] : null,
                    record[5],
                    !record[6].isEmpty() ? parseNotificationTypes(record[6]) : null,
                    !record[7].isEmpty() ? parseNotificationChannels(record[7]) : null,
                    !record[8].isEmpty() ? PaymentMethod.valueOf(record[8]) : null
            );
            passengers.add(passenger);
        }
        return passengers;
    }

    private void saveAllPassengers() throws IOException {
        List<String[]> records = new ArrayList<>();
        records.add(new String[]{"username", "name", "surname", "email", "phoneNumber", "password", "notificationTypes",
                "notificationChannel", "paymentMethod"});
        for (Passenger passenger : passengers) {

            records.add(new String[]{
                    passenger.getUsername(),
                    passenger.getName(),
                    passenger.getSurname(),
                    passenger.getEmail(),
                    passenger.getPhoneNumber(),
                    passenger.getPassword(),
                    formatNotificationTypes(passenger.getPreferredTypes()),
                    formatNotificationChannels(passenger.getChannels()),
                    passenger.getPaymentMethod() != null ? passenger.getPaymentMethod().name() : "No payment method set"

            });
        }
        try {
            csvManager.writeAll(records, csvFilePath);
        } catch (IOException e) {
            log.error("An error occurred while saving passengers on file CSV: " + e.getMessage());
            throw e;
        }
    }

    // Metodo per registrare un passeggero
    public void registerPassenger(Passenger passenger) throws IOException {
        for (Passenger existingPassenger : passengers) {
            if (existingPassenger.getUsername().equalsIgnoreCase(passenger.getUsername()) &&
                    existingPassenger.getSurname().equalsIgnoreCase(passenger.getSurname()) &&
                    existingPassenger.getName().equalsIgnoreCase(passenger.getName())) {
                throw new IllegalArgumentException("Passenger " + passenger.getUsername() + " " +
                        passenger.getName() + " " + passenger.getSurname() + " already exists.");
            }
        }
        passengers.add(passenger);

        String[] record = {
                passenger.getUsername(),
                passenger.getName(),
                passenger.getSurname(),
                passenger.getEmail(),
                passenger.getPassword(),
                formatNotificationTypes(passenger.getPreferredTypes()),
                formatNotificationChannels(passenger.getChannels()),
                passenger.getPaymentMethod() != null ? passenger.getPaymentMethod().name() : "No payment method set"

        };
        try {
            csvManager.appendRecord(record, csvFilePath);
        } catch (IOException e) {
            log.error("An error occurred while writing a passenger on file CSV", e);
            throw e;
        }
        System.out.println("Passenger registered: " + passenger.getUsername() + "\nWelcome " + passenger.getName() + "!");
    }

    public void unregisterPassenger(String username) throws IOException {
        Passenger passengerToRemove = null;
        Iterator<Passenger> iterator = passengers.iterator();
        for (Passenger passenger : passengers) {
            if (passenger.getUsername().equals(username)) {
                passengerToRemove = passenger;
                break;
            }
        }

        // Eliminare anche passeggeri correlati mai registrati all'app
        if (passengerToRemove != null) {
            while (iterator.hasNext()) {
                Passenger p = iterator.next();
                if (p.getUsername().equalsIgnoreCase("not defined") &&
                        p.getEmail().equalsIgnoreCase(passengerToRemove.getEmail())) {
                    iterator.remove(); // Usare l'iteratore per rimuovere l'elemento per evitare ConcurrentModificationException
                    // che può avvenire quando si cerca di rimuovere elementi di una lista mentre si itera su di esso
                    System.out.println("Related passenger removed: " + p.getName() + " " + p.getSurname());
                }
            }

            passengers.remove(passengerToRemove);
            System.out.println("Passenger removed: " + passengerToRemove.getUsername() + " " + passengerToRemove.getName() +
                    " " + passengerToRemove.getSurname());
            saveAllPassengers();
        } else {
            System.out.println("Passenger not found.");
        }
    }

    public void updatePassenger(Passenger passenger) throws IOException {
        boolean passengerExists = false; // Flag per verificare se il passeggero esiste
        for (int i = 0; i < passengers.size(); i++) {
            Passenger existingPassenger = passengers.get(i);
            if (existingPassenger.getName().equalsIgnoreCase(passenger.getName()) &&
                    existingPassenger.getSurname().equalsIgnoreCase(passenger.getSurname()) && existingPassenger.getUsername().equalsIgnoreCase(passenger.getUsername())) {
                passengers.set(i, passenger);
                passengerExists = true;
                break;
            }
        }

        if (!passengerExists) {
            throw new IllegalArgumentException("Passenger " + passenger.getUsername() + " not found.");
        }
        saveAllPassengers();
        System.out.println("Passenger updated: (Username: " + passenger.getUsername() + ", Name: " + passenger.getName() +
                ", Surname: " + passenger.getSurname() + ")");
    }

    // Metodo per disiscrivere un passeggero da un volo
    public void unregisterFromFlight(Passenger passenger, String flightNumber) {
        if (passenger == null || flightNumber == null || flightNumber.isEmpty()) {
            System.out.println("Passenger or flight number cannot be null or empty.");
            return;
        }

        Flight flight = flightManager.getFlightByNumber(flightNumber);
        if (flight == null) {
            System.out.println("Flight with number " + flightNumber + " not found.");
            return;
        }

        passenger.unregisterFromFlight(flight);
    }

    // Metodo per aggiornare le preferenze di notifica di un passeggero
    public void updateNotificationPreferences(Passenger passenger, Set<NotificationType> types, List<NotificationChannel> channels) throws IOException {
        passenger.updatePreferences(types, channels);
        System.out.println("Updated preferences: " + passenger.getPreferredTypes() + ", " + passenger.getChannels());
        updatePassenger(passenger);
    }

    public List<Passenger> getAllPassengers() {
        return passengers;
    }

    public Passenger getPassengerByFullName(String name, String surname) {
        List<Passenger> passengers = getAllPassengers();
        for (Passenger passenger : passengers) {
            if (passenger.getName().equalsIgnoreCase(name) && passenger.getSurname().equalsIgnoreCase(surname))
                return passenger;
        }
        return null;
    }

    public Passenger getPassengerByUsername(String username) {
        for (Passenger passenger : passengers) {
            if (passenger.getUsername().equalsIgnoreCase(username)) {
                return passenger;
            }
        }
        return null;// Se non si trova passenger con questo username
    }

    // Converte le stringhe in NotificationType
    private Set<NotificationType> parseNotificationTypes(String typesStr) {
        Set<NotificationType> notificationTypes = new HashSet<>();
        if (typesStr != null && !typesStr.trim().isEmpty()) {
            String[] typesArray = typesStr.split("-");//split su base di hyphen
            for (String type : typesArray) {
                String trimmedType = type.trim();
                try {
                    notificationTypes.add(NotificationType.valueOf(trimmedType));
                } catch (IllegalArgumentException e) {
                    System.err.println("Unknown NotificationType: " + trimmedType);
                }
            }
        }
        return notificationTypes;
    }

    // Converte NotificationType in stringhe
    private String formatNotificationTypes(Set<NotificationType> types) {
        return types.isEmpty() ? "" : String.join("-", types.stream().map(Enum::name).toArray(String[]::new));
    }

    private List<NotificationChannel> parseNotificationChannels(String channelsStr) {
        List<NotificationChannel> channels = new ArrayList<>();
        if (channelsStr != null && !channelsStr.trim().isEmpty()) {
            String[] channelsArray = channelsStr.split("-");
            for (String channel : channelsArray) {
                String trimmedChannel = channel.trim();
                if (trimmedChannel.equalsIgnoreCase("EMAIL")) {
                    channels.add(new EmailNotification());
                } else if (trimmedChannel.equalsIgnoreCase("SMS")) {
                    channels.add(new SmsNotification());
                }
            }
        }
        return channels;
    }

    private String formatNotificationChannels(List<NotificationChannel> channels) {
        return channels.isEmpty() ? "" : String.join("-", channels.stream()
                .map(channel -> channel instanceof EmailNotification ? "EMAIL" : "SMS")
                .toArray(String[]::new));
    }

}
