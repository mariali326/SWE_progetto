package flightPlanner;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.time.LocalDateTime;

public class PaymentManager {
    private CSVManager csvManager;
    private List<Payment> payments;

    public PaymentManager(String csvFilePath) throws IOException {
        // Carica il file CSV dal classpath
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
        // Salta l'header
        for (int i = 1; i < records.size(); i++) {
            String[] record = records.get(i);
            Payment payment = new Payment(
                    record[0],
                    record[1],
                    Double.parseDouble(record[2]),
                    LocalDateTime.parse(record[3]),
                    PaymentMethod.valueOf(record[4]) // converte la stringa letta in un enum
            );
            payments.add(payment);
        }
    }

    public void addPayment(Payment payment) throws IOException {
        payments.add(payment);
        String[] record = {
                payment.getPaymentId(),
                payment.getBookingId(),
                String.valueOf(payment.getAmount()),
                payment.getPaymentMethod().name(),  // Converte l'enum in stringa
                payment.getPaymentDate().toString()
        };
        csvManager.appendRecord(record);
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
        }
    }

    public Payment getPaymentById(String paymentId) {
        for (Payment payment : payments) {
            if (payment.getPaymentId().equals(paymentId)) {
                return payment;
            }
        }
        return null;
    }

    private void saveAllPayments() throws IOException {
        List<String[]> records = new ArrayList<>();
        // Header
        records.add(new String[]{"paymentId", "bookingId", "amount", "paymentMethod", "paymentDate"});
        // Dati
        for (Payment payment : payments) {
            records.add(new String[]{
                    payment.getPaymentId(),
                    payment.getBookingId(),
                    String.valueOf(payment.getAmount()),
                    payment.getPaymentMethod().name(),  // Converte l'enum in stringa
                    payment.getPaymentDate().toString()
            });
        }
        csvManager.writeAll(records);
    }

    public void updatePayment(Payment updatedPayment) throws IOException {
        for (int i = 0; i < payments.size(); i++) {
            Payment payment = payments.get(i);
            if (payment.getPaymentId().equalsIgnoreCase(updatedPayment.getPaymentId())) {
                payments.set(i, updatedPayment);
                break;
            }
        }
        saveAllPayments();
    }

    public List<Payment> getAllPayments() {
        return payments;
    }
}
