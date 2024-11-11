package businessLogicTest;

import businessLogic.FlightPlanner;
import businessLogic.FlightPlannerApp;
import domainModel.*;
import manager_CSV.*;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxAssert;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.base.WindowMatchers;
import org.testfx.matcher.control.TextInputControlMatchers;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.assertions.api.Assertions.assertThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import static org.testfx.matcher.control.LabeledMatchers.hasText;


public class FlightPlannerAppTest extends ApplicationTest {

    private FlightPlannerApp flightPlannerApp;
    private AuthManager authManager;
    private FlightPlanner flightPlanner;

    @Override
    public void start(Stage stage) throws Exception {
        flightPlannerApp = new FlightPlannerApp();
        flightPlannerApp.start(stage);
        authManager = flightPlannerApp.getAuthManager();
        flightPlanner = flightPlannerApp.getFlightPlanner();
    }

    @Test
    @DisplayName("Test that simulates the login process of a user with valid credentials")
    public void testShowLoginScreenWithValidCredentials() throws IOException {
        authManager.register("testuser", "password123", "testuser@example.com", "Passenger");

        clickOn("#usernameField").write("testuser");
        clickOn("#passwordField").write("password123");
        clickOn("#loginButton");

        // Verifica che la schermata principale venga mostrata dopo il login
        assertThat(flightPlannerApp.getPrimaryStage().getTitle()).isEqualTo("Flight Planner App");
    }

    @Test
    @DisplayName("Test that simulates the login process of a user with invalid credentials")
    public void testShowLoginScreenWithInvalidCredentials() {
        clickOn("#usernameField").write("wronguser");
        clickOn("#passwordField").write("wrongpassword");
        clickOn("#loginButton");

        verifyAlertDialog("Invalid username or password.");
    }

    @Test
    @DisplayName("Test that simulates the registration of a new user with valid data")
    public void testShowLoginScreenRegisterWithValidData() {
        clickOn("#usernameField").write("newuser");
        clickOn("#passwordField").write("password123");
        clickOn("#emailField").write("newuser@example.com");
        clickOn("#registerButton");

        assertThat(authManager.getUsers().containsKey("newuser")).isTrue();
    }

    @Test
    @DisplayName("Test that simulates the registration of a user with an invalid password")
    public void testShowLoginScreenRegisterWithInvalidData() {
        clickOn("#usernameField").write("newuser1");
        clickOn("#passwordField").write("pass");
        clickOn("#emailField").write("newuser1@example.com");
        clickOn("#registerButton");

        verifyAlertDialog("Password must be at least 6 characters long.");
    }

    private void verifyAlertDialog(String expectedContent) {
        // Trova il DialogPane di alert
        DialogPane dialogPane = lookup(".dialog-pane").queryAs(DialogPane.class);

        assertThat(dialogPane.getHeaderText()).isNull();
        assertThat(dialogPane.getContentText()).isEqualTo(expectedContent);
    }

    @Test
    @DisplayName("Test that simulates the layout of the logout process")
    public void testShowLoginScreenLogoutButtonAction() {

        authManager.login("admin", "admin123");
        flightPlannerApp.showAppScreen();

        sleep(100);
        clickOn("#logoutButton");

        assertThat(flightPlannerApp.getPrimaryStage().getTitle()).isEqualTo("Flight Planner - Login");
    }

    // Test su interfaccia utente
    @Test
    @DisplayName("Test that simulates the layout of the viewBookingsButton for a user who already has a booking")
    public void testShowViewBookings_WithBookings() {
        authManager.login("jdoe", "pass123");
        flightPlannerApp.showAppScreen();
        sleep(100);
        clickOn("#viewBookingsButton");

        verifyThat("#titleLabel", hasText("My bookings"));// hasText si può usare solo per nodi
        TextArea resultArea = lookup("#resultArea").query();
        String expectedText = """
                Booking ID: BK001
                Flight number: F001
                Date: 2024-09-19T08:05
                Total Amount: 1000.0 EUR
                Tickets:
                Ticket Number: T001, Flight Number: F001, Seat: 1A, Passenger: John Doe
                Luggage: cabin luggage: 40.0x30.0x20.0, 8.0kg, Cost: 0.0 EUR
                """;
        assertTrue(resultArea.getText().contains(expectedText), "Expected text not found in TextArea.");

        verifyThat("#backButton", isVisible());
    }

    @Test
    @DisplayName("Test that simulates the layout of the viewBookingsButton for a user who doesn't have a booking")
    public void testShowViewBookings_NoBookings() throws IOException {
        authManager.register("emptyBookingsUser", "password", "emptyUser@example.com", "Passenger");
        authManager.login("emptyBookingsUser", "password");
        flightPlanner.registerPassenger(new Passenger("emptyBookingsUser", "Blank", "Bookings", "emptyUser@example.com",
                null, "password", null, null, null, null, null));

        flightPlannerApp.showAppScreen();
        sleep(100);
        clickOn("#viewBookingsButton");

        verifyThat("#titleLabel", hasText("My bookings"));

        TextArea resultArea = lookup("#resultArea").query();
        String expectedText = "No bookings found.";
        assertTrue(resultArea.getText().contains(expectedText), "Expected text not found in TextArea.");

        verifyThat("#backButton", isVisible());
    }

    @Test
    @DisplayName("Test simulating the successful flight booking process for a passenger")
    public void testShowBookFlight() {
        authManager.login("jsmith", "password145");
        Passenger mainPassenger = flightPlanner.getPassenger("jsmith");
        flightPlannerApp.showAppScreen();
        sleep(100);

        // Caso in cui si cancella la prenotazione all'ultimo momento
        clickOn("#bookFlightButton");

        write("F004");
        clickOn("OK");
        // Dare conferma per il nome, cognome e tipo di documento mostrato per default
        clickOn("OK");
        clickOn("OK");
        clickOn("OK"); // Serve per chiudere la notifica
        clickOn("OK");
        write("ID123H456");
        clickOn("OK");
        write("1"); // Numero di biglietti
        clickOn("OK");
        clickOn("OK"); // Non si sceglie il posto
        clickOn("OK"); // Serve per chiudere la notifica sui bagagli
        write("0"); // Non si sceglie bagagli a mano
        clickOn("OK");
        write("0"); // Non si sceglie bagagli da stiva
        clickOn("OK");
        clickOn("OK"); // Si chiude la notifica sul prezzo

        clickOn("#cancelButton");
        verifyAlertDialog("You didn't finish the payment. Your booking has been cancelled.");

        // Caso in cui si porta a termine la prenotazione
        clickOn("OK");
        clickOn("#bookFlightButton");

        write("F004");
        clickOn("OK");
        // Dare conferma per il nome, cognome e tipo di documento mostrato per default
        clickOn("OK");
        clickOn("OK");
        clickOn("OK"); // Serve per chiudere la notifica
        clickOn("OK");
        write("ID123H456");
        clickOn("OK");
        write("1"); // Numero di biglietti
        clickOn("OK");
        clickOn("OK"); // Non si sceglie il posto
        clickOn("OK"); // Serve per chiudere la notifica sui bagagli
        write("0"); // Non si sceglie bagagli a mano
        clickOn("OK");
        write("0"); // Non si sceglie bagagli da stiva
        clickOn("OK");
        clickOn("OK"); // Si chiude la notifica sul prezzo

        clickOn("#payButton");
        verifyAlertDialog("Payment successful with " + mainPassenger.getPaymentMethod() + "!");
    }

