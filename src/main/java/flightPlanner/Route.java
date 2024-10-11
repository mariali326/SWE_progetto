package flightPlanner;

public class Route {

    private String routeId;
    private String departureAirportCode;
    private String arrivalAirportCode;
    private double distance; // in km

    public Route(String routeId, String departureAirportCode, String arrivalAirportCode, double distance) {
        this.routeId = routeId;
        this.departureAirportCode = departureAirportCode;
        this.arrivalAirportCode = arrivalAirportCode;
        this.distance = distance;
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

    @Override
    public String toString() {
        return routeId + ": " + departureAirportCode + " to " + arrivalAirportCode + " the distance is " + distance;
    }
}
