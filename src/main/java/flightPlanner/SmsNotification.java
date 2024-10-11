package flightPlanner;

public class SmsNotification implements NotificationChannel{
     //Simulazione invio sms
    @Override
    public void sendNotification(String message, Passenger passenger) {
                System.out.println("Sms to " + passenger.getName() + " " + passenger.getSurname() + ": " + message);
    }
}
