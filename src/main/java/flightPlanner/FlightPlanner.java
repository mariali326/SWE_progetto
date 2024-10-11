package flightPlanner;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FlightPlanner {
    private Map<String, Passenger> passengers;

    private FlightManager flightManager;
    private AirportManager airportManager;
    private RouteManager routeManager;
    private SeatManager seatManager;
    private BookingManager bookingManager;
    private PaymentManager paymentManager;

    private FlightSearchService flightSearchService;

    public FlightPlanner(String flightsCsv, String routesCsv, String seatsCsv, String bookingsCsv, String paymentsCsv) throws IOException {
        // Inizializza i manager
        this.airportManager = new AirportManager();
        this.routeManager = new RouteManager(routesCsv);
        this.seatManager = new SeatManager(seatsCsv);
        this.paymentManager = new PaymentManager(paymentsCsv);
        this.bookingManager = new BookingManager(bookingsCsv, new TicketManager("csv/tickets.csv"));
        this.flightManager = new FlightManager(flightsCsv);

        this.passengers = new HashMap<>();
    }

    public void addFlight(Flight flight) throws IOException {
        flightManager.addFlight(flight);
        System.out.println("Flight added: " + flight);
    }

    public void removeFlight(String stringNumber) throws IOException {
        flightManager.removeFlight(stringNumber);
        System.out.println("Flight " + stringNumber + " has been removed.");
    }

    public void addAirport(Airport airport) throws IOException {
        airportManager.addAirport(airport);
    }

    public void removeAirport(String code) throws IOException {
        airportManager.removeAirport(code);
    }

    // Notifiche
    public void updateFlightStatus(String flightNumber, LocalDateTime newDepartureTime, LocalDateTime newArrivalTime, String updateMessage, NotificationType type) throws IOException {
        flightManager.updateFlightStatus(flightNumber, newDepartureTime, newArrivalTime, updateMessage, type);
    }

    public void addBooking(Booking booking) throws IOException {
        bookingManager.addBooking(booking);
        System.out.println("Booking added: " + booking);
    }

    public void removeBooking(String bookingId) throws IOException {
        Booking booking = bookingManager.getBookingById(bookingId);
        if (booking != null) {
            bookingManager.removeBooking(bookingId);
            System.out.println("Booking " + bookingId + " removed.");
        } else {
            System.out.println("Booking " + bookingId + " not found.");
        }
    }

    public Booking findBooking(String bookingId) {
        return bookingManager.getBookingById(bookingId);
    }

    public void addRoute(Route route) throws IOException {
        routeManager.addRoute(route);
        System.out.println("Route added: " + route);
    }

    public void removeRoute(String routeId) throws IOException {
        Route route = routeManager.getRouteById(routeId);
        if (route != null) {
            routeManager.removeRoute(routeId);
            System.out.println("Route " + routeId + " removed.");
        } else {
            System.out.println("Route " + routeId + " not found.");
        }
    }

    public void addSeat(String flightNumber, Seat seat) throws IOException {
        Flight flight = flightManager.getFlightByNumber(flightNumber);
        if (flight != null) {
            seatManager.addSeat(seat);
            System.out.println("Seat added to flight: " + flightNumber);
        } else {
            System.out.println("Flight " + flightNumber + " not found.");
        }
    }

    public void removeSeat(String flightNumber, String seatId) throws IOException {
        Flight flight = flightManager.getFlightByNumber(flightNumber);
        if (flight != null) {
            seatManager.releaseSeat(seatId);
            System.out.println("Seat removed from flight: " + flightNumber);
        } else {
            System.out.println("Flight " + flightNumber + " not found.");
        }
    }

    public void addPayment(Payment payment) throws IOException {
        paymentManager.addPayment(payment);
        System.out.println("Payment added: " + payment);
    }

    public void removePayment(String paymentId) throws IOException {
        Payment payment = paymentManager.getPaymentById(paymentId);
        if (payment != null) {
            paymentManager.removePayment(paymentId);
            System.out.println("Payment " + paymentId + " removed.");
        } else {
            System.out.println("Payment " + paymentId + " not found.");
        }
    }

    public Payment findPayment(String paymentId) {
        return paymentManager.getPaymentById(paymentId);
    }

    public void addTicket(String bookingId, Ticket ticket) throws IOException {
        Booking booking = bookingManager.getBookingById(bookingId);
        if (booking != null) {
            booking.addTicket(ticket);
            bookingManager.updateBooking(booking);
            System.out.println("Ticket added to booking: " + bookingId);
        } else {
            System.out.println("Booking " + bookingId + " not found.");
        }
    }

    public void removeTicket(String bookingId, String ticketId) throws IOException {
        Booking booking = bookingManager.getBookingById(bookingId);
        if (booking != null) {
            booking.removeTicket(ticketId);
            bookingManager.updateBooking(booking);
            System.out.println("Ticket " + ticketId + " removed from booking: " + bookingId);
        } else {
            System.out.println("Booking " + bookingId + " not found.");
        }
    }

    public void updateNotificationPreferences(Passenger passenger, Set<NotificationType> types, List<NotificationChannel> channels) {
        passenger.updatePreferences(types, channels);
        System.out.println("Notification preferences updated for: " + passenger.getName());
    }

    public void initializeFlightSearchService() {
        this.flightSearchService = new FlightSearchService(flightManager, airportManager, routeManager, seatManager);
    }

    public List<Flight> findFlights(String departure, String arrival) {
        return flightSearchService.searchFlights(departure, arrival);
    }

    public FlightSearchService getFlightSearchService() {
        return flightSearchService;
    }

    public void registerPassenger(Passenger passenger) {
        passengers.put(passenger.getUsername(), passenger);
        System.out.println("Passenger registered: " + passenger.getName() + " " + passenger.getSurname());
    }

    public Passenger login(String username, String password) {
        Passenger passenger = passengers.get(username);
        if (passenger != null && passenger.getPassword().equals(password)) {
            System.out.println("Login successful for: " + passenger.getName());
            return passenger;
        } else {
            System.out.println("Invalid username or password.");
            return null;
        }
    }

    // Metodo per aggiornare le preferenze di notifica di un passeggero
    public void updateNotificationPreferences(Passenger passenger, NotificationPreferences preferences) {
        passenger.setPreferences(preferences);
        System.out.println("Notification preferences updated for: " + passenger.getName());
    }

}

