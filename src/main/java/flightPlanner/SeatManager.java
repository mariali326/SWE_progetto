package flightPlanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SeatManager {
    private static final Log log = LogFactory.getLog(SeatManager.class);
    private final CSVManager csvManager;
    private final List<Seat> seats;
    private final String csvFilePath = "csv/seats.csv";

    public SeatManager() throws IOException {
        // Viene caricato il file CSV dal classpath
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(csvFilePath);
        if (inputStream == null) {
            throw new FileNotFoundException("File not found in resources: " + csvFilePath);
        }
        this.csvManager = new CSVManager(new InputStreamReader(inputStream));
        this.seats = new ArrayList<>();
        loadSeats();
    }

    private void loadSeats() throws IOException {
        List<String[]> records = csvManager.readAll();
        // Si salta l'header
        for (int i = 1; i < records.size(); i++) {
            String[] record = records.get(i);
            Seat seat = new Seat(
                    record[0],
                    record[1],
                    record[2],
                    Boolean.parseBoolean(record[3])
            );
            seats.add(seat);
        }
    }

    public void addSeat(Seat seat) throws IOException {
        if (!seats.contains(seat)) {
            seat.setAvailable(true);
            seats.add(seat);
            saveAllSeats();
            System.out.println("Seat " + seat.getSeatNumber() + " added to flight " + seat.getFlightNumber());
        } else {
            throw new IllegalArgumentException("Seat already exists on flight " + seat.getFlightNumber() + ": " + seat.getSeatNumber());
        }
    }

    public void removeSeats(Seat seat) throws IOException {
        if (seats.contains(seat)) {
            seats.remove(seat);
            saveAllSeats();
            System.out.println("Seat " + seat.getSeatNumber() + " has been removed from flight " + seat.getFlightNumber());
        } else {
            throw new IllegalArgumentException("Seat doesn't exist : " + seat.getSeatNumber());
        }
    }

    private void saveAllSeats() throws IOException {
        List<String[]> records = new ArrayList<>();
        // Header
        records.add(new String[]{"seatNumber", "classType", "flightNumber", "isAvailable"});
        // Dati
        for (Seat seat : seats) {
            records.add(new String[]{
                    seat.getSeatNumber(),
                    seat.getClassType(),
                    seat.getFlightNumber(),
                    String.valueOf(seat.isAvailable()),
            });
        }
        try {
            csvManager.writeAll(records, csvFilePath);
        } catch (IOException e) {
            log.error("An error occurred while saving seats to the CSV file: " + e.getMessage());
            throw e;
        }
    }

    // Metodo per aggiornare la disponibilità di un posto
    public void updateSeatAvailability(String seatNumber, String flightNumber, boolean isAvailable) throws IOException {
        Seat seat = getSeatByNumber(seatNumber, flightNumber);
        if (seat != null) {
            seat.setAvailable(isAvailable);
            saveAllSeats();
            System.out.println("Seat " + seatNumber + " updated: Available = " + isAvailable);
        } else {
            System.out.println("Seat " + seatNumber + " not found.");
        }
    }

    public void bookSeat(String flightNumber, String seatNumber) throws IOException {
        List<Seat> seats = getSeatsByFlightNumber(flightNumber);
        boolean seatFound = false;
        for (Seat seat : seats) {
            if (seat.getSeatNumber().equalsIgnoreCase(seatNumber) && seat.isAvailable()) {
                seat.setAvailable(false); // Viene impostato il posto come prenotato (non disponibile)
                saveAllSeats();
                System.out.println("Seat " + seatNumber + " successfully booked.");
                seatFound = true; // Prenotazione completata con successo
                updateSeatAvailability(seatNumber, flightNumber, false);
                break;
            }
        }
        if (!seatFound) {
            System.out.println("Seat " + seatNumber + " is not available or doesn't exist.");
        }
    }

    // Metodo per rilasciare un posto (cancellare la prenotazione)
    public void releaseSeat(String seatNumber, String flightNumber) throws IOException {
        Seat seat = getSeatByNumber(seatNumber, flightNumber);
        if (seat != null && !seat.isAvailable()) {
            seat.releaseSeat();
            saveAllSeats();
            updateSeatAvailability(seatNumber, flightNumber, true);
            //System.out.println("Seat " + seatNumber + " successfully released and it's now available.");
        } else {
            System.out.println("Seat " + seatNumber + " is already available or doesn't exist.");
        }
    }

    public List<Seat> findAvailableSeats(String flightNumber) {
        List<Seat> allSeats = getSeatsByFlightNumber(flightNumber);
        List<Seat> availableSeats = new ArrayList<>();
        for (Seat seat : allSeats) {
            if (seat.isAvailable()) {
                availableSeats.add(seat);
            }
        }
        return availableSeats;
    }

    public Map<String, String> getAvailableSeatsWithClassType(String flightNumber) {
        List<Seat> availableSeats = findAvailableSeats(flightNumber);
        Map<String, String> availableSeatsNumWithClassType = new HashMap<>();
        for (Seat seat : availableSeats) {
            availableSeatsNumWithClassType.put(seat.getSeatNumber(), seat.getClassType());
        }
        return availableSeatsNumWithClassType;
    }

    public List<Seat> getAllSeats() {
        return seats;
    }

    public Seat getSeatByNumber(String seatNumber, String flightNumber) {
        return seats.stream()
                .filter(seat -> seat.getSeatNumber().equalsIgnoreCase(seatNumber) &&
                        seat.getFlightNumber().equalsIgnoreCase(flightNumber))
                .findFirst()
                .orElse(null);
    }

    public List<Seat> getSeatsByFlightNumber(String flightNumber) {
        return seats.stream()
                .filter(seat -> seat.getFlightNumber().equals(flightNumber))
                .collect(Collectors.toList());
    }
}
