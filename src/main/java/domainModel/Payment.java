package domainModel;

import java.time.LocalDateTime;

public class Payment {
    private final String paymentId;
    private final String bookingId;
    private final double amountPayed;
    private final LocalDateTime paymentDate;
    private final String passengerUsername;
    private final PaymentMethod method;

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

    public String getPassengerUsername() {
        return passengerUsername;
    }

    @Override
    public String toString() {
        return "Payment ID: " + paymentId + ", Booking Id: " + bookingId + ", Amount Payed: " + amountPayed +
                " EUR, Payment Date: " + paymentDate + ", Payment Method: " + method + ", Passenger Username: " + passengerUsername;
    }
}