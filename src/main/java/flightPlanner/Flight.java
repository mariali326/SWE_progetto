package flightPlanner;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Flight implements Subject {
    private final String flightNumber;
    private final String departureAirportCode;
    private final String arrivalAirportCode;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private List<Observer> observers;

    public Flight(String flightNumber, String departureAirportCode, String arrivalAirportCode, LocalDateTime departureTime, LocalDateTime arrivalTime) {
        this.flightNumber = flightNumber;
        this.departureAirportCode = departureAirportCode;
        this.arrivalAirportCode = arrivalAirportCode;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.observers = new ArrayList<>();
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

    @Override
    public String toString() {
        return flightNumber + " - " + departureAirportCode + " to " + arrivalAirportCode + " | Departure: " + departureTime + " | Arrival: " + arrivalTime;
    }
}
