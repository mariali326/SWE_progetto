package flightPlanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TicketManager {
    private static final Log log = LogFactory.getLog(TicketManager.class);
    private final CSVManager csvManager;
    private final List<Ticket> tickets;
    private final String csvFilePath = "csv/tickets.csv";

    public TicketManager(LuggageManager luggageManager) throws IOException {
        // Viene caricato il file CSV dal classpath
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(csvFilePath);
        if (inputStream == null) {
            throw new FileNotFoundException("File not found in resources: " + csvFilePath);
        }
        this.csvManager = new CSVManager(new InputStreamReader(inputStream));
        this.tickets = new ArrayList<>();
        loadTickets(luggageManager);
    }

    private void loadTickets(LuggageManager luggageManager) throws IOException {
        List<String[]> records = csvManager.readAll();
        // Si salta l'header
        for (int i = 1; i < records.size(); i++) {
            String[] record = records.get(i);

            String[] luggageIds = record[7].split("\\|");
            List<Luggage> luggageList = new ArrayList<>();
            for (String luggageId : luggageIds) {
                Luggage luggage = luggageManager.getLuggageById(luggageId);
                if (luggage != null) {
                    luggageList.add(luggage);
                }
            }

            Ticket ticket = new Ticket(
                    record[0],
                    record[1],
                    record[2],
                    record[3],
                    Double.parseDouble(record[4]),
                    record[5],
                    record[6],
                    luggageList
            );
            tickets.add(ticket);
            ticket.setDocumentType(record[8]);
            ticket.setDocumentId(record[9]);
        }
    }

    public void addTicket(Ticket ticket) throws IOException {
        for (Ticket existingTicket : tickets) {
            if (existingTicket.getTicketNumber().equalsIgnoreCase(ticket.getTicketNumber())) {
                throw new IllegalArgumentException("The ticket " + ticket.getTicketNumber() + " already exists.");
            }
        }
        tickets.add(ticket);
        String luggageIdsConcatenated = String.join("|",
                ticket.getLuggageList().stream().map(Luggage::getLuggageId).toArray(String[]::new));
        String[] record = {
                ticket.getTicketNumber(),
                ticket.getBookingId(),
                ticket.getFlightNumber(),
                ticket.getSeatNumber(),
                String.valueOf(ticket.getPrice()),
                ticket.getPassengerName(),
                ticket.getPassengerSurname(),
                luggageIdsConcatenated,
                ticket.getDocumentType(),
                ticket.getDocumentId()
        };
        try {
            csvManager.appendRecord(record, csvFilePath);
        } catch (IOException e) {
            log.error("An error occurred while writing a ticket to the CSV file", e);
            throw e;
        }
    }

    public void removeTicket(String ticketId) throws IOException {
        Ticket toRemove = null;
        for (Ticket ticket : tickets) {
            if (ticket.getTicketNumber().equalsIgnoreCase(ticketId)) {
                toRemove = ticket;
                break;
            }
        }
        if (toRemove != null) {
            tickets.remove(toRemove);
            saveAllTickets();
        } else {
            System.out.println("Ticket " + ticketId + " not found.");
        }
    }

    public void updateTicket(String ticketNumber) throws IOException {
        Ticket updatedTicket = getTicketByNumber(ticketNumber);
        for (int i = 0; i < tickets.size(); i++) {
            Ticket ticket = tickets.get(i);
            if (ticket.getTicketNumber().equalsIgnoreCase(updatedTicket.getTicketNumber())) {
                tickets.set(i, updatedTicket);
                break;
            }
        }
        saveAllTickets();
    }

    private void saveAllTickets() throws IOException {
        List<String[]> records = new ArrayList<>();
        // Header
        records.add(new String[]{"ticketNumber", "bookingId", "flightNumber", "seatNumber", "price", "passengerName",
                "passengerSurname", "luggageIds", "documentType", "documentId"});
        // Dati
        for (Ticket ticket : tickets) {
            String luggageIdsConcatenated = String.join("|",
                    ticket.getLuggageList().stream().map(Luggage::getLuggageId).toArray(String[]::new));
            records.add(new String[]{
                    ticket.getTicketNumber(),
                    ticket.getBookingId(),
                    ticket.getFlightNumber(),
                    ticket.getSeatNumber(),
                    String.valueOf(ticket.getPrice()),
                    ticket.getPassengerName(),
                    ticket.getPassengerSurname(),
                    luggageIdsConcatenated,
                    ticket.getDocumentType(),
                    ticket.getDocumentId()
            });
        }
        try {
            csvManager.writeAll(records, csvFilePath);
        } catch (IOException e) {
            log.error("An error occurred while saving tickets to the CSV file: " + e.getMessage());
            throw e;
        }
    }

    public List<Ticket> getAllTickets() {
        return tickets;
    }

    public Ticket getTicketByNumber(String ticketNumber) {
        for (Ticket ticket : tickets) {
            if (ticket.getTicketNumber().equalsIgnoreCase(ticketNumber)) {
                return ticket;
            }
        }
        return null;
    }
}
