package flightPlanner;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Collections;

public class Passenger extends User implements Observer {
    private String name;
    private String surname;
    private String phoneNumber; // Facoltativo
    private List<Flight> registeredFlights;
    private NotificationPreferences preferences;

    public Passenger(String username, String name, String surname, String email, String phoneNumber, String password,
                     Set<NotificationType> notificationTypes, List<NotificationChannel> channels) {
        super(username, password, email);
        this.name = name;
        this.surname = surname;
        this.phoneNumber = phoneNumber;
        this.registeredFlights = new ArrayList<>();
        this.preferences = new NotificationPreferences();

        setDefaultPreferences(notificationTypes, channels);//se non fornite
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public NotificationPreferences getPreferences() {
        return preferences;
    }

    public void setPreferences(NotificationPreferences preferences) {
        this.preferences = preferences;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void displayRole() {
        System.out.println("Role: Passenger");
    }

    public void registerForFlight(Flight flight) {
        if (!isRegisteredForFlight(flight)) {
            registeredFlights.add(flight);
            System.out.println(name + " " + surname + " registered for flight: " + flight.getFlightNumber());
        } else {
            System.out.println(name + " " + surname + " is already registered for flight: " + flight.getFlightNumber());
        }
    }

    // Metodo per disiscrivere un passeggero da un volo
    public void unregisterFromFlight(Flight flight) {
        if (isRegisteredForFlight(flight)) {
            registeredFlights.remove(flight);
            System.out.println(name + " " + surname + " unregistered from flight: " + flight.getFlightNumber());
        } else {
            System.out.println(name + " " + surname + " is not registered for flight: " + flight.getFlightNumber());
        }
    }

    // Metodo per verificare se il passeggero è registrato a un volo
    public boolean isRegisteredForFlight(Flight flight) {
        return registeredFlights.contains(flight);
    }

    private void setDefaultPreferences(Set<NotificationType> notificationTypes, List<NotificationChannel> channels) {
        if (notificationTypes.isEmpty()) {
            this.preferences.addPreference(NotificationType.GATE_CHANGE);
            this.preferences.addPreference(NotificationType.CANCELLATION);
        } else {
            notificationTypes.forEach(this.preferences::addPreference);
        }

        if (channels.isEmpty()) {
            this.preferences.addChannel(new EmailNotification()); // Default to Email
        } else {
            channels.forEach(this.preferences::addChannel);
        }
    }

    @Override
    // Update per ricevere notifiche
    public void update(String message, NotificationType type) {
        if (preferences == null || preferences.getPreferredTypes().isEmpty()) {
            // Nessuna preferenza impostata, inviare notifiche di default
            sendDefaultNotifications(message, type);
        } else {
            // Preferenze impostate, inviare notifiche basate sulle preferenze
            if (preferences.isPreferred(type)) {
                for (NotificationChannel channel : preferences.getChannels()) {
                    channel.sendNotification(message, this);
                }
            }
        }
    }

    private void sendDefaultNotifications(String message, NotificationType type) {
        // Definiamo i tipi di notifica di default che vogliamo inviare
        // Altri tipi di notifica non vengono inviati per i passeggeri senza preferenze
        Set<NotificationType> defaultTypes = Set.of(NotificationType.GATE_CHANGE, NotificationType.CANCELLATION);

        if (defaultTypes.contains(type)) {
            // Inviare tramite Email di default
            NotificationChannel defaultChannel = new EmailNotification();
            defaultChannel.sendNotification(message, this);
        }

    }

    // Metodo per configurare le preferenze di notifica
    public void updatePreferences(Set<NotificationType> types, List<NotificationChannel> channels) {
        // Resetta le preferenze esistenti
        this.preferences.getPreferredTypes().clear();
        this.preferences.getChannels().clear();

        // Imposta le nuove preferenze
        for (NotificationType type : types) {
            this.preferences.addPreference(type);
        }

        for (NotificationChannel channel : channels) {
            if (channel instanceof SmsNotification && (phoneNumber == null || phoneNumber.isEmpty())) {
                throw new IllegalArgumentException("Phone number is required for SMS notifications.");
            }
            this.preferences.addChannel(channel);
        }

        // Notifica il sistema di cambiamento delle preferenze
        notifyPreferenceChange();
    }

    private void notifyPreferenceChange() {
        System.out.println("Notification preferences changed for: " + name + " " + surname);
    }

    public Set<NotificationType> getPreferredTypes(){
        return Collections.unmodifiableSet(preferences.getPreferredTypes());
    }

    public List<NotificationChannel> getChannels(){
        return Collections.unmodifiableList(preferences.getChannels());
    }
}
