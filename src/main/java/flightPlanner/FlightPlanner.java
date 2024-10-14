package flightPlanner;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private Map<String, Map<String, Double>> flightClassPrices;

    public FlightPlanner() throws IOException {
        // Si inizializzano i manager
        this.flightManager = new FlightManager();
        this.passengerManager = new PassengerManager(flightManager);
        this.ticketManager = new TicketManager();
        this.bookingManager = new BookingManager(flightManager, passengerManager, ticketManager);
        this.airportManager = new AirportManager();
        this.routeManager = new RouteManager();
        this.seatManager = new SeatManager();
        this.paymentManager = new PaymentManager();
        this.flightSearchService = new FlightSearchService(flightManager, airportManager);


        flightClassPrices = new HashMap<>();
        // Prezzi per le varie classi di ogni volo
        addFlightClassPrices("F001", 200.0, 500.0, 700.0);
        addFlightClassPrices("F002", 100.0, 300.0, 500.0);
        addFlightClassPrices("F003", 260.0, 560.0, 760.0);
        addFlightClassPrices("F004", 575.0, 775.0, 975.0);
        addFlightClassPrices("F005", 550.0, 750.0, 950.0);
        addFlightClassPrices("F006", 160.0, 360.0, 560.0);
    }

    public void addFlightClassPrices(String flightNumber, double economyPrice, double businessPrice, double firstClassPrice) {
        Map<String, Double> classPrices = new HashMap<>();
        classPrices.put("Economy", economyPrice);
        classPrices.put("Business", businessPrice);
        classPrices.put("First", firstClassPrice);
        flightClassPrices.put(flightNumber, classPrices);
    }

    public Map<String, Map<String, Double>> getFlightClassPrices() {
        return flightClassPrices;
    }

    public void updateFlightClassPrices(String flightNumber, String classType, Double newPrice) {
        if (flightClassPrices.containsKey(flightNumber)) {
            Map<String, Double> classPrices = flightClassPrices.get(flightNumber);
            if (classPrices.containsKey(classType)) {
                classPrices.put(classType, newPrice);
//                System.out.println("Updated prices for flight " + flightNumber + ": " + classPrices);
                System.out.println("Updated price for flight: " + flightNumber + ", Class: " + classType + ", New Price: " + newPrice + " EUR");
            } else {
                System.out.println("Class type " + classType + " not found for flight " + flightNumber);
            }
        } else {
            System.out.println("Flight number " + flightNumber + " not found.");
        }
    }

    public double getPrice(String flightNumber, String selectedClassType) {
        if (flightClassPrices.containsKey(flightNumber)) {
            Map<String, Double> classPrices = flightClassPrices.get(flightNumber);
            for (String classType : classPrices.keySet()) {
                if (classType.equalsIgnoreCase(selectedClassType)) {
                    //System.out.println("check " + classType + " " + classPrices.get(classType));
                    return classPrices.get(classType);
                }
            }
            throw new IllegalArgumentException("Selected class type not available for this flight.");

        } else {
            throw new IllegalArgumentException("Flight number not found.");
        }
    }

    public boolean checkFlightExistence(String flightNumber) {
        Flight flight = flightManager.getFlightByNumber(flightNumber);
        return flight != null;
    }

    public void addFlight(Flight flight) throws IOException {
        flightManager.addFlight(flight);
        System.out.println("Flight added: " + flight);
    }

    public void removeFlight(String stringNumber) throws IOException {
        flightManager.removeFlight(stringNumber);
        System.out.println("Flight " + stringNumber + " has been removed.");
    }

    public List<Flight> getAllFlights() {
        return flightManager.getAllFlights();
    }

    public Flight findFlight(String flightNumber) {
        return flightManager.getFlightByNumber(flightNumber);
    }

    // Notifiche
    public void updateFlightStatus(String flightNumber, LocalDateTime newDepartureTime, LocalDateTime newArrivalTime, String updateMessage, NotificationType type) throws IOException {
        flightManager.updateFlightStatus(flightNumber, newDepartureTime, newArrivalTime, updateMessage, type);
    }

    public void addAirport(Airport airport) throws IOException {
        airportManager.addAirport(airport);
    }

    public void removeAirport(String code) throws IOException {
        airportManager.removeAirport(code);
    }

    public List<Airport> getAllAirports() {
        return airportManager.getAllAirports();
    }

    public Booking findBooking(String bookingId) {
        return bookingManager.getBookingById(bookingId);
    }

    public boolean checkBookingExistence(String bookingId) {
        Booking booking = bookingManager.getBookingById(bookingId);
        return booking != null;
    }

    public void bookFlightForPassenger(String flightNumber, Passenger passenger, List<Ticket> tickets, String bookingId) throws IOException {
        Passenger existingPassenger = passengerManager.getPassengerByUsername(passenger.getUsername());
        if (existingPassenger == null) {
            passengerManager.registerPassenger(passenger);
            System.out.println("Passenger registered: " + passenger.getUsername() + " " + passenger.getName() + " " + passenger.getSurname());
        }

        Flight flight = flightManager.getFlightByNumber(flightNumber);
        if (flight != null) {
            // Calcolare la quantità totale da pagare in base ai biglietti
            passenger.registerForFlight(flight);
            double totalAmount = calculateTotalAmount(tickets);

            Booking booking = new Booking(bookingId, passenger.getUsername(), flightNumber, LocalDateTime.now(), tickets, totalAmount);
            bookingManager.addBooking(booking);
            System.out.println("Flight booked for passenger: " + passenger.getUsername() + " " + passenger.getName() + " "
                    + passenger.getSurname() +
                    ". Total amount to pay is " + totalAmount + " EUR.");

            tickets.stream().skip(1).forEach(ticket -> {
                String name = ticket.getPassengerName();
                String surname = ticket.getPassengerSurname();
                Passenger additionalPassenger = passengerManager.getPassengerByFullName(name, surname);
                if (additionalPassenger != null) {
                    additionalPassenger.registerForFlight(flight);
                } else {
                    System.out.println("Passenger not found: " + name + " " + surname);
                }
            });
        } else {
            System.out.println("Flight not found.");
        }
    }

    public void cancelBooking(String bookingId, Passenger passenger) throws IOException {
        Booking booking = bookingManager.getBookingById(bookingId);
        double refundAmount = 0.0;
        if (booking != null) {
            // Disiscrivere un passeggero dalle notifiche di un volo
            passengerManager.unregisterFromFlight(passenger, booking.getFlightNumber());

            // Liberare i posti
            for (Ticket ticket : booking.getTickets()) {
                refundAmount += ticket.getPrice() * 0.40;
                seatManager.releaseSeat(ticket.getSeatNumber(), ticket.getFlightNumber());
                ticketManager.removeTicket(ticket.getTicketNumber());
            }

            bookingManager.removeBooking(bookingId);
            System.out.println("Booking canceled: " + bookingId);
            System.out.println("Refund of 40% (" + refundAmount + " EUR) for booking " + bookingId + " has been processed.");
        } else {
            System.out.println("Booking not found.");
        }
    }

    public List<Booking> getBookingsForPassenger(String username) {
        return bookingManager.getBookingsForPassenger(username);
    }

    public Booking getBookingById(String bookingId) {
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

    public Route getRouteByAirportsCode(String departureCode, String arrivalCode) {
        return routeManager.getRouteByAirportsCode(departureCode, arrivalCode);
    }

    public void bookSeat(String flightNumber, String seatNumber) throws IOException {
        Flight flight = flightManager.getFlightByNumber(flightNumber);
        if (flight != null) {
            seatManager.bookSeat(flightNumber, seatNumber);
        }
    }

    public void releaseSeat(String flightNumber, String seatId) throws IOException {
        Flight flight = flightManager.getFlightByNumber(flightNumber);
        if (flight != null) {
            seatManager.releaseSeat(seatId, flightNumber);
            System.out.println("Seat released from flight: " + flightNumber);
        } else {
            System.out.println("Flight " + flightNumber + " not found.");
        }
    }

    public Map<String, String> getAvailableSeatsWithClass(String flightNumber) {
        return seatManager.getAvailableSeatsWithClassType(flightNumber);
    }

    // Usato se la prenotazione viene terminata o cancellata prima di portare a buon fine
    public void releaseSeatsForCurrentBooking(String flightNumber, Map<String, String> currentBookingSeats) throws IOException {
        for (Map.Entry<String, String> entry : currentBookingSeats.entrySet()) {
            String passengerName = entry.getKey();
            String seat = entry.getValue();
            System.out.println("Releasing seat for " + passengerName + ": " + seat);
            releaseSeat(flightNumber, seat);
        }
    }

    public void addSeatToFlight(String flightNumber, Seat seat) throws IOException {
        Flight flight = flightManager.getFlightByNumber(flightNumber);
        if (flight != null) {
            seatManager.addSeat(seat);
        } else {
            throw new IllegalArgumentException("Flight " + flightNumber + " not found. Cannot add seat " + seat.getSeatNumber() + ". ");
        }
    }

    public void removeSeatFromFlight(String flightNumber, String seatNumber) throws IOException {
        Flight flight = flightManager.getFlightByNumber(flightNumber);
        Seat seat = seatManager.getSeatByNumber(seatNumber, flightNumber);
        if (flight != null) {
            if (seat != null) {
                seatManager.removeSeats(seat);
            } else {
                throw new IllegalArgumentException("Seat " + seatNumber + " not found. ");
            }
        } else {
            throw new IllegalArgumentException("Flight " + flightNumber + " not found.");
        }
    }

    public void addPayment(Payment payment) throws IOException {
        paymentManager.addPayment(payment);
        System.out.println("Payment added: " + payment);
    }

//    public void removePayment(String paymentId) throws IOException {
//        Payment payment = paymentManager.getPaymentById(paymentId);
//        if (payment != null) {
//            paymentManager.removePayment(paymentId);
//            System.out.println("Payment " + paymentId + " removed.");
//        } else {
//            System.out.println("Payment " + paymentId + " not found.");
//        }
//    }

    public void addTicket(Ticket ticket) throws IOException {
        ticketManager.addTicket(ticket);
    }

    public void cancelTicket(String bookingId, Ticket ticket) throws IOException {
        Booking booking = findBooking(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("Booking ID " + bookingId + " not found.");
        }

        // Rimuovere il biglietto dalla prenotazione
        List<Ticket> tickets = booking.getTickets();
        String passengerName = ticket.getPassengerName();
        String passengerSurname = ticket.getPassengerSurname();
        double ticketPrice = ticket.getPrice();
        boolean removed = tickets.remove(ticket);  // Viene rimosso il biglietto dalla lista

        if (!removed) {
            throw new IllegalArgumentException("Ticket " + ticket.getTicketNumber() + " not found in booking " + bookingId);
        } else {
            booking.setTotalAmount(booking.getTotalAmount() - ticketPrice);
            System.out.println("Refund of 40% (" + ticketPrice * 0.40 + " EUR) for ticket " + ticket.getTicketNumber() +
                    " from booking " + bookingId + " has been processed.");

            seatManager.releaseSeat(ticket.getSeatNumber(), ticket.getFlightNumber());
            ticketManager.removeTicket(ticket.getTicketNumber());
            Flight flight = flightManager.getFlightByNumber(booking.getFlightNumber());
            if (flight != null) {
                passengerManager.unregisterFromFlight(passengerManager.getPassengerByFullName(passengerName, passengerSurname), flight.getFlightNumber());
            } else {
                System.out.println("Flight " + ticket.getFlightNumber() + " not found .");
            }

            System.out.println("Ticket number " + ticket.getTicketNumber() + " canceled from " + bookingId + ". ");
        }

        // Si controlla se la prenotazione è ancora valida (ha altri biglietti)
        if (tickets.isEmpty()) {
            // Se non ci sono più biglietti, viene cancellato l'intera prenotazione
            Passenger passenger = passengerManager.getPassengerByUsername(booking.getPassengerUsername());
            cancelBooking(bookingId, passenger);
        } else {
            // Si aggiorna la prenotazione con i biglietti rimanenti
            bookingManager.updateBooking(booking);
        }
    }

    public Ticket findTicket(String bookingId, String ticketNumber) {
        Booking booking = findBooking(bookingId);
        if (booking == null) {
            System.out.println("Booking ID " + bookingId + " not found");
            return null;
        }
        return booking.getTickets().stream()
                .filter(ticket -> ticket.getTicketNumber().equalsIgnoreCase(ticketNumber))
                .findFirst()
                .orElse(null);

    }

    private double calculateTotalAmount(List<Ticket> tickets) {
        double total = 0.0;
        for (Ticket ticket : tickets) {
            total += ticket.getPrice();
        }
        return total;
    }

    public boolean checkPassengerExistence(Passenger passenger) {
        List<Passenger> passengers = passengerManager.getAllPassengers();
        for (Passenger p : passengers) {
            if (p.getUsername().equalsIgnoreCase(passenger.getUsername()) && p.getSurname().equalsIgnoreCase(passenger.getSurname())
                    && p.getName().equalsIgnoreCase(passenger.getName())) {
                return true;
            }
        }
        return false;
    }

    public void registerPassenger(Passenger passenger) throws IOException {
        passengerManager.registerPassenger(passenger);
    }

    public void removePassenger(Passenger passenger) throws IOException {
        passengerManager.unregisterPassenger(passenger.getUsername());
    }

    public void updatePassenger(Passenger passenger) throws IOException {
        passengerManager.updatePassenger(passenger);
    }

    public List<Passenger> getPassengers() {
        return passengerManager.getAllPassengers();
    }

    public Passenger getPassenger(String username) {
        List<Passenger> passengers = passengerManager.getAllPassengers();
        for (Passenger passenger : passengers) {
            if (passenger.getUsername().equals(username)) {
                return passenger;
            }
        }
        return null;
    }

    public void updatePassengerNotificationPreferences(String username, Set<NotificationType> types, List<NotificationChannel> channels) throws IOException {
        Passenger passenger = passengerManager.getPassengerByUsername(username);
        if (passenger != null) {
            passengerManager.updateNotificationPreferences(passenger, types, channels);
        } else {
            System.out.println("Passenger not found.");
        }
    }

    public List<Flight> findFlights(String departure, String arrival, LocalDate flightDate) {
        return flightSearchService.searchFlights(departure, arrival, flightDate);
    }

}