    @Test
    @DisplayName("Test simulating a failed booking flight process")
    public void testShowBookingFlight_Error() {
        authManager.login("jdoe", "pass123");
        flightPlannerApp.showAppScreen();
        sleep(100);

        clickOn("#bookFlightButton");
        String flightNumber = "F001";

        write(flightNumber);
        clickOn("OK");
        // Dare conferma per il nome, cognome e tipo di documento mostrato per default
        clickOn("OK");
        clickOn("OK");
        clickOn("OK"); // Serve per chiudere la notifica
        clickOn("OK");
        write("ID0123456");
        clickOn("OK");
        verifyAlertDialog("You have already a booking for the flight " + flightNumber);

        String wrongFlightNumber = "NotExistingFlight";
        clickOn("OK");
        clickOn("#bookFlightButton");
        write(wrongFlightNumber);
        clickOn("OK");
        verifyAlertDialog("Flight number " + wrongFlightNumber + " not found !");

        clickOn("OK");
        clickOn("#bookFlightButton");
        write("F004");
        clickOn("OK");
        // Dare conferma per il nome, cognome e tipo di documento mostrato per default
        clickOn("OK");
        clickOn("OK");
        clickOn("OK"); // Serve per chiudere la notifica
        clickOn("OK");
        write("ID0123456");
        clickOn("OK");
        write("2"); // Numero di biglietti
        clickOn("OK");
        clickOn("Annulla"); // Viene chiuso la pagina di selezione del posto
        verifyAlertDialog("You closed the seat selection. Booking has been cancelled.");
    }

    @Test
    @DisplayName("Test simulating the cancellation of a booking")
    public void testShowCancelBooking() throws IOException {
        String bookingId = "BK198";
        Passenger mainPassenger = flightPlanner.getPassenger("jdoe");
        List<Ticket> tickets = List.of(new Ticket("T150", bookingId, "F002", "10D", 250, "John"
                , "Doe", new ArrayList<>()));
        flightPlanner.bookFlightForPassenger("F002", mainPassenger, new ArrayList<>(), tickets, bookingId);

        authManager.login("jdoe", "pass123");
        flightPlannerApp.showAppScreen();
        sleep(100);

        clickOn("#cancelBookingButton");

        sleep(200);
        write(bookingId);

        clickOn("OK");
        verifyAlertDialog("You cancelled the booking " + bookingId);

        clickOn("OK");
        DialogPane dialogPane = lookup(".dialog-pane").queryAs(DialogPane.class);

        String expectedContent = "Refund of 40% (100.0 EUR excluding luggage cost) for booking " + bookingId + " has been processed automatically.";
        assertThat(dialogPane.getHeaderText()).isNotNull();
        assertThat(dialogPane.getContentText()).isEqualTo(expectedContent);
    }

    @Test
    @DisplayName("Test simulating a failed booking cancellation")
    public void testShowCancelBooking_Error() {
        authManager.login("jdoe", "pass123");
        flightPlannerApp.showAppScreen();
        sleep(100);

        clickOn("#cancelBookingButton");

        clickOn("OK");
        verifyAlertDialog("Enter the booking Id to proceed.");

        clickOn("OK");

        clickOn("#cancelBookingButton");
        write("BK002");
        clickOn("OK");
        verifyAlertDialog("You cannot cancel a booking not booked by you!");
    }

    @Test
    @DisplayName("Test simulating the successful cancellation of a ticket")
    public void testShowCancelTicket() throws IOException {
        String bookingId = "BK199";
        String ticketNumber = "T151";
        Passenger mainPassenger = flightPlanner.getPassenger("jdoe");
        List<Ticket> tickets = List.of(new Ticket(ticketNumber, bookingId, "F002", "10D", 250, "John"
                , "Doe", new ArrayList<>()));
        flightPlanner.bookFlightForPassenger("F002", mainPassenger, new ArrayList<>(), tickets, bookingId);

        authManager.login("jdoe", "pass123");
        flightPlannerApp.showAppScreen();
        sleep(100);

        clickOn("#cancelTicketBtn");

        write(bookingId);
        clickOn("OK");
        write(ticketNumber);
        clickOn("OK");

        verifyAlertDialog("You cancelled the ticket " + ticketNumber + " in booking " + bookingId);
        clickOn("OK");
        DialogPane dialogPane = lookup(".dialog-pane").queryAs(DialogPane.class);

        String expectedContent = "Refund of 40% (100.0 EUR excluding luggage cost) for ticket " + ticketNumber + " has been processed automatically.";
        assertThat(dialogPane.getHeaderText()).isNotNull();
        assertThat(dialogPane.getContentText()).isEqualTo(expectedContent);
    }

    @Test
    @DisplayName("Test simulating a failed ticket cancellation process")
    public void testShowCancelTicket_Error() {
        authManager.login("jdoe", "pass123");
        flightPlannerApp.showAppScreen();
        sleep(100);

        clickOn("#cancelTicketBtn");

        clickOn("OK");
        verifyAlertDialog("Please enter a bookingId to proceed.");

        clickOn("OK");

        clickOn("#cancelTicketBtn");
        write("BK002");
        clickOn("OK");
        write("T002");
        clickOn("OK");
        verifyAlertDialog("You cannot cancel ticket for a booking not booked by you!");
    }

