package flightPlanner;

import java.time.LocalDateTime;

public class Payment {
    private String paymentId;
    private String bookingId;
    private double amount;
    private LocalDateTime paymentDate;
    private PaymentMethod method;
    private String passengerUsername;

    public Payment(String paymentId, String bookingId, double amount, LocalDateTime paymentDate, PaymentMethod method, String passengerUsername) {
        this.paymentId = paymentId;
        this.bookingId = bookingId;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.method = method;
        this.passengerUsername = passengerUsername;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getBookingId() {
        return bookingId;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public PaymentMethod getPaymentMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    public String getPassengerUsername() {
        return passengerUsername;
    }

    @Override
    public String toString() {
        return "Payment ID: " + paymentId + ", Amount: " + amount + ", Method: " + method;
    }
}