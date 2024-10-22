package flightPlanner;

public class EmailNotification implements NotificationChannel {

    @Override
    public void sendNotification(String message, Passenger passenger) {
        // Simulazione invio email
        System.out.println("Email to " + passenger.getName() + " " + passenger.getSurname() + ": " + message);
    }

    @Override
    public String toString() {
        return "Email Notification";
    }
}
