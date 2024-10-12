package flightPlanner;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;

public class FlightPlannerApp extends Application {
    private FlightPlanner flightPlanner;
    private AuthManager authManager;
    private Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        flightPlanner = new FlightPlanner();
        authManager = new AuthManager();
        primaryStage = stage;

        showLoginScreen();

    }

    private void showLoginScreen() {
        VBox vbox = new VBox(10);
        Label titleLabel = new Label("Login");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        Button loginButton = new Button("Login");
        Button registerButton = new Button("Register");

        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            if (authManager.login(username, password)) {
                showMainAppScreen();
            } else {
                showAlert("Login Failed", "Invalid username or password.");
            }
        });

        registerButton.setOnAction(e -> {
            // Otteniamo le informazioni digitati dall'utente
            String username = usernameField.getText();
            String password = passwordField.getText();
            String email = emailField.getText();
            if (authManager.register(username, password, email)) {
                showAlert("Registration Successful", "You can now log in.");
            } else {
                showAlert("Registration Failed", "Username or email already exists.");
            }
        });

        vbox.getChildren().addAll(titleLabel, usernameField, passwordField, emailField, loginButton, registerButton);
        Scene scene = new Scene(vbox, 300, 250);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Flight Planner - Login");
        primaryStage.show();
    }

    private void showMainAppScreen(){
        // Layout dell'interfaccia utente
        VBox vbox = new VBox(10);
        Label titleLable = new Label("Flight Planner");

        // Campi e pulsanti per aggiungere un volo
        Label flightLabel = new Label("Add Flight:");
        TextField flightNumberField = new TextField();
        flightNumberField.setPromptText("Flight Number");
        TextField departureField = new TextField();
        departureField.setPromptText("Departure");
        TextField arrivalField= new TextField();
        arrivalField.setPromptText("Arrival");
        Button addFlightBtn = new Button("Add Flight");

        Button logoutButton = new Button("Logout");

        addFlightBtn.setOnAction(e -> {
            String flightNumber = flightNumberField.getText();
            String departure = departureField.getText();
            String arrival = arrivalField.getText();
            Flight flight = new Flight(flightNumber, departure, arrival, LocalDateTime.of(2024,8,24,8,0), LocalDateTime.of(2024,8,24,11,0));
            try {
                flightPlanner.addFlight(flight);
                showAlert("Flight added", "Flight " + flight.getFlightNumber() + " has been added successfully.");
            } catch (IOException ex) {
                showAlert("Error", "Error adding flight.");
                throw new RuntimeException(ex);
            }
            flightNumberField.clear();
            departureField.clear();
            arrivalField.clear();
        });

        logoutButton.setOnAction(e -> {
            authManager.logout();
            showLoginScreen();
        });

        // Aggiunge elementi al layout
        vbox.getChildren().addAll(titleLable, flightLabel, flightNumberField, departureField, arrivalField, addFlightBtn, logoutButton);

        // Configura scena e stage
        Scene scene = new Scene(vbox, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Flight Planner App");
        primaryStage.show();
    }

    // Metodo di utilità per mostrare messaggi di dialogo
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
