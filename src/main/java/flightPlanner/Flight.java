package flightPlanner;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

public class Flight implements Subject{
    private String flightNumber;
    private String departureAirportCode;
    private String arrivalAirportCode;
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

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public List<Observer> getObservers() {
        return observers;
    }

    // Aggiorna lo stato del volo e notifica gli Observers
    public void updateFlightStatus(LocalDateTime newDepartureTime, LocalDateTime newArrivalTime, String updateMessage, NotificationType type) {
        this.departureTime = newDepartureTime;
        this.arrivalTime = newArrivalTime;
        notify(updateMessage, type);
    }

    @Override
    public String toString() {
        return flightNumber + " - " + departureAirportCode + " to " + arrivalAirportCode + " | Departure: " + departureTime + " | Arrival: " + arrivalTime;
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
}
