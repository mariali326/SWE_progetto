package flightPlanner;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PaymentManager {
    private CSVManager csvManager;
    private List<Payment> payments;
    private String csvFilePath ="csv/payments.csv";

    public PaymentManager() throws IOException {
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
                String.valueOf(payment.getAmount()),
                payment.getPaymentDate().toString(),
                payment.getPaymentMethod().name(),  // Converte l'enum in stringa
                payment.getPassengerUsername()
        };
        try {
            csvManager.appendRecord(record,csvFilePath);
        }catch (IOException e) {
            System.out.println("Error details:");
            e.printStackTrace();
            throw new IOException("An error occurred while writing a payment on file CSV", e);
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
        records.add(new String[]{"paymentId", "bookingId", "amount", "paymentDate", "paymentMethod", "passengerUsername"});
        // Dati
        for (Payment payment : payments) {
            records.add(new String[]{
                    payment.getPaymentId(),
                    payment.getBookingId(),
                    String.valueOf(payment.getAmount()),
                    payment.getPaymentDate().toString(),
                    payment.getPaymentMethod().name(),  // Converte l'enum in stringa
                    payment.getPassengerUsername()

            });
        }
        try {
            csvManager.writeAll(records,csvFilePath);
        }catch (IOException e) {
            System.err.println("An error occurred while saving payments on file CSV: " + e.getMessage());
            System.out.println("Error details:");
            e.printStackTrace();
        }
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
