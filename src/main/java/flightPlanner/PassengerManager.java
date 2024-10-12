package flightPlanner;

import java.io.*;
import java.util.*;

public class PassengerManager {
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
        this.passengers = new ArrayList<>();
        this.flightManager = flightManager;
        loadPassengers();
    }

    public Passenger getPassengerByUsername(String username) {
        for (Passenger passenger : passengers) {
            if (passenger.getUsername().equalsIgnoreCase(username)) {
                return passenger;
            }
        }
        return null;// se non si trova passenger con questo username
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

    private void loadPassengers() throws IOException {
        List<String[]> records = csvManager.readAll();
        if (!records.isEmpty()) {
            records.removeFirst(); // Rimuovere la riga di header
        }
        for (String[] record : records) {

            String username = record[0].trim();//trim rimuove ogni spazio iniziale o finale
            String name = record[1].trim();
            String surname = record[2].trim();
            String email = record[3].trim();
            String phoneNumber = !record[4].trim().isEmpty() ? record[4].trim() : null;
            String password = record[5].trim();

            Set<NotificationType> notificationTypes = parseNotificationTypes(record[6].trim());
            List<NotificationChannel> channels = parseNotificationChannels(record[7].trim());


            Passenger passenger = new Passenger(username, name, surname, email, phoneNumber, password, notificationTypes, channels);
            passengers.add(passenger);

        }

    }

    private void saveAllPassengers() {
        List<String[]> records = new ArrayList<>();
        records.add(new String[]{"username", "name", "surname", "email", "phoneNumber", "password", "notificationTypes", "notificationChannel"});
        for (Passenger passenger : passengers) {

            records.add(new String[]{
                    passenger.getUsername(),
                    passenger.getName(),
                    passenger.getSurname(),
                    passenger.getEmail(),
                    passenger.getPhoneNumber(),
                    passenger.getPassword(),
                    formatNotificationTypes(passenger.getPreferredTypes()),
                    formatNotificationChannels(passenger.getChannels())

            });
        }
        try {
            csvManager.writeAll(records, csvFilePath);
        }catch (IOException e) {
            System.err.println("An error occurred while saving passengers on file CSV: " + e.getMessage());
            System.out.println("Error details:");
            e.printStackTrace();
        }
    }

    public List<Passenger> getAllPassengers() {
        return passengers;
    }

    // Metodo per registrare un passeggero
    public void registerPassenger(Passenger passenger) throws IOException {
        for (Passenger existingPassenger : passengers) {
            if (existingPassenger.getUsername().equalsIgnoreCase(passenger.getUsername())) {
                throw new IllegalArgumentException("Passenger " + passenger.getUsername() + " " +
                        passenger.getName() + " " + passenger.getSurname() + " already exists.");
            }
        }
        passengers.add(passenger);

        String[] record ={
                passenger.getUsername(),
                passenger.getName(),
                passenger.getSurname(),
                passenger.getEmail(),
                passenger.getPassword(),
                formatNotificationTypes(passenger.getPreferredTypes()),
                formatNotificationChannels(passenger.getChannels())

        };
        try {
            csvManager.appendRecord(record, csvFilePath);
        } catch (IOException e) {
            System.out.println("Error details:");
            e.printStackTrace();
            throw new IOException("An error occurred while writing a passenger on file CSV", e);
        }
        System.out.println("Passenger registered: " + passenger.getUsername()+ "\nWelcome " + passenger.getName() + "!");
    }

    public void unregisterPassenger(String username) {
        Passenger passengerToRemove = null;
        for (Passenger passenger : passengers) {
            if (passenger.getUsername().equals(username)) {
                passengerToRemove = passenger;
                break;
            }
        }

        if (passengerToRemove != null) {
            passengers.remove(passengerToRemove);
            System.out.println("Passenger removed: " + passengerToRemove.getName());
            saveAllPassengers();
        } else {
            System.out.println("Passenger not found.");
        }
    }

    // Metodo per registrare un passeggero a un volo
    public void registerForFlight(Passenger passenger, Flight flight) {
        if (passenger == null || flight == null) {
            System.out.println("Passenger or flight cannot be null.");
            return;
        }

        // Assicurare che il volo esista
        if (flightManager.getFlightByNumber(flight.getFlightNumber()) == null) {
            System.out.println("Flight not found.");
            return;
        }

        passenger.registerForFlight(flight);
        System.out.println("Passenger " + passenger.getName() + " registered for flight: " + flight.getFlightNumber());
    }

    // Metodo per disiscrivere un passeggero da un volo
    public void unregisterFromFlight(Passenger passenger, String flightNumber) {
        if (passenger == null || flightNumber == null || flightNumber.isEmpty()) {
            System.out.println("Passenger or flight number cannot be null or empty.");
            return;
        }

        // Trova il volo usando il flightManager
        Flight flight = flightManager.getFlightByNumber(flightNumber);
        if (flight == null) {
            System.out.println("Flight with number " + flightNumber + " not found.");
            return;
        }

        passenger.unregisterFromFlight(flight);
        System.out.println("Passenger " + passenger.getName() + " unregistered from flight: " + flightNumber);
    }

    public List<Passenger> getPassengersByFlight(Flight flight) {
        List<Passenger> passengersOnFlight = new ArrayList<>();
        for (Passenger passenger : passengers) {
            if (passenger.isRegisteredForFlight(flight)) {
                passengersOnFlight.add(passenger);
            }
        }
        return passengersOnFlight;
    }

    // Metodo per aggiornare le preferenze di notifica di un passeggero
    public void updateNotificationPreferences(Passenger passenger, Set<NotificationType> types, List<NotificationChannel> channels) {
        passenger.updatePreferences(types, channels);
        // Notifica il cambio di preferenze
        System.out.println("Notification preferences updated for: " + passenger.getName());
    }

}
