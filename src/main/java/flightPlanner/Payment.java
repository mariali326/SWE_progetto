package flightPlanner;

import java.time.LocalDateTime;

public class Payment {
    private final String paymentId;
    private final String bookingId;
    private final double amountPayed;
    private final LocalDateTime paymentDate;
    private PaymentMethod method;
    private final String passengerUsername;

    public Payment(String paymentId, String bookingId, double amountPayed, LocalDateTime paymentDate, PaymentMethod method, String passengerUsername) {
        this.paymentId = paymentId;
        this.bookingId = bookingId;
        this.amountPayed = amountPayed;
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

    public double getAmountPayed() {
        return amountPayed;
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
        return "Payment ID: " + paymentId + ", Amount: " + amountPayed + ", Method: " + method;
    }
}