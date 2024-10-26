package flightPlanner;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FlightPlanner {
    private final FlightManager flightManager;
    private final AirportManager airportManager;
    private final RouteManager routeManager;
    private final SeatManager seatManager;
    private final BookingManager bookingManager;
    private final PaymentManager paymentManager;
    private final PassengerManager passengerManager;
    private final TicketManager ticketManager;
    private final LuggageManager luggageManager;

    private final FlightSearchService flightSearchService;

    private final Map<String, Map<String, Double>> flightClassPrices;

    public FlightPlanner() throws IOException {
        // Si inizializzano i manager
        this.flightManager = new FlightManager();
        this.passengerManager = new PassengerManager(flightManager);
        this.luggageManager = new LuggageManager();
        this.ticketManager = new TicketManager(luggageManager);
        this.bookingManager = new BookingManager(flightManager, passengerManager, ticketManager);
        this.airportManager = new AirportManager();
        this.routeManager = new RouteManager();
        this.seatManager = new SeatManager();
        this.paymentManager = new PaymentManager();
        this.flightSearchService = new FlightSearchService(flightManager, airportManager);


        flightClassPrices = new HashMap<>();
        // Prezzi per le varie classi di ogni volo
        addFlightClassPrices("F001", 500.0, 1000.0, 1500.0);
        addFlightClassPrices("F002", 250.0, 750.0, 1250.0);
        addFlightClassPrices("F003", 760.0, 1260.0, 1760.0);
        addFlightClassPrices("F004", 820.0, 1320.0, 1820.0);
        addFlightClassPrices("F005", 1000.0, 1500.0, 2000.0);
        addFlightClassPrices("F006", 270.0, 770.0, 1270.0);
    }

    public void addFlightClassPrices(String flightNumber, double economyPrice, double businessPrice, double firstClassPrice) {
        Map<String, Double> classPrices = new HashMap<>();
        classPrices.put("Economy", economyPrice);
        classPrices.put("Business", businessPrice);
        classPrices.put("First", firstClassPrice);
        flightClassPrices.put(flightNumber, classPrices);
    }

    public void addFlightClassPrice(String flightNumber, String classType, double classPrice) {
        Map<String, Double> classPrices;
        if (getFlightClassPrices().containsKey(flightNumber)) {
            // Si riprende la mappa esistente di prezzi per il volo specifico
            classPrices = flightClassPrices.get(flightNumber);
            //System.out.println("Uploaded flightClassPrices: " + getFlightClassPrices());
        } else {
            // Se il volo non esiste, viene creato una nuova mappa di prezzi
            classPrices = new HashMap<>();
            //System.out.println("Added new flight with class prices: " + getFlightClassPrices());
        }
        classPrices.put(classType, classPrice);
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
                throw new IllegalArgumentException("Class type " + classType + " not found for flight " + flightNumber);
            }
        } else {
            throw new IllegalArgumentException("Flight number " + flightNumber + " not found.");
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
            throw new IllegalArgumentException("Flight number " + flightNumber + " not found.");
        }
    }

    public boolean checkFlightNotExistence(String flightNumber) {
        Flight flight = flightManager.getFlightByNumber(flightNumber);
        return flight == null;
    }

    public void addFlight(Flight flight) throws IOException {
        flightManager.addFlight(flight);
        System.out.println("Flight added: " + flight);
    }

    public void removeFlight(String flightNumber) throws IOException {
        Flight flight = findFlight(flightNumber);
        List<Seat> seats = seatManager.getAllSeats();
        List<Booking> bookings = bookingManager.getAllBookings();
        if (flight != null) {
            // Rimuovere anche tutti i posti e tutte le prenotazioni associate a quel volo
            for (int i = seats.size() - 1; i >= 0; i--) {
                Seat seat = seats.get(i);
                if (seat.getFlightNumber().equalsIgnoreCase(flightNumber)) {
                    removeSeatFromFlight(flightNumber, seat.getSeatNumber());
                }
            }

            notifyCancellationToPassengers(flightNumber);

            for (int i = bookings.size() - 1; i >= 0; i--) {
                Booking booking = bookings.get(i);
                if (booking.getFlightNumber().equalsIgnoreCase(flightNumber)) {
                    cancelBooking(booking.getBookingId(), getPassenger(booking.getPassengerUsername()));
                }
            }
            flightManager.removeFlight(flightNumber);
            System.out.println("Flight " + flightNumber + " has been removed.");
        } else {
            throw new IllegalArgumentException("Flight " + flightNumber + " not found.");
        }
    }

    public void notifyCancellationToPassengers(String flightNumber) {
        Flight flight = findFlight(flightNumber);
        List<Observer> observers = flight.getObservers();
        for (Observer observer : observers) {
            if (observer instanceof Passenger passenger) {
                for (Booking booking : getBookingsForPassenger(passenger.getUsername())) {
                    double refundAmount = booking.getTotalAmount();
                    passenger.update("Sorry for the inconvenience, the flight " + flightNumber + " is cancelled. Your refund of " +
                            refundAmount + " EUR including luggage cost for the booking " + booking.getBookingId() + " is processed automatically.", NotificationType.CANCELLATION);
                }
            }
        }
    }

    public List<Flight> getAllFlights() {
        return flightManager.getAllFlights();
    }

    public Flight findFlight(String flightNumber) {
        return flightManager.getFlightByNumber(flightNumber);
    }

    // Notifiche
    // Aggiornare lo stato di un volo implica aggiornare anche la corrispondente rotta
    public void updateFlightStatus(String flightNumber, LocalDateTime newDepartureTime, LocalDateTime newArrivalTime, String updateMessage, NotificationType type) throws IOException {
        flightManager.updateFlightStatus(flightNumber, newDepartureTime, newArrivalTime, updateMessage, type);
        Flight flight = findFlight(flightNumber);
        Route route = getRouteByAirportsCode(flight.getDepartureAirportCode(), flight.getArrivalAirportCode());
        updateRoute(route, newDepartureTime, newArrivalTime);
    }

    public void addAirport(Airport airport) throws IOException {
        airportManager.addAirport(airport);
    }

    public void removeAirport(String code) throws IOException {
        Airport airport = airportManager.getAirportByCode(code);
        if (airport != null) {
            List<Route> routes = routeManager.getAllRoutes();
            // Rimuovere tutte le rotte e i voli associati all'aeroporto rimosso
            for (int i = routes.size() - 1; i >= 0; i--) { // Si itera in modo inverso per evitare ConcurrentModificationException
                Route route = routes.get(i);
                if (route.getDepartureAirportCode().equalsIgnoreCase(code) || route.getArrivalAirportCode().equalsIgnoreCase(code)) {
                    removeRoute(route.getRouteId());
                }
            }
            airportManager.removeAirport(code);
            System.out.println("Airport " + code + " removed.");
        } else {
            throw new IllegalArgumentException("Airport " + code + " not found.");
        }
    }

    public List<Airport> getAllAirports() {
        return airportManager.getAllAirports();
    }

    public boolean checkAirportNotExistence(String code) {
        Airport airport = airportManager.getAirportByCode(code);
        return airport == null;
    }

    public Booking findBooking(String bookingId) {
        return bookingManager.getBookingById(bookingId);
    }

    public boolean checkBookingNotExistence(String bookingId) {
        Booking booking = bookingManager.getBookingById(bookingId);
        return booking == null;
    }

    public void bookFlightForPassenger(String flightNumber, Passenger passenger, List<Passenger> additionalPassengers, List<Ticket> tickets, String bookingId) throws IOException {
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

            for (Passenger p : additionalPassengers) {
                p.registerForFlight(flight);
            }

        } else {
            throw new IllegalArgumentException("Flight "+ flightNumber + " not found.");
        }
    }

    public void cancelBooking(String bookingId, Passenger passenger) throws IOException {
        Booking booking = bookingManager.getBookingById(bookingId);
        if (booking != null) {
            // Disiscrivere il passeggero principale dalle notifiche di un volo
            passengerManager.unregisterFromFlight(passenger, booking.getFlightNumber());
            List<Ticket> tickets = booking.getTickets();
            Ticket firstTicket = tickets.getFirst();
            seatManager.releaseSeat(firstTicket.getSeatNumber(), firstTicket.getFlightNumber());
            removeLuggage(firstTicket.getLuggageList());
            ticketManager.removeTicket(firstTicket.getTicketNumber());

            // Liberare i posti, cancellare tutti i bagagli e biglietti prenotati e disiscrivere i passeggeri della stessa prenotazione dal volo
            tickets.stream().skip(1).forEach(ticket -> {
                String additionalPassengerName = ticket.getPassengerName();
                String additionalPassengerSurname = ticket.getPassengerSurname();
                String documentType = ticket.getDocumentType();
                String documentId = ticket.getDocumentId();
                Passenger additionalPassenger = getPassengerByFullNameAndDocument(additionalPassengerName, additionalPassengerSurname,
                        documentType, documentId);
                passengerManager.unregisterFromFlight(additionalPassenger, booking.getFlightNumber());
                try {
                    seatManager.releaseSeat(ticket.getSeatNumber(), ticket.getFlightNumber());
                    removeLuggage(ticket.getLuggageList());
                    ticketManager.removeTicket(ticket.getTicketNumber());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            bookingManager.removeBooking(bookingId);
            System.out.println("Booking canceled: " + bookingId);

        } else {
            throw new IllegalArgumentException("Booking " + bookingId +" not found.");
        }
    }

    public List<Booking> getBookingsForPassenger(String username) {
        return bookingManager.getBookingsForPassenger(username);
    }

    public List<Booking> getAllBookings(){
        return bookingManager.getAllBookings();
    }

    public void addRoute(Route route) throws IOException {
        routeManager.addRoute(route);
        System.out.println("Route added: " + route);
    }

    public void removeRoute(String routeId) throws IOException {
        Route route = routeManager.getRouteById(routeId);
        List<Flight> flights = getAllFlights();
        if (route != null) {
            // Rimuovere tutti i voli associati alla rotta cancellata
            for (int i = flights.size() - 1; i >= 0; i--) {
                Flight flight = flights.get(i);
                if (flight.getDepartureAirportCode().equalsIgnoreCase(route.getDepartureAirportCode()) &&
                        flight.getArrivalAirportCode().equalsIgnoreCase(route.getArrivalAirportCode())) {
                    removeFlight(flight.getFlightNumber());
                }
            }
            routeManager.removeRoute(routeId);
            System.out.println("Route " + routeId + " removed.");
        } else {
            throw new IllegalArgumentException("Route " + routeId + " not found.");
        }
    }

    public void updateRoute(Route route, LocalDateTime newDepartureTime, LocalDateTime newArrivalTime) throws IOException {
        Duration newDuration = Duration.between(newDepartureTime, newArrivalTime);
        route.setFlightDuration(newDuration);
        routeManager.updateRoute(route);
    }

    public Route getRouteByAirportsCode(String departureCode, String arrivalCode) {
        return routeManager.getRouteByAirportsCode(departureCode, arrivalCode);
    }

    public List<Route> getAllRoutes(){
        return routeManager.getAllRoutes();
    }

    public boolean checkRouteNotExistence(String routeId) {
        Route route = routeManager.getRouteById(routeId);
        return route == null;
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
            System.out.println("Seat " + seatId + " released from flight " + flightNumber);
        } else {
            throw new IllegalArgumentException("Flight " + flightNumber + " not found.");
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

    public String getSeatClass(String seatNumber, String flightNumber) {
        Seat seat = seatManager.getSeatByNumber(seatNumber, flightNumber);
        if (seat != null) {
            return seat.getClassType();
        }
        return null;
    }

    public int getSeatClassCountsForFlight(String flightNumber, String classType) {
        List<Seat> seats = seatManager.getAllSeats();
        int seatCount = 0;
        for (Seat seat : seats) {
            if (seat.getFlightNumber().equalsIgnoreCase(flightNumber) && seat.getClassType().equalsIgnoreCase(classType)) {
                seatCount++;
            }
        }
        System.out.println("Flight " + flightNumber + " has already registered " + seatCount + " " + classType + " seats in CSV file.");
        return seatCount;
    }

    public boolean checkSeatNotExistence(String seatNumber, String flightNumber) {
        Seat seat = seatManager.getSeatByNumber(seatNumber, flightNumber);
        return seat == null;
    }

    public void addPayment(Payment payment) throws IOException {
        paymentManager.addPayment(payment);
        System.out.println("Payment added: " + payment);
    }

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
        String documentType = ticket.getDocumentType();
        String documentId = ticket.getDocumentId();
        double ticketPrice = ticket.getPrice();
        double luggagePrice = 0;

        for (Luggage luggage : ticket.getLuggageList()) {
            luggagePrice += luggage.getCost();
        }

        if (tickets.size() > 1) {
            boolean removed = tickets.remove(ticket);  // Viene rimosso il biglietto dalla lista

            if (!removed) {
                throw new IllegalArgumentException("Ticket " + ticket.getTicketNumber() + " not found in booking " + bookingId);
            } else {
                booking.setTotalAmount(booking.getTotalAmount() - ticketPrice);
                System.out.println("Refund of 40% (" + (ticketPrice - luggagePrice) * 0.40 + " EUR, excluding luggage cost) for ticket " + ticket.getTicketNumber() +
                        " from booking " + bookingId + " has been processed.");

                seatManager.releaseSeat(ticket.getSeatNumber(), ticket.getFlightNumber());
                removeLuggage(ticket.getLuggageList());
                ticketManager.removeTicket(ticket.getTicketNumber());
                Flight flight = flightManager.getFlightByNumber(booking.getFlightNumber());
                if (flight != null) {
                    Passenger passenger = passengerManager.getPassengerByFullNameAndDocument(passengerName, passengerSurname, documentType, documentId);
                    passengerManager.unregisterFromFlight(passenger, flight.getFlightNumber());
                } else {
                    throw new IllegalArgumentException("Flight " + ticket.getFlightNumber() + " not found .");
                }

                System.out.println("Ticket number " + ticket.getTicketNumber() + " canceled from " + bookingId + ". ");
                bookingManager.updateBooking(booking);
            }
        } else {
            Passenger passenger = passengerManager.getPassengerByUsername(booking.getPassengerUsername());
            cancelBooking(bookingId, passenger);
        }
    }

    public void cancelCurrentTickets(List<Ticket> currentTickets) throws IOException {
        for (int i = currentTickets.size() - 1; i >= 0; i--) {
            Ticket ticket = currentTickets.get(i);
            boolean removed = currentTickets.remove(ticket);

            if (!removed) {
                throw new IllegalArgumentException("Ticket " + ticket.getTicketNumber() + " not found.");
            } else {
                ticketManager.removeTicket(ticket.getTicketNumber());
                System.out.println("Ticket number " + ticket.getTicketNumber() + " canceled because the passenger didn't finish the booking process.");
            }
        }
    }

    // Usato quando si effettua un pagamento per cambiare posto
    public void updateTicketSeatPrice(String ticketNumber, String newSeatNumber, double newSeatPrice) throws IOException {
        Ticket ticket = ticketManager.getTicketByNumber(ticketNumber);
        double originalTicketPrice = ticket.getPrice();
        String originalClassType = getSeatClass(ticket.getSeatNumber(), ticket.getFlightNumber());
        double originalSeatPrice = getPrice(ticket.getFlightNumber(),originalClassType);
        double newPrice = originalTicketPrice - originalSeatPrice + newSeatPrice;

        Booking booking = bookingManager.getBookingById(ticket.getBookingId());
        if (booking != null) {
            ticket.setPrice(newPrice);
            ticket.setSeatNumber(newSeatNumber);
            ticketManager.updateTicket(ticketNumber);
            booking.setTotalAmount(booking.getTotalAmount() - originalTicketPrice + newPrice);
            bookingManager.updateBooking(booking);
        }
    }

    // Usato quando si effettua un pagamento per aggiungere dei bagagli
    public void updateTicketPrice(String ticketNumber, double newPrice) throws IOException {
        Ticket ticket = ticketManager.getTicketByNumber(ticketNumber);
        if (ticket != null) {
            double originalPrice = ticket.getPrice();
            Booking booking = bookingManager.getBookingById(ticket.getBookingId());
            ticket.setPrice(newPrice);
            ticketManager.updateTicket(ticketNumber);
            booking.setTotalAmount(booking.getTotalAmount() - originalPrice + newPrice);
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
                    && p.getName().equalsIgnoreCase(passenger.getName()) && p.getEmail().equalsIgnoreCase(passenger.getEmail())) {
                return true;
            }
        }
        return false;
    }

    public Passenger findDuplicatePassenger(String name, String surname, String documentType, String documentId) {
        List<Passenger> passengers = getPassengers();
        for (Passenger passenger : passengers) {
            if (passenger.getName().equals(name) &&
                    passenger.getSurname().equals(surname) &&
                    passenger.getUsername().equals("not defined") && passenger.getDocumentType().equals(documentType)
                    && passenger.getDocumentId().equals(documentId)) {
                return passenger;  // Trovato duplicato con informazioni incomplete
            }
        }
        return null;  // Nessun duplicato trovato
    }

    public boolean checkDuplicatePassenger(String name, String surname, String documentType, String documentId) {
        Passenger duplicatePassenger = findDuplicatePassenger(name, surname, documentType, documentId);
        int count = 0;
        for (Passenger passenger : getPassengers()) {
            if (duplicatePassenger != null && passenger.getName().equals(duplicatePassenger.getName()) && passenger.getSurname().equals(duplicatePassenger.getSurname())
                    && passenger.getDocumentType().equals(documentType)
                    && passenger.getDocumentId().equals(documentId)) {
                count++;
            }
        }
        return count > 1;
    }

    public void removeDuplicatePassenger(String username, String name, String surname, String documentType, String documentId) throws IOException {
        Passenger currentPassenger = getPassenger(username);
        Passenger duplicatePassenger = findDuplicatePassenger(name, surname, documentType, documentId);
        if (duplicatePassenger != null && duplicatePassenger.getUsername().equals("not defined") && duplicatePassenger.getName().equals(name)
                && duplicatePassenger.getSurname().equals(surname) && duplicatePassenger.getDocumentType().equals(documentType)
                && duplicatePassenger.getDocumentId().equals(documentId)) {
            mergePassengerData(currentPassenger, duplicatePassenger);
        }
    }

    public void mergePassengerData(Passenger registeredPassenger, Passenger duplicatePassenger) throws IOException {
        List<Flight> registeredFlights = duplicatePassenger.getRegisteredFlights();
        for (Flight flight : registeredFlights) {
            registeredPassenger.getRegisteredFlights().add(flight);
        }
        removePassenger(duplicatePassenger);
        updatePassenger(registeredPassenger);
        System.out.println("Data merged of duplicate passenger: " + registeredPassenger.getUsername());
    }


    public void registerPassenger(Passenger passenger) throws IOException {
        passengerManager.registerPassenger(passenger);
    }

    public void removePassenger(Passenger passenger) throws IOException {
        passengerManager.unregisterPassenger(passenger);
    }

    public void updatePassenger(Passenger passenger) throws IOException {
        passengerManager.updatePassenger(passenger);
    }

    public List<Passenger> getPassengers() {
        return passengerManager.getAllPassengers();
    }

    public Passenger getPassenger(String username) {
        return passengerManager.getPassengerByUsername(username);
    }

    public Passenger getPassengerByFullNameAndDocument(String name, String surname, String documentType, String documentId) {
        return passengerManager.getPassengerByFullNameAndDocument(name, surname, documentType, documentId);
    }

    public void updatePassengerNotificationPreferences(String username, Set<NotificationType> types, List<NotificationChannel> channels) throws IOException {
        Passenger passenger = passengerManager.getPassengerByUsername(username);
        if (passenger != null) {
            passengerManager.updateNotificationPreferences(passenger, types, channels);
        } else {
            throw new IllegalArgumentException("Passenger not found.");
        }
    }

    public List<Flight> findFlights(String departure, String arrival, LocalDate flightDate) {
        return flightSearchService.searchFlights(departure, arrival, flightDate);
    }

    public double calculateLuggageCost(String classType, String flightNumber, String luggageType, double length, double width, double height, double weight, int cabinLuggageCount, int holdLuggageCount) {
        double extraCost = 0;
        Flight flight = flightManager.getFlightByNumber(flightNumber);
        Route route = getRouteByAirportsCode(flight.getDepartureAirportCode(), flight.getArrivalAirportCode());
        long flightDuration = route.getFlightDuration().toHours();

        // Controllo per bagaglio a mano
        if (luggageType.equals("cabin")) {
            // Il primo bagaglio è gratuito se rientra nei limiti
            if (cabinLuggageCount > 1) {
                extraCost += Luggage.EXTRA_COST * 2;
            }

            if (width > Luggage.MAX_WIDTH_CABIN || height > Luggage.MAX_HEIGHT_CABIN ||
                    length > Luggage.MAX_LENGTH_CABIN) {
                extraCost += Luggage.EXTRA_COST;
            }

            // Se il bagaglio supera il peso gratuito
            if (weight > Luggage.MAX_WEIGHT_CABIN) {
                extraCost += (weight - Luggage.MAX_WEIGHT_CABIN) * Luggage.EXTRA_COST;
            }
        }

        // Controllo per bagaglio in stiva
        if (luggageType.equals("hold")) {
            // Il primo bagaglio in stiva è gratuito per voli > 6 ore
            boolean freeHoldLuggage = flightDuration > 6 && holdLuggageCount == 1;
            double maxLength = Luggage.getMaxLengthHold(classType);
            double maxWidth = Luggage.getMaxWidthHold(classType);
            double maxHeight = Luggage.getMaxHeightHold(classType);
            double maxWeight = Luggage.getMaxWeightHold(classType);

            if (!freeHoldLuggage) {
                extraCost += Luggage.EXTRA_COST * 2;
            }

            if (width > maxWidth || height > maxHeight || length > maxLength) {
                extraCost += Luggage.EXTRA_COST;
            }

            if (weight > maxWeight) {
                extraCost += (weight - maxWeight) * Luggage.EXTRA_COST;
            }
        }

        return extraCost;
    }

    public void addLuggage(Luggage luggage) throws IOException {
        luggageManager.addLuggage(luggage);
    }

    public void removeLuggage(List<Luggage> luggageList) throws IOException {
        for (Luggage luggage : luggageList) {
            luggageManager.removeLuggage(luggage.getLuggageId());
        }
    }

    public List<Luggage> getAllLuggage(){
        return luggageManager.getAllLuggage();
    }

    public boolean isWithin24HoursOfBooking(String bookingId, LocalDateTime currentTime) {
        Booking booking = findBooking(bookingId);
        long hoursSinceBooking = Duration.between(booking.getBookingDate(), currentTime).toHours();
        return hoursSinceBooking <= 24;
    }

    public boolean isAtLeast7DaysBeforeFlight(String bookingId, LocalDateTime currentTime) {
        Booking booking = findBooking(bookingId);
        Flight flight = findFlight(booking.getFlightNumber());
        long daysBeforeFlight = ChronoUnit.DAYS.between(currentTime, flight.getDepartureTime());
        return daysBeforeFlight >= 7;
    }
}

