package flightPlanner;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class FlightSearchService {
    private FlightManager flightManager;
    private AirportManager airportManager;
    private RouteManager routeManager;
    private SeatManager seatManager;


    public FlightSearchService(FlightManager flightManager, AirportManager airportManager, RouteManager routeManager, SeatManager seatManager) {
        this.flightManager = flightManager;
        this.airportManager = airportManager;
        this.routeManager = routeManager;
        this.seatManager = seatManager;
    }

    public List<Flight> searchByDeparture(String departure) {
        return flightManager.getAllFlights().stream()
                .filter(f -> f.getDepartureAirportCode().equalsIgnoreCase(departure))
                .collect(Collectors.toList());
    }

    public List<Flight> searchByArrival(String arrival) {
        return flightManager.getAllFlights().stream()
                .filter(f -> f.getArrivalAirportCode().equalsIgnoreCase(arrival))
                .collect(Collectors.toList());
    }

    // Metodo per cercare voli per partenza e destinazione
    public List<Flight> searchFlights(String departure, String arrival) {
        return flightManager.getAllFlights().stream()
                .filter(flight -> flight.getDepartureAirportCode().equalsIgnoreCase(departure) &&
                        flight.getArrivalAirportCode().equalsIgnoreCase(arrival))
                .collect(Collectors.toList());
    }


    public List<Flight> searchByDate(LocalDateTime date) {
        return flightManager.getAllFlights().stream()
                .filter(f -> f.getDepartureTime().equals(date))
                .collect(Collectors.toList());
    }

    public List<Flight> searchByRoute(String departure, String arrival) {
        return flightManager.getAllFlights().stream()
                .filter(f -> f.getDepartureAirportCode().equalsIgnoreCase(departure) && f.getArrivalAirportCode().equalsIgnoreCase(arrival))
                .collect(Collectors.toList());
    }

    // cercare voli per aeroporto di partenza e di arrivo
    public List<Flight> searchFlightsByAirports(String departureCode, String arrivalCode) {
        String departure = airportManager.getAirportByCode(departureCode).getName();
        String arrival = airportManager.getAirportByCode(arrivalCode).getName();

        return searchFlights(departure, arrival);
    }

    public List<Flight> searchFlightsByRoute(String routeId) {
        Route route = routeManager.getRouteById(routeId);
        if (route != null) {
            return searchFlights(route.getDepartureAirportCode(), route.getArrivalAirportCode());
        }
        return List.of(); // Nessun volo trovato
    }

    //flitra i voli in base alla disponibilità di posti
    public List<Flight> searchAvailableFlights(String departure, String arrival) {
        List<Flight> flights = searchFlights(departure, arrival);
        return flights.stream()
                .filter(flight -> seatManager.hasAvailableSeats(flight))
                .collect(Collectors.toList());
    }
}
