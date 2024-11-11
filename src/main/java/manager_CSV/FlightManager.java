package manager_CSV;

import domainModel.Flight;
import domainModel.NotificationType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FlightManager {
    private static final Log log = LogFactory.getLog(FlightManager.class);
    private final CSVManager csvManager;
    private final List<Flight> flights;
    private final String csvFilePath = "csv/flights.csv";

    public FlightManager() throws IOException {
        // Viene caricato il file CSV dal classpath
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
        // Si salta header
        for (int i = 1; i < records.size(); i++) {
            String[] record = records.get(i);
            Flight flight = new Flight(
                    record[0],
                    record[1],
                    record[2],
                    LocalDateTime.parse(record[3]),
                    LocalDateTime.parse(record[4]),
                    Integer.parseInt(record[5]),
                    Integer.parseInt(record[6]),
                    Integer.parseInt(record[7])
            );
            flights.add(flight);
        }
        return flights;
    }

    public void addFlight(Flight flight) throws IOException {
        Flight existingFlight = getFlightByNumber(flight.getFlightNumber());
        if (existingFlight != null) {
            throw new IllegalArgumentException("Flight " + flight.getFlightNumber() + " already exists.");
        } else {
            flights.add(flight);
            String[] record = {
                    flight.getFlightNumber(),
                    flight.getDepartureAirportCode(),
                    flight.getArrivalAirportCode(),
                    String.valueOf(flight.getDepartureTime()),
                    String.valueOf(flight.getArrivalTime()),
                    String.valueOf(flight.getEconomySeats()),
                    String.valueOf(flight.getBusinessSeats()),
                    String.valueOf(flight.getFirstSeats())
            };
            try {
                csvManager.appendRecord(record, csvFilePath);
            } catch (IOException e) {
                log.error("An error occurred while writing a flight to the CSV file", e);
                throw e;
            }
        }
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
            System.out.println("Flight " + flightNumber + " status changed, now departure time is " + newDepartureTime +
                    " and arrival time is " + newArrivalTime);
            saveAllFlights();
        }
    }

    private void saveAllFlights() throws IOException {
        List<String[]> records = new ArrayList<>();
        // Header
        records.add(new String[]{"flightNumber", "departureAirportCode", "arrivalAirportCode", "departureTime", "arrivalTime",
                "economySeats", "businessSeats", "firstSeats"});
        // Dati da aggiungere
        for (Flight flight : flights) {
            records.add(new String[]{
                    flight.getFlightNumber(),
                    flight.getDepartureAirportCode(),
                    flight.getArrivalAirportCode(),
                    String.valueOf(flight.getDepartureTime()),
                    String.valueOf(flight.getArrivalTime()),
                    String.valueOf(flight.getEconomySeats()),
                    String.valueOf(flight.getBusinessSeats()),
                    String.valueOf(flight.getFirstSeats())
            });
        }

        try {
            csvManager.writeAll(records, csvFilePath);
        } catch (IOException e) {
            log.error("An error occurred while saving flights to the CSV file: " + e.getMessage());
            throw e;
        }
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
}
