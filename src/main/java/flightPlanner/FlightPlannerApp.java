package flightPlanner;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;

public class FlightPlannerApp extends Application {
    private FlightPlanner flightPlanner;
    private AuthManager authManager;
    private Stage primaryStage; // Finestra principale che contiene scene e tuttò che è visibile nell'interfaccia utente

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        flightPlanner = new FlightPlanner();
        authManager = new AuthManager();
        primaryStage = stage;

        showLoginScreen();
    }

    public AuthManager getAuthManager(){
        return authManager;
    }

    public Stage getPrimaryStage(){
        return primaryStage;
    }

    public FlightPlanner getFlightPlanner(){
        return flightPlanner;
    }

    private void showLoginScreen() {
        VBox vbox = new VBox(10);// Layout verticale con spaziatura di 10 pixel tra gli elementi
        Label titleLabel = new Label("Login");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setId("usernameField");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setId("passwordField");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setId("emailField");

        Button loginButton = new Button("Login");
        loginButton.setId("loginButton");
        Button registerButton = new Button("Register");
        registerButton.setId("registerButton");

        loginButton.setOnAction(_ -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            if (authManager.login(username, password)) {
                showMainAppScreen();
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid username or password.");
            }
        });

        registerButton.setOnAction(_ -> {
            // Otteniamo le informazioni digitati dall'utente
            String username = usernameField.getText();
            String password = passwordField.getText();
            String email = emailField.getText();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Please fill in all the fields!");
                return; // Si esce dalla funzione se i campi non sono completi
            }

            if (username.equals("not defined")) {
                showAlert(Alert.AlertType.ERROR, "Error", "You cannot use this username! Please change to another one.");
                return;
            }

            if (!email.matches("^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$")) {
                showAlert(Alert.AlertType.ERROR, "Invalid Email", "Please provide a valid email address.");
                return;
            }

            if (password.length() < 6) {
                showAlert(Alert.AlertType.ERROR, "Invalid Password", "Password must be at least 6 characters long.");
                return;
            }

            try {
                if (authManager.register(username, password, email, "Passenger")) {
                    Passenger passenger = (Passenger) authManager.getUsers().get(username);
                    try {
                        flightPlanner.registerPassenger(passenger);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    showAlert(Alert.AlertType.INFORMATION, "Registration Successful", "You can now log in.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Registration Failed", "Username already exists.");
                }
            } catch (IOException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "An error occurred during registration.");
            }
        });

        vbox.getChildren().addAll(titleLabel, usernameField, passwordField, emailField, loginButton, registerButton);
        Scene scene = new Scene(vbox, 300, 250);// Contenuto visibile della finestra che contiene i nodi disposti in un layout (lxh)
        primaryStage.setScene(scene);
        primaryStage.setTitle("Flight Planner - Login");
        primaryStage.show();
    }

    private void showMainAppScreen() {
// Layout dell'interfaccia utente
        VBox vbox = new VBox(10);
        Label titleLabel = new Label("Flight Planner");

        if (authManager.getCurrentUserRole().equals("Admin")) {
            showAdminInterface(vbox);
        } else {
            showUserInterface(vbox);
        }

        Button logoutButton = new Button("Logout");
        logoutButton.setId("logoutButton");

        logoutButton.setOnAction(_ -> {
            authManager.logout();
            showLoginScreen();
        });

        // Si aggiungono elementi al layout
        vbox.getChildren().addAll(titleLabel, logoutButton);

        // Si configurano scena e stage
        Scene scene = new Scene(vbox, 500, 550);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Flight Planner App");
        primaryStage.show();
    }

    public void showAppScreen(){
        Platform.runLater(this::showMainAppScreen);
    }

    private void showUserInterface(VBox vbox) {
        Label userLabel = new Label("Passenger Label");
        Label welcomeLabel = new Label("Welcome " + authManager.getLoggedInUser() + " !");

        Button viewBookingsButton = new Button("View Bookings");
        viewBookingsButton.setId("viewBookingsButton");
        viewBookingsButton.setOnAction(_ -> showViewBookings());

        Button bookFlightButton = new Button("Book Flight");
        bookFlightButton.setId("bookFlightButton");
        bookFlightButton.setOnAction(_ -> {
            try {
                showBookFlight();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        Button cancelBookingButton = new Button("Cancel Booking");
        cancelBookingButton.setId("cancelBookingButton");
        cancelBookingButton.setOnAction(_ -> showCancelBooking());

        Button cancelTicketBtn = new Button("Cancel Ticket");
        cancelTicketBtn.setId("cancelTicketBtn");
        cancelTicketBtn.setOnAction(_ -> showCancelTicket());

        Button addLuggageButton = new Button("Add additional Luggage");
        addLuggageButton.setId("addLuggageButton");
        addLuggageButton.setOnAction(_ -> {
            try {
                showAddLuggageForm();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        Button changeSeatButton = new Button("Change Seat");
        changeSeatButton.setId("changeSeatButton");
        changeSeatButton.setOnAction(_ -> {
            try {
                showChangeSeat();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        Button searchFlightsButton = new Button("Search Flights");
        searchFlightsButton.setId("searchFlightsButton");
        searchFlightsButton.setOnAction(_ -> showSearchFlights());

        Button manageNotificationsButton = new Button("Manage Notifications");
        manageNotificationsButton.setId("manageNotificationButton");
        manageNotificationsButton.setOnAction(_ -> {
            try {
                showManageNotificationsScreen();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        Button paymentMethodButton = new Button("Payment Methods");
        paymentMethodButton.setId("paymentMethodButton");
        paymentMethodButton.setOnAction(_ -> showPaymentMethod());

        Button updateUserCredentialsBtn = new Button("Update my credentials");
        updateUserCredentialsBtn.setId("updateUserCredentialsBtn");
        updateUserCredentialsBtn.setOnAction(_ -> showUpdateCredentials());

        Button unsubscribeButton = getUnsubscribeButton();
        unsubscribeButton.setId("unsubscribeButton");

        vbox.getChildren().addAll(userLabel, welcomeLabel, viewBookingsButton, bookFlightButton, cancelBookingButton,
                cancelTicketBtn, addLuggageButton, changeSeatButton, searchFlightsButton, manageNotificationsButton,
                paymentMethodButton, updateUserCredentialsBtn, unsubscribeButton);
    }

    private Button getUnsubscribeButton() {
        Button unsubscribeButton = new Button("Unsubscribe");
        unsubscribeButton.setOnAction(_ -> {
            boolean confirmed = showConfirmationDialog();

            if (confirmed) {
                Passenger loggedPassenger = flightPlanner.getPassenger(authManager.getLoggedInUser());
                if (loggedPassenger != null) {
                    // Si cancellano i dati del passeggero
                    try {
                        flightPlanner.removePassenger(loggedPassenger);
                        authManager.removeUser(authManager.getLoggedInUser());

                    } catch (IOException ex) {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to unsubscribe: " + authManager.getLoggedInUser() + " and other related additional passengers.");
                        throw new RuntimeException(ex);
                    }

                    authManager.logout();

                    showAlert(Alert.AlertType.INFORMATION, "Unsubscribed", "You have successfully unsubscribed from the application.");

                    showLoginScreen();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "No logged-in passenger found.");
                }
            }
        });
        return unsubscribeButton;
    }

    private void showAddLuggageForm() throws IOException {
        VBox vbox = new VBox(10);

        TextInputDialog bookingDialog = new TextInputDialog();
        bookingDialog.setHeaderText("Enter Booking ID.");
        String bookingId = bookingDialog.showAndWait().orElse("");

        if (bookingId.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter a bookingId to proceed.");
            return;
        } else if (flightPlanner.checkBookingNotExistence(bookingId)) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Booking Id " + bookingId + " not found.");
            return;
        }

        TextInputDialog ticketDialog = new TextInputDialog();
        ticketDialog.setHeaderText("Enter Ticket Number.");
        String ticketNumber = ticketDialog.showAndWait().orElse("");

        if (ticketNumber.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Enter the ticket number to proceed.");
            return;
        }

        Ticket ticket = flightPlanner.findTicket(bookingId, ticketNumber);

        if (ticket == null) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Ticket not found. Please check the Ticket Number.");
            return;
        }

        List<Booking> bookings = flightPlanner.getBookingsForPassenger(authManager.getLoggedInUser());
        boolean ticketFound = false;

        for (Booking booking : bookings) {
            if (booking.getTickets().contains(ticket)) {
                ticketFound = true;
                break;
            }
        }

        if (!ticketFound) {
            showAlert(Alert.AlertType.ERROR, "Invalid Access", "You cannot add luggage for a booking not booked by you!");
            return;
        }

        showAlert(Alert.AlertType.INFORMATION, "Luggage Information",
                """
                        Standard luggage sizes:
                        - Cabin Luggage: ONE FREE for all passengers (Max Size: 40x30x20 cm)
                        - Hold Luggage: ONE FREE for flights longer than 6 hours
                              - Economy Max Size 50x40x20 cm and Max Weight 23 kg
                              - Business Max Size 60x50x25 cm and Max Weight 32 kg
                              - First Max Size 70x60x30 and Max Weight 34 kg
                        Note:
                        -For exceeding luggage dimensions, you need to pay an extra cost of 50 EUR.
                        -For every kg of luggage weight exceeding, you need to pay an extra of 50 EUR.
                        -For each additional cabin/hold luggage, you need to pay an extra of 100 EUR.""");

        Passenger loggedInUser = flightPlanner.getPassenger(authManager.getLoggedInUser());

        // Si deve verificare quanti bagagli ha già l'utente
        int cabinLuggageCount = ticket.getCabinLuggageCount();
        int holdLuggageCount = ticket.getHoldLuggageCount();

        if(cabinLuggageCount >= 2 && holdLuggageCount >= 3){
            showAlert(Alert.AlertType.ERROR,"All Limit Exceeded","You cannot add more any luggage!");
            return;
        }

        if (cabinLuggageCount >= 2) {
            showAlert(Alert.AlertType.ERROR, "Limit Exceeded", "You cannot add more cabin luggage.");
        }

        if (holdLuggageCount >= 3) {
            showAlert(Alert.AlertType.ERROR, "Limit Exceeded", "You cannot add more hold luggage.");
        }

        int possibleCabinLuggageCount = 2 - cabinLuggageCount;
        int possibleHoldLuggageCount = 3 - holdLuggageCount;

        List<Luggage> luggageList = new ArrayList<>();
        double totalLuggagePrice = 0;
        String classType = flightPlanner.getSeatClass(ticket.getSeatNumber(), ticket.getFlightNumber());

        TextInputDialog numberOfCabinLuggageDialog = new TextInputDialog();
        numberOfCabinLuggageDialog.setTitle("Cabin Luggage Registration");
        numberOfCabinLuggageDialog.setHeaderText("How many cabin bags do you want to register for " + ticket.getPassengerName() + "?");
        String numberOfCabinLuggageStr = numberOfCabinLuggageDialog.showAndWait().orElse("");
        int numberOfCabinLuggage;
        try {
            numberOfCabinLuggage = Integer.parseInt(numberOfCabinLuggageStr);
            if (numberOfCabinLuggage < 0 || numberOfCabinLuggage > possibleCabinLuggageCount) {
                showAlert(Alert.AlertType.ERROR, "Invalid Cabin Luggage Number", "You can only add " + possibleCabinLuggageCount + " more cabin luggage !");
                flightPlanner.removeLuggage(luggageList);
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter a valid number between 0 and " + possibleCabinLuggageCount);
            flightPlanner.removeLuggage(luggageList);
            return;
        }

        for (int j = 0; j < numberOfCabinLuggage; j++) {
            TextInputDialog lengthDialog = new TextInputDialog();
            lengthDialog.setHeaderText("Enter Length (cm) for luggage " + (j + 1));
            String lengthStr = lengthDialog.showAndWait().orElse("");
            double length;
            try {
                length = Double.parseDouble(lengthStr);
                if (length < 0 || length > 45) {
                    showAlert(Alert.AlertType.ERROR, "Invalid input", "The maximum length of the luggage is 45 cm.");
                    flightPlanner.removeLuggage(luggageList);
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid length number.");
                flightPlanner.removeLuggage(luggageList);
                return;
            }

            TextInputDialog widthDialog = new TextInputDialog();
            widthDialog.setHeaderText("Enter Width (cm) for luggage " + (j + 1));
            String widthStr = widthDialog.showAndWait().orElse("");
            double width;
            try {
                width = Double.parseDouble(widthStr);
                if (width < 0 || width > 35) {
                    showAlert(Alert.AlertType.ERROR, "Invalid input", "The maximum width of the luggage is 35 cm.");
                    flightPlanner.removeLuggage(luggageList);
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid width number.");
                flightPlanner.removeLuggage(luggageList);
                return;
            }

            TextInputDialog heightDialog = new TextInputDialog();
            heightDialog.setHeaderText("Enter Height (cm) for luggage " + (j + 1));
            String heightStr = heightDialog.showAndWait().orElse("");
            double height;
            try {
                height = Double.parseDouble(heightStr);
                if (height < 0 || height > 25) {
                    showAlert(Alert.AlertType.ERROR, "Invalid input", "The maximum height of the luggage is 25 cm.");
                    flightPlanner.removeLuggage(luggageList);
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid height number.");
                flightPlanner.removeLuggage(luggageList);
                return;
            }

            TextInputDialog weightDialog = new TextInputDialog();
            weightDialog.setHeaderText("Enter Weight (kg) for luggage " + (j + 1));
            String weightString = weightDialog.showAndWait().orElse("");
            double weight;
            try {
                weight = Double.parseDouble(weightString);
                if (weight < 0 || weight > 10) {
                    showAlert(Alert.AlertType.ERROR, "Invalid input", "The maximum weight of the luggage is 10 kg.");
                    flightPlanner.removeLuggage(luggageList);
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid weight number.");
                flightPlanner.removeLuggage(luggageList);
                return;
            }

            double luggageCost = flightPlanner.calculateLuggageCost(classType, ticket.getFlightNumber(), "cabin", length, width, height, weight, ticket.getCabinLuggageCount() + j + 1, 0);

            totalLuggagePrice += luggageCost;
            Luggage luggage = new Luggage(UUID.randomUUID().toString(), weight, null, luggageCost, length, width, height, ticketNumber);
            luggage.setType("cabin");
            luggageList.add(luggage);
            flightPlanner.addLuggage(luggage);
        }

        TextInputDialog numberOfHoldLuggageDialog = new TextInputDialog();
        numberOfHoldLuggageDialog.setTitle("Hold Luggage Registration");
        numberOfHoldLuggageDialog.setHeaderText("How many hold bags do you want to register for " + ticket.getPassengerName() + "?");
        String numberOfHoldLuggageStr = numberOfHoldLuggageDialog.showAndWait().orElse("");
        int numberOfHoldLuggage;
        try {
            numberOfHoldLuggage = Integer.parseInt(numberOfHoldLuggageStr);
            if (numberOfHoldLuggage < 0 || numberOfHoldLuggage > possibleHoldLuggageCount) {
                showAlert(Alert.AlertType.ERROR, "Invalid Hold Luggage Number", "You can only add " + possibleHoldLuggageCount + " more hold luggage !");
                flightPlanner.removeLuggage(luggageList);
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter a valid number between 0 and 3.");
            flightPlanner.removeLuggage(luggageList);
            return;
        }

        for (int j = 0; j < numberOfHoldLuggage; j++) {
            TextInputDialog lengthDialog = new TextInputDialog();
            lengthDialog.setHeaderText("Enter Length (cm) for luggage " + (j + 1));
            String lengthStr = lengthDialog.showAndWait().orElse("");
            double length;
            try {
                length = Double.parseDouble(lengthStr);
                if (length < 0 || length > 105) {
                    showAlert(Alert.AlertType.ERROR, "Invalid input", "The maximum length of the luggage is 105 cm.");
                    flightPlanner.removeLuggage(luggageList);
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Invalid length number.");
                flightPlanner.removeLuggage(luggageList);
                return;
            }

            TextInputDialog widthDialog = new TextInputDialog();
            widthDialog.setHeaderText("Enter Width (cm) for luggage " + (j + 1));
            String widthStr = widthDialog.showAndWait().orElse("");
            double width;
            try {
                width = Double.parseDouble(widthStr);
                if (width < 0 || width > 70) {
                    showAlert(Alert.AlertType.ERROR, "Invalid input", "The maximum width of the luggage is 70 cm.");
                    flightPlanner.removeLuggage(luggageList);
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid width number.");
                flightPlanner.removeLuggage(luggageList);
                return;
            }

            TextInputDialog heightDialog = new TextInputDialog();
            heightDialog.setHeaderText("Enter Height (cm) for luggage " + j + 1);
            String heightStr = heightDialog.showAndWait().orElse("");
            double height;
            try {
                height = Double.parseDouble(heightStr);
                if (height < 0 || height > 40) {
                    showAlert(Alert.AlertType.ERROR, "Invalid input", "The maximum height of the luggage is 40.");
                    flightPlanner.removeLuggage(luggageList);
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid height number.");
                flightPlanner.removeLuggage(luggageList);
                return;
            }

            TextInputDialog weightDialog = new TextInputDialog();
            weightDialog.setHeaderText("Enter Weight (kg) for luggage " + (j + 1));
            String weightString = weightDialog.showAndWait().orElse("");
            double weight;
            try {
                weight = Double.parseDouble(weightString);
                if (weight < 0 || weight > 34) {
                    showAlert(Alert.AlertType.ERROR, "Invalid input", "The maximum weight of the luggage is 34 kg.");
                    flightPlanner.removeLuggage(luggageList);
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid weight number.");
                flightPlanner.removeLuggage(luggageList);
                return;
            }

            double luggageCost = flightPlanner.calculateLuggageCost(classType, ticket.getFlightNumber(), "hold", length, width, height, weight, 0, ticket.getHoldLuggageCount() + j + 1);

            totalLuggagePrice += luggageCost;
            Luggage luggage = new Luggage(UUID.randomUUID().toString(), weight, null, luggageCost, length, width, height, ticketNumber);
            luggage.setType("hold");
            luggageList.add(luggage);
            flightPlanner.addLuggage(luggage);
        }

        Button payButton = new Button("Pay");
        payButton.setId("payButton");
        double finalTotalLuggagePrice = totalLuggagePrice;
        payButton.setOnAction(_ -> {
            Payment newPayAfterChoice = new Payment(UUID.randomUUID().toString(), bookingId, finalTotalLuggagePrice, LocalDateTime.now(), loggedInUser.getPaymentMethod(), loggedInUser.getUsername());
            try {
                ticket.getLuggageList().addAll(luggageList);
                flightPlanner.addPayment(newPayAfterChoice);
                flightPlanner.updateTicketPrice(ticketNumber, ticket.getPrice() + finalTotalLuggagePrice);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            showAlert(Alert.AlertType.INFORMATION, "Success", "The total cost for luggage addition is " + finalTotalLuggagePrice + " EUR." +
                    "\nPayment successful with " + loggedInUser.getPaymentMethod() + "!");
            showMainAppScreen();
        });


        Button backButton = new Button("Back");
        backButton.setId("backButton");
        backButton.setOnAction(_ -> {
            try {
                flightPlanner.removeLuggage(luggageList);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            showAlert(Alert.AlertType.INFORMATION, "Payment Not Completed", "You do not finish the addition or the payment.");
            showMainAppScreen();
        });

        vbox.getChildren().addAll(
                new Label("Your luggage addition is almost done ... "), payButton, backButton
        );

        Scene scene = new Scene(vbox, 300, 300);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Add Additional Luggage");
        primaryStage.show();
    }


    private void showChangeSeat() throws IOException {
        VBox vBox = new VBox(10);
        Label changeSeatLabel = new Label("Change your seat to an equal or higher class type.\nWhen you choose a higher class type, you need to pay the price difference.");

        TextInputDialog bookingDialog = new TextInputDialog();
        bookingDialog.setHeaderText("Enter Booking Id to change");
        String bookingId = bookingDialog.showAndWait().orElse("");

        if (bookingId.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Enter the booking Id to proceed.");
            return;
        } else if (flightPlanner.checkBookingNotExistence(bookingId)) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Booking Id " + bookingId + " not found.");
            return;
        }

        TextInputDialog ticketDialog = new TextInputDialog();
        ticketDialog.setHeaderText("Enter Ticket number to change");

        String ticketNumber = ticketDialog.showAndWait().orElse("");

        if (ticketNumber.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Enter the ticket number to proceed.");
            return;
        }

        Ticket ticket = flightPlanner.findTicket(bookingId, ticketNumber);

        if (ticket == null) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Ticket not found. Please check the Ticket Number.");
            return;
        }

        List<Booking> bookings = flightPlanner.getBookingsForPassenger(authManager.getLoggedInUser());
        boolean ticketFound = false;

        for (Booking booking : bookings) {
            if (booking.getTickets().contains(ticket)) {
                ticketFound = true;
                break;
            }
        }

        if (!ticketFound) {
            showAlert(Alert.AlertType.ERROR, "Invalid Access", "You cannot change seat for a booking not booked by you!");
            return;
        }

        String flightNumber = ticket.getFlightNumber();
        String currentSeatNumber = ticket.getSeatNumber();
        String currentClass = flightPlanner.getSeatClass(currentSeatNumber, flightNumber);
        Map<String, String> availableSeats = flightPlanner.getAvailableSeatsWithClass(flightNumber);

        if (availableSeats.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "No available seats for this flight.");
            return;
        }

        Map<String, String> currentBookingSeats = new HashMap<>();
        List<String> seatOptions = new ArrayList<>();
        for (Map.Entry<String, String> entry : availableSeats.entrySet()) {
            seatOptions.add("Seat: " + entry.getKey() + " (" + entry.getValue() + ")");
        }

        ChoiceDialog<String> seatDialog = new ChoiceDialog<>(seatOptions.getFirst(), seatOptions);
        seatDialog.setHeaderText("Choose seat for ticket " + ticketNumber);
        Optional<String> selectedSeatOption = seatDialog.showAndWait();

        String selectedSeat;
        String selectedClassType;
        if (selectedSeatOption.isPresent()) {
            String[] seatParts = selectedSeatOption.get().split(" ");
            selectedSeat = seatParts[1];  // Estrae il numero del posto
            selectedClassType = seatParts[2].replace("(", "").replace(")", "");  // Estrae il tipo di classe

            currentBookingSeats.put(ticket.getPassengerName(), selectedSeat);
            flightPlanner.bookSeat(flightNumber, selectedSeat);
            availableSeats.remove(selectedSeat);
        } else {
            // Se l'utente chiude la finestra senza scegliere nulla, fallisce la modifica
            flightPlanner.releaseSeatsForCurrentBooking(flightNumber, currentBookingSeats);
            showAlert(Alert.AlertType.ERROR, "Error", "You closed the seat selection. Booking has been cancelled.");
            return;
        }

        double newSeatPrice = flightPlanner.getPrice(flightNumber, selectedClassType);
        Passenger loggedInUser = flightPlanner.getPassenger(authManager.getLoggedInUser());

        Button changeSeatAndPayButton = new Button("Change Seat and Pay");
        changeSeatAndPayButton.setId("changeSeatAndPayButton");

        Map<String, Integer> classRanking = new HashMap<>();
        classRanking.put("Economy", 1);
        classRanking.put("Business", 2);
        classRanking.put("First", 3);

        changeSeatAndPayButton.setOnAction(_ -> {
            int currentClassRank = classRanking.get(currentClass);
            int newClassRank = classRanking.get(selectedClassType);
            double currentPrice = flightPlanner.getPrice(flightNumber, currentClass);

            if (newClassRank == currentClassRank) {
                // Stessa classe, nessun pagamento aggiuntivo
                try {
                    flightPlanner.releaseSeat(flightNumber, currentSeatNumber);
                    flightPlanner.updateTicketSeatPrice(ticket.getTicketNumber(), selectedSeat, newSeatPrice);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                showAlert(Alert.AlertType.INFORMATION, "Success",
                        "You do not need to pay the price difference!\nNow your seat is " + selectedSeat + " " + selectedClassType + ".");

            } else if (newClassRank > currentClassRank) {
                // Classe superiore
                double priceDifference = newSeatPrice - currentPrice;
                String paymentId = UUID.randomUUID().toString();

                Payment newPay = new Payment(paymentId, ticket.getBookingId(), priceDifference, LocalDateTime.now(), loggedInUser.getPaymentMethod(), loggedInUser.getUsername());
                try {
                    flightPlanner.releaseSeat(ticket.getFlightNumber(), ticket.getSeatNumber());
                    flightPlanner.addPayment(newPay);
                    flightPlanner.updateTicketSeatPrice(ticket.getTicketNumber(), selectedSeat, newSeatPrice);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

                showAlert(Alert.AlertType.INFORMATION, "Payment Success",
                        "Payment successful with " + loggedInUser.getPaymentMethod() +
                                "!\nNow your seat is " + selectedSeat + " " + selectedClassType + ".");
            }
            showMainAppScreen();
        });
        Button cancelButton = getCancelButton(ticket, currentBookingSeats);
        cancelButton.setId("cancelButton");

        vBox.getChildren().addAll(changeSeatLabel, changeSeatAndPayButton, cancelButton);

        Scene scene = new Scene(vBox, 440, 200);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Button getCancelButton(Ticket ticket, Map<String, String> currentBookingSeats) {
        Button cancelButton = new Button("Cancel Changing");
        cancelButton.setOnAction(_ -> {
            try {
                flightPlanner.releaseSeatsForCurrentBooking(ticket.getFlightNumber(), currentBookingSeats);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            showAlert(Alert.AlertType.ERROR, "Cancelled", "You didn't finish your modification or the payment. Your changing has been cancelled.");
            showMainAppScreen();
        });
        return cancelButton;
    }

    private void showUpdateCredentials() {
        GridPane grid = new GridPane();// Griglia
        grid.setPadding(new Insets(10, 10, 10, 10));// Definire i margini attorno al contenuto di grid(valori visti in modo orario)
        grid.setVgap(8);// Spazio verticale tra le righe
        grid.setHgap(10);// Spazio orizzontale tra le colonne

        Passenger loginUser = flightPlanner.getPassenger(authManager.getLoggedInUser());

        Label emailLabel = new Label("New Email:");
        GridPane.setConstraints(emailLabel, 0, 0);// Serve per posizionare in una determinata cella cxr
        TextField emailInput = new TextField(loginUser.getEmail());
        emailInput.setId("emailInput");
        emailInput.setPromptText("Enter new email");
        emailInput.setText(authManager.getLoggedInUserEmail());// Se l'utente inizia a digitare, appare il testo di prompt
        GridPane.setConstraints(emailInput, 1, 0);

        Label oldPasswordLabel = new Label("Old Password:");
        GridPane.setConstraints(oldPasswordLabel, 0, 1);
        PasswordField oldPasswordInput = new PasswordField();
        oldPasswordInput.setId("oldPasswordInput");
        oldPasswordInput.setPromptText("Enter your old password");
        GridPane.setConstraints(oldPasswordInput, 1, 1);

        Label newPasswordLabel = new Label("New Password:");
        GridPane.setConstraints(newPasswordLabel, 0, 2);
        PasswordField newPasswordInput = new PasswordField();
        newPasswordInput.setId("newPasswordInput");
        newPasswordInput.setPromptText("Enter new password");
        GridPane.setConstraints(newPasswordInput, 1, 2);

        Label confirmPasswordLabel = new Label("Confirm Password:");
        GridPane.setConstraints(confirmPasswordLabel, 0, 3);
        PasswordField confirmPasswordInput = new PasswordField();
        confirmPasswordInput.setId("confirmPasswordInput");
        confirmPasswordInput.setPromptText("Re-enter new password");
        GridPane.setConstraints(confirmPasswordInput, 1, 3);


        emailInput.setPrefWidth(300); // Aumenta la larghezza del campo
        newPasswordInput.setPrefWidth(300);

        Button updateButton = new Button("Update");
        updateButton.setId("updateButton");
        GridPane.setConstraints(updateButton, 1, 4);

        updateButton.setOnAction(_ -> {
            String newEmail = emailInput.getText();
            String oldPassword = oldPasswordInput.getText();
            String newPassword = newPasswordInput.getText();
            String confirmPassword = confirmPasswordInput.getText();

            if (newEmail.isEmpty() && oldPassword.isEmpty() && newPassword.isEmpty() && confirmPassword.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Please provide at least a new email or a new password.");
                return;
            }

            if (newEmail.isEmpty()) {
                newEmail = authManager.getLoggedInUserEmail();
            }

            if (!newEmail.matches("^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$")) {
                showAlert(Alert.AlertType.ERROR, "Invalid Email", "Please provide a valid email address.");
                emailInput.clear();
                return;
            }

            if (newPassword.isEmpty() && confirmPassword.isEmpty()) {
                newPassword = authManager.getLoggedInUserPassword();
                confirmPassword = newPassword;
            }

            if (!oldPassword.equals(authManager.getLoggedInUserPassword())) {
                showAlert(Alert.AlertType.ERROR, "Input Error",
                        "Old password is incorrect. Provide the correct password to update your credentials");
                oldPasswordInput.clear();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                showAlert(Alert.AlertType.ERROR, "Password Mismatch", "New passwords do not match.");
                newPasswordInput.clear();
                confirmPasswordInput.clear();
                return;
            }

            if (newPassword.length() < 6) {
                showAlert(Alert.AlertType.ERROR, "Invalid Password", "Password must be at least 6 characters long.");
                newPasswordInput.clear();
                confirmPasswordInput.clear();
                return;
            }

            try {
                boolean success = authManager.updateUser(authManager.getLoggedInUser(), newPassword, newEmail);
                if (success) {
                    loginUser.setEmail(newEmail);
                    loginUser.setPassword(newPassword);

                    flightPlanner.updatePassenger(loginUser);
                    System.out.println("Credentials updated: " + authManager.getLoggedInUser() + " " + authManager.getLoggedInUserEmail()
                            + " " + authManager.getLoggedInUserPassword());
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Credentials updated successfully!");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Failure", "Failed to update your credentials.");
                }
            } catch (Exception ex) {
                System.out.println("Failed updating credentials for " + authManager.getLoggedInUser() +
                        " , actual password: " + authManager.getLoggedInUserPassword() + "| new password: "
                        + newPassword + ", actual email: " + authManager.getLoggedInUserEmail() + "| new email: " + newEmail);
                showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while updating your credentials.");
            }

            oldPasswordInput.clear();
            newPasswordInput.clear();
            confirmPasswordInput.clear();
        });

        Button backButton = new Button("Back");
        backButton.setId("backButton");
        GridPane.setConstraints(backButton, 0, 4);
        backButton.setOnAction(_ -> showMainAppScreen());

        grid.getChildren().addAll(emailLabel, emailInput, oldPasswordLabel, oldPasswordInput, newPasswordLabel, newPasswordInput,
                confirmPasswordLabel, confirmPasswordInput, backButton, updateButton);

        Scene scene = new Scene(grid, 450, 200);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Update Credentials");
        primaryStage.show();
    }

    private boolean showConfirmationDialog() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to unsubscribe? This will delete your account and all associated data.");

        // Si mostra il dialogo e si attende la risposta dell'utente
        Optional<ButtonType> result = alert.showAndWait();

        // Se l'utente clicca OK, ritorna true, altrimenti false
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private void showCancelTicket() {
        TextInputDialog bookingDialog = new TextInputDialog();
        bookingDialog.setHeaderText("Enter Booking ID to cancel a ticket.");
        String bookingId = bookingDialog.showAndWait().orElse("");

        if (bookingId.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please enter a bookingId to proceed.");
            return;
        } else if (flightPlanner.checkBookingNotExistence(bookingId)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Booking Id " + bookingId + " not found.");
            return;
        }

        TextInputDialog ticketDialog = new TextInputDialog();
        ticketDialog.setHeaderText("Enter Ticket Number to cancel.");
        String ticketNumber = ticketDialog.showAndWait().orElse("");

        if (ticketNumber.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Enter the ticket number to proceed.");
            return;
        }

        Ticket ticket = flightPlanner.findTicket(bookingId, ticketNumber);

        if (ticket == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Ticket not found. Please check the Ticket Number.");
            return;
        }

        List<Booking> bookings = flightPlanner.getBookingsForPassenger(authManager.getLoggedInUser());
        boolean ticketFound = false;

        for (Booking booking : bookings) {
            if (booking.getTickets().contains(ticket)) {
                ticketFound = true;
                break;
            }
        }

        if (!ticketFound) {
            showAlert(Alert.AlertType.ERROR, "Invalid Access", "You cannot cancel ticket for a booking not booked by you!");
            return;
        }

        double luggagePrice = 0;
        for (Luggage luggage : ticket.getLuggageList()) {
            luggagePrice += luggage.getCost();
        }
        double refundAmount = (ticket.getPrice() - luggagePrice) * 0.40;
        LocalDateTime currentTime = LocalDateTime.now();
        boolean isWithin24HoursOfBooking = flightPlanner.isWithin24HoursOfBooking(bookingId, currentTime);
        boolean isAtLeast7DaysBeforeFlight = flightPlanner.isAtLeast7DaysBeforeFlight(bookingId, currentTime);

        if (isWithin24HoursOfBooking || isAtLeast7DaysBeforeFlight) {
            try {
                flightPlanner.cancelTicket(bookingId, ticket);
                Alert successesAlert = new Alert(Alert.AlertType.INFORMATION);
                successesAlert.setTitle("Success");
                successesAlert.setHeaderText(null);
                successesAlert.setContentText("You cancelled the ticket " + ticketNumber + " in booking " + bookingId);
                successesAlert.showAndWait();

                Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);// Aspetta che il precedente alert venga chiuso
                infoAlert.setTitle("Important Information");
                infoAlert.setHeaderText("Don't worry for your refund!");
                infoAlert.setContentText("Refund of 40% (" + refundAmount + " EUR excluding luggage cost) for ticket " + ticketNumber + " has been processed automatically.");
                infoAlert.showAndWait();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Cancellation Not Allowed", "You can only cancel a ticket within 24 hours of purchase or 7 days before flight departure.");
        }
    }

    private void showCancelBooking() {
        TextInputDialog bookingDialog = new TextInputDialog();
        bookingDialog.setHeaderText("Enter Booking ID to Cancel");
        String bookingId = bookingDialog.showAndWait().orElse("");

        if (bookingId.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Enter the booking Id to proceed.");
            return;
        } else if (flightPlanner.checkBookingNotExistence(bookingId)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Booking Id " + bookingId + " not found.");
            return;
        }

        Passenger passenger = flightPlanner.getPassenger(authManager.getLoggedInUser());

        Booking booking = flightPlanner.findBooking(bookingId);

        List<Booking> bookings = flightPlanner.getBookingsForPassenger(authManager.getLoggedInUser());
        boolean bookingFound = bookings.contains(booking);


        if (!bookingFound) {
            showAlert(Alert.AlertType.ERROR, "Invalid Access", "You cannot cancel a booking not booked by you!");
            return;
        }

        double refundAmount = 0;
        double luggagePrice = 0;

        for (Ticket ticket : booking.getTickets()) {
            for (Luggage luggage : ticket.getLuggageList()) {
                luggagePrice += luggage.getCost();
            }
            refundAmount += (ticket.getPrice() - luggagePrice) * 0.40;
        }

        LocalDateTime currentTime = LocalDateTime.now();
        boolean isWithin24HoursOfBooking = flightPlanner.isWithin24HoursOfBooking(bookingId, currentTime);
        boolean isAtLeast7DaysBeforeFlight = flightPlanner.isAtLeast7DaysBeforeFlight(bookingId, currentTime);

        if (isWithin24HoursOfBooking || isAtLeast7DaysBeforeFlight) {
            try {
                flightPlanner.cancelBooking(bookingId, passenger);
                Alert successesAlert = new Alert(Alert.AlertType.INFORMATION);
                successesAlert.setTitle("Success");
                successesAlert.setHeaderText(null);
                successesAlert.setContentText("You cancelled the booking " + bookingId);
                successesAlert.showAndWait();

                Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
                infoAlert.setTitle("Important Information");
                infoAlert.setHeaderText("Don't worry for your refund!");
                infoAlert.setContentText("Refund of 40% (" + refundAmount + " EUR excluding luggage cost) for booking " + bookingId + " has been processed automatically.");
                System.out.println("Refund of 40% (" + refundAmount + " EUR excluding luggage cost) for booking " + bookingId + " has been processed.");
                infoAlert.showAndWait();
            } catch (IOException ex) {
                showAlert(Alert.AlertType.ERROR, "Cancellation Failure", "An error occurred while the booking was being canceled");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Cancellation Not Allowed", "You can only cancel a booking within 24 hours of purchase or 7 days before flight departure.");
        }
    }

    private void showBookFlight() throws IOException {
        VBox vbox = new VBox(10);
        // Dialogo per inserire numero volo
        TextInputDialog flightNumberDialog = new TextInputDialog();
        flightNumberDialog.setHeaderText("Enter Flight Number to Book");
        String flightNumber = flightNumberDialog.showAndWait().orElse("");

        if (flightNumber.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Flight number is required.");
            return;
        } else if (flightPlanner.checkFlightNotExistence(flightNumber)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Flight number " + flightNumber + " not found !");
            return;
        }

        // Si recupera il passeggero loggato
        Passenger mainPassenger = flightPlanner.getPassenger(authManager.getLoggedInUser());
        if (mainPassenger == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Passenger not found.");
            return;
        }

        // se l'utente in precedenza aveva già effettuato delle prenotazioni, il suo nome e cognome vengono recuperati per default
        TextInputDialog firstNameDialog = new TextInputDialog(mainPassenger.getName().isEmpty() ? "" : mainPassenger.getName());
        firstNameDialog.setHeaderText("Enter Main Passenger First Name");
        String firstName = firstNameDialog.showAndWait().orElse("");

        if (firstName.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Passenger first name is required.");
            return;
        }

        TextInputDialog lastNameDialog = new TextInputDialog(mainPassenger.getSurname().isEmpty() ? "" : mainPassenger.getSurname());
        lastNameDialog.setHeaderText("Enter Main Passenger Last Name");
        String lastName = lastNameDialog.showAndWait().orElse("");

        if (lastName.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Passenger last name is required.");
            return;
        }
        mainPassenger.setName(firstName);
        mainPassenger.setSurname(lastName);

        showAlert(Alert.AlertType.INFORMATION, "Important Notification",
                """
                        If you are booking for an international flight, you must provide passport as document.
                        In other case you can choose to provide your Id card as document.""");

        TextInputDialog documentTypeDialog = new TextInputDialog(mainPassenger.getDocumentType().isEmpty() ? "" : mainPassenger.getDocumentType());
        documentTypeDialog.setHeaderText("Which document type do you want to use for your booking?");
        documentTypeDialog.setContentText("Enter PASSPORT/ID CARD:");
        String documentType = documentTypeDialog.showAndWait().orElse("");

        if (documentType.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "The document type is required.");
            return;
        }

        if (!documentType.equals("PASSPORT") && !documentType.equals("ID CARD")) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid format of document type: PASSPORT or ID CARD.");
            return;
        }

        TextInputDialog documentIdDialog = new TextInputDialog();
        documentIdDialog.setHeaderText("Enter your document ID.");
        String documentId = documentIdDialog.showAndWait().orElse("");

        if (documentId.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "The document ID is required.");
            return;
        }

        if (documentId.length() != 9) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "The document ID must be exactly 9 characters long.");
            return;
        }

        mainPassenger.setDocumentType(documentType);
        mainPassenger.setDocumentId(documentId);

        if (flightPlanner.checkDuplicatePassenger(firstName, lastName, documentType, documentId)) {
            flightPlanner.removeDuplicatePassenger(mainPassenger.getUsername(), firstName, lastName, documentType, documentId);
        }

        if (mainPassenger.isRegisteredForFlight(flightPlanner.findFlight(flightNumber))) {
            showAlert(Alert.AlertType.ERROR, "Duplicate Booking", "You have already a booking for the flight " + flightNumber);
            return;
        }

        // Dialogo per numero di biglietti
        TextInputDialog ticketCountDialog = new TextInputDialog();
        ticketCountDialog.setHeaderText("How many tickets do you want to book?");
        String ticketCountStr = ticketCountDialog.showAndWait().orElse("");
        int ticketCount;

        try {
            ticketCount = Integer.parseInt(ticketCountStr);

            if (ticketCount <= 0) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "You must choose a number greater than 0.");
                return;
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Invalid ticket number.");
            return;
        }

        // Si recuperano i posti disponibili per il volo con il tipo di classe associato
        Map<String, String> availableSeats = flightPlanner.getAvailableSeatsWithClass(flightNumber);

        // Se non ci sono posti disponibili
//        //if (availableSeats.isEmpty()) {
//            showAlert(Alert.AlertType.ERROR, "Error", "No available seats for this flight.");
//           return;
//        }

        if (availableSeats.size() < ticketCount) {
            showAlert(Alert.AlertType.ERROR, "Error", "No enough available seats for this flight as you requested.");
            return;
        }

        // Una mappa per tenere traccia dei posti assegnati a ciascun passeggero nella prenotazione attuale
        Map<String, String> currentBookingSeats = new HashMap<>();
        // Lista dei biglietti prenotati
        List<Ticket> tickets = new ArrayList<>();

        // Si genera un ID unico per la prenotazione
        String bookingId = UUID.randomUUID().toString();

        double totalPrice = 0;
        List<Luggage> allLuggage = new ArrayList<>();
        List<Passenger> additionalPassengers = new ArrayList<>();

        // Ciclo per ogni biglietto da creare
        for (int i = 0; i < ticketCount; i++) {
            // Si chiede nome e cognome per ogni passeggero
            String passengerName;
            String passengerLastName;
            String passengerDocumentType;
            String passengerDocumentId;

            // Se è il primo biglietto, usa il nome del main passenger
            if (i == 0) {
                passengerName = firstName;  // Nome principale (main passenger)
                passengerLastName = lastName; // Cognome principale (main passenger)
                passengerDocumentType = documentType;
                passengerDocumentId = documentId;
            } else {
                TextInputDialog additionalFirstNameDialog = new TextInputDialog();
                additionalFirstNameDialog.setHeaderText("Enter First Name for Passenger " + (i + 1));
                passengerName = additionalFirstNameDialog.showAndWait().orElse("");

                if (passengerName.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter the name of the passenger.");
                    flightPlanner.cancelCurrentTickets(tickets);
                    flightPlanner.releaseSeatsForCurrentBooking(flightNumber, currentBookingSeats);
                    flightPlanner.removeLuggage(allLuggage);
                    return;
                }

                TextInputDialog additionalLastNameDialog = new TextInputDialog();
                additionalLastNameDialog.setHeaderText("Enter Last Name for Passenger " + (i + 1));
                passengerLastName = additionalLastNameDialog.showAndWait().orElse("");

                if (passengerLastName.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter the surname of the passenger.");
                    flightPlanner.cancelCurrentTickets(tickets);
                    flightPlanner.releaseSeatsForCurrentBooking(flightNumber, currentBookingSeats);
                    flightPlanner.removeLuggage(allLuggage);
                    return;
                }

                TextInputDialog additionalDocumentTypeDialog = new TextInputDialog("ID CARD");
                additionalDocumentTypeDialog.setHeaderText("Which document type do you want to use for your booking?");
                additionalDocumentTypeDialog.setContentText("Enter PASSPORT/ID CARD:");
                String additionalDocumentType = additionalDocumentTypeDialog.showAndWait().orElse("");

                if (additionalDocumentType.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Input Error", "The document type is required.");
                    flightPlanner.cancelCurrentTickets(tickets);
                    flightPlanner.releaseSeatsForCurrentBooking(flightNumber, currentBookingSeats);
                    flightPlanner.removeLuggage(allLuggage);
                    return;
                }

                if (!additionalDocumentType.equals("PASSPORT") && !additionalDocumentType.equals("ID CARD")) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid format.");
                    flightPlanner.cancelCurrentTickets(tickets);
                    flightPlanner.releaseSeatsForCurrentBooking(flightNumber, currentBookingSeats);
                    flightPlanner.removeLuggage(allLuggage);
                    return;
                }

                TextInputDialog additionalDocumentIdDialog = new TextInputDialog();
                additionalDocumentIdDialog.setHeaderText("Enter your document ID.");
                String additionalDocumentId = additionalDocumentIdDialog.showAndWait().orElse("");

                if (additionalDocumentId.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Input Error", "The document ID is required.");
                    flightPlanner.cancelCurrentTickets(tickets);
                    flightPlanner.releaseSeatsForCurrentBooking(flightNumber, currentBookingSeats);
                    flightPlanner.removeLuggage(allLuggage);
                    return;
                }

                if (additionalDocumentId.length() != 9) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input", "The document ID must be exactly 9 characters long.");
                    flightPlanner.cancelCurrentTickets(tickets);
                    flightPlanner.releaseSeatsForCurrentBooking(flightNumber, currentBookingSeats);
                    flightPlanner.removeLuggage(allLuggage);
                    return;
                }

                Passenger additionalPassenger = flightPlanner.getPassengerByFullNameAndDocument(passengerName, passengerLastName, additionalDocumentType, additionalDocumentId);

                if (additionalPassenger != null) {
                    if (flightPlanner.checkDuplicatePassenger(passengerName, passengerLastName, additionalDocumentType, additionalDocumentId)) {
                        flightPlanner.removeDuplicatePassenger(additionalPassenger.getUsername(), additionalPassenger.getName(),
                                additionalPassenger.getSurname(), additionalPassenger.getDocumentType(), additionalPassenger.getDocumentId());
                    }
                } else {
                    additionalPassenger = new Passenger("not defined", passengerName, passengerLastName, authManager.getLoggedInUserEmail(),
                            "", "", new HashSet<>(), new ArrayList<>(), null, additionalDocumentType, additionalDocumentId);

                    if (!flightPlanner.checkPassengerExistence(additionalPassenger)) {
                        flightPlanner.registerPassenger(additionalPassenger);
                    } else {
                        additionalPassenger.setDocumentType(additionalDocumentType);
                        additionalPassenger.setDocumentId(additionalDocumentId);
                        flightPlanner.updatePassenger(additionalPassenger);
                    }
                }

                if (additionalPassenger.isRegisteredForFlight(flightPlanner.findFlight(flightNumber))) {
                    showAlert(Alert.AlertType.ERROR, "Duplicate Booking", "This passenger has already a booking for flight " + flightNumber);
                    flightPlanner.cancelCurrentTickets(tickets);
                    flightPlanner.releaseSeatsForCurrentBooking(flightNumber, currentBookingSeats);
                    flightPlanner.removeLuggage(allLuggage);
                    return;
                }

                passengerDocumentId = additionalDocumentId;
                passengerDocumentType = additionalDocumentType;
                additionalPassengers.add(additionalPassenger);
            }
            // Si presenta una lista di posti con il tipo di classe accanto
            List<String> seatOptions = new ArrayList<>();
            for (Map.Entry<String, String> entry : availableSeats.entrySet()) {
                seatOptions.add("Seat: " + entry.getKey() + " (" + entry.getValue() + ")");
            }

            if (seatOptions.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "No available seat options.");
                flightPlanner.cancelCurrentTickets(tickets);
                flightPlanner.releaseSeatsForCurrentBooking(flightNumber, currentBookingSeats);
                flightPlanner.removeLuggage(allLuggage);
                return;
            }

            ChoiceDialog<String> seatDialog = new ChoiceDialog<>(seatOptions.getFirst(), seatOptions);
            seatDialog.setHeaderText("Choose seat for ticket " + (i + 1));
            Optional<String> selectedSeatOption = seatDialog.showAndWait();

            String selectedSeat;
            String selectedClassType;
            if (selectedSeatOption.isPresent()) {
                String[] seatParts = selectedSeatOption.get().split(" ");
                selectedSeat = seatParts[1];  // Estrae il numero del posto
                selectedClassType = seatParts[2].replace("(", "").replace(")", "");  // Estrae il tipo di classe

                currentBookingSeats.put(passengerName, selectedSeat);
                flightPlanner.bookSeat(flightNumber, selectedSeat);
                availableSeats.remove(selectedSeat);

            } else {
                // Se l'utente chiude la finestra senza scegliere nulla, fallisce la prenotazione
                flightPlanner.cancelCurrentTickets(tickets);
                flightPlanner.releaseSeatsForCurrentBooking(flightNumber, currentBookingSeats);
                flightPlanner.removeLuggage(allLuggage);
                showAlert(Alert.AlertType.ERROR, "Error", "You closed the seat selection. Booking has been cancelled.");
                return; // Prenotazione fallita
            }

            // Si calcola il prezzo in base al tipo di classe del posto selezionato
            double pricePerTicket;
            try {
                pricePerTicket = flightPlanner.getPrice(flightNumber, selectedClassType);
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Unable to retrieve price for the selected seat " + selectedSeat + " and class type " + selectedClassType + ". ");
                flightPlanner.cancelCurrentTickets(tickets);
                flightPlanner.releaseSeatsForCurrentBooking(flightNumber, currentBookingSeats);
                flightPlanner.removeLuggage(allLuggage);
                return;
            }
            totalPrice += pricePerTicket;
            // Si genera un numero di biglietto unico
            String ticketNumber = UUID.randomUUID().toString();
            List<Luggage> luggageList = new ArrayList<>();
            double totalLuggagePrice = 0;

            showAlert(Alert.AlertType.INFORMATION, "Luggage Information",
                    """
                            Standard luggage sizes:
                            - Cabin Luggage: ONE FREE for all passengers (Max Size: 40x30x20 cm)
                            - Hold Luggage: ONE FREE for flights longer than 6 hours
                                  - Economy Max Size 50x40x20 cm and Max Weight 23 kg
                                  - Business Max Size 60x50x25 cm and Max Weight 32 kg
                                  - First Max Size 70x60x30 and Max Weight 34 kg
                            Note:
                            -For exceeding luggage dimensions, you need to pay an extra cost of 50 EUR.
                            -For every kg of luggage weight exceeding, you need to pay an extra of 50 EUR.
                            -For each additional cabin/hold luggage, you need to pay an extra of 100 EUR.""");

            TextInputDialog numberOfCabinLuggageDialog = new TextInputDialog();
            numberOfCabinLuggageDialog.setTitle("Cabin Luggage Registration");
            numberOfCabinLuggageDialog.setHeaderText("How many cabin bags do you want to register for " + passengerName + "?");
            numberOfCabinLuggageDialog.setContentText("Enter a number (0-2):");
            String numberOfCabinLuggageStr = numberOfCabinLuggageDialog.showAndWait().orElse("");
            int numberOfCabinLuggage;
            try {
                numberOfCabinLuggage = Integer.parseInt(numberOfCabinLuggageStr);
                if (numberOfCabinLuggage < 0 || numberOfCabinLuggage > 2) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Cabin Luggage Number", "You cannot register more than two cabin luggage !");
                    flightPlanner.cancelCurrentTickets(tickets);
                    flightPlanner.releaseSeatsForCurrentBooking(flightNumber, currentBookingSeats);
                    flightPlanner.removeLuggage(allLuggage);
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter a valid number between 0 and 2.");
                flightPlanner.cancelCurrentTickets(tickets);
                flightPlanner.releaseSeatsForCurrentBooking(flightNumber, currentBookingSeats);
                flightPlanner.removeLuggage(allLuggage);
                return;
            }

            for (int j = 0; j < numberOfCabinLuggage; j++) {
                TextInputDialog lengthDialog = new TextInputDialog();
                lengthDialog.setHeaderText("Enter Length (cm) for luggage " + (j + 1));
                String lengthStr = lengthDialog.showAndWait().orElse("");
                double length;
                try {
                    length = Double.parseDouble(lengthStr);
                    if (length < 0 || length > 45) {
                        showAlert(Alert.AlertType.ERROR, "Invalid input", "The maximum length of the luggage is 45 cm.");
                        flightPlanner.cancelCurrentTickets(tickets);
                        flightPlanner.releaseSeatsForCurrentBooking(flightNumber, currentBookingSeats);
                        flightPlanner.removeLuggage(allLuggage);
                        return;
                    }
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Invalid length number.");
                    flightPlanner.cancelCurrentTickets(tickets);
                    flightPlanner.releaseSeatsForCurrentBooking(flightNumber, currentBookingSeats);
                    flightPlanner.removeLuggage(allLuggage);
                    return;
                }

                TextInputDialog widthDialog = new TextInputDialog();
                widthDialog.setHeaderText("Enter Width (cm) for luggage " + (j + 1));
                String widthStr = widthDialog.showAndWait().orElse("");
                double width;
                try {
                    width = Double.parseDouble(widthStr);
                    if (width < 0 || width > 35) {
                        showAlert(Alert.AlertType.ERROR, "Invalid input", "The maximum width of the luggage is 35 cm.");
                        flightPlanner.cancelCurrentTickets(tickets);
                        flightPlanner.releaseSeatsForCurrentBooking(flightNumber, currentBookingSeats);
                        flightPlanner.removeLuggage(allLuggage);
                        return;
                    }
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Invalid width number.");
                    flightPlanner.cancelCurrentTickets(tickets);
                    flightPlanner.releaseSeatsForCurrentBooking(flightNumber, currentBookingSeats);
                    flightPlanner.removeLuggage(allLuggage);
                    return;
                }

                TextInputDialog heightDialog = new TextInputDialog();
                heightDialog.setHeaderText("Enter Height (cm) for luggage " + (j + 1));
                String heightStr = heightDialog.showAndWait().orElse("");
                double height;
                try {
                    height = Double.parseDouble(heightStr);
                    if (height < 0 || height > 25) {
                        showAlert(Alert.AlertType.ERROR, "Invalid input", "The maximum height of the luggage is 25 cm.");
                        flightPlanner.cancelCurrentTickets(tickets);
                        flightPlanner.releaseSeatsForCurrentBooking(flightNumber, currentBookingSeats);
                        flightPlanner.removeLuggage(allLuggage);
                        return;
                    }
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Invalid height number.");
                    flightPlanner.cancelCurrentTickets(tickets);
                    flightPlanner.releaseSeatsForCurrentBooking(flightNumber, currentBookingSeats);
                    flightPlanner.removeLuggage(allLuggage);
                    return;
                }

                TextInputDialog weightDialog = new TextInputDialog();
                weightDialog.setHeaderText("Enter Weight (kg) for luggage " + (j + 1));
                String weightString = weightDialog.showAndWait().orElse("");
                double weight;
                try {
                    weight = Double.parseDouble(weightString);
                    if (weight < 0 || weight > 10) {
                        showAlert(Alert.AlertType.ERROR, "Invalid input", "The maximum weight of the luggage is 10 kg.");
                        flightPlanner.cancelCurrentTickets(tickets);
                        flightPlanner.releaseSeatsForCurrentBooking(flightNumber, currentBookingSeats);
                        flightPlanner.removeLuggage(allLuggage);
                        return;
                    }
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Invalid weight number.");
                    flightPlanner.cancelCurrentTickets(tickets);
                    flightPlanner.releaseSeatsForCurrentBooking(flightNumber, currentBookingSeats);
                    flightPlanner.removeLuggage(allLuggage);
                    return;
                }

                double luggageCost = flightPlanner.calculateLuggageCost(selectedClassType, flightNumber, "cabin", length, width, height, weight, j + 1, 0);

                totalLuggagePrice += luggageCost;
                Luggage luggage = new Luggage(UUID.randomUUID().toString(), weight, "cabin", luggageCost, length, width, height, ticketNumber);
                luggageList.add(luggage);
                allLuggage.add(luggage);
                flightPlanner.addLuggage(luggage);
            }

            TextInputDialog numberOfHoldLuggageDialog = new TextInputDialog();
            numberOfHoldLuggageDialog.setTitle("Hold Luggage Registration");
            numberOfHoldLuggageDialog.setHeaderText("How many hold bags do you want to register for " + passengerName + "?");
            numberOfHoldLuggageDialog.setContentText("Enter a number (0-3):");
            String numberOfHoldLuggageStr = numberOfHoldLuggageDialog.showAndWait().orElse("");
            int numberOfHoldLuggage;
            try {
                numberOfHoldLuggage = Integer.parseInt(numberOfHoldLuggageStr);
                if (numberOfHoldLuggage < 0 || numberOfHoldLuggage > 3) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Hold Luggage Number", "You cannot register more than three hold luggage !");
                    flightPlanner.cancelCurrentTickets(tickets);
                    flightPlanner.releaseSeatsForCurrentBooking(flightNumber, currentBookingSeats);
                    flightPlanner.removeLuggage(allLuggage);
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter a valid number between 0 and 3.");
                flightPlanner.cancelCurrentTickets(tickets);
                flightPlanner.releaseSeatsForCurrentBooking(flightNumber, currentBookingSeats);
                flightPlanner.removeLuggage(allLuggage);
                return;
            }

            for (int j = 0; j < numberOfHoldLuggage; j++) {
                TextInputDialog lengthDialog = new TextInputDialog();
                lengthDialog.setHeaderText("Enter Length (cm) for luggage " + (j + 1));
                String lengthStr = lengthDialog.showAndWait().orElse("");
                double length;
                try {
                    length = Double.parseDouble(lengthStr);
                    if (length < 0 || length > 105) {
                        showAlert(Alert.AlertType.ERROR, "Invalid input", "The maximum length of the luggage is 105 cm.");
                        flightPlanner.cancelCurrentTickets(tickets);
                        flightPlanner.releaseSeatsForCurrentBooking(flightNumber, currentBookingSeats);
                        flightPlanner.removeLuggage(allLuggage);
                        return;
                    }
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Input Error", "Invalid length number.");
                    flightPlanner.cancelCurrentTickets(tickets);
                    flightPlanner.releaseSeatsForCurrentBooking(flightNumber, currentBookingSeats);
                    flightPlanner.removeLuggage(allLuggage);
                    return;
                }

                TextInputDialog widthDialog = new TextInputDialog();
                widthDialog.setHeaderText("Enter Width (cm) for luggage " + (j + 1));
                String widthStr = widthDialog.showAndWait().orElse("");
                double width;
                try {
                    width = Double.parseDouble(widthStr);
                    if (width < 0 || width > 70) {
                        showAlert(Alert.AlertType.ERROR, "Invalid input", "The maximum width of the luggage is 70 cm.");
                        flightPlanner.cancelCurrentTickets(tickets);
                        flightPlanner.releaseSeatsForCurrentBooking(flightNumber, currentBookingSeats);
                        flightPlanner.removeLuggage(allLuggage);
                        return;
                    }
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Invalid width number.");
                    flightPlanner.cancelCurrentTickets(tickets);
                    flightPlanner.releaseSeatsForCurrentBooking(flightNumber, currentBookingSeats);
                    flightPlanner.removeLuggage(allLuggage);
                    return;
                }

                TextInputDialog heightDialog = new TextInputDialog();
                heightDialog.setHeaderText("Enter Height (cm) for luggage " + j + 1);
                String heightStr = heightDialog.showAndWait().orElse("");
                double height;
                try {
                    height = Double.parseDouble(heightStr);
                    if (height < 0 || height > 40) {
                        showAlert(Alert.AlertType.ERROR, "Invalid input", "The maximum height of the luggage is 40 cm.");
                        flightPlanner.cancelCurrentTickets(tickets);
                        flightPlanner.releaseSeatsForCurrentBooking(flightNumber, currentBookingSeats);
                        flightPlanner.removeLuggage(allLuggage);
                        return;
                    }
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Invalid height number.");
                    flightPlanner.cancelCurrentTickets(tickets);
                    flightPlanner.releaseSeatsForCurrentBooking(flightNumber, currentBookingSeats);
                    flightPlanner.removeLuggage(allLuggage);
                    return;
                }

                TextInputDialog weightDialog = new TextInputDialog();
                weightDialog.setHeaderText("Enter Weight (kg) for luggage " + (j + 1));
                String weightString = weightDialog.showAndWait().orElse("");
                double weight;
                try {
                    weight = Double.parseDouble(weightString);
                    if (weight < 0 || weight > 34) {
                        showAlert(Alert.AlertType.ERROR, "Invalid input", "The maximum weight of the luggage is 34 kg.");
                        flightPlanner.cancelCurrentTickets(tickets);
                        flightPlanner.releaseSeatsForCurrentBooking(flightNumber, currentBookingSeats);
                        flightPlanner.removeLuggage(allLuggage);
                        return;
                    }
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Invalid weight number.");
                    flightPlanner.cancelCurrentTickets(tickets);
                    flightPlanner.releaseSeatsForCurrentBooking(flightNumber, currentBookingSeats);
                    flightPlanner.removeLuggage(allLuggage);
                    return;
                }

                double luggageCost = flightPlanner.calculateLuggageCost(selectedClassType, flightNumber, "hold", length, width, height, weight, 0, j + 1);

                totalLuggagePrice += luggageCost;
                Luggage luggage = new Luggage(UUID.randomUUID().toString(), weight, "hold", luggageCost, length, width, height, ticketNumber);
                luggageList.add(luggage);
                allLuggage.add(luggage);
                flightPlanner.addLuggage(luggage);
            }

            pricePerTicket += totalLuggagePrice;
            totalPrice += totalLuggagePrice;

            // Si crea un nuovo biglietto con i parametri richiesti
            Ticket ticket = new Ticket(ticketNumber, bookingId, flightNumber, selectedSeat, pricePerTicket, passengerName, passengerLastName, luggageList);
            ticket.setDocumentType(passengerDocumentType);
            ticket.setDocumentId(passengerDocumentId);
            tickets.add(ticket);
            flightPlanner.addTicket(ticket);
        }

        LocalDateTime currentDT = LocalDateTime.now();

        // Si aggiunge la prenotazione e i biglietti al sistema passando l'ID generato
        try {
            flightPlanner.bookFlightForPassenger(flightNumber, mainPassenger, additionalPassengers, tickets, bookingId);
            showAlert(Alert.AlertType.INFORMATION, "Notification", "Flight almost successfully booked!\nTotal Price: " + totalPrice + " EUR");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to book flight: " + e.getMessage());
            flightPlanner.cancelCurrentTickets(tickets);
            flightPlanner.releaseSeatsForCurrentBooking(flightNumber, currentBookingSeats);
            flightPlanner.removeLuggage(allLuggage);
        }

        Label paymentLabel = new Label("Payment Process:");
        Button payButton = new Button("Pay");
        payButton.setId("payButton");
        payButton.setDisable(true);  // Disabilitato fino a quando non viene selezionato un metodo di pagamento
        String paymentId = UUID.randomUUID().toString();

        if (mainPassenger.getPaymentMethod() != null) {
            payButton.setDisable(false);
            double finalTotalPrice = totalPrice;
            payButton.setOnAction(_ -> {
                Payment newPay = new Payment(paymentId, bookingId, finalTotalPrice, currentDT, mainPassenger.getPaymentMethod(), mainPassenger.getUsername());
                try {
                    flightPlanner.addPayment(newPay);
                } catch (IOException ex) {
                    try {
                        flightPlanner.cancelBooking(bookingId, mainPassenger);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    throw new RuntimeException(ex);
                }
                showMainAppScreen();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Payment successful with " + mainPassenger.getPaymentMethod() + "!");
            });
        } else {
            ChoiceDialog<String> paymentMethodDialog = new ChoiceDialog<>("CREDIT_CARD", "DEBIT_CARD", "PAYPAL", "BANK_TRANSFER");
            paymentMethodDialog.setHeaderText("Choose a Payment Method");
            Optional<String> selectedPaymentMethod = paymentMethodDialog.showAndWait();

            if (selectedPaymentMethod.isPresent()) {
                PaymentMethod selectedMethod = PaymentMethod.valueOf(selectedPaymentMethod.get());
                mainPassenger.setPaymentMethod(selectedMethod);
                flightPlanner.updatePassenger(mainPassenger);
                double finalTotalPrice1 = totalPrice;
                payButton.setOnAction(_ -> {
                    Payment newPayAfterChoice = new Payment(paymentId, bookingId, finalTotalPrice1, currentDT, mainPassenger.getPaymentMethod(), mainPassenger.getUsername());
                    try {
                        flightPlanner.addPayment(newPayAfterChoice);
                    } catch (IOException ex) {
                        try {
                            flightPlanner.cancelBooking(bookingId, mainPassenger);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        throw new RuntimeException(ex);
                    }

                    showMainAppScreen();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Payment successful with " + mainPassenger.getPaymentMethod() + "!");

                });
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "You need to select a payment method to proceed.");
                try {
                    flightPlanner.cancelBooking(bookingId, mainPassenger);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return;
            }
        }
        Button cancelButton = getCancelBookingFlightButton(bookingId, mainPassenger);

        vbox.getChildren().addAll(paymentLabel, payButton, cancelButton);

        Scene scene = new Scene(vbox, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setTitle("New Flight Booking");
    }

    private Button getCancelBookingFlightButton(String bookingId, Passenger mainPassenger) {
        Button cancelButton = new Button("Cancel Booking");
        cancelButton.setId("cancelButton");
        cancelButton.setOnAction(_ -> {
            try {
                flightPlanner.cancelBooking(bookingId, mainPassenger);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            showMainAppScreen();
            showAlert(Alert.AlertType.INFORMATION, "Cancelled", "You didn't finish the payment. Your booking has been cancelled.");

        });
        return cancelButton;
    }

    private void showSearchFlights() {
        VBox vbox = new VBox(10);
        Label titleLabel = new Label("Searching Results");
        TextInputDialog departureDialog = new TextInputDialog();
        departureDialog.setHeaderText("Enter Departure City or Airport Code");
        String departure = departureDialog.showAndWait().orElse("");

        if (notValidAirportOrCity(departure)) {
            showAlert(Alert.AlertType.ERROR, "Invalid Departure", "Please enter a valid departure city or airport code.");
            return;
        }

        TextArea resultArea = new TextArea();
        resultArea.setId("resultTextArea");
        resultArea.setEditable(false);

        TextInputDialog arrivalDialog = new TextInputDialog();
        arrivalDialog.setHeaderText("Enter Arrival City or Airport Code");
        String arrival = arrivalDialog.showAndWait().orElse("");

        if (notValidAirportOrCity(arrival)) {
            showAlert(Alert.AlertType.ERROR, "Invalid Arrival", "Please enter a valid arrival city or airport code.");
            return;
        }

        TextInputDialog dateDialog = new TextInputDialog();
        dateDialog.setHeaderText("Enter Flight Date (format: yyyy-MM-dd)");
        String dateInput = dateDialog.showAndWait().orElse("");

        LocalDate flightDate;
        // Si controlla se la data è nel formato corretto
        if (!dateInput.isEmpty()) {
            try {
                flightDate = LocalDate.parse(dateInput);
            } catch (DateTimeParseException e) {
                showAlert(Alert.AlertType.ERROR, "Invalid Date", "Please enter a valid date in the format yyyy-MM-dd.");
                return;
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter a date.");
            return;
        }

        List<Flight> flights = flightPlanner.findFlights(departure, arrival, flightDate);
        if (flights.isEmpty()) {
            resultArea.setText("No flights found.");
        } else {
            StringBuilder flightsText = new StringBuilder();
            for (Flight flight : flights) {
                flightsText.append("Flight: ")
                        .append(flight.getFlightNumber())
                        .append(" Departure Airport Code: ")
                        .append(flight.getDepartureAirportCode())
                        .append(" Departure Time: ")
                        .append(flight.getDepartureTime())
                        .append(" Arrival Airport Code: ")
                        .append(flight.getArrivalAirportCode())
                        .append(" Arrival Time: ")
                        .append(flight.getArrivalTime())
                        .append("\n");

                Route route = flightPlanner.getRouteByAirportsCode(flight.getDepartureAirportCode(), flight.getArrivalAirportCode());
                if (route != null) {
                    flightsText.append("Route ID: ")
                            .append(route.getRouteId())
                            .append(", Distance: ")
                            .append(route.getDistance())
                            .append(" km, Flight Duration: ")
                            .append(route.getFormattedDuration())
                            .append("\n");
                } else {
                    flightsText.append("No route information available.\n");
                }
            }
            resultArea.setText(flightsText.toString());
        }
        Button backButton = new Button("Back");
        backButton.setOnAction(_ -> showMainAppScreen());

        vbox.getChildren().addAll(titleLabel, resultArea, backButton);

        Scene scene = new Scene(vbox, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private boolean notValidAirportOrCity(String input) {
        List<Airport> airports = flightPlanner.getAllAirports();
        return airports.stream()
                .noneMatch(a -> a.getCode().equalsIgnoreCase(input) || a.getCity().equalsIgnoreCase(input));
    }

    private void showViewBookings() {
        VBox vbox = new VBox(10);
        Label titleLabel = new Label("My bookings");
        titleLabel.setId("titleLabel");
        TextArea resultArea = new TextArea();
        resultArea.setId("resultArea");
        resultArea.setEditable(false);
        List<Booking> bookings = flightPlanner.getBookingsForPassenger(authManager.getLoggedInUser());
        if (bookings.isEmpty()) {
            resultArea.setText("No bookings found.");
        } else {
            StringBuilder bookingsText = new StringBuilder();
            for (Booking booking : bookings) {
                bookingsText.append("Booking ID: ").append(booking.getBookingId()).append("\n");
                bookingsText.append("Flight number: ").append(booking.getFlightNumber()).append("\n");
                bookingsText.append("Date: ").append(booking.getBookingDate()).append("\n");
                bookingsText.append("Total Amount: ").append(booking.getTotalAmount()).append(" EUR\n");
                bookingsText.append("Tickets:\n");

                // Si itera attraverso i biglietti della prenotazione e si stampano i dettagli di ciascun biglietto
                for (Ticket ticket : booking.getTickets()) {
                    bookingsText.append("Ticket Number: ").append(ticket.getTicketNumber())
                            .append(", Flight Number: ").append(ticket.getFlightNumber())
                            .append(", Seat: ").append(ticket.getSeatNumber())
                            .append(", Passenger: ").append(ticket.getPassengerName())
                            .append(" ").append(ticket.getPassengerSurname()).append("\n");

                    List<Luggage> luggageList = ticket.getLuggageList();
                    if (luggageList == null || luggageList.isEmpty()) {
                        bookingsText.append("Luggage: No luggage booked.\n");
                    } else {
                        String luggageString = ticket.getLuggageList().toString()
                                .replace("[", "")
                                .replace("]", "");

                        bookingsText.append("Luggage: ").append(luggageString).append("\n");
                    }
                }
                bookingsText.append("\n"); // Si aggiunge una riga vuota tra una prenotazione e l'altra
            }

            resultArea.setText(bookingsText.toString());
        }
        Button backButton = new Button("Back");
        backButton.setId("backButton");
        backButton.setOnAction(_ -> showMainAppScreen());

        vbox.getChildren().addAll(titleLabel, resultArea, backButton);

        Scene scene = new Scene(vbox, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showPaymentMethod() {
        VBox vbox = new VBox(10);
        Label titleLabel = new Label("Payment Method Settings");

        ComboBox<PaymentMethod> payMethodComboBox = new ComboBox<>();
        payMethodComboBox.setId("payMethodComboBox");
        payMethodComboBox.getItems().addAll(PaymentMethod.values());
        payMethodComboBox.setPromptText("Select Payment Method");

        Button confirmButton = new Button("Confirm");
        confirmButton.setId("confirmButton");
        confirmButton.setOnAction(_ -> {
            PaymentMethod selectedPaymentMethod = payMethodComboBox.getValue();
            Passenger passenger = flightPlanner.getPassenger(authManager.getLoggedInUser());
            if (passenger == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Passenger not found.");
                alert.show();
                return;
            }

            if (selectedPaymentMethod != null) {
                // Si imposta il metodo di pagamento sul passeggero
                passenger.setPaymentMethod(selectedPaymentMethod);

                try {
                    flightPlanner.updatePassenger(passenger);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                showAlert(Alert.AlertType.INFORMATION, "Success", "Payment method updated to: " + selectedPaymentMethod);
            } else {
                // Alert se non è stato selezionato alcun metodo di pagamento
                showAlert(Alert.AlertType.WARNING, "Warning", "Please select a payment method.");
            }
        });

        Button backButton = new Button("Back");
        backButton.setId("backButton");
        backButton.setOnAction(_ -> showMainAppScreen());

        vbox.getChildren().addAll(titleLabel, payMethodComboBox, confirmButton, backButton);

        Scene scene = new Scene(vbox, 300, 200);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showManageNotificationsScreen() throws IOException {
        VBox vbox = new VBox(10);
        Label titleLabel = new Label("Notification Settings");
        Label subTitleLabel1 = new Label("Preferred Types");

        CheckBox gateChangeCheckBox = new CheckBox("Gate Change");
        gateChangeCheckBox.setId("gateChangeCheckBox");
        CheckBox cancellationCheckBox = new CheckBox("Cancellation");
        cancellationCheckBox.setId("cancellationCheckBox");
        CheckBox specialOfferCheckBox = new CheckBox("Special Offers");
        specialOfferCheckBox.setId("specialOfferCheckBox");
        CheckBox delayCheckBox = new CheckBox("Delay");
        delayCheckBox.setId("delayCheckBox");

        Label subTitleLabel2 = new Label("Preferred Channels");
        CheckBox emailCheckBox = new CheckBox("Email");
        emailCheckBox.setId("emailCheckBox");
        CheckBox smsCheckBox = new CheckBox("SMS");
        smsCheckBox.setId("smsCheckBox");

        TextField phoneNumberField = new TextField();
        phoneNumberField.setId("phoneNumberField");
        phoneNumberField.setPromptText("Phone Number");
        phoneNumberField.setDisable(true);  // Disattivo per default

        // Si inserisce il numero di telefono dopo aver selezionato sms
        smsCheckBox.selectedProperty().addListener((_, _, newValue) -> {
            phoneNumberField.setDisable(!newValue);
            if (!newValue) {
                phoneNumberField.clear();  // Cancellare phoneNumber se sms non viene più selezionato
            }
        });


        Passenger passenger = flightPlanner.getPassenger(authManager.getLoggedInUser());
        if (passenger == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Passenger not found.");
            return;
        }

        Set<NotificationType> existingTypes = passenger.getPreferredTypes();
        List<NotificationChannel> existingChannels = passenger.getChannels();

        // Selezionare le caselle in base alle preferenze esistenti
        gateChangeCheckBox.setSelected(existingTypes.contains(NotificationType.GATE_CHANGE));
        cancellationCheckBox.setSelected(existingTypes.contains(NotificationType.CANCELLATION));
        specialOfferCheckBox.setSelected(existingTypes.contains(NotificationType.SPECIAL_OFFER));
        delayCheckBox.setSelected(existingTypes.contains(NotificationType.DELAY));

        emailCheckBox.setSelected(existingChannels.stream().anyMatch(channel -> channel instanceof EmailNotification));
        smsCheckBox.setSelected(existingChannels.stream().anyMatch(channel -> channel instanceof SmsNotification));

        if (passenger.getPhoneNumber() != null) {
            phoneNumberField.setText(passenger.getPhoneNumber());
        }

        Button saveButton = new Button("Save");
        saveButton.setId("saveButton");
        saveButton.setOnAction(_ -> {
            // Collezionare i tipi di notificazione selezionati
            Set<NotificationType> types = new HashSet<>();
            if (gateChangeCheckBox.isSelected()) {
                types.add(NotificationType.GATE_CHANGE);
            }
            if (cancellationCheckBox.isSelected()) {
                types.add(NotificationType.CANCELLATION);
            }
            if (specialOfferCheckBox.isSelected()) {
                types.add(NotificationType.SPECIAL_OFFER);
            }
            if (delayCheckBox.isSelected()) {
                types.add(NotificationType.DELAY);
            }

            // Collezionare i canali selezionati
            List<NotificationChannel> channels = new ArrayList<>();
            if (emailCheckBox.isSelected()) {
                channels.add(new EmailNotification());
            }
            if (smsCheckBox.isSelected()) {
                String phoneNumber = phoneNumberField.getText().trim();
                if (!phoneNumber.isEmpty()) {
                    // Si verifica che il numero inizi con il prefisso internazionale di 1-4 cifre + seguito da 6-13 cifre di numeri
                    if (!phoneNumber.matches("^\\+\\d{1,4} \\d{6,13}$")) {
                        showAlert(Alert.AlertType.WARNING, "Warning",
                                "Please include the international prefix (e.g. +39 xxxxxxxxxx for Italy) and the phone number must have between 6 and 13 digits (excluding the international prefix).");
                        return;
                    }

                    channels.add(new SmsNotification());
                    passenger.setPhoneNumber(phoneNumber);
                } else {
                    showAlert(Alert.AlertType.WARNING, "Warning", "Please provide a phone number for SMS notifications.");
                    return;// Impedire il salvataggio se non si ha fornito il numero di telefono
                }
            }

            // Setting di default
            if (types.isEmpty()) {
                types.add(NotificationType.GATE_CHANGE);
                types.add(NotificationType.CANCELLATION);
            }
            if (channels.isEmpty()) {
                channels.add(new EmailNotification());
            }
            try {
                flightPlanner.updatePassengerNotificationPreferences(passenger.getUsername(), types, channels);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            showAlert(Alert.AlertType.INFORMATION, "Success", "Your setting is saved successfully!");
        });

        Button backButton = new Button("Back");
        backButton.setId("backButton");
        backButton.setOnAction(_ -> showMainAppScreen());

        vbox.getChildren().addAll(titleLabel, subTitleLabel1, gateChangeCheckBox, cancellationCheckBox, delayCheckBox,
                specialOfferCheckBox, subTitleLabel2, emailCheckBox, smsCheckBox, phoneNumberField, saveButton, backButton);

        Scene scene = new Scene(vbox, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showAdminInterface(VBox vbox) {
        Label adminLabel = new Label("Admin Panel");

        Button viewFlightsButton = new Button("View Flights");
        viewFlightsButton.setId("viewFlightsButton");
        viewFlightsButton.setOnAction(_ -> showFlightList());

        Button viewPassengersButton = new Button("View Passengers");
        viewPassengersButton.setId("viewPassengersButton");
        viewPassengersButton.setOnAction(_ -> showPassengersList());

        Button addFlightButton = new Button("Add Flight");
        addFlightButton.setId("addFlightButton");
        addFlightButton.setOnAction(_ -> showAddFlightForm());

        Button removeFlightButton = new Button("Remove Flight");
        removeFlightButton.setId("removeFlightButton");
        removeFlightButton.setOnAction((_ -> showRemoveFlightForm()));

        Button updateFlightStatusButton = new Button("Update Flight Status");
        updateFlightStatusButton.setId("updateFlightStatusButton");
        updateFlightStatusButton.setOnAction(_ -> showUpdateFlightStatus());

        Button manageSeatsButton = new Button("Manage Seats");
        manageSeatsButton.setId("manageSeatsButton");
        manageSeatsButton.setOnAction(_ -> showSeatManagementForm());

        Button addAirportButton = new Button("Add Airport");
        addAirportButton.setId("addAirportButton");
        addAirportButton.setOnAction(_ -> showAddAirportForm());

        Button removeAirportButton = new Button("Remove Airport");
        removeAirportButton.setId("removeAirportButton");
        removeAirportButton.setOnAction(_ -> showRemoveAirportForm());

        Button addRouteButton = new Button("Add Route");
        addRouteButton.setId("addRouteButton");
        addRouteButton.setOnAction(_ -> showAddRouteForm());

        Button removeRouteButton = new Button("Remove Route");
        removeRouteButton.setId("removeRouteButton");
        removeRouteButton.setOnAction(_ -> showRemoveRouteForm());


        vbox.getChildren().addAll(adminLabel, viewFlightsButton, viewPassengersButton, addFlightButton,
                removeFlightButton, updateFlightStatusButton, manageSeatsButton, addAirportButton, removeAirportButton,
                addRouteButton, removeRouteButton);

    }

    private void showRemoveRouteForm() {
        VBox routeBox = new VBox(10);

        Label routeIdLabel = new Label("Route ID:");
        TextField routeIdField = new TextField();
        routeIdField.setId("routeIdField");

        Button removeRouteButton = new Button("Remove Route");
        removeRouteButton.setId("removeRouteButton");
        removeRouteButton.setOnAction(_ -> {
            String routeId = routeIdField.getText();

            if (routeId.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter a Route ID.");
                return;
            }

            if (flightPlanner.checkRouteNotExistence(routeId)) {
                showAlert(Alert.AlertType.ERROR, "Invalid Route Id", "Route " + routeId + " not found.");
                return;
            }

            try {
                flightPlanner.removeRoute(routeId);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Route removed: " + routeId);
                routeIdField.clear();
            } catch (IOException ex) {
                showAlert(Alert.AlertType.ERROR, "Removing Route Failed", "Route " + routeId + " not removed. An error occurred while the route was being removed.");
                throw new RuntimeException(ex);
            }


        });

        Button backButton = new Button("Back");
        backButton.setId("backButton");
        backButton.setOnAction(_ -> showMainAppScreen());

        routeBox.getChildren().addAll(routeIdLabel, routeIdField, removeRouteButton, backButton);

        Scene scene = new Scene(routeBox, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Remove Route");
    }

    private void showAddRouteForm() {
        VBox routeBox = new VBox(10);

        Label routeIdLabel = new Label("Route ID:");
        TextField routeIdField = new TextField();
        routeIdField.setId("routeIdField");

        Label departureAirportLabel = new Label("Departure Airport Code:");
        TextField departureAirportField = new TextField();
        departureAirportField.setId("departureAirportField");

        Label arrivalAirportLabel = new Label("Arrival Airport Code:");
        TextField arrivalAirportField = new TextField();
        arrivalAirportField.setId("arrivalAirportField");

        Label distanceLabel = new Label("Distance (km):");
        TextField distanceField = new TextField();
        distanceField.setId("distanceField");

        Label durationLabel = new Label("Duration (HH:MM):");
        TextField durationField = new TextField();
        durationField.setId("durationField");

        Button addRouteButton = new Button("Add Route");
        addRouteButton.setId("addRouteButton");
        addRouteButton.setOnAction(_ -> {
            String routeId = routeIdField.getText();
            String departureAirportCode = departureAirportField.getText();
            String arrivalAirportCode = arrivalAirportField.getText();
            double distance;
            Duration duration;

            if (routeId.isEmpty() || departureAirportCode.isEmpty() || arrivalAirportCode.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Please fill in all the fields.");
                return;
            }

            if(flightPlanner.checkAirportNotExistence(departureAirportCode) || flightPlanner.checkAirportNotExistence(arrivalAirportCode)){
                showAlert(Alert.AlertType.ERROR,"Invalid Input","Check first the existence of the airport.");
                return;
            }

            try {
                distance = Double.parseDouble(distanceField.getText());
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid number for the distance.");
                return;
            }

            try {
                String[] parts = durationField.getText().split(":");
                long hours = Long.parseLong(parts[0]);
                long minutes = Long.parseLong(parts[1]);
                duration = Duration.ofHours(hours).plusMinutes(minutes);
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter the duration in hours:minutes(HH:MM) format.");
                return;
            }

            Route newRoute = new Route(routeId, departureAirportCode, arrivalAirportCode, distance, duration);
            try {
                flightPlanner.addRoute(newRoute);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

            showAlert(Alert.AlertType.INFORMATION, "Success", "Route added: " + routeId);

            routeIdField.clear();
            departureAirportField.clear();
            arrivalAirportField.clear();
            distanceField.clear();
        });

        Button backButton = new Button("Back");
        backButton.setId("backButton");
        backButton.setOnAction(_ -> showMainAppScreen());

        routeBox.getChildren().addAll(routeIdLabel, routeIdField, departureAirportLabel, departureAirportField, arrivalAirportLabel,
                arrivalAirportField, distanceLabel, distanceField, durationLabel, durationField, addRouteButton, backButton);

        Scene scene = new Scene(routeBox, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Add Route");
    }

    private void showRemoveAirportForm() {
        VBox vbox = new VBox(10);

        Label airportCodeLabel = new Label("Airport Code:");
        TextField airportCodeField = new TextField();
        airportCodeField.setId("airportCodeField");
        airportCodeField.setPromptText("Es. FLR");

        Button removeButton = new Button("Remove Airport");
        removeButton.setId("removeButton");
        removeButton.setOnAction(_ -> {
            String code = airportCodeField.getText();

            if (code.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Airport code must be provided.");
                return;
            }

            if (flightPlanner.checkAirportNotExistence(code)) {
                showAlert(Alert.AlertType.ERROR, "Invalid Airport Code", "Airport " + code + " not found.");
                return;
            }

            try {
                flightPlanner.removeAirport(code);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Airport " + code + " removed successfully.");
            } catch (IOException ex) {
                showAlert(Alert.AlertType.ERROR, "Removing Airport Failed", "Airport " + code + " not removed. An error occurred while tha airport was being removed.");
                throw new RuntimeException(ex);
            }

            airportCodeField.clear();
        });

        Button backButton = new Button("Back");
        backButton.setId("backButton");
        backButton.setOnAction(_ -> showMainAppScreen());

        vbox.getChildren().addAll(airportCodeLabel, airportCodeField, removeButton, backButton);

        Scene scene = new Scene(vbox, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Remove Airport");
    }

    private void showAddAirportForm() {
        VBox vbox = new VBox(10);

        Label airportCodeLabel = new Label("Airport Code:");
        TextField airportCodeField = new TextField();
        airportCodeField.setId("airportCodeField");
        airportCodeField.setPromptText("e.g. FLR");

        Label airportNameLabel = new Label("Airport Name:");
        TextField airportNameField = new TextField();
        airportNameField.setId("airportNameField");
        airportNameField.setPromptText("e.g. Amerigo-Vespucci");

        Label cityLabel = new Label("City:");
        TextField cityField = new TextField();
        cityField.setId("cityField");
        cityField.setPromptText("e.g. Florence");

        Label countryLabel = new Label("Country:");
        TextField countryField = new TextField();
        countryField.setId("countryField");
        countryField.setPromptText("e.g. Italy");

        Button addButton = new Button("Add Airport");
        addButton.setId("addButton");
        addButton.setOnAction(_ -> {
            String code = airportCodeField.getText();
            String name = airportNameField.getText();
            String city = cityField.getText();
            String country = countryField.getText();

            if (code.isEmpty() || name.isEmpty() || city.isEmpty() || country.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "All fields must be filled.");
                return;
            }

            Airport airport = new Airport(code, name, city, country);
            try {
                flightPlanner.addAirport(airport);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            showAlert(Alert.AlertType.INFORMATION, "Success", "Airport " + code + " added successfully.");

            airportCodeField.clear();
            airportNameField.clear();
            cityField.clear();
            countryField.clear();
        });

        Button backButton = new Button("Back");
        backButton.setId("backButton");
        backButton.setOnAction(_ -> showMainAppScreen());

        vbox.getChildren().addAll(airportCodeLabel, airportCodeField, airportNameLabel, airportNameField, cityLabel, cityField, countryLabel, countryField, addButton, backButton);

        Scene scene = new Scene(vbox, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Add Airport");
    }

    private void showSeatManagementForm() {
        VBox seatBox = new VBox(10);

        Label flightNumberLabel = new Label("Flight Number:");
        TextField flightNumberField = new TextField();
        flightNumberField.setId("flightNumberField");

        Label classTypeLabel = new Label("Class Type (Economy, Business, First):");
        TextField classTypeField = new TextField();
        classTypeField.setId("classTypeField");

        Label priceLabel = new Label("Seat Price:");
        TextField priceField = new TextField();
        priceField.setId("priceField");

        Button addSeatPriceButton = new Button("Upload/Update Seat Price");
        addSeatPriceButton.setId("addSeatPriceButton");
        addSeatPriceButton.setOnAction(_ -> {
            String flightNumber = flightNumberField.getText();
            String classType = classTypeField.getText();
            double price;

            if (flightNumber.isEmpty() || classType.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter all the fields.");
                return;
            } else if (flightPlanner.checkFlightNotExistence(flightNumber)) {
                showAlert(Alert.AlertType.ERROR, "Error", "Flight number " + flightNumber + " not found!");
                return;
            }

            try {
                price = Double.parseDouble(priceField.getText());
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid number for the price.");
                return; // se il prezzo non è valido, si esce dalla finestra
            }

            if (!classType.equals("Economy") && !classType.equals("Business") && !classType.equals("First")) {
                showAlert(Alert.AlertType.ERROR, "Invalid Class Type",
                        "Please enter a valid class type (Economy, Business, First).");
                return;
            }

            // O si fa aggiornamento dei prezzi per voli esistenti o si caricano nuovi prezzi per i nuovi voli
            if (flightPlanner.getFlightClassPrices().containsKey(flightNumber) && flightPlanner.getFlightClassPrices().get(flightNumber).containsKey(classType)) {
                flightPlanner.updateFlightClassPrices(flightNumber, classType, price);
                showAlert(Alert.AlertType.INFORMATION, "Success Updating", "Seat price update for flight: " + flightNumber + ", Class: " + classType + ", Price: " + price + " EUR");
//                /*double updatedPrice = flightPlanner.getFlightClassPrices().get(flightNumber).get(classType);
//                System.out.println("Updated Price: " + updatedPrice);*/
            } else {
                flightPlanner.addFlightClassPrice(flightNumber, classType, price);
                showAlert(Alert.AlertType.INFORMATION, "Success Uploading", "Seat price set for flight: " + flightNumber + ", Class: " + classType + ", Price: " + price + " EUR");
            }


            flightNumberField.clear();
            classTypeField.clear();
            priceField.clear();
        });
        Button backButton = new Button("Back");
        backButton.setId("backButton");
        backButton.setOnAction(_ -> showMainAppScreen());
        Label seatLabel = new Label("Other Functionality:");
        Button addSeatsToFlightBtn = new Button("Add Seats");
        addSeatsToFlightBtn.setId("addSeatsToFlightBtn");
        addSeatsToFlightBtn.setOnAction(_ -> showAddSeatsToFlight());
        Button removeSeatsFromFlightBtn = new Button("Remove Seats");
        removeSeatsFromFlightBtn.setId("removeSeatsFromFlightBtn");
        removeSeatsFromFlightBtn.setOnAction(_ -> showRemoveSeatsFromFlight());


        seatBox.getChildren().addAll(flightNumberLabel, flightNumberField, classTypeLabel, classTypeField, priceLabel,
                priceField, addSeatPriceButton, seatLabel, addSeatsToFlightBtn, removeSeatsFromFlightBtn, backButton);

        Scene scene = new Scene(seatBox, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Manage Seats, Classes and Prices");
    }

    private void showRemoveSeatsFromFlight() {
        VBox vbox = new VBox(10);

        Label flightNumberLabel = new Label("Flight Number:");
        TextField flightNumberField = new TextField();
        flightNumberField.setId("flightNumberField");
        flightNumberField.setPromptText("Flight Number");

        Label seatNumbersLabel = new Label("Seat Numbers to Remove (comma-separated):");
        TextField seatNumbersField = new TextField();
        seatNumbersField.setId("seatNumbersField");
        seatNumbersField.setPromptText("e.g., 12A, 12B, 13A");

        Button removeSeatsButton = new Button("Remove Seats");
        removeSeatsButton.setId("removeSeatsButton");

        removeSeatsButton.setOnAction(_ -> {
            String flightNumber = flightNumberField.getText();
            String seatNumbers = seatNumbersField.getText();

            String[] seatArray = seatNumbers.split(",");

            if (flightNumber.isEmpty() || seatNumbers.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please fill all the fields.");
                return;
            }

            if(flightPlanner.checkFlightNotExistence(flightNumber)){
                showAlert(Alert.AlertType.ERROR,"Input Error","Flight " + flightNumber + " not found.");
                return;
            }

            for (String seatNumber : seatArray) {
                seatNumber = seatNumber.trim();
                if (flightPlanner.checkSeatNotExistence(seatNumber, flightNumber)) {
                    showAlert(Alert.AlertType.ERROR, "Seat Number Invalid", "Seat " + seatNumber + " not found on flight " + flightNumber);
                    return;
                }
                try {
                    flightPlanner.removeSeatFromFlight(flightNumber, seatNumber);
                } catch (IOException ex) {
                    showAlert(Alert.AlertType.ERROR,"Removing Seat Failed","An error occurred while the seat was being removed.");
                    throw new RuntimeException(ex);
                }
            }

            showAlert(Alert.AlertType.INFORMATION, "Success", "Seats " + seatNumbers + " removed from flight " + flightNumber);
            flightNumberField.clear();
            seatNumbersField.clear();
        });
        Button backButton = new Button("Back");
        backButton.setId("backButton");
        backButton.setOnAction(_ -> showSeatManagementForm());

        vbox.getChildren().addAll(flightNumberLabel, flightNumberField, seatNumbersLabel, seatNumbersField, removeSeatsButton, backButton);
        Scene scene = new Scene(vbox, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Remove Seats from Flight");
    }

    private void showAddSeatsToFlight() {
        VBox vbox = new VBox(10);
        Label flightNumberLabel = new Label("Flight Number:");
        TextField flightNumberField = new TextField();
        flightNumberField.setId("flightNumberField");
        flightNumberField.setPromptText("Enter flight number");

        Label seatListLabel = new Label("Seat List (comma-separated):");
        TextField seatListField = new TextField();
        seatListField.setId("seatListField");
        seatListField.setPromptText("Enter seats (e.g., 13A, 14B)");

        Label classTypeLabel = new Label("Class Type (Economy, Business, First):");
        TextField classTypeField = new TextField();
        classTypeField.setId("classTypeField");
        classTypeField.setPromptText("Enter class type");

        Button addSeatsButton = new Button("Add Seats");
        addSeatsButton.setId("addSeatsButton");

        addSeatsButton.setOnAction(_ -> {
            String flightNumber = flightNumberField.getText();
            String seatList = seatListField.getText();
            String classType = classTypeField.getText();

            if (seatList.isEmpty() || flightNumber.isEmpty() || classType.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please fill all the fields.");
                return;
            }

            if (flightPlanner.checkFlightNotExistence(flightNumber)) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Flight " + flightNumber + " not found.");
                return;
            }

            if (!classType.equals("Economy") &&
                    !classType.equals("Business") &&
                    !classType.equals("First")) {
                showAlert(Alert.AlertType.ERROR, "Invalid Class Type", "Please enter a valid class type (Economy, Business, First).");
                return;
            }

            String[] seats = seatList.split(",");
            Flight flight = flightPlanner.findFlight(flightNumber);

            for (String seatNumber : seats) {
                Seat seat = new Seat(seatNumber.trim(), classType, flightNumber, true);
                int seatCountsOnCSV = flightPlanner.getSeatClassCountsForFlight(flightNumber, seat.getClassType());
                int maxSeatCountForClass = flight.getSeatClassCount().getOrDefault(seat.getClassType(), 0);
                if (maxSeatCountForClass > seatCountsOnCSV) {
                    try {
                        flightPlanner.addSeatToFlight(flightNumber, seat);
                    } catch (IOException ex) {
                        showAlert(Alert.AlertType.ERROR,"Adding Seat Failed","An error occurred while the seat was being added.");
                        throw new RuntimeException(ex);
                    }
                } else {
                    showAlert(Alert.AlertType.ERROR, "Adding Seat Failed", "Cannot add seat " + seat.getSeatNumber() + " to flight "
                            + flightNumber + ". Maximum capacity for " + seat.getClassType() +
                            " reached or there's no seats for this class on this flight.");
                    return;
                }
            }

            showAlert(Alert.AlertType.INFORMATION, "Success", "Seats added to flight " + flightNumber);
            flightNumberField.clear();
            seatListField.clear();
            classTypeField.clear();
        });
        Button backButton = new Button("Back");
        backButton.setId("backButton");
        backButton.setOnAction(_ -> showSeatManagementForm());

        vbox.getChildren().addAll(flightNumberLabel, flightNumberField, seatListLabel, seatListField, classTypeLabel, classTypeField, addSeatsButton, backButton);

        Scene scene = new Scene(vbox, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Add Seats to Flight");
    }

    private void showUpdateFlightStatus() {
        VBox vBox = new VBox(10);

        Label flightLabel = new Label("Flight Number: ");
        TextField flightNumberField = new TextField();
        flightNumberField.setId("flightNumberField");
        flightNumberField.setPromptText("Enter Flight Number");

        Label departureDateLabel = new Label("New Departure Date and Time: ");
        DatePicker departureDatePicker = new DatePicker();
        departureDatePicker.setId("departureDatePicker");
        TextField departureTimeField = new TextField();
        departureTimeField.setId("departureTimeField");
        departureTimeField.setPromptText("New departure Time (HH:MM)");

        Label arrivalDateLabel = new Label("New Arrival Date and Time: ");
        DatePicker arrivalDatePicker = new DatePicker();
        arrivalDatePicker.setId("arrivalDatePicker");
        TextField arrivalTimeField = new TextField();
        arrivalTimeField.setId("arrivalTimeField");
        arrivalTimeField.setPromptText("New arrival Time (HH:MM)");

        TextField updateMsgField = new TextField();
        updateMsgField.setId("updateMsgField");
        updateMsgField.setPromptText("Update Message");

        ComboBox<NotificationType> notificationTypeComboBox = new ComboBox<>();
        notificationTypeComboBox.setId("notificationTypeComboBox");
        notificationTypeComboBox.getItems().addAll(Arrays.stream(NotificationType.values())
                .filter(type -> type != NotificationType.CANCELLATION)
                .toList());
        notificationTypeComboBox.setPromptText("Select Notification Type");

        Button updateFlightStatusBtn = new Button("Update Flight Status");
        updateFlightStatusBtn.setId("updateFlightStatusBtn");

        updateFlightStatusBtn.setOnAction(_ -> {
            String flightNumber = flightNumberField.getText();
            LocalDate newDepartureDate = departureDatePicker.getValue();
            LocalDate newArrivalDate = arrivalDatePicker.getValue();
            NotificationType notificationType = notificationTypeComboBox.getValue();
            String updateMsg = updateMsgField.getText();

            if (flightNumber.isEmpty() || newDepartureDate == null || newArrivalDate == null) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Please fill all the fields.");
                return;
            }

            if (flightPlanner.checkFlightNotExistence(flightNumber)) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Flight " + flightNumber + " not found");
                return;
            }

            // Verificare se il tempo sia nella forma HH:MM
            if (notValidTimeFormat(departureTimeField.getText())) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter the departure time in the format HH:MM.");
                return;
            }
            if (notValidTimeFormat(arrivalTimeField.getText())) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter the arrival time in the format HH:MM.");
                return;
            }

            // Controllare se update message è vuota
            if (updateMsg == null || updateMsg.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter an update message.");
                return;
            }

            try {
                LocalTime newDepartureTime = LocalTime.parse(departureTimeField.getText());
                LocalDateTime newDepartureDateTime = LocalDateTime.of(newDepartureDate, newDepartureTime);
                LocalTime newArrivalTime = LocalTime.parse(arrivalTimeField.getText());
                LocalDateTime newArrivalDateTime = LocalDateTime.of(newArrivalDate, newArrivalTime);

                flightPlanner.updateFlightStatus(flightNumber, newDepartureDateTime, newArrivalDateTime, updateMsg, notificationType);

                showAlert(Alert.AlertType.INFORMATION, "Status Changed", "Status for flight " + flightNumber + " has been updated and notified to passengers who opted for " + notificationType + ". ");
            } catch (IOException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Error updating flight status.");
                throw new RuntimeException(ex);
            }
            flightNumberField.clear();
            departureDatePicker.setValue(null);
            arrivalDatePicker.setValue(null);
            departureTimeField.clear();
            arrivalTimeField.clear();
            updateMsgField.clear();
            notificationTypeComboBox.getSelectionModel().clearSelection();

        });

        Button backButton = new Button("Back");
        backButton.setId("backButton");
        backButton.setOnAction(_ -> showMainAppScreen());

        vBox.getChildren().addAll(flightLabel, flightNumberField, departureDateLabel, departureDatePicker, departureTimeField, arrivalDateLabel, arrivalDatePicker,
                arrivalTimeField, updateMsgField, notificationTypeComboBox, updateFlightStatusBtn, backButton);
        Scene scene = new Scene(vBox, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Update Flight Status");
    }

    private void showRemoveFlightForm() {
        VBox vBox = new VBox(10);
        Label flightLabel = new Label("Remove Flight");

        TextField removeFlightNumberField = new TextField();
        removeFlightNumberField.setId("removeFlightNumberField");
        removeFlightNumberField.setPromptText("Flight Number");

        Button removeFlightBtn = new Button("Remove Flight");
        removeFlightBtn.setId("removeFlightBtn");

        removeFlightBtn.setOnAction(_ -> {
            String flightNumber = removeFlightNumberField.getText();
            if (flightNumber.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "You must enter a flight number.");
                return;
            } else if (flightPlanner.checkFlightNotExistence(flightNumber)) {
                showAlert(Alert.AlertType.ERROR, "Invalid Flight Number", "Flight number " + flightNumber + " not found.");
                return;
            }
            try {
                flightPlanner.removeFlight(flightNumber);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Flight " + flightNumber + " has been removed successfully.");
            } catch (IOException ex) {
                showAlert(Alert.AlertType.ERROR, "Removing Flight Failed", "Flight not removed. An error occurred while the flight " + flightNumber +" was being removed.");
                throw new RuntimeException(ex);
            }
            removeFlightNumberField.clear();
        });

        Button backButton = new Button("Back");
        backButton.setId("backButton");
        backButton.setOnAction(_ -> showMainAppScreen());

        vBox.getChildren().addAll(flightLabel, removeFlightNumberField, removeFlightBtn, backButton);
        Scene scene = new Scene(vBox, 400, 400);
        primaryStage.setScene(scene);
    }

    private void showAddFlightForm() {
        VBox vbox = new VBox(10);
        Label flightLabel = new Label("Add Flight:");

        TextField flightNumberField = new TextField();
        flightNumberField.setId("flightNumberField");
        flightNumberField.setPromptText("Flight Number");

        TextField departureField = new TextField();
        departureField.setId("departureField");
        departureField.setPromptText("Departure Airport Code");

        TextField arrivalField = new TextField();
        arrivalField.setId("arrivalField");
        arrivalField.setPromptText("Arrival Airport Code");

        DatePicker departureDatePicker = new DatePicker();
        departureDatePicker.setId("departureDatePicker");

        TextField departureTimeField = new TextField();
        departureTimeField.setId("departureTimeField");
        departureTimeField.setPromptText("Departure Time (HH:MM)");

        DatePicker arrivalDatePicker = new DatePicker();
        arrivalDatePicker.setId("arrivalDatePicker");
        TextField arrivalTimeField = new TextField();
        arrivalTimeField.setId("arrivalTimeField");
        arrivalTimeField.setPromptText("Arrival Time (HH:MM)");

        TextField economySeatsNumberField = new TextField();
        economySeatsNumberField.setId("economySeatsNumberField");
        economySeatsNumberField.setPromptText("Economy Seats Number");

        TextField businessSeatsNumberField = new TextField();
        businessSeatsNumberField.setId("businessSeatsNumberField");
        businessSeatsNumberField.setPromptText("Business Seats Number");

        TextField firstSeatsNumberField = new TextField();
        firstSeatsNumberField.setId("firstSeatsNumberField");
        firstSeatsNumberField.setPromptText("First Seats Number");

        Button addFlightBtn = new Button("Add Flight");
        addFlightBtn.setId("addFlightBtn");

        addFlightBtn.setOnAction(_ -> {
            String flightNumber = flightNumberField.getText();
            String departure = departureField.getText();
            String arrival = arrivalField.getText();
            LocalDate departureDate = departureDatePicker.getValue();
            LocalDate arrivalDate = arrivalDatePicker.getValue();
            String economySeatsNumberStr = economySeatsNumberField.getText();
            int economySeatsNumber;
            String businessSeatsNumberStr = businessSeatsNumberField.getText();
            int businessSeatsNumber;
            String firstSeatsNumberStr = firstSeatsNumberField.getText();
            int firstSeatsNumber;

            if (flightNumber.isEmpty() || departure.isEmpty() || arrival.isEmpty() || economySeatsNumberStr.isEmpty() || businessSeatsNumberStr.isEmpty()
            || firstSeatsNumberStr.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "All fields are required.");
                return;
            }

            if (departureDate == null || arrivalDate == null) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Please select both departure and arrival dates.");
                return;
            }

            if (notValidTimeFormat(departureTimeField.getText())) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter the departure time in the format HH:MM.");
                return;
            }
            if (notValidTimeFormat(arrivalTimeField.getText())) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter the arrival time in the format HH:MM.");
                return;
            }

            try {
                economySeatsNumber = Integer.parseInt(economySeatsNumberStr);
                businessSeatsNumber = Integer.parseInt(businessSeatsNumberStr);
                firstSeatsNumber = Integer.parseInt(firstSeatsNumberStr);
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter valid numbers.");
                return;
            }

            try {
                LocalTime departureTime = LocalTime.parse(departureTimeField.getText());
                LocalDateTime departureDateTime = LocalDateTime.of(departureDate, departureTime);
                LocalTime arrivalTime = LocalTime.parse(arrivalTimeField.getText());
                LocalDateTime arrivalDateTime = LocalDateTime.of(arrivalDate, arrivalTime);

                Flight flight = new Flight(flightNumber, departure, arrival, departureDateTime, arrivalDateTime, economySeatsNumber, businessSeatsNumber, firstSeatsNumber);

                flightPlanner.addFlight(flight);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Flight " + flight.getFlightNumber() + " has been added successfully.");
            } catch (IOException ex) {
                showAlert(Alert.AlertType.INFORMATION, "Adding Flight Failed", "An error occurred while the flight " + flightNumber +" was being added.");
            }

            flightNumberField.clear();
            departureField.clear();
            arrivalField.clear();
            departureDatePicker.setValue(null);
            arrivalDatePicker.setValue(null);
            departureTimeField.clear();
            arrivalTimeField.clear();
            economySeatsNumberField.clear();
            businessSeatsNumberField.clear();
            firstSeatsNumberField.clear();
        });

        Button backButton = new Button("Back");
        backButton.setId("backButton");
        backButton.setOnAction(_ -> showMainAppScreen());

        vbox.getChildren().addAll(flightLabel, flightNumberField, departureField, arrivalField,
                departureDatePicker, departureTimeField, arrivalDatePicker, arrivalTimeField, economySeatsNumberField,
                businessSeatsNumberField, firstSeatsNumberField, addFlightBtn, backButton);
        Scene scene = new Scene(vbox, 400, 480);
        primaryStage.setScene(scene);
    }

    private void showPassengersList() {
        VBox vbox = new VBox(10);
        Label passengerLabel = new Label("Passengers List");
        passengerLabel.setId("passengerLabel");

        TextArea passengersTextArea = new TextArea();
        passengersTextArea.setId("passengersTextArea");
        passengersTextArea.setEditable(false);
        try {
            List<Passenger> passengers = flightPlanner.getPassengers();
            StringBuilder sb = new StringBuilder();
            for (Passenger passenger : passengers) {
                sb.append(passenger.toString()).append("\n");
            }
            passengersTextArea.setText(sb.toString());
        } catch (Exception e) {
            passengersTextArea.setText("Error retrieving passengers.");
        }
        Button backButton = new Button("Back");
        backButton.setId("backButton");
        backButton.setOnAction(_ -> showMainAppScreen());

        vbox.getChildren().addAll(passengerLabel, passengersTextArea, backButton);
        Scene scene = new Scene(vbox, 600, 400);
        primaryStage.setScene(scene);
    }

    private void showFlightList() {
        VBox vbox = new VBox(10);
        Label flightsLabel = new Label("Flights List");
        flightsLabel.setId("flightsLabel");

        TextArea flightsTextArea = new TextArea();
        flightsTextArea.setId("flightsTextArea");
        flightsTextArea.setEditable(false);

        try {
            List<Flight> flights = flightPlanner.getAllFlights();
            StringBuilder sb = new StringBuilder();
            for (Flight flight : flights) {
                sb.append(flight.toString()).append("\n");
            }
            flightsTextArea.setText(sb.toString());
        } catch (Exception e) {
            flightsTextArea.setText("Error retrieving flights.");
        }

        Button backButton = new Button("Back");
        backButton.setId("backButton");
        backButton.setOnAction(_ -> showMainAppScreen());

        vbox.getChildren().addAll(flightsLabel, flightsTextArea, backButton);
        Scene scene = new Scene(vbox, 550, 400);
        primaryStage.setScene(scene);
    }

    private boolean notValidTimeFormat(String time) {
        try {
            LocalTime.parse(time); // Se il formato non è giusto, restituisce true e viene lanciato un eccezione
            return false;
        } catch (DateTimeParseException e) {
            return true;
        }
    }

    // Metodo di utilità per mostrare messaggi di dialogo
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
