package flightPlanner;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TicketManager {
    private CSVManager csvManager;
    private List<Ticket> tickets;

    public TicketManager(String csvFilePath) throws IOException {
        // Carica il file CSV dal classpath
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(csvFilePath);
        if (inputStream == null) {
            throw new FileNotFoundException("File not found in resources: " + csvFilePath);
        }
        this.csvManager = new CSVManager(new InputStreamReader(inputStream));
        this.tickets = new ArrayList<>();
        loadTickets();
    }

    private void loadTickets() throws IOException {
        List<String[]> records = csvManager.readAll();
        // Salta l'header
        for (int i = 1; i < records.size(); i++) {
            String[] record = records.get(i);
            Ticket ticket = new Ticket(
                    record[0],
                    record[1],
                    record[2],
                    record[3],
                    Double.parseDouble(record[4])
            );
            tickets.add(ticket);
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

    public void addTicket(Ticket ticket) throws IOException {
        tickets.add(ticket);
        String[] record = {
                ticket.getTicketNumber(),
                ticket.getBookingId(),
                ticket.getFlightNumber(),
                ticket.getSeatNumber(),
                String.valueOf(ticket.getPrice())
        };
        csvManager.appendRecord(record);
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
        }
    }

    public void updateTicket(Ticket updatedTicket) throws IOException {
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
        records.add(new String[]{"ticketNumber", "bookingId", "flightNumber", "seatNumber", "price"});
        // Dati
        for (Ticket ticket : tickets) {
            records.add(new String[]{
                    ticket.getTicketNumber(),
                    ticket.getBookingId(),
                    ticket.getFlightNumber(),
                    ticket.getSeatNumber(),
                    String.valueOf(ticket.getPrice())
            });
        }
        csvManager.writeAll(records);
    }
}