    @Test
    @DisplayName("Test simulating the successful addition of luggage for a passenger")
    public void testShowAddLuggageForm() {
        authManager.login("bbrown", "pass01234");
        Passenger loggedInUser = flightPlanner.getPassenger("bbrown");
        flightPlannerApp.showAppScreen();
        sleep(100);

        clickOn("#addLuggageButton");

        String bookingId = "BK004";
        String ticketNumber = "T004";

        write(bookingId);
        clickOn("OK");
        write(ticketNumber);
        clickOn("OK");

        clickOn("OK"); // Serve per chiudere la finestra delle informazioni sui bagagli

        // Selezioniamo un bagaglio a mano
        write("1");
        clickOn("OK");
        write("30");
        clickOn("OK");
        write("20");
        clickOn("OK");
        write("20");
        clickOn("OK");
        write("8");
        clickOn("OK");

        // Selezioniamo un bagaglio da stiva
        write("1");
        clickOn("OK");
        write("60");
        clickOn("OK");
        write("50");
        clickOn("OK");
        write("25");
        clickOn("OK");
        write("30");
        clickOn("OK");

        clickOn("#backButton"); // Aggiunta bagagli non terminata
        verifyAlertDialog("You do not finish the addition or the payment.");
        clickOn("OK");

        clickOn("#addLuggageButton");
        write(bookingId);
        clickOn("OK");
        write(ticketNumber);
        clickOn("OK");

        clickOn("OK"); // Serve per chiudere la finestra delle informazioni sui bagagli

        // Selezioniamo di nuovo un bagaglio a mano
        write("1");
        clickOn("OK");
        write("30");
        clickOn("OK");
        write("20");
        clickOn("OK");
        write("20");
        clickOn("OK");
        write("8");
        clickOn("OK");

        write("0"); // Nessun bagaglio a stiva in aggiunta
        clickOn("OK");

        clickOn("#payButton");
        verifyAlertDialog("The total cost for luggage addition is 100.0 EUR." +
                "\nPayment successful with " + loggedInUser.getPaymentMethod() + "!");
    }

    @Test
    @DisplayName("Test simulating a failed luggage addition process for a passenger")
    public void testShowAddLuggageForm_Error() {
        authManager.login("smorini", "pass45678");
        flightPlannerApp.showAppScreen();
        sleep(100);

        clickOn("#addLuggageButton");

        String bookingId = "BK005";
        String ticketNumber = "T005";

        // Tentativo di aggiungere bagagli per una prenotazione non fatta dall'utente
        write("BK001");
        clickOn("OK");
        write("T001");
        clickOn("OK");
        verifyAlertDialog("You cannot add luggage for a booking not booked by you!");

        // Tentativo di aggiungere più bagagli delle limitazioni imposte
        clickOn("OK");
        clickOn("#addLuggageButton");
        write(bookingId);
        clickOn("OK");
        write(ticketNumber);
        clickOn("OK");

        clickOn("OK");

        write("2"); // Samanta ha già un bagaglio a mano
        clickOn("OK");
        verifyAlertDialog("You can only add 1 more cabin luggage !");
    }

    @Test
    @DisplayName("Test simulating the successful seat change for a passenger")
    public void testShowChangeSeat() {
        authManager.login("bbrown", "pass01234");
        Passenger loggedInUser = flightPlanner.getPassenger("bbrown");
        flightPlannerApp.showAppScreen();
        sleep(100);

        clickOn("#changeSeatButton");

        String bookingId = "BK004";
        String ticketNumber = "T004";

        // Caso in cui non si porta a termine il processo di cambiamento del posto
        write(bookingId);
        clickOn("OK");
        write(ticketNumber);
        clickOn("OK");

        clickOn("Seat: 9C (Business)"); // Serve per aprire le selezioni disponibili
        clickOn("Seat: 9C (Business)");
        clickOn("OK");
        clickOn("#cancelButton");
        verifyAlertDialog("You didn't finish your modification or the payment. Your changing has been cancelled.");

        // Caso in cui si sceglie una stessa classe di posto
        clickOn("OK");
        clickOn("#changeSeatButton");

        write(bookingId);
        clickOn("OK");
        write(ticketNumber);
        clickOn("OK");

        clickOn("Seat: 9C (Business)"); // Serve per aprire le selezioni disponibili
        clickOn("Seat: 9C (Business)");
        clickOn("OK");
        clickOn("#changeSeatAndPayButton");
        verifyAlertDialog("You do not need to pay the price difference!\nNow your seat is 9C Business.");

        // Caso in cui si sceglie un posto di classe superiore
        clickOn("OK");
        clickOn("#changeSeatButton");

        write(bookingId);
        clickOn("OK");
        write(ticketNumber);
        clickOn("OK");

        clickOn("Seat: 1B (Business)"); // Era il posto inizialmente prenotato dal passeggero
        clickOn("Seat: 6B (First)");
        clickOn("OK");
        clickOn("#changeSeatAndPayButton");
        verifyAlertDialog("Payment successful with " + loggedInUser.getPaymentMethod() +
                "!\nNow your seat is 6B First.");
    }

    @Test
    @DisplayName("Test simulating a failed seat change process for a passenger")
    public void testShowChangeSeat_Error() {
        authManager.login("bbrown", "pass01234");
        flightPlannerApp.showAppScreen();
        sleep(100);

        clickOn("#changeSeatButton");

        write("BK001");
        clickOn("OK");
        write("T001");
        clickOn("OK");
        verifyAlertDialog("You cannot change seat for a booking not booked by you!");

        // Annullare il processo di selezione prima di portare al termine la modifica
        clickOn("OK");
        clickOn("#changeSeatButton");

        write("BK004");
        clickOn("OK");
        write("T004");
        clickOn("OK");
        clickOn("Annulla");
        verifyAlertDialog("You closed the seat selection. Booking has been cancelled.");
    }

    @Test
    @DisplayName("Test simulating a successful flight search with specified departure and arrival locations")
    public void testShowSearchFlights() {
        authManager.login("jdoe", "pass123");
        flightPlannerApp.showAppScreen();
        sleep(100);

        clickOn("#searchFlightsButton");

        write("new york");
        clickOn("OK");
        write("LAX");
        clickOn("OK");
        write("2024-12-25");
        clickOn("OK");

        TextArea resultArea = lookup("#resultTextArea").query();

        String expecteString = """
                Flight: F001 Departure Airport Code: JFK Departure Time: 2024-12-25T08:00 Arrival Airport Code: LAX Arrival Time: 2024-12-25T13:30
                Route ID: R001, Distance: 2811.0 km, Flight Duration: 5h30m
                Flight: F006 Departure Airport Code: LGA Departure Time: 2024-12-25T10:30 Arrival Airport Code: LAX Arrival Time: 2024-12-25T16:00
                Route ID: R006, Distance: 2800.0 km, Flight Duration: 5h30m""";

        assertTrue(resultArea.getText().contains(expecteString));
    }

