package flightPlanner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FlightSearchService {
    private FlightManager flightManager;
    private AirportManager airportManager;


    public FlightSearchService(FlightManager flightManager, AirportManager airportManager) {
        this.flightManager = flightManager;
        this.airportManager = airportManager;
    }

    // Metodo per cercare voli per partenza e destinazione(codice o città)
    // Può capitare che l'utente inserisca il nome di una città in cui ci siano presenti più di un aeroporto
    public List<Flight> searchFlights(String departure, String arrival, LocalDate date) {
        List<Flight> result = new ArrayList<>();
        List<Flight> flights = flightManager.getAllFlights();
        List<Airport> airports = airportManager.getAllAirports();

        // Si trovano gli aeroporti di partenza e di arrivo
        List<Airport> departureAirports = airports.stream()
                .filter(a -> a.getCode().equalsIgnoreCase(departure) || a.getCity().equalsIgnoreCase(departure))
                .toList();

        List<Airport> arrivalAirports = airports.stream()
                .filter(a -> a.getCode().equalsIgnoreCase(arrival) || a.getCity().equalsIgnoreCase(arrival))
                .toList();

        for (Flight flight : flights) {
            // Si verifica se il volo parte da uno degli aeroporti di partenza
            boolean matchesDeparture = departureAirports.stream()
                    .anyMatch(a -> a.getCode().equalsIgnoreCase(flight.getDepartureAirportCode()));

            // Si verifica se il volo arriva in uno degli aeroporti di arrivo
            boolean matchesArrival = arrivalAirports.stream()
                    .anyMatch(a -> a.getCode().equalsIgnoreCase(flight.getArrivalAirportCode()));

            // Se entrambi corrispondono, viene controllato la data
            if (matchesDeparture && matchesArrival) {
                LocalDate flightDate = flight.getDepartureTime().toLocalDate();
                if (flightDate.equals(date)) {
                    result.add(flight);
                }
            }
        }

        return result;
    }
}