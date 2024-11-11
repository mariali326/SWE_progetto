package domainModel;

import java.time.Duration;

public class Route {

    private final String routeId;
    private final String departureAirportCode;
    private final String arrivalAirportCode;
    private final double distance; // In km
    private Duration flightDuration;

    public Route(String routeId, String departureAirportCode, String arrivalAirportCode, double distance, Duration flightDuration) {
        this.routeId = routeId;
        this.departureAirportCode = departureAirportCode;
        this.arrivalAirportCode = arrivalAirportCode;
        this.distance = distance;
        this.flightDuration = flightDuration;
    }

    // Metodo per mostrare "xxhxxm" in Duration
    public static Duration parseDuration(String durationStr) {
        String[] parts = durationStr.split("[hm]");
        long hours = Long.parseLong(parts[0]);
        long minutes = Long.parseLong(parts[1]);
        return Duration.ofHours(hours).plusMinutes(minutes);
    }

    public String getRouteId() {
        return routeId;
    }

    public String getDepartureAirportCode() {
        return departureAirportCode;
    }

    public String getArrivalAirportCode() {
        return arrivalAirportCode;
    }

    public double getDistance() {
        return distance;
    }

    public Duration getFlightDuration() {
        return flightDuration;
    }

    public void setFlightDuration(Duration flightDuration) {
        this.flightDuration = flightDuration;
    }

    // Metodo per convertire Duration in "xxhxxm"
    public String getFormattedDuration() {
        long hours = flightDuration.toHours();
        long minutes = flightDuration.toMinutes() % 60;
        return String.format("%dh%dm", hours, minutes);
    }

    @Override
    public String toString() {
        long hours = flightDuration.toHours();
        long minutes = flightDuration.toMinutesPart();
        return routeId + ": " + departureAirportCode + " - " + arrivalAirportCode + " the distance is " + distance +
                "km and the flight duration is " + hours + "h" + minutes + "m";
    }
}