    @Test
    @DisplayName("Test simulating a flight search with no results and an unsuccessful search with incorrect data")
    public void testShowSearchFlights_NoResults_Error() {
        authManager.login("jdoe", "pass123");
        flightPlannerApp.showAppScreen();
        sleep(100);

        // Se non si inserisce niente
        clickOn("#searchFlightsButton");
        clickOn("OK");
        verifyAlertDialog("Please enter a valid departure city or airport code.");

        // Se non si inserisce la data nel formato corretto
        clickOn("OK");
        clickOn("#searchFlightsButton");
        write("new york");
        clickOn("OK");
        write("LAX");
        clickOn("OK");
        write("2024.12.25");
        clickOn("OK");
        verifyAlertDialog("Please enter a valid date in the format yyyy-MM-dd.");

        // Se il volo non esiste o non c'è un volo per la data ricercata
        clickOn("OK");
        clickOn("#searchFlightsButton");
        write("new york");
        clickOn("OK");
        write("LAX");
        clickOn("OK");
        write("2024-12-26");
        clickOn("OK");

        TextArea resultArea = lookup("#resultTextArea").query();

        String expectedString = "No flights found.";

        assertTrue(resultArea.getText().contains(expectedString));
    }

    @Test
    @DisplayName("Test simulating the successful management of new notification preferences")
    public void testShowManageNotificationScreen() {
        authManager.login("jdoe", "pass123");
        flightPlannerApp.showAppScreen();
        sleep(100);

        clickOn("#manageNotificationButton");

        CheckBox specialOfferCheckBox = lookup("#specialOfferCheckBox").queryAs(CheckBox.class);
        CheckBox delayCheckBox = lookup("#delayCheckBox").queryAs(CheckBox.class);
        CheckBox emailCheckBox = lookup("#emailCheckBox").queryAs(CheckBox.class);
        CheckBox smsCheckBox = lookup("#smsCheckBox").queryAs(CheckBox.class);

        verifyThat("#gateChangeCheckBox", CheckBox::isSelected);
        verifyThat("#cancellationCheckBox", CheckBox::isSelected);
        verifyThat(emailCheckBox, CheckBox::isSelected);

        verifyThat(specialOfferCheckBox, checkBox -> !checkBox.isSelected());
        verifyThat(delayCheckBox, checkBox -> !checkBox.isSelected());
        verifyThat(smsCheckBox, checkBox -> !checkBox.isSelected());

        clickOn(specialOfferCheckBox);
        clickOn(delayCheckBox);
        clickOn(smsCheckBox);
        clickOn("#phoneNumberField").write("+1 3271555555");

        verifyThat("#gateChangeCheckBox", CheckBox::isSelected);
        verifyThat("#cancellationCheckBox", CheckBox::isSelected);
        verifyThat(specialOfferCheckBox, CheckBox::isSelected);
        verifyThat(delayCheckBox, CheckBox::isSelected);
        verifyThat(emailCheckBox, CheckBox::isSelected);
        verifyThat(smsCheckBox, CheckBox::isSelected);

        verifyThat("#phoneNumberField", textField -> ((TextField) textField).getText().equals("+1 3271555555"));

        clickOn("#saveButton");
        verifyAlertDialog("Your setting is saved successfully!");

        verifyThat("#backButton", isVisible());

        // Per ripristinare l'impostazione precedente alla modifica
        clickOn("OK");
        clickOn(specialOfferCheckBox);
        clickOn(delayCheckBox);
        clickOn(smsCheckBox);
        verifyThat("#phoneNumberField", textField -> ((TextField) textField).getText().isEmpty());

        clickOn("#saveButton");
    }

    @Test
    @DisplayName("Test simulating a failed management notification process")
    public void testShowManageNotificationScreen_Error() {
        authManager.login("jdoe", "pass123");
        flightPlannerApp.showAppScreen();
        sleep(100);

        // Salvare senza fornire un numero di cellulare
        clickOn("#manageNotificationButton");
        clickOn("#smsCheckBox");

        clickOn("#saveButton");
        verifyAlertDialog("Please provide a phone number for SMS notifications.");

        clickOn("OK");

        // Salvare senza fornire un numero nel formato corretto
        clickOn("#phoneNumberField").write("1234567890");
        clickOn("#saveButton");
        verifyAlertDialog("Please include the international prefix (e.g. +39 xxxxxxxxxx for Italy) and the phone number must have between 6 and 13 digits (excluding the international prefix).");
    }

    @Test
    @DisplayName("Test simulating the successful selection of payment method")
    public void testShowPaymentMethod() {
        authManager.login("jdoe", "pass123");
        flightPlannerApp.showAppScreen();
        sleep(100);

        clickOn("#paymentMethodButton");

        ComboBox<Object> paymentMethodComboBox = lookup("#payMethodComboBox").queryComboBox();
        interact(() -> paymentMethodComboBox.getSelectionModel().select(PaymentMethod.BANK_TRANSFER));
        clickOn("#confirmButton");
        verifyAlertDialog("Payment method updated to: BANK_TRANSFER");

        // Per ripristinare all'impostazione originale dell'utente
        clickOn("OK");
        interact(() -> paymentMethodComboBox.getSelectionModel().select(PaymentMethod.CREDIT_CARD));
        clickOn("#confirmButton");
    }

    @Test
    @DisplayName("Test simulating a failed payment method selection process")
    public void testShowPaymentMethod_Error() {
        authManager.login("jdoe", "pass123");
        flightPlannerApp.showAppScreen();
        sleep(100);

        clickOn("#paymentMethodButton");
        clickOn("#confirmButton");
        verifyAlertDialog("Please select a payment method.");
    }

