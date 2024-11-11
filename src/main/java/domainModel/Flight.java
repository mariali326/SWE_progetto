package domainModel;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Flight implements Subject {
    private final String flightNumber;
    private final String departureAirportCode;
    private final String arrivalAirportCode;
    private final List<Observer> observers;
    private final int economySeats;
    private final int businessSeats;
    private final int firstSeats;
    private final Map<String, Integer> seatClassCount;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;

    public Flight(String flightNumber, String departureAirportCode, String arrivalAirportCode, LocalDateTime departureTime, LocalDateTime arrivalTime,
                  int economySeats, int businessSeats, int firstSeats) {
        this.flightNumber = flightNumber;
        this.departureAirportCode = departureAirportCode;
        this.arrivalAirportCode = arrivalAirportCode;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.observers = new ArrayList<>();
        this.economySeats = economySeats;
        this.businessSeats = businessSeats;
        this.firstSeats = firstSeats;
        this.seatClassCount = new HashMap<>();

        seatClassCount.put("Economy", economySeats);
        seatClassCount.put("Business", businessSeats);
        seatClassCount.put("First", firstSeats);
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public String getDepartureAirportCode() {
        return departureAirportCode;
    }

    public String getArrivalAirportCode() {
        return arrivalAirportCode;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalDateTime departureTime) {
        this.departureTime = departureTime;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalDateTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public List<Observer> getObservers() {
        return observers;
    }

    // Viene aggiornato lo stato del volo e gli Observers vengono notificati
    public void updateFlightStatus(LocalDateTime newDepartureTime, LocalDateTime newArrivalTime, String updateMessage, NotificationType type) {
        setDepartureTime(newDepartureTime);
        setArrivalTime(newArrivalTime);
        notify(updateMessage, type);
    }

    @Override
    public void notify(String message, NotificationType type) {
        for (Observer observer : observers) {
            observer.update(message, type);
        }
    }

    @Override
    public void subscribe(Observer observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void unsubscribe(Observer observer) {
        observers.remove(observer);
    }

    public int getEconomySeats() {
        return economySeats;
    }

    public int getBusinessSeats() {
        return businessSeats;
    }

    public int getFirstSeats() {
        return firstSeats;
    }

    public Map<String, Integer> getSeatClassCount() {
        return seatClassCount;
    }

    @Override
    public String toString() {
        return flightNumber + " - " + departureAirportCode + " to " + arrivalAirportCode +
                " | Departure: " + departureTime + " | Arrival: " + arrivalTime
                + " | Economy Seats: " + economySeats
                + " | Business Seats: " + businessSeats
                + " | First Class Seats: " + firstSeats;
    }
}
