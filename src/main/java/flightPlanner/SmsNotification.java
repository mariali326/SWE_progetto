package flightPlanner;

public class SmsNotification implements NotificationChannel {
    // Simulazione invio sms
    @Override
    public void sendNotification(String message, Passenger passenger) {
        if (passenger.getPhoneNumber() != null && !passenger.getPhoneNumber().isEmpty()) {
            System.out.println("SMS sent to " + passenger.getName() + " " + passenger.getSurname() + ": " + message);
        } else {
            System.out.println("SMS notification failed: no phone number provided for passenger " + passenger.getName()
                    + " " + passenger.getSurname());
        }
    }

    @Override
    public String toString() {
        return "Sms Notification";
    }
}