    @Test
    @DisplayName("Test that simulates the successful update of a user's credentials")
    public void testShowUpdateCredentials() {
        String oldEmail = "jdoe@example.com";
        String oldPassword = "pass123";

        authManager.login("jdoe", "pass123");
        flightPlannerApp.showAppScreen();
        sleep(100);

        clickOn("#updateUserCredentialsBtn");

        doubleClickOn("#emailInput").eraseText(((TextField) lookup("#emailInput").query()).getText().length());
        write("john_doe@example.com");
        clickOn("#oldPasswordInput").write(oldPassword);
        clickOn("#newPasswordInput").write("password123");
        clickOn("#confirmPasswordInput").write("password123");

        clickOn("#updateButton");
        verifyAlertDialog("Credentials updated successfully!");

        // Per ripristinare le credenziali originali
        clickOn("OK");
        verifyThat("#emailInput", TextInputControlMatchers.hasText("john_doe@example.com"));
        verifyThat("#oldPasswordInput", TextInputControlMatchers.hasText(""));
        verifyThat("#newPasswordInput", TextInputControlMatchers.hasText(""));
        verifyThat("#confirmPasswordInput", TextInputControlMatchers.hasText(""));

        doubleClickOn("#emailInput").eraseText(((TextField) lookup("#emailInput").query()).getText().length());
        write(oldEmail);
        clickOn("#oldPasswordInput").write("password123");
        clickOn("#newPasswordInput").write(oldPassword);
        clickOn("#confirmPasswordInput").write(oldPassword);
        clickOn("#updateButton");
        verifyAlertDialog("Credentials updated successfully!");
    }

    @Test
    @DisplayName("Test that checks the failure of the user credential update process")
    public void testShowUpdateCredentials_Error() {
        authManager.login("jdoe", "pass123");
        flightPlannerApp.showAppScreen();
        sleep(100);

        // Se tutti i campi sono vuoti
        clickOn("#updateUserCredentialsBtn");
        doubleClickOn("#emailInput").eraseText(((TextField) lookup("#emailInput").query()).getText().length());
        clickOn("#updateButton");
        verifyAlertDialog("Please provide at least a new email or a new password.");

        // Se si fornisce solo il nuovo email
        clickOn("OK");
        doubleClickOn("#emailInput").eraseText(((TextField) lookup("#emailInput").query()).getText().length());
        write("john_doe@example.com");
        clickOn("#oldPasswordInput").write("wrongPassword");
        clickOn("#updateButton");
        verifyAlertDialog("Old password is incorrect. Provide the correct password to update your credentials");

        // Nel caso in cui si vuole aggiornare password, ma il password di conferma non sia uguale con il nuovo password inserito
        clickOn("OK");
        doubleClickOn("#emailInput").eraseText(((TextField) lookup("#emailInput").query()).getText().length());
        clickOn("#oldPasswordInput").write("pass123");
        clickOn("#newPasswordInput").write("password123");
        clickOn("#confirmPasswordInput").write("notMatchedPassword");
        clickOn("#updateButton");
        verifyAlertDialog("New passwords do not match.");
    }

    @Test
    @DisplayName("Test that simulates the unsubscription of a user")
    public void testUnsubscribeButton() throws IOException {
        authManager.register("unsubscribeUser", "password", "unsubUser@exxample.com", "Passenger");
        authManager.login("unsubscribeUser", "password");
        flightPlanner.registerPassenger(new Passenger("unsubscribeUser", "Someone", "Surname", "unsunUser@example.com",
                null, "password", null, null, null, null, null));
        flightPlannerApp.showAppScreen();
        sleep(100);
        clickOn("#unsubscribeButton");

        verifyAlertDialog(
                "Are you sure you want to unsubscribe? This will delete your account and all associated data.");

        clickOn("OK");

        assertNull(authManager.getLoggedInUser(), "User should not be logged in after unsubscribing.");
        assertFalse(authManager.getUsers().containsKey("unsubscribeUser"));
        assertNull(flightPlanner.getPassenger("unsubscribeUser"));
    }

    // Test su Interfaccia Admin
    @Test
    @DisplayName("Test that simulates the layout of the viewFlightsButton")
    public void testShowFlightList() {
        authManager.login("admin", "admin123");
        flightPlannerApp.showAppScreen();
        sleep(100);

        clickOn("#viewFlightsButton");


        verifyThat("#flightsLabel", hasText("Flights List"));

        TextArea flightsTextArea = lookup("#flightsTextArea").query();
        String expectedText = """
                F001 - JFK to LAX | Departure: 2024-12-25T08:00 | Arrival: 2024-12-25T13:30 | Economy Seats: 200 | Business Seats: 45 | First Class Seats: 5
                F002 - LAX to ORD | Departure: 2024-12-26T10:00 | Arrival: 2024-12-26T14:10 | Economy Seats: 200 | Business Seats: 45 | First Class Seats: 5
                F003 - LHR to LAX | Departure: 2024-11-27T07:00 | Arrival: 2024-11-27T14:30 | Economy Seats: 300 | Business Seats: 40 | First Class Seats: 10
                F004 - CDG to SIN | Departure: 2024-10-28T21:00 | Arrival: 2024-10-29T10:25 | Economy Seats: 340 | Business Seats: 100 | First Class Seats: 10
                F005 - FLR to PVG | Departure: 2024-11-05T09:00 | Arrival: 2024-11-06T21:55 | Economy Seats: 340 | Business Seats: 100 | First Class Seats: 10
                F006 - LGA to LAX | Departure: 2024-12-25T10:30 | Arrival: 2024-12-25T16:00 | Economy Seats: 200 | Business Seats: 45 | First Class Seats: 5""";

        assertTrue(flightsTextArea.getText().contains(expectedText), "Expected text not found in TextArea.");

        verifyThat("#backButton", isVisible());
    }

    @Test
    @DisplayName("Test that simulates the layout of the viewPassengersButton")
    public void testShowPassengersList() {
        authManager.login("admin", "admin123");
        flightPlannerApp.showAppScreen();
        sleep(100);

        clickOn("#viewPassengersButton");

        verifyThat("#passengerLabel", hasText("Passengers List"));

        TextArea passengersTextArea = lookup("#passengersTextArea").query();

        // Usando il StringBuilder evita il problema di ordinamento casuale nell'uso del set di NotificationType
        List<Passenger> passengers = flightPlanner.getPassengers();
        StringBuilder sb = new StringBuilder();
        for (Passenger passenger : passengers) {
            sb.append(passenger.toString()).append("\n");
        }

        assertEquals(passengersTextArea.getText(), sb.toString());

        verifyThat("#backButton", isVisible());
    }

