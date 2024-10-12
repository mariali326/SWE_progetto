package flightPlanner;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

public class FlightPlanner {
    private FlightManager flightManager;
    private AirportManager airportManager;
    private RouteManager routeManager;
    private SeatManager seatManager;
    private BookingManager bookingManager;
    private PaymentManager paymentManager;
    private PassengerManager passengerManager;
    private TicketManager ticketManager;

    private FlightSearchService flightSearchService;

    public FlightPlanner() throws IOException {
        // Inizializza i manager
        this.flightManager = new FlightManager();
        this.airportManager = new AirportManager();
        this.routeManager = new RouteManager();
        this.seatManager = new SeatManager();
        this.paymentManager = new PaymentManager();
        this.passengerManager = new PassengerManager(flightManager);
        this.ticketManager = new TicketManager();
        this.bookingManager = new BookingManager(flightManager,ticketManager);
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

    public void registerPassenger(Passenger passenger) throws IOException {
        passengerManager.registerPassenger(passenger);
    }

    public List<Passenger> getPassengers() {
        return passengerManager.getAllPassengers();
    }

    public List<Flight> getAllFlights(){
        return flightManager.getAllFlights();
    }

    // Metodo per aggiornare le preferenze di notifica di un passeggero
    public void updateNotificationPreferences(Passenger passenger, NotificationPreferences preferences) {
        passenger.setPreferences(preferences);
        System.out.println("Notification preferences updated for: " + passenger.getName());
    }

    public void updatePassengerNotificationPreferences(String username, Set<NotificationType> types, List<NotificationChannel> channels) {
        Passenger passenger = passengerManager.getPassengerByUsername(username);
        if (passenger != null) {
            passengerManager.updateNotificationPreferences(passenger, types, channels);
            System.out.println("Notification preferences updated for passenger: " + username);
        } else {
            System.out.println("Passenger not found.");
        }
    }

    public void assignSeatsForBooking(String bookingId) throws IOException {
        Booking booking = bookingManager.getBookingById(bookingId);
        if (booking != null) {
            for (Ticket ticket : booking.getTickets()) {
                Seat availableSeat = seatManager.findAvailableSeat(booking.getFlightNumber());
                if (availableSeat != null) {
                    seatManager.assignSeatToPassenger(availableSeat.getSeatNumber(), passengerManager.getPassengerByUsername(booking.getPassengerUsername()));
                    System.out.println("Seat " + availableSeat.getSeatNumber() + " assigned.");
                } else {
                    System.out.println("No available seats.");
                }
            }
        } else {
            System.out.println("Booking not found.");
        }
    }

    public void assignSeatToPassenger(String flightNumber, String username) throws IOException {
        Passenger passenger = passengerManager.getPassengerByUsername(username);
        if (passenger != null) {
            seatManager.assignSeatToPassenger(flightNumber, passenger);
        } else {
            System.out.println("Passenger not found: " + username);
        }
    }

    public void bookFlightForPassenger(String flightNumber, Passenger passenger, List<Ticket> tickets) throws IOException {
        Passenger existingPassenger = passengerManager.getPassengerByUsername(passenger.getUsername());
        if (existingPassenger == null) {
            passengerManager.registerPassenger(passenger);
            System.out.println("Passenger registered: " + passenger.getName());
        }

        Flight flight = flightManager.getFlightByNumber(flightNumber);
        if (flight != null) {
            // Calcolare la quantità totale da pagare in base ai biglietti
            double totalAmount = calculateTotalAmount(tickets);

            String bookingId = UUID.randomUUID().toString();

            Booking booking = new Booking(bookingId, passenger.getUsername(), flightNumber, LocalDateTime.now(), tickets, totalAmount);
            bookingManager.addBooking(booking);
            System.out.println("Flight booked for passenger: " + passenger.getName());

            for (Ticket ticket : tickets) {
                seatManager.assignSeatToPassenger(ticket.getSeatNumber(), passenger);
            }
        } else {
            System.out.println("Flight not found.");
        }
    }

    private double calculateTotalAmount(List<Ticket> tickets) {
        double total = 0.0;
        for (Ticket ticket : tickets) {
            total += ticket.getPrice();
        }
        return total;
    }

    public void cancelBooking(String bookingId, Passenger passenger) throws IOException {
        Booking booking = bookingManager.getBookingById(bookingId);
        if (booking != null) {
            // Disiscrivere un passeggero dalle notifiche di un volo
            passengerManager.unregisterFromFlight(passenger, booking.getFlightNumber());

            // Liberare i posti
            for (Ticket ticket : booking.getTickets()) {
                seatManager.releaseSeat(ticket.getSeatNumber());
            }

            bookingManager.removeBooking(bookingId);
            System.out.println("Booking canceled: " + bookingId);
        } else {
            System.out.println("Booking not found.");
        }
    }
}

