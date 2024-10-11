package flightPlanner;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import java.io.IOException;

public class FlightManager {
    private CSVManager csvManager;
    private List<Flight> flights;

    public FlightManager(String csvFilePath) throws IOException {
        // Carica il file CSV dal classpath
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(csvFilePath);
        if (inputStream == null) {
            throw new FileNotFoundException("File not found in resources: " + csvFilePath);
        }
        this.csvManager = new CSVManager(new InputStreamReader(inputStream));
        this.flights = loadFlights();

    }


    private List<Flight> loadFlights() throws IOException {
        List<String[]> records = csvManager.readAll();
        List<Flight> flights = new ArrayList<>();
        // Salta l'header
        for (int i = 1; i < records.size(); i++) {
            String[] record = records.get(i);
            Flight flight = new Flight(
                    record[0],
                    record[1],
                    record[2],
                    LocalDateTime.parse(record[3]),
                    LocalDateTime.parse(record[4])
            );
            flights.add(flight);
        }
        return flights;
    }

    public List<Flight> getAllFlights() {
        return flights;
    }

    public Flight getFlightByNumber(String flightNumber) {
        for (Flight flight : flights) {
            if (flight.getFlightNumber().equalsIgnoreCase(flightNumber)) {
                return flight;
            }
        }
        return null;
    }

    public void addFlight(Flight flight) throws IOException {
        flights.add(flight);
        String[] record = {
                flight.getFlightNumber(),
                flight.getDepartureAirportCode(),
                flight.getArrivalAirportCode(),
                String.valueOf(flight.getDepartureTime()),
                String.valueOf(flight.getArrivalTime())
        };
        csvManager.appendRecord(record);
    }

    public void removeFlight(String flightNumber) throws IOException {
        Flight toRemove = null;
        for (Flight flight : flights) {
            if (flight.getFlightNumber().equalsIgnoreCase(flightNumber)) {
                toRemove = flight;
                break;
            }
        }
        if (toRemove != null) {
            flights.remove(toRemove);
            saveAllFlights();
        }
    }

    public void updateFlightStatus(String flightNumber, LocalDateTime newDepartureTime, LocalDateTime newArrivalTime, String updateMessage, NotificationType type) throws IOException {
        Flight flight = getFlightByNumber(flightNumber);
        if (flight != null) {
            flight.updateFlightStatus(newDepartureTime, newArrivalTime, updateMessage, type);
            saveAllFlights();
        }
    }

    private void saveAllFlights() throws IOException {
        List<String[]> records = new ArrayList<>();
        // Header
        records.add(new String[]{"flightNumber", "departureAirportCode", "arrivalAirportCode", "departureTime", "arrivalTime"});
        // Dati da aggiungere
        for (Flight flight : flights) {
            records.add(new String[]{
                    flight.getFlightNumber(),
                    flight.getDepartureAirportCode(),
                    flight.getArrivalAirportCode(),
                    String.valueOf(flight.getDepartureTime()),
                    String.valueOf(flight.getArrivalTime())
            });
        }
        csvManager.writeAll(records);
    }
}
