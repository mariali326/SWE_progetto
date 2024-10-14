package flightPlanner;
//da sistemare

import javafx.application.Application;
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

    private void showLoginScreen() {
        VBox vbox = new VBox(10);// Layout verticale con spaziatura di 10 pixel tra gli elementi
        Label titleLabel = new Label("Login");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        Button loginButton = new Button("Login");
        Button registerButton = new Button("Register");

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
                    authManager.getUsers().put(username, password);
                    authManager.getEmails().put(username, email);
                    authManager.getUserRoles().put(username, "Passenger");
                    Passenger passenger = new Passenger(username, "", "", email, "", password, new HashSet<>(), new ArrayList<>(), null);
                    try {
                        flightPlanner.registerPassenger(passenger);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    showAlert(Alert.AlertType.INFORMATION, "Registration Successful", "You can now log in.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Registration Failed", "Username or email already exists.");
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
        Label titleLable = new Label("Flight Planner");

        if (authManager.getCurrentUserRole().equals("Admin")) {
            showAdminInterface(vbox);
        } else {
            showUserInterface(vbox);
        }

        Button logoutButton = new Button("Logout");

        logoutButton.setOnAction(_ -> {
            authManager.logout();
            showLoginScreen();
        });

        // Si aggiungono elementi al layout
        vbox.getChildren().addAll(titleLable, logoutButton);

        // Si configurano scena e stage
        Scene scene = new Scene(vbox, 500, 550);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Flight Planner App");
        primaryStage.show();
    }

    private void showUserInterface(VBox vbox) {
        Label userLabel = new Label("Passenger Label");
        Label welcomeLabel = new Label("Welcome " + authManager.getLoggedInUser() + " !");

        Button viewBookingsButton = new Button("View Bookings");
        viewBookingsButton.setOnAction(_ -> showViewBookings());

        Button bookFlightButton = new Button("Book Flight");
        bookFlightButton.setOnAction(_ -> {
            try {
                showBookFlight();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        Button cancelBookingButton = new Button("Cancel Booking");
        cancelBookingButton.setOnAction(_ -> showCancelBooking());

        Button cancelTicketBtn = new Button("Cancel ticket");
        cancelTicketBtn.setOnAction(_ -> showCancelTicket());

        Button searchFlightsButton = new Button("Search Flights");
        searchFlightsButton.setOnAction(_ -> showSearchFlights());

        Button manageNotificationsButton = new Button("Manage Notifications");
        manageNotificationsButton.setOnAction(_ -> {
            try {
                showManageNotificationsScreen();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        Button paymentMethodButton = new Button("Payment Methods");
        paymentMethodButton.setOnAction(_ -> showPaymentMethod());

        Button updateUserCredentialsBtn = new Button("Update my credentials");
        updateUserCredentialsBtn.setOnAction(_ -> showUpdateCredentials());

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

        vbox.getChildren().addAll(userLabel, welcomeLabel, viewBookingsButton, bookFlightButton, cancelBookingButton,
                cancelTicketBtn, searchFlightsButton, manageNotificationsButton, paymentMethodButton, updateUserCredentialsBtn, unsubscribeButton);
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
        emailInput.setPromptText("Enter new email");
        emailInput.setText(authManager.getLoggedInUserEmail());// Se l'utente inizia a digitare, appare il testo di prompt
        GridPane.setConstraints(emailInput, 1, 0);

        Label oldPasswordLabel = new Label("Old Password:");
        GridPane.setConstraints(oldPasswordLabel, 0, 1);
        PasswordField oldPasswordInput = new PasswordField();
        oldPasswordInput.setPromptText("Enter your old password");
        GridPane.setConstraints(oldPasswordInput, 1, 1);

        Label newPasswordLabel = new Label("New Password:");
        GridPane.setConstraints(newPasswordLabel, 0, 2);
        PasswordField newPasswordInput = new PasswordField();
        newPasswordInput.setPromptText("Enter new password");
        GridPane.setConstraints(newPasswordInput, 1, 2);

        Label confirmPasswordLabel = new Label("Confirm Password:");
        GridPane.setConstraints(confirmPasswordLabel, 0, 3);
        PasswordField confirmPasswordInput = new PasswordField();
        confirmPasswordInput.setPromptText("Re-enter new password");
        GridPane.setConstraints(confirmPasswordInput, 1, 3);


        emailInput.setPrefWidth(300); // Aumenta la larghezza del campo
        newPasswordInput.setPrefWidth(300);

        Button updateButton = new Button("Update");
        GridPane.setConstraints(updateButton, 0, 4);

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
                return;
            }

            if (newPassword.isEmpty() && confirmPassword.isEmpty()) {
                newPassword = authManager.getLoggedInUserPassword();
                ;
                confirmPassword = newPassword;
            }

            if (!oldPassword.equals(authManager.getLoggedInUserPassword())) {
                showAlert(Alert.AlertType.ERROR, "Input Error",
                        "Old password is incorrect. Provide the correct password to update your credentials");
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                showAlert(Alert.AlertType.ERROR, "Password Mismatch", "New passwords do not match.");
                return;
            }

            if (newPassword.length() < 6) {
                showAlert(Alert.AlertType.ERROR, "Invalid Password", "Password must be at least 6 characters long.");
                return;
            }

            updateButton.setDisable(true);
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
            } finally {
                updateButton.setDisable(false);
            }
        });

        Button backButton = new Button("Back");
        GridPane.setConstraints(backButton, 1, 4);
        backButton.setOnAction(_ -> showMainAppScreen());

        grid.getChildren().addAll(emailLabel, emailInput, oldPasswordLabel, oldPasswordInput, newPasswordLabel, newPasswordInput, confirmPasswordLabel, confirmPasswordInput, backButton, updateButton);

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
        } else if (!flightPlanner.checkBookingExistence(bookingId)) {
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

        double refundAmount = ticket.getPrice() * 0.40;

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
            infoAlert.setContentText("Refund of 40% (" + refundAmount + "EUR) for ticket " + ticketNumber + " has been proceed automatically.");
            infoAlert.showAndWait();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void showCancelBooking() {
        TextInputDialog bookingDialog = new TextInputDialog();
        bookingDialog.setHeaderText("Enter Booking ID to Cancel");
        String bookingId = bookingDialog.showAndWait().orElse("");

        if (bookingId.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Enter the booking Id to proceed.");
            return;
        } else if (!flightPlanner.checkBookingExistence(bookingId)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Booking Id " + bookingId + " not found.");
            return;
        }

        Passenger passenger = flightPlanner.getPassenger(authManager.getLoggedInUser());

        Booking booking = flightPlanner.getBookingById(bookingId);
        double refundAmount = booking.getTotalAmount() * 0.40;

        try {
            flightPlanner.cancelBooking(bookingId, passenger);
            //showAlert(Alert.AlertType.INFORMATION, "Success", "You cancelled the booking " + bookingId);
            Alert successesAlert = new Alert(Alert.AlertType.INFORMATION);
            successesAlert.setTitle("Success");
            successesAlert.setHeaderText(null);
            successesAlert.setContentText("You cancelled the booking " + bookingId);
            successesAlert.showAndWait();

            Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
            infoAlert.setTitle("Important Information");
            infoAlert.setHeaderText("Don't worry for your refund!");
            infoAlert.setContentText("Refund of 40% (" + refundAmount + "EUR) for booking " + bookingId + " has been proceed automatically.");
            infoAlert.showAndWait();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
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
        } else if (!flightPlanner.checkFlightExistence(flightNumber)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Flight number " + flightNumber + " not found !");
            return;
        }

        // Si recupera il passeggero loggato
        Passenger mainPassenger = flightPlanner.getPassenger(authManager.getLoggedInUser());
        if (mainPassenger == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Passenger not found.");
            return;
        }

        if (mainPassenger.isRegisteredForFlight(flightPlanner.findFlight(flightNumber))) {
            showAlert(Alert.AlertType.ERROR, "Duplicate Booking", "You have already a booking for flight " + flightNumber);
            return;
        }

        // se l'utente in precedenza aveva già effettuato delle prenotazioni, il suo nome e cognome vengono recuperati per default
        TextInputDialog firstNameDialog = new TextInputDialog(mainPassenger.getName().isEmpty() ? "" : mainPassenger.getName());
        firstNameDialog.setHeaderText("Enter Main Passenger First Name");
        String firstName = firstNameDialog.showAndWait().orElse("");

        TextInputDialog lastNameDialog = new TextInputDialog(mainPassenger.getSurname().isEmpty() ? "" : mainPassenger.getSurname());
        lastNameDialog.setHeaderText("Enter Main Passenger Last Name");
        String lastName = lastNameDialog.showAndWait().orElse("");

        if (firstName.isEmpty() || lastName.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Passenger first name and last name are required.");
            return;
        }
        mainPassenger.setName(firstName);
        mainPassenger.setSurname(lastName);

        // Dialogo per numero di biglietti
        TextInputDialog ticketCountDialog = new TextInputDialog();
        ticketCountDialog.setHeaderText("How many tickets do you want to book?");
        String ticketCountStr = ticketCountDialog.showAndWait().orElse("");
        int ticketCount;

        try {
            ticketCount = Integer.parseInt(ticketCountStr);

            if (ticketCount <= 0) {
                showAlert(Alert.AlertType.ERROR, "Error", "You must choose a number greater than 0.");
                return;
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid ticket number.");
            return;
        }

        // Si recuperano i posti disponibili per il volo con il tipo di classe associato
        Map<String, String> availableSeats = flightPlanner.getAvailableSeatsWithClass(flightNumber);

        // Se non ci sono posti disponibili
        if (availableSeats.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "No available seats for this flight.");
            return;
        }

        // Una mappa per tenere traccia dei posti assegnati a ciascun passeggero nella prenotazione attuale
        Map<String, String> currentBookingSeats = new HashMap<>();
        // Lista dei biglietti prenotati
        List<Ticket> tickets = new ArrayList<>();

        // Si genera un ID unico per la prenotazione
        String bookingId = UUID.randomUUID().toString();

        double totalPrice = 0;

        // Ciclo per ogni biglietto da creare
        for (int i = 0; i < ticketCount; i++) {
            // Si chiede nome e cognome per ogni passeggero
            String passengerName;
            String passengerLastName;

            // Se è il primo biglietto, usa il nome del main passenger
            if (i == 0) {
                passengerName = firstName;  // Nome principale (main passenger)
                passengerLastName = lastName; // Cognome principale (main passenger)
            } else {
                TextInputDialog additionalFirstNameDialog = new TextInputDialog();
                additionalFirstNameDialog.setHeaderText("Enter First Name for Passenger " + (i + 1));
                passengerName = additionalFirstNameDialog.showAndWait().orElse("");

                TextInputDialog additionalLastNameDialog = new TextInputDialog();
                additionalLastNameDialog.setHeaderText("Enter Last Name for Passenger " + (i + 1));
                passengerLastName = additionalLastNameDialog.showAndWait().orElse("");

                Passenger additionalPassenger = new Passenger("not defined", passengerName, passengerLastName, authManager.getLoggedInUserEmail(), "", "", new HashSet<>(), new ArrayList<>(), null);
                if (!flightPlanner.checkPassengerExistence(additionalPassenger))
                    flightPlanner.registerPassenger(additionalPassenger);
            }
            // Si presenta una lista di posti con il tipo di classe accanto
            List<String> seatOptions = new ArrayList<>();
            for (Map.Entry<String, String> entry : availableSeats.entrySet()) {
                seatOptions.add("Seat: " + entry.getKey() + " (" + entry.getValue() + ")");
            }

            if (seatOptions.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "No available seat options.");
                return;
            }

            ChoiceDialog<String> seatDialog = new ChoiceDialog<>(seatOptions.get(0), seatOptions);
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
                flightPlanner.releaseSeatsForCurrentBooking(flightNumber, currentBookingSeats);
                showAlert(Alert.AlertType.ERROR, "Error", "You closed the seat selection. Booking has been cancelled.");
                return; // Prenotazione fallita
            }

            // Si calcola il prezzo in base al tipo di classe del posto selezionato
            double pricePerTicket;
            try {
                pricePerTicket = flightPlanner.getPrice(flightNumber, selectedClassType);
                System.out.println("The ticket price for " + selectedSeat + " " + selectedClassType + " of flight " + flightNumber + " is " + pricePerTicket + " EUR.");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Unable to retrieve price for the selected seat " + selectedSeat + " and class type " + selectedClassType + ". ");
                return;
            }
            totalPrice += pricePerTicket;
            // Si genera un numero di biglietto unico
            String ticketNumber = UUID.randomUUID().toString();

            // Si crea un nuovo biglietto con i parametri richiesti
            Ticket ticket = new Ticket(ticketNumber, bookingId, flightNumber, selectedSeat, pricePerTicket, passengerName, passengerLastName);
            tickets.add(ticket);
            flightPlanner.addTicket(ticket);
        }

        LocalDateTime currentDT = LocalDateTime.now();

        // Si aggiunge la prenotazione e i biglietti al sistema passando l'ID generato
        try {
            flightPlanner.bookFlightForPassenger(flightNumber, mainPassenger, tickets, bookingId);
            showAlert(Alert.AlertType.INFORMATION, "Notification", "Flight almost successfully booked!\nTotal Price: " + totalPrice + " EUR");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to book flight: " + e.getMessage());
        }

        Label paymentLabel = new Label("Payment Process:");
        Button payButton = new Button("Pay");
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
                payButton.setDisable(false);
                double finalTotalPrice1 = totalPrice;
                payButton.setOnAction(_ -> {
                    Payment newPayAfterChoice = new Payment(paymentId, bookingId, finalTotalPrice1, currentDT, mainPassenger.getPaymentMethod(), mainPassenger.getUsername());
                    try {
                        flightPlanner.addPayment(newPayAfterChoice);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }

                    showMainAppScreen();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Payment successful with " + mainPassenger.getPaymentMethod() + "!");

                });
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "You need to select a payment method to proceed.");
                return;
            }
        }
        Button cancelButton = new Button("Cancel Booking");
        cancelButton.setOnAction(_ -> {
            try {
                flightPlanner.cancelBooking(bookingId, mainPassenger);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            showMainAppScreen();
            showAlert(Alert.AlertType.INFORMATION, "Cancelled", "You didn't finish the payment. Your booking has been cancelled.");

        });

        vbox.getChildren().addAll(paymentLabel, payButton, cancelButton);

        Scene scene = new Scene(vbox, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setTitle("New Flight Booking");
    }

    private void showSearchFlights() {
        VBox vbox = new VBox(10);
        Label titleLabel = new Label("Searching Results");
        TextInputDialog departureDialog = new TextInputDialog();
        departureDialog.setHeaderText("Enter Departure City or Airport Code");
        String departure = departureDialog.showAndWait().orElse("");

        if (!isValidAirportOrCity(departure)) {
            showAlert(Alert.AlertType.ERROR, "Invalid Departure", "Please enter a valid departure city or airport code.");
            return;
        }

        TextArea resultArea = new TextArea();
        resultArea.setEditable(false);

        TextInputDialog arrivalDialog = new TextInputDialog();
        arrivalDialog.setHeaderText("Enter Arrival City or Airport Code");
        String arrival = arrivalDialog.showAndWait().orElse("");

        if (!isValidAirportOrCity(arrival)) {
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

    private boolean isValidAirportOrCity(String input) {
        List<Airport> airports = flightPlanner.getAllAirports();
        return airports.stream()
                .anyMatch(a -> a.getCode().equalsIgnoreCase(input) || a.getCity().equalsIgnoreCase(input));
    }

    private void showViewBookings() {
        VBox vbox = new VBox(10);
        Label titleLabel = new Label("My bookings");
        TextArea resultArea = new TextArea();
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
                bookingsText.append("Total Amount: ").append(booking.getTotalAmount()).append("\n");
                bookingsText.append("Tickets:\n");

                // Si itera attraverso i biglietti della prenotazione e si stampano i dettagli di ciascun biglietto
                for (Ticket ticket : booking.getTickets()) {
                    bookingsText.append("Ticket Number: ").append(ticket.getTicketNumber())
                            .append(", Flight Number: ").append(ticket.getFlightNumber())
                            .append(", Seat: ").append(ticket.getSeatNumber())
                            .append(", Passenger Name: ").append(ticket.getPassengerName())
                            .append(" Passenger Surname: ").append(ticket.getPassengerSurname())
                            .append("\n");
                }
                bookingsText.append("\n"); // Si aggiunge una riga vuota tra una prenotazione e l'altra
            }

            resultArea.setText(bookingsText.toString());
        }
        Button backButton = new Button("Back");
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
        payMethodComboBox.getItems().addAll(PaymentMethod.values());
        payMethodComboBox.setPromptText("Select Payment Method");

        Button confirmButton = new Button("Confirm");
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
        CheckBox cancellationCheckBox = new CheckBox("Cancellation");
        CheckBox specialOfferCheckBox = new CheckBox("Special Offers");
        CheckBox delayCheckBox = new CheckBox("Delay");

        Label subTitleLabel2 = new Label("Preferred Channels");
        CheckBox emailCheckBox = new CheckBox("Email");
        CheckBox smsCheckBox = new CheckBox("SMS");

        TextField phoneNumberField = new TextField();
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
        viewFlightsButton.setOnAction(_ -> showFlightList());

        Button viewPassengersButton = new Button("View Passengers");
        viewPassengersButton.setOnAction(_ -> showPassengersList());

        Button addFlightButton = new Button("Add Flight");
        addFlightButton.setOnAction(_ -> showAddFlightForm());

        Button removeFlightButton = new Button("Remove Flight");
        removeFlightButton.setOnAction((_ -> showRemoveFlightForm()));

        Button updateFlightStatusButton = new Button("Update Flight Status");
        updateFlightStatusButton.setOnAction(_ -> showUpdateFlightStatus());

        Button manageSeatsButton = new Button("Manage Seats");
        manageSeatsButton.setOnAction(_ -> showSeatManagementForm());

        Button addAirportButton = new Button("Add Airport");
        addAirportButton.setOnAction(_ -> showAddAirportForm());

        Button removeAirportButton = new Button("Remove Airport");
        removeAirportButton.setOnAction(_ -> showRemoveAirportForm());

        Button addRouteButton = new Button("Add Route");
        addRouteButton.setOnAction(_ -> showAddRouteForm());

        Button removeRouteButton = new Button("Remove Route");
        removeRouteButton.setOnAction(_ -> showRemoveRouteForm());


        vbox.getChildren().addAll(adminLabel, viewFlightsButton, viewPassengersButton, addFlightButton,
                removeFlightButton, updateFlightStatusButton, manageSeatsButton, addAirportButton, removeAirportButton,
                addRouteButton, removeRouteButton);

    }

    private void showRemoveRouteForm() {
        VBox routeBox = new VBox(10);

        Label routeIdLabel = new Label("Route ID:");
        TextField routeIdField = new TextField();

        Button removeRouteButton = new Button("Remove Route");
        removeRouteButton.setOnAction(_ -> {
            String routeId = routeIdField.getText();

            if (routeId.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter a Route ID.");
                return;
            }

            try {
                flightPlanner.removeRoute(routeId);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Route removed: " + routeId);
                routeIdField.clear();
            } catch (IOException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Route " + routeId + " not removed. An error occurred while removing the route.");
                throw new RuntimeException(ex);
            }


        });

        Button backButton = new Button("Back");
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

        Label departureAirportLabel = new Label("Departure Airport Code:");
        TextField departureAirportField = new TextField();

        Label arrivalAirportLabel = new Label("Arrival Airport Code:");
        TextField arrivalAirportField = new TextField();

        Label distanceLabel = new Label("Distance (km):");
        TextField distanceField = new TextField();

        Label durationLabel = new Label("Duration (HH:MM):");
        TextField durationField = new TextField();

        Button addRouteButton = new Button("Add Route");
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
        airportCodeField.setPromptText("Es. FLR");

        Button removeButton = new Button("Remove Airport");
        removeButton.setOnAction(_ -> {
            String code = airportCodeField.getText();

            if (code.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Airport code must be provided.");
                return;
            }

            try {
                flightPlanner.removeAirport(code);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Airport " + code + " removed successfully.");
            } catch (IOException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Airport " + code + " not removed.");
                throw new RuntimeException(ex);
            }

            airportCodeField.clear();
        });

        Button backButton = new Button("Back");
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
        airportCodeField.setPromptText("e.g. FLR");

        Label airportNameLabel = new Label("Airport Name:");
        TextField airportNameField = new TextField();
        airportNameField.setPromptText("e.g. Amerigo-Vespucci");

        Label cityLabel = new Label("City:");
        TextField cityField = new TextField();
        cityField.setPromptText("e.g. Florence");

        Label countryLabel = new Label("Country:");
        TextField countryField = new TextField();
        countryField.setPromptText("e.g. Italy");

        Button addButton = new Button("Add Airport");
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
            showAlert(Alert.AlertType.INFORMATION, "Success", "Airport " + name + " added successfully.");

            airportCodeField.clear();
            airportNameField.clear();
            cityField.clear();
            countryField.clear();
        });

        Button backButton = new Button("Back");
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

        Label classTypeLabel = new Label("Class Type (Economy, Business, First):");
        TextField classTypeField = new TextField();

        Label priceLabel = new Label("Seat Price:");
        TextField priceField = new TextField();

        Button addSeatPriceButton = new Button("Upload/Update Seat Price");
        addSeatPriceButton.setOnAction(_ -> {
            String flightNumber = flightNumberField.getText();
            String classType = classTypeField.getText();
            double price;

            if (flightNumber.isEmpty() || classType.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter all the fields.");
                return;
            } else if (!flightPlanner.checkFlightExistence(flightNumber)) {
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

            // Prima di aggiornare si deve controllare se il volo esiste
            if (flightPlanner.getFlightClassPrices().containsKey(flightNumber)) {
                flightPlanner.updateFlightClassPrices(flightNumber, classType, price);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Seat price set for flight: " + flightNumber + ", Class: " + classType + ", Price: " + price + " EUR");
                //double updatedPrice = flightPlanner.getFlightClassPrices().get(flightNumber).get(classType);
                //System.out.println("Updated Price: " + updatedPrice);
            } else {
                showAlert(Alert.AlertType.ERROR, "Invalid Flight Number", "Flight number " + flightNumber + " not found.");
                System.out.println("Invalid flight number: " + flightNumber);
            }


            flightNumberField.clear();
            classTypeField.clear();
            priceField.clear();
        });
        Button backButton = new Button("Back");
        backButton.setOnAction(_ -> showMainAppScreen());
        Label seatLabel = new Label("Other Functionality:");
        Button addSeatsToFlightBtn = new Button("Add Seats");
        addSeatsToFlightBtn.setOnAction(_ -> showAddSeatsToFlight());
        Button removeSeatsFromFlightBtn = new Button("Remove Seats");
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
        flightNumberField.setPromptText("Flight Number");

        Label seatNumbersLabel = new Label("Seat Numbers to Remove (comma-separated):");
        TextField seatNumbersField = new TextField();
        seatNumbersField.setPromptText("e.g., 12A, 12B, 13A");

        Button removeSeatsButton = new Button("Remove Seats");

        removeSeatsButton.setOnAction(_ -> {
            String flightNumber = flightNumberField.getText();
            String seatNumbers = seatNumbersField.getText();

            String[] seatArray = seatNumbers.split(",");

            if (flightNumber.isEmpty() || seatNumbers.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please fill all the fields.");
                return;
            }

            for (String seatNumber : seatArray) {
                seatNumber = seatNumber.trim();
                try {
                    flightPlanner.removeSeatFromFlight(flightNumber, seatNumber);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }

            showAlert(Alert.AlertType.INFORMATION, "Success", "Seats " + seatNumbers + " removed from flight " + flightNumber);
            flightNumberField.clear();
            seatNumbersField.clear();
        });
        Button backButton = new Button("Back");
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
        flightNumberField.setPromptText("Enter flight number");

        Label seatListLabel = new Label("Seat List (comma-separated):");
        TextField seatListField = new TextField();
        seatListField.setPromptText("Enter seats (e.g., 13A, 14B)");

        Label classTypeLabel = new Label("Class Type (Economy, Business, First):");
        TextField classTypeField = new TextField();
        classTypeField.setPromptText("Enter class type");

        Button addSeatsButton = new Button("Add Seats");

        addSeatsButton.setOnAction(_ -> {
            String flightNumber = flightNumberField.getText();
            String seatList = seatListField.getText();
            String classType = classTypeField.getText();

            if (seatList.isEmpty() || flightNumber.isEmpty() || classType.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please fill all the fields.");
                return;
            }
            if (!classType.equals("Economy") &&
                    !classType.equals("Business") &&
                    !classType.equals("First")) {
                showAlert(Alert.AlertType.ERROR, "Invalid Class Type", "Please enter a valid class type (Economy, Business, First).");
                return;
            }

            String[] seats = seatList.split(",");

            for (String seatNumber : seats) {
                Seat seat = new Seat(seatNumber.trim(), classType, flightNumber, true);
                try {
                    flightPlanner.addSeatToFlight(flightNumber, seat);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }

            showAlert(Alert.AlertType.INFORMATION, "Success", "Seats added to flight " + flightNumber);
            flightNumberField.clear();
            seatListField.clear();
            classTypeField.clear();
        });
        Button backButton = new Button("Back");
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
        flightNumberField.setPromptText("Enter Flight Number");

        Label departureDateLabel = new Label("New Departure Date and Time: ");
        DatePicker departureDatePicker = new DatePicker();
        TextField departureTimeField = new TextField();
        departureTimeField.setPromptText("New departure Time (HH:MM)");

        Label arrivalDateLabel = new Label("New Arrival Date and Time: ");
        DatePicker arrivalDatePicker = new DatePicker();
        TextField arrivalTimeField = new TextField();
        arrivalTimeField.setPromptText("New arrival Time (HH:MM)");

        TextField updateMsgField = new TextField();
        updateMsgField.setPromptText("Update Message");

        ComboBox<NotificationType> notificationTypeComboBox = new ComboBox<>();
        notificationTypeComboBox.getItems().addAll(NotificationType.values());
        notificationTypeComboBox.setPromptText("Select Notification Type");

        Button updateFlightStatusBtn = new Button("Update Flight Status");

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

            if (!flightPlanner.checkFlightExistence(flightNumber)) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Flight " + flightNumber + " not found");
                return;
            }

            // Verificare che il tempo sia nella forma HH:MM
            if (!isValidTimeFormat(departureTimeField.getText())) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter the departure time in the format HH:MM.");
                return;
            }
            if (!isValidTimeFormat(arrivalTimeField.getText())) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter the arrival time in the format HH:MM.");
                return;
            }

            // Controllare se l'update message è vuota
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
        removeFlightNumberField.setPromptText("Flight Number");

        Button removeFlightBtn = new Button("Remove Flight");

        removeFlightBtn.setOnAction(_ -> {
            String flightNumber = removeFlightNumberField.getText();
            if (flightNumber.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "You must enter a flight number.");
                return;
            } else if (!flightPlanner.checkFlightExistence(flightNumber)) {
                showAlert(Alert.AlertType.ERROR, "Invalid Flight Number", "Flight number " + flightNumber + " not found.");
                return;
            }
            try {
                flightPlanner.removeFlight(flightNumber);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Flight " + flightNumber + " has been removed successfully.");
            } catch (IOException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Error removing flight.");
                throw new RuntimeException(ex);
            }
            removeFlightNumberField.clear();
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(_ -> showMainAppScreen());

        vBox.getChildren().addAll(flightLabel, removeFlightNumberField, removeFlightBtn, backButton);
        Scene scene = new Scene(vBox, 400, 400);
        primaryStage.setScene(scene);
    }

    private void showAddFlightForm() {
        VBox vbox = new VBox(10);
        Label flightLabel = new Label("Add Flight:");

        TextField flightNumberField = new TextField();
        flightNumberField.setPromptText("Flight Number");

        TextField departureField = new TextField();
        departureField.setPromptText("Departure Airport Code");

        TextField arrivalField = new TextField();
        arrivalField.setPromptText("Arrival Airport Code");

        DatePicker departureDatePicker = new DatePicker();

        TextField departureTimeField = new TextField();
        departureTimeField.setPromptText("Departure Time (HH:MM)");

        DatePicker arrivalDatePicker = new DatePicker();
        TextField arrivalTimeField = new TextField();
        arrivalTimeField.setPromptText("Arrival Time (HH:MM)");


        Button addFlightBtn = new Button("Add Flight");

        addFlightBtn.setOnAction(_ -> {
            String flightNumber = flightNumberField.getText();
            String departure = departureField.getText();
            String arrival = arrivalField.getText();
            LocalDate departureDate = departureDatePicker.getValue();
            LocalDate arrivalDate = arrivalDatePicker.getValue();

            if (flightNumber.isEmpty() || departure.isEmpty() || arrival.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "All fields are required.");
                return;
            }

            if (departureDate == null || arrivalDate == null) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Please select both departure and arrival dates.");
                return;
            }

            if (!isValidTimeFormat(departureTimeField.getText())) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter the departure time in the format HH:MM.");
                return;
            }
            if (!isValidTimeFormat(arrivalTimeField.getText())) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter the arrival time in the format HH:MM.");
                return;
            }

            try {
                LocalTime departureTime = LocalTime.parse(departureTimeField.getText());
                LocalDateTime departureDateTime = LocalDateTime.of(departureDate, departureTime);
                LocalTime arrivalTime = LocalTime.parse(arrivalTimeField.getText());
                LocalDateTime arrivalDateTime = LocalDateTime.of(arrivalDate, arrivalTime);

                Flight flight = new Flight(flightNumber, departure, arrival, departureDateTime, arrivalDateTime);

                flightPlanner.addFlight(flight);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Flight " + flight.getFlightNumber() + " has been added successfully.");
            } catch (IOException ex) {
                showAlert(Alert.AlertType.INFORMATION, "Error", "Error adding flight.");
            }

            flightNumberField.clear();
            departureField.clear();
            arrivalField.clear();
            departureDatePicker.setValue(null);
            arrivalDatePicker.setValue(null);
            departureTimeField.clear();
            arrivalTimeField.clear();
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(_ -> showMainAppScreen());

        vbox.getChildren().addAll(flightLabel, flightNumberField, departureField, arrivalField,
                departureDatePicker, departureTimeField,
                arrivalDatePicker, arrivalTimeField,
                addFlightBtn, backButton);
        Scene scene = new Scene(vbox, 400, 400);
        primaryStage.setScene(scene);
    }

    private void showPassengersList() {
        VBox vbox = new VBox(10);
        Label passengerLabel = new Label("Passengers List");

        TextArea passengersTextArea = new TextArea();
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
        backButton.setOnAction(_ -> showMainAppScreen());

        vbox.getChildren().addAll(passengerLabel, passengersTextArea, backButton);
        Scene scene = new Scene(vbox, 600, 400);
        primaryStage.setScene(scene);
    }

    private void showFlightList() {
        VBox vbox = new VBox(10);
        Label flightsLabel = new Label("Flights List");

        TextArea flightsTextArea = new TextArea();
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
        backButton.setOnAction(_ -> showMainAppScreen());

        vbox.getChildren().addAll(flightsLabel, flightsTextArea, backButton);
        Scene scene = new Scene(vbox, 550, 400);
        primaryStage.setScene(scene);
    }

    private boolean isValidTimeFormat(String time) {
        try {
            LocalTime.parse(time); // Viene lanciato un'eccezione se il formato è sbagliato
            return true;
        } catch (DateTimeParseException e) {
            return false;
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
