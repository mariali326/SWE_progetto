import flightPlanner.Payment;
import flightPlanner.PaymentManager;
import flightPlanner.PaymentMethod;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PaymentManagerTest {

    private static PaymentManager paymentManager;

    @BeforeAll
    public static void setUp() throws IOException {
        paymentManager = new PaymentManager();
    }

    @Test
    @DisplayName("Test that checks loading a payment from the file CSV")
    public void testLoadPaymentFromCSV() {
        List<Payment> payments = paymentManager.getAllPayments();

        assertNotNull(payments);
        assertFalse(payments.isEmpty());

        Payment payment = paymentManager.getPaymentById("P001");
        assertEquals("P001", payment.getPaymentId());
    }

    @Test
    @DisplayName("Test that checks adding a new payment works well and after addition the file CSV is updated")
    public void testAddPayment() throws IOException {
        Payment newPayment = new Payment("P007", "BK007", 450.0, LocalDateTime.now(),
                PaymentMethod.BANK_TRANSFER, "someone");

        paymentManager.addPayment(newPayment);

        Payment addedPayment = paymentManager.getPaymentById("P007");

        assertEquals("BK007", addedPayment.getBookingId());
        assertEquals(PaymentMethod.BANK_TRANSFER, addedPayment.getPaymentMethod());
        assertEquals("someone", addedPayment.getPassengerUsername());
    }

    @Test
    @DisplayName("Test that checks it's impossible to add the same payment more than once")
    public void testAddDuplicatePayment() throws IOException {
        Payment payment = new Payment("P011", "BK011", 560.0, LocalDateTime.now(),
                PaymentMethod.DEBIT_CARD, "freedom");

        paymentManager.addPayment(payment);
        assertNotNull(payment);

        assertThrows(IllegalArgumentException.class, () -> paymentManager.addPayment(payment));
    }

    @Test
    @DisplayName("Test that checks removing a payment from the CSV file")
    public void testRemovePayment() throws IOException {
        String paymentId = "P002";

        paymentManager.removePayment(paymentId);

        Payment removedPayment = paymentManager.getPaymentById(paymentId);
        assertNull(removedPayment);
    }

    @Test
    @DisplayName("Test that checks updating a payment status works correctly")
// Anche se penso un payment non dovrebbe essere aggiornato, nemmeno in caso di cambio di modalità di pagamento
    public void testUpdatePayment() throws IOException {
        Payment updatePayment = new Payment("P001", "BK001", 1500.0, LocalDateTime.of(2024, 12, 20, 8, 0), PaymentMethod.PAYPAL, "jdoe");
        paymentManager.updatePayment(updatePayment);

        Payment updated = paymentManager.getPaymentById("P001");
        assertEquals(PaymentMethod.PAYPAL,updated.getPaymentMethod());
    }
}
