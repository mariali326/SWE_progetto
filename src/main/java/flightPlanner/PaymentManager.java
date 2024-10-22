package flightPlanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PaymentManager {
    private static final Log log = LogFactory.getLog(PaymentManager.class);
    private final CSVManager csvManager;
    private final List<Payment> payments;
    private final String csvFilePath = "csv/payments.csv";

    public PaymentManager() throws IOException {
        // Viene caricato il file CSV dal classpath
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(csvFilePath);
        if (inputStream == null) {
            throw new FileNotFoundException("File not found in resources: " + csvFilePath);
        }
        this.csvManager = new CSVManager(new InputStreamReader(inputStream));
        this.payments = new ArrayList<>();
        loadPayments();
    }

    private void loadPayments() throws IOException {
        List<String[]> records = csvManager.readAll();
        // Si salta l'header
        for (int i = 1; i < records.size(); i++) {
            String[] record = records.get(i);
            Payment payment = new Payment(
                    record[0],
                    record[1],
                    Double.parseDouble(record[2]),
                    LocalDateTime.parse(record[3]),
                    PaymentMethod.valueOf(record[4]), // converte la stringa letta in un enum
                    record[5]
            );
            payments.add(payment);
        }
    }

    public void addPayment(Payment payment) throws IOException {
        for (Payment existingPayment : payments) {
            if (existingPayment.getPaymentId().equalsIgnoreCase(payment.getPaymentId())) {
                throw new IllegalArgumentException("Payment " + payment.getPaymentId() + " already exists.");
            }
        }
        payments.add(payment);
        String[] record = {
                payment.getPaymentId(),
                payment.getBookingId(),
                String.valueOf(payment.getAmountPayed()),
                payment.getPaymentDate().toString(),
                payment.getPaymentMethod().name(),  // Converte l'enum in stringa
                payment.getPassengerUsername()
        };
        try {
            csvManager.appendRecord(record, csvFilePath);
        } catch (IOException e) {
            log.error("An error occurred while writing a payment to the CSV file", e);
            throw e;
        }
    }

    public void removePayment(String paymentId) throws IOException {
        Payment toRemove = null;
        for (Payment payment : payments) {
            if (payment.getPaymentId().equalsIgnoreCase(paymentId)) {
                toRemove = payment;
                break;
            }
        }
        if (toRemove != null) {
            payments.remove(toRemove);
            saveAllPayments();
        } else {
            System.out.println("Payment " + paymentId + " not found.");
        }
    }

    private void saveAllPayments() throws IOException {
        List<String[]> records = new ArrayList<>();
        // Header
        records.add(new String[]{"paymentId", "bookingId", "amount", "paymentDate", "paymentMethod", "passengerUsername"});
        // Dati
        for (Payment payment : payments) {
            records.add(new String[]{
                    payment.getPaymentId(),
                    payment.getBookingId(),
                    String.valueOf(payment.getAmountPayed()),
                    payment.getPaymentDate().toString(),
                    payment.getPaymentMethod().name(),  // Converte l'enum in stringa
                    payment.getPassengerUsername()

            });
        }
        try {
            csvManager.writeAll(records, csvFilePath);
        } catch (IOException e) {
            log.error("An error occurred while saving payments to the CSV file: " + e.getMessage());
            throw e;
        }
    }

    public List<Payment> getAllPayments() {
        return payments;
    }

    public Payment getPaymentById(String paymentId) {
        for (Payment payment : payments) {
            if (payment.getPaymentId().equals(paymentId)) {
                return payment;
            }
        }
        return null;
    }
}