    @Test
    @DisplayName("Test that simulates the successful addition of a flight")
    public void testShowAddFlightForm() {
        authManager.login("admin", "admin123");
        flightPlannerApp.showAppScreen();
        sleep(100);

        clickOn("#addFlightButton");

        clickOn("#flightNumberField").write("F101");
        clickOn("#departureField").write("JFK");
        clickOn("#arrivalField").write("LAX");
        DatePicker departureDatePicker = lookup("#departureDatePicker").query();
        interact(() -> departureDatePicker.setValue(LocalDate.of(2024, 11, 10)));
        clickOn("#departureTimeField").write("11:00");
        DatePicker arrivalDatePicker = lookup("#arrivalDatePicker").query();
        interact(() -> arrivalDatePicker.setValue(LocalDate.of(2024, 12, 10)));
        clickOn("#arrivalTimeField").write("16:30");
        clickOn("#economySeatsNumberField").write("270");
        clickOn("#businessSeatsNumberField").write("50");
        clickOn("#firstSeatsNumberField").write("12");

        clickOn("#addFlightBtn");

        verifyAlertDialog("Flight F101 has been added successfully.");

        assertNotNull(flightPlanner.findFlight("F101"));

        clickOn("OK");

        verifyThat("#flightNumberField", TextInputControlMatchers.hasText(""));
        verifyThat("#departureField", TextInputControlMatchers.hasText(""));
        verifyThat("#arrivalField", TextInputControlMatchers.hasText(""));

        verifyThat("#backButton", isVisible());
    }

    @Test
    @DisplayName("Test that simulates the failure of the flight addition process")
    public void testShowAddFlightForm_Error() {
        authManager.login("admin", "admin123");
        flightPlannerApp.showAppScreen();
        sleep(100);

        clickOn("#addFlightButton");

        // Alcuni campi non riempiti
        clickOn("#flightNumberField").write("");
        clickOn("#departureField").write("JFK");
        clickOn("#arrivalField").write("LAX");

        clickOn("#addFlightBtn");

        verifyAlertDialog("All fields are required.");

        clickOn("OK");
        sleep(100);

        // Formato del tempo invalido rispetto al requisito
        clickOn("#flightNumberField").write("F102");

        DatePicker departureDatePicker = lookup("#departureDatePicker").query();
        interact(() -> departureDatePicker.setValue(LocalDate.of(2024, 12, 10)));

        DatePicker arrivalDatePicker = lookup("#arrivalDatePicker").query();
        interact(() -> arrivalDatePicker.setValue(LocalDate.of(2024, 12, 10)));

        clickOn("#departureTimeField").write("10:xx");
        clickOn("#arrivalTimeField").write("13:50");

        clickOn("#economySeatsNumberField").write("270");
        clickOn("#businessSeatsNumberField").write("50");
        clickOn("#firstSeatsNumberField").write("12");

        clickOn("#addFlightBtn");
        verifyAlertDialog("Please enter the departure time in the format HH:MM.");
    }

    @Test
    @DisplayName("Test that simulates the successful removal of an existing flight")
    public void testShowRemoveFlight() throws IOException {
        flightPlanner.addFlight(new Flight("F120", "YOU", "BOTH", LocalDateTime.of(2024, 11, 5, 8, 0),
                LocalDateTime.of(2024, 11, 5, 12, 30), 300, 65, 10));

        authManager.login("admin", "admin123");
        flightPlannerApp.showAppScreen();
        sleep(100);

        clickOn("#removeFlightButton");

        clickOn("#removeFlightNumberField").write("F120");

        clickOn("#removeFlightBtn");

        verifyAlertDialog("Flight F120 has been removed successfully.");

        verifyThat("#backButton", isVisible());
    }

    @Test
    @DisplayName("Test that simulates the failure of a flight removal process")
    public void testShowRemoveFlight_Error() {
        authManager.login("admin", "admin123");
        flightPlannerApp.showAppScreen();
        sleep(100);

        clickOn("#removeFlightButton");

        clickOn("#removeFlightNumberField").write("");
        clickOn("#removeFlightBtn");
        verifyAlertDialog("You must enter a flight number.");

        clickOn("OK");
        sleep(100);

        clickOn("#removeFlightNumberField").write("Fks0");
        clickOn("#removeFlightBtn");
        verifyAlertDialog("Flight number Fks0 not found.");
    }

    @Test
    @DisplayName("Test that simulates the successful updating of flight status, ensuring that the related route is also updated")
    public void testShowUpdateFlightStatus() throws IOException {
        flightPlanner.addFlight(new Flight("F500", "DO", "YOU", LocalDateTime.of(2024, 10, 30, 8, 40),
                LocalDateTime.of(2024, 10, 30, 14, 30), 260, 40, 10));
        Duration flightDuration = Duration.between(LocalDateTime.of(2024, 10, 30, 8, 40),
                LocalDateTime.of(2024, 10, 30, 14, 30));
        flightPlanner.addRoute(new Route("R500", "DO", "YOU", 10340, flightDuration));

        authManager.login("admin", "admin123");
        flightPlannerApp.showAppScreen();
        sleep(100);

        clickOn("#updateFlightStatusButton");

        clickOn("#flightNumberField").write("F500");
        DatePicker departureDatePicker = lookup("#departureDatePicker").query();
        interact(() -> departureDatePicker.setValue(LocalDate.of(2024, 10, 30)));
        DatePicker arrivalDatePicker = lookup("#arrivalDatePicker").query();
        interact(() -> arrivalDatePicker.setValue(LocalDate.of(2024, 10, 30)));
        clickOn("#departureTimeField").write("08:30");
        clickOn("#arrivalTimeField").write("15:30");
        clickOn("#updateMsgField").write("Flight delayed");
        ComboBox<Object> notificationTypeComboBox = lookup("#notificationTypeComboBox").queryComboBox();
        interact(() -> notificationTypeComboBox.getSelectionModel().select(NotificationType.DELAY));

        clickOn("#updateFlightStatusBtn");

        verifyAlertDialog("Status for flight F500 has been updated and notified to passengers who opted for DELAY. ");

        verifyThat("#backButton", isVisible());
    }

