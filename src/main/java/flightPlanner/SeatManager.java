package flightPlanner;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SeatManager {
    private CSVManager csvManager;
    private List<Seat> seats;

    public SeatManager(String csvFilePath) throws IOException {
        // Carica il file CSV dal classpath
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
        // Salta l'header
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

    public List<Seat> getAllSeats() {
        return seats;
    }

    // Metodo per ottenere tutti i posti associati a un determinato volo
    public List<Seat> getSeatsByFlight(String stringNumber) {
        return seats.stream()
                .filter(seat -> seat.getFlightNumber().equals(stringNumber))
                .collect(Collectors.toList());
    }

    public Seat getSeatByNumber(String seatNumber) {
        return seats.stream()
                .filter(seat -> seat.getSeatNumber().equalsIgnoreCase(seatNumber))
                .findFirst()
                .orElse(null);
    }

    public List<Seat> getSeatsByFlightNumber(String stringNumber) {
        return seats.stream()
                .filter(seat -> seat.getFlightNumber().equals(stringNumber))
                .collect(Collectors.toList());
    }

    public void addSeat(Seat seat) throws IOException {
        if (!seats.contains(seat)) {
            seat.setAvailable(true);
            seats.add(seat);
            saveAllSeats();
            System.out.println("Seat added: " + seat.getSeatNumber());
        } else {
            System.out.println("Seat already exists: " + seat.getSeatNumber());
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
                    String.valueOf(seat.isAvailable())
            });
        }
        csvManager.writeAll(records);
    }

    // Metodo per aggiornare la disponibilità di un posto
    public void updateSeatAvailability(String seatNumber, boolean isAvailable) throws IOException {
        Seat seat = getSeatByNumber(seatNumber);
        if (seat != null) {
            seat.setAvailable(isAvailable);
            saveAllSeats();
            System.out.println("Seat " + seatNumber + " updated: Available = " + isAvailable);
        } else {
            System.out.println("Seat " + seatNumber + " not found.");
        }
    }

    // Verifica se ci sono posti disponibili per un determinato volo
    public boolean hasAvailableSeats(Flight flight) {
        List<Seat> seatsForFlight = getSeatsByFlightNumber(flight.getFlightNumber());

        // Controlla se almeno uno di questi posti è disponibile
        for (Seat seat : seatsForFlight) {
            if (seat.isAvailable()) {
                return true;
            }
        }

        return false; // Nessun posto disponibile
    }

    public boolean bookSeat(String seatNumber) throws IOException {
        Seat seat = getSeatByNumber(seatNumber);
        if (seat != null && seat.isAvailable()) {
            seat.setAvailable(false); // Imposta il posto come prenotato (non disponibile)
            saveAllSeats();
            System.out.println("Seat " + seatNumber + " successfully booked.");
            return true; // Prenotazione completata con successo
        } else {
            System.out.println("Seat " + seatNumber + " is not available or doesn't exist.");
            return false; // Prenotazione fallita
        }
    }

    // Metodo per rilasciare un posto (cancellare la prenotazione)
    public boolean releaseSeat(String seatNumber) throws IOException {
        Seat seat = getSeatByNumber(seatNumber);
        if (seat != null && !seat.isAvailable()) {
            seat.setAvailable(true); // Imposta il posto come disponibile
            saveAllSeats(); // Salva lo stato aggiornato
            System.out.println("Seat " + seatNumber + " successfully released.");
            return true; // Cancellazione della prenotazione avvenuta
        } else {
            System.out.println("Seat " + seatNumber + " is already available or doesn't exist.");
            return false; // Errore nella cancellazione
        }
    }
}
