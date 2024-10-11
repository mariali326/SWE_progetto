package flightPlanner;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Passenger extends User implements Observer {
    private String name;
    private String surname;
    private List<Booking> bookings;
    private NotificationPreferences preferences;

    public Passenger(String name, String surname, String password, String username) {
        super(username, password);
        this.name = name;
        this.surname = surname;
        this.bookings = new ArrayList<>();
        this.preferences = new NotificationPreferences();
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public void addBookings(Booking booking) {
        bookings.add(booking);
    }

    public NotificationPreferences getPreferences() {
        return preferences;
    }

    public void setPreferences(NotificationPreferences preferences) {
        this.preferences = preferences;
    }

    public void displayRole() {
        System.out.println("Role: Passenger");
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
            this.preferences.addChannel(channel);
        }

        // Notifica il sistema di cambiamento delle preferenze
        notifyPreferenceChange();
    }

    private void notifyPreferenceChange() {
        System.out.println("Notification preferences changed for: " + name + " " + surname);
    }
}