    @Test
    @DisplayName("Test that simulates the failure of a flight status update process")
    public void testShowUpdateFlightStatus_Error() {
        authManager.login("admin", "admin123");
        flightPlannerApp.showAppScreen();
        sleep(100);

        clickOn("#updateFlightStatusButton");

        clickOn("#updateFlightStatusBtn");
        verifyAlertDialog("Please fill all the fields.");
        clickOn("OK");

        clickOn("#flightNumberField").write("F002");
        DatePicker departureDatePicker = lookup("#departureDatePicker").query();
        interact(() -> departureDatePicker.setValue(LocalDate.of(2024, 12, 26)));
        DatePicker arrivalDatePicker = lookup("#arrivalDatePicker").query();
        interact(() -> arrivalDatePicker.setValue(LocalDate.of(2024, 12, 26)));
        clickOn("#departureTimeField").write("10:00");
        clickOn("#arrivalTimeField").write("InvalidTime");
        clickOn("#updateMsgField").write("Gate has changed");
        ComboBox<Object> notificationTypeComboBox = lookup("#notificationTypeComboBox").queryComboBox();
        interact(() -> notificationTypeComboBox.getSelectionModel().select(NotificationType.GATE_CHANGE));

        clickOn("#updateFlightStatusBtn");

        verifyAlertDialog("Please enter the arrival time in the format HH:MM.");
    }

    @Test
    @DisplayName("Test that simulates the process of updating and uploading a seat-class price")
    public void testShowUpdateOrUploadSeatClassPrice() throws IOException {
        authManager.login("admin", "admin123");
        flightPlannerApp.showAppScreen();
        sleep(100);

        clickOn("#manageSeatsButton");

        // Aggiornamento del prezzo per la classe Economy
        String flightNumber = "F002";
        String classType = "Economy";

        assertEquals(250, flightPlanner.getPrice(flightNumber, classType));

        clickOn("#flightNumberField").write(flightNumber);
        clickOn("#classTypeField").write(classType);
        clickOn("#priceField").write("300");

        clickOn("#addSeatPriceButton");

        verifyAlertDialog("Seat price update for flight: " + flightNumber + ", Class: " + classType + ", Price: 300.0 EUR");
        assertEquals(300, flightPlanner.getPrice(flightNumber, classType));

        clickOn("OK");

        verifyThat("#flightNumberField", TextInputControlMatchers.hasText(""));
        verifyThat("#classTypeField", TextInputControlMatchers.hasText(""));
        verifyThat("#priceField", TextInputControlMatchers.hasText(""));

        verifyThat("#backButton", isVisible());

        // Caricamento di prezzo per la classe Economy per un nuovo volo
        flightPlanner.addFlight(new Flight("F800", "JO", "YO", LocalDateTime.now(),
                LocalDateTime.now().plusHours(5), 240, 70, 13));

        assertThrows(IllegalArgumentException.class, () -> flightPlanner.getPrice("F800", classType));

        clickOn("#flightNumberField").write("F800");
        clickOn("#classTypeField").write(classType);
        clickOn("#priceField").write("450");

        clickOn("#addSeatPriceButton");

        verifyAlertDialog("Seat price set for flight: F800, Class: " + classType + ", Price: 450.0 EUR");
        assertEquals(450, flightPlanner.getPrice("F800", classType));

        verifyThat("#backButton", isVisible());
        verifyThat("#addSeatsToFlightBtn", isVisible());
        verifyThat("#removeSeatsFromFlightBtn", isVisible());
    }

    @Test
    @DisplayName("Test that checks the failure of a seat-class price update process")
    public void testShowUpdateOrUploadSeatClassPrice_Error() {
        authManager.login("admin", "admin123");
        flightPlannerApp.showAppScreen();
        sleep(100);

        clickOn("#manageSeatsButton");

        clickOn("#addSeatPriceButton");
        verifyAlertDialog("Please enter all the fields.");

        clickOn("OK");

        clickOn("#flightNumberField").write("F002");
        clickOn("#classTypeField").write("InvalidClass");
        clickOn("#priceField").write("150.00");

        clickOn("#addSeatPriceButton");
        verifyAlertDialog("Please enter a valid class type (Economy, Business, First).");

        clickOn("OK");
        clickOn("#backButton");
        // Si controlla se viene visualizzata la schermata principale
        FxAssert.verifyThat(window("Flight Planner App"), WindowMatchers.isShowing());
    }

    @Test
    @DisplayName("Test that simulates the successful addition of seats to an existing flight")
    public void testShowAddSeatsToFlight() {
        authManager.login("admin", "admin123");
        flightPlannerApp.showAppScreen();
        sleep(100);

        clickOn("#manageSeatsButton");
        clickOn("#addSeatsToFlightBtn");

        String flightNumber = "F002";

        clickOn("#flightNumberField").write(flightNumber);
        clickOn("#seatListField").write("25A,25B");
        clickOn("#classTypeField").write("Economy");

        clickOn("#addSeatsButton");

        verifyAlertDialog("Seats added to flight " + flightNumber);
    }

    @Test
    @DisplayName("Test that simulates the failure of a seat addition process for a flight")
    public void testShowAddSeatsToFlight_Error() {
        authManager.login("admin", "admin123");
        flightPlannerApp.showAppScreen();
        sleep(100);

        clickOn("#manageSeatsButton");
        clickOn("#addSeatsToFlightBtn");

        String flightNumber = "Fjh1";

        clickOn("#addSeatsButton");
        verifyAlertDialog("Please fill all the fields.");

        clickOn("OK");

        clickOn("#flightNumberField").write(flightNumber);
        clickOn("#seatListField").write("25A,25B");
        clickOn("#classTypeField").write("Economy");
        clickOn("#addSeatsButton");

        verifyAlertDialog("Flight " + flightNumber + " not found.");
    }

    @Test
    @DisplayName("Test that simulates the successful removal of seats from a flight")
    public void testShowRemoveSeatsFromFlight() throws IOException {
        String flightNumber = "F002";
        flightPlanner.addSeatToFlight(flightNumber, new Seat("30A", "Economy", flightNumber, true));
        flightPlanner.addSeatToFlight(flightNumber, new Seat("30B", "Economy", flightNumber, true));


        authManager.login("admin", "admin123");
        flightPlannerApp.showAppScreen();
        sleep(100);

        clickOn("#manageSeatsButton");
        clickOn("#removeSeatsFromFlightBtn");

        clickOn("#flightNumberField").write(flightNumber);
        clickOn("#seatNumbersField").write("30A,30B");

        clickOn("#removeSeatsButton");
        verifyAlertDialog("Seats 30A,30B removed from flight " + flightNumber);
    }

