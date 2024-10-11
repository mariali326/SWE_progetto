package flightPlanner;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

public class BookingManager {
    private CSVManager csvManager;
    private List<Booking> bookings;
    private FlightManager flightManager;

    public BookingManager(String csvFilePath, TicketManager ticketManager) throws IOException {
        // Carica il file CSV dal classpath
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(csvFilePath);
        if (inputStream == null) {
            throw new FileNotFoundException("File not found in resources: " + csvFilePath);
        }
        this.csvManager = new CSVManager(new InputStreamReader(inputStream));
        this.bookings = new ArrayList<>();
        loadBookings(ticketManager);
    }

    public BookingManager(FlightManager flightManager) {
        this.flightManager = flightManager;
    }

    public void bookFlight(String stringId, Passenger passenger) throws IOException {
        Flight flight = flightManager.getFlightByNumber(stringId);
        if (flight != null) {
            flight.subscribe(passenger);  // Aggiungere il passeggero come osservatore del volo
            System.out.println("Passenger " + passenger.getName() + " booked flight " + flight.getFlightNumber());
        } else {
            System.out.println("Flight not found.");
        }
    }

    public void addBooking(Booking booking) throws IOException {
        bookings.add(booking);
        String ticketIdsConcatenated = String.join("|",
                booking.getTickets().stream().map(Ticket::getTicketNumber).toArray(String[]::new));
        String[] record = {
                booking.getBookingId(),
                booking.getPassengerUsername(),
                ticketIdsConcatenated,
                String.valueOf(booking.getTotalAmount())
        };
        csvManager.appendRecord(record);
    }

    public void removeBooking(String bookingId) throws IOException {
        Booking toRemove = null;
        for (Booking booking : bookings) {
            if (booking.getBookingId().equalsIgnoreCase(bookingId)) {
                toRemove = booking;
                break;
            }
        }
        if (toRemove != null) {
            bookings.remove(toRemove);
            saveAllBookings();
        }
    }

    public Booking getBookingById(String bookingId) {
        for (Booking booking : bookings) {
            if (booking.getBookingId().equalsIgnoreCase(bookingId)) {
                return booking;
            }
        }
        return null;
    }

    public List<Booking> getAllBookings() {
        return bookings;
    }

    private void loadBookings(TicketManager ticketManager) throws IOException {
        List<String[]> records = csvManager.readAll();
        // Salta l'header
        for (int i = 1; i < records.size(); i++) {
            String[] record = records.get(i);
            String bookingId = record[0];
            String passengerUsername = record[1];
            String flightNumber = record[2];
            LocalDateTime bookingDate = LocalDateTime.parse(record[3]);
            String[] ticketNumbers = record[4].split("\\|");
            List<Ticket> tickets = new ArrayList<>();
            double totalAmount = Double.parseDouble(record[5]);

            for (String ticketNumber : ticketNumbers) {
                Ticket ticket = ticketManager.getTicketByNumber(ticketNumber);
                if (ticket != null) {
                    tickets.add(ticket);
                }
            }
        }
    }

    public void updateBooking(Booking updatedBooking) throws IOException {
        for (int i = 0; i < bookings.size(); i++) {
            Booking booking = bookings.get(i);
            if (booking.getBookingId().equalsIgnoreCase(updatedBooking.getBookingId())) {
                bookings.set(i, updatedBooking);
                break;
            }
        }
        saveAllBookings();
    }

    private void saveAllBookings() throws IOException {
        List<String[]> records = new ArrayList<>();
        // Header
        records.add(new String[]{"bookingId", "passengerUsername", "flightNumber", "bookingDate", "ticketIds", "totalAmount"});
        // Dati da aggiungere
        for (Booking booking : bookings) {
            String ticketIdsConcatenated = String.join("|",
                    booking.getTickets().stream().map(Ticket::getTicketNumber).toArray(String[]::new));
            records.add(new String[]{
                    booking.getBookingId(),
                    booking.getPassengerUsername(),
                    booking.getFlightNumber(),
                    String.valueOf(booking.getBookingDate()),
                    ticketIdsConcatenated,
                    String.valueOf(booking.getTotalAmount())
            });
        }
        csvManager.writeAll(records);
    }

}
