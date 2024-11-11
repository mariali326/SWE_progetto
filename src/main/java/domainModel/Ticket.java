package domainModel;

import java.util.ArrayList;
import java.util.List;

public class Ticket {
    private final String ticketNumber;
    private final String bookingId;
    private final String flightNumber;
    private final String passengerName;
    private final String passengerSurname;
    private final List<Luggage> luggageList;
    private String seatNumber;
    private double price;
    private String documentType;
    private String documentId;

    public Ticket(String ticketNumber, String bookingId, String flightNumber, String seatNumber, double price, String passengerName, String passengerSurname, List<Luggage> luggageList) {
        this.ticketNumber = ticketNumber;
        this.bookingId = bookingId;
        this.flightNumber = flightNumber;
        this.seatNumber = seatNumber;
        this.price = price;
        this.passengerName = passengerName;
        this.passengerSurname = passengerSurname;

        this.luggageList = !luggageList.isEmpty() ? luggageList : new ArrayList<>();
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public String getBookingId() {
        return bookingId;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public String getPassengerSurname() {
        return passengerSurname;
    }

    public List<Luggage> getLuggageList() {
        return luggageList;
    }

    public int getCabinLuggageCount() {
        int count = 0;
        for (Luggage luggage : luggageList) {
            if (luggage.getType().equals("cabin"))
                count++;
        }
        return count;
    }

    public int getHoldLuggageCount() {
        int count = 0;
        for (Luggage luggage : luggageList) {
            if (luggage.getType().equals("hold"))
                count++;
        }
        return count;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    @Override
    public String toString() {
        return "Ticket Number: " + ticketNumber + ", Flight Number: " + flightNumber + ", Seat: " + seatNumber +
                ", Passenger: " + passengerName + " " + passengerSurname + ", Luggage List: " + luggageList;
    }
}