    @Test
    @DisplayName("Test that simulates the failure of a seat removal process for a flight")
    public void testShowRemovalSeatsFromFlight_Error() {
        String flightNumber = "F002";
        String seatNumber = "1000C";

        authManager.login("admin", "admin123");
        flightPlannerApp.showAppScreen();
        sleep(100);

        clickOn("#manageSeatsButton");
        clickOn("#removeSeatsFromFlightBtn");

        clickOn("#removeSeatsButton");
        verifyAlertDialog("Please fill all the fields.");

        clickOn("OK");

        clickOn("#flightNumberField").write(flightNumber);
        clickOn("#seatNumbersField").write(seatNumber);

        clickOn("#removeSeatsButton");
        verifyAlertDialog("Seat " + seatNumber + " not found on flight " + flightNumber);
    }

    @Test
    @DisplayName("Test that simulates the successful addition of an airport")
    public void testShowAddAirportForm() {
        authManager.login("admin", "admin123");
        flightPlannerApp.showAppScreen();
        sleep(100);

        clickOn("#addAirportButton");

        String code = "ITA";

        clickOn("#airportCodeField").write(code);
        clickOn("#airportNameField").write("Italy International");
        clickOn("#cityField").write("Prato");
        clickOn("#countryField").write("Italy");

        clickOn("#addButton");

        verifyAlertDialog("Airport " + code + " added successfully.");

        verifyThat("#backButton", isVisible());

        clickOn("OK");

        verifyThat("#airportCodeField", TextInputControlMatchers.hasText(""));
        verifyThat("#airportNameField", TextInputControlMatchers.hasText(""));
        verifyThat("#cityField", TextInputControlMatchers.hasText(""));
        verifyThat("#countryField", TextInputControlMatchers.hasText(""));
    }

    @Test
    @DisplayName("Test that simulates the failure of an airport addition process")
    public void testShowAddAirportForm_Error() {
        authManager.login("admin", "admin123");
        flightPlannerApp.showAppScreen();
        sleep(100);

        clickOn("#addAirportButton");

        clickOn("#addButton");
        verifyAlertDialog("All fields must be filled.");

        clickOn("OK");

        clickOn("#airportCodeField").write("");
        clickOn("#airportNameField").write("IBC International");
        clickOn("#cityField").write("Prato");
        clickOn("#countryField").write("Italy");

        clickOn("#addButton");
        verifyAlertDialog("All fields must be filled.");

        clickOn("OK");
        clickOn("#backButton");
        FxAssert.verifyThat(window("Flight Planner App"), WindowMatchers.isShowing());
    }

    @Test
    @DisplayName("Test that simulates the successful removal of an existing airport")
    public void testShowRemoveAirport() throws IOException {
        String code = "IBC";
        flightPlanner.addAirport(new Airport(code, "IBC international", "Prato", "Italy"));

        authManager.login("admin", "admin123");
        flightPlannerApp.showAppScreen();
        sleep(100);

        clickOn("#removeAirportButton");

        clickOn("#airportCodeField").write(code);

        clickOn("#removeButton");

        verifyAlertDialog("Airport " + code + " removed successfully.");

        verifyThat("#backButton", isVisible());
    }

    @Test
    @DisplayName("Test that simulates the failure of an airport removal process")
    public void testShowRemoveAirport_Error() {
        authManager.login("admin", "admin123");
        flightPlannerApp.showAppScreen();
        sleep(100);

        clickOn("#removeAirportButton");

        clickOn("#removeButton");
        verifyAlertDialog("Airport code must be provided.");

        clickOn("OK");

        clickOn("#airportCodeField").write("BLABLA");

        clickOn("#removeButton");
        verifyAlertDialog("Airport BLABLA not found.");
    }

    @Test
    @DisplayName("Test that simulates the successful addition of a route")
    public void testShowAddRouteForm() {
        authManager.login("admin", "admin123");
        flightPlannerApp.showAppScreen();
        sleep(100);

        clickOn("#addRouteButton");

        String routeId = "R200";

        clickOn("#routeIdField").write(routeId);
        clickOn("#departureAirportField").write("PVG");
        clickOn("#arrivalAirportField").write("CDG");
        clickOn("#distanceField").write("11457");
        clickOn("#durationField").write("11:00");

        clickOn("#addRouteButton");
        verifyAlertDialog("Route added: " + routeId);

        verifyThat("#backButton", isVisible());
    }

    @Test
    @DisplayName("Test that simulates the failure of a route addition process")
    public void testShowAddRouteForm_Error() {
        authManager.login("admin", "admin123");
        flightPlannerApp.showAppScreen();
        sleep(100);

        clickOn("#addRouteButton");

        clickOn("#addRouteButton");
        verifyAlertDialog("Please fill in all the fields.");

        clickOn("OK");

        clickOn("#routeIdField").write("R109");
        clickOn("#departureAirportField").write("PVG");
        clickOn("#arrivalAirportField").write("CDG");
        clickOn("#distanceField").write("InvalidDistance");
        clickOn("#durationField").write("11:00");

        clickOn("#addRouteButton");
        verifyAlertDialog("Please enter a valid number for the distance.");
    }

    @Test
    @DisplayName("Test that simulates the successful removal of a route")
    public void testShowRemoveRouteForm() throws IOException {
        String routeId = "R300";
        flightPlanner.addRoute(new Route(routeId, "AAA", "BBB", 12345, Route.parseDuration("5h30m")));

        authManager.login("admin", "admin123");
        flightPlannerApp.showAppScreen();
        sleep(100);

        clickOn("#removeRouteButton");

        clickOn("#routeIdField").write(routeId);

        clickOn("#removeRouteButton");
        verifyAlertDialog("Route removed: " + routeId);

        verifyThat("#backButton", isVisible());
    }

    @Test
    @DisplayName("Test that simulates the failure of a route removal process")
    public void testShowRemoveRouteForm_Error() {
        authManager.login("admin", "admin123");
        flightPlannerApp.showAppScreen();
        sleep(100);

        clickOn("#removeRouteButton");

        clickOn("#removeRouteButton");
        verifyAlertDialog("Please enter a Route ID.");

        clickOn("OK");

        clickOn("#routeIdField").write("BLABLA");
        clickOn("#removeRouteButton");
        verifyAlertDialog("Route BLABLA not found.");
    }
}