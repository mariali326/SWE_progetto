package flightPlanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BookingManager {
    private static final Log log = LogFactory.getLog(BookingManager.class);
    private final CSVManager csvManager;
    private final List<Booking> bookings;
    private final FlightManager flightManager;
    private final PassengerManager passengerManager;
    private final String csvFilePath = "csv/bookings.csv";

    public BookingManager(FlightManager flightManager, PassengerManager passengerManager, TicketManager ticketManager) throws IOException {
        // Viene caricato il file CSV dal classpath
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(csvFilePath);
        if (inputStream == null) {
            throw new FileNotFoundException("File not found in resources: " + csvFilePath);
        }

        this.flightManager = flightManager;
        this.passengerManager = passengerManager;
        this.csvManager = new CSVManager(new InputStreamReader(inputStream));
        this.bookings = loadBookings(ticketManager);
    }

    static void registerAdditionalPassenger(List<Ticket> tickets, Flight flight, PassengerManager passengerManager) {
        tickets.stream().skip(1).forEach(ticket -> {
            String name = ticket.getPassengerName();
            String surname = ticket.getPassengerSurname();
            String documentType = ticket.getDocumentType();
            String documentId = ticket.getDocumentId();

            Passenger additionalPassenger = passengerManager.getPassengerByFullNameAndDocument(name, surname, documentType, documentId);

            if (additionalPassenger != null) {
                additionalPassenger.registerForFlight(flight);
            } else {
                System.out.println("Passenger not found: " + name + " " + surname);
            }
        });
    }

    public void addBooking(Booking booking) throws IOException {
        for (Booking existingBooking : bookings) {
            if (existingBooking.getBookingId().equalsIgnoreCase(booking.getBookingId())) {
                throw new IllegalArgumentException("Booking " + booking.getBookingId() + " already exists.");
            }
        }
        bookings.add(booking);
        String ticketIdsConcatenated = String.join("|",
                booking.getTickets().stream().map(Ticket::getTicketNumber).toArray(String[]::new));
        String[] record = {
                booking.getBookingId(),
                booking.getPassengerUsername(),
                booking.getFlightNumber(),
                booking.getBookingDate().toString(),
                ticketIdsConcatenated,
                String.valueOf(booking.getTotalAmount())
        };
        try {
            csvManager.appendRecord(record, csvFilePath);
        } catch (IOException e) {
            log.error("An error occurred while writing a booking to the CSV file", e);
            throw e;
        }
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
        } else {
            System.out.println("Booking " + bookingId + " not found.");
        }
    }

    private List<Booking> loadBookings(TicketManager ticketManager) throws IOException {
        List<String[]> records = csvManager.readAll();
        List<Booking> bookings = new ArrayList<>();
        // Si salta l'header
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

            Passenger passenger = passengerManager.getPassengerByUsername(passengerUsername);
            Flight flight = flightManager.getFlightByNumber(flightNumber);
            passenger.registerForFlight(flight);
            registerAdditionalPassenger(tickets, flight, passengerManager);

            Booking booking = new Booking(
                    bookingId,
                    passengerUsername,
                    flightNumber,
                    bookingDate,
                    tickets,
                    totalAmount
            );
            bookings.add(booking);
        }
        return bookings;
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
        try {
            csvManager.writeAll(records, csvFilePath);
        } catch (IOException e) {
            log.error("An error occurred while saving bookings to the CSV file: " + e.getMessage());
            throw e;
        }
    }

    public List<Booking> getBookingsForPassenger(String username) {
        List<Booking> passengerBookings = new ArrayList<>();
        for (Booking booking : bookings) {
            if (booking.getPassengerUsername().equals(username)) {
                passengerBookings.add(booking);
            }
        }
        return passengerBookings;
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
}