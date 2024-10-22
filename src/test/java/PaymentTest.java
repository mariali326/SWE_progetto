import flightPlanner.Payment;
import flightPlanner.PaymentMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PaymentTest {

    @Test
    @DisplayName("Test that checks the creation of a payment")
    public void testPaymentCreation() {
        String paymentId = "P009";
        String bookingId = "BK014";
        double amount = 440;
        LocalDateTime paymentDate = LocalDateTime.now();
        PaymentMethod method = PaymentMethod.CREDIT_CARD;
        String passengerUsername = "beatrix";

        Payment payment = new Payment(paymentId, bookingId, amount, paymentDate, method, passengerUsername);

        assertEquals(paymentId, payment.getPaymentId());
        assertEquals(bookingId, payment.getBookingId());
        assertEquals(amount, payment.getAmountPayed());
        assertEquals(paymentDate, payment.getPaymentDate());
        assertEquals(method, payment.getPaymentMethod());
        assertEquals(passengerUsername, payment.getPassengerUsername());
    }

    @Test
    @DisplayName("Test that checks the toString method returns the right string format")
    public void testToString() {
        LocalDateTime paymentDate = LocalDateTime.now();
        Payment payment = new Payment("P018", "BK018", 500.0, paymentDate, PaymentMethod.PAYPAL, "mysterious");

        String expectedString = "Payment ID: P018, Booking Id: BK018, Amount Payed: 500.0 EUR, Payment Date: " + paymentDate +
                ", Payment Method: PAYPAL, Passenger Username: mysterious";

        assertEquals(expectedString, payment.toString());
    }
}

