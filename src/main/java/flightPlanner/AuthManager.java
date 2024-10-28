package flightPlanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuthManager {
    private static final Log log = LogFactory.getLog(AuthManager.class);
    private final Map<String, User> users;  // Mappa di username e oggetti User (Admin o Passenger)
    private final CSVManager csvManager;
    private String loggedInUser;

    public AuthManager() throws IOException {
        String csvFilePath = "csv/users.csv";
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(csvFilePath);
        if (inputStream == null) {
            throw new FileNotFoundException("File not found in resources: " + csvFilePath);
        }
        this.csvManager = new CSVManager(new InputStreamReader(inputStream));
        users = new HashMap<>();
        loadUsersFromCSV();

        // Aggiungere un amministratore predefinito se non esiste
        if (!users.containsKey("admin")) {
            users.put("admin", new Admin("admin", "admin123", "admin@example.com"));
        }
        loggedInUser = null;
    }

    public boolean login(String username, String password) {
        User user = users.get(username);
        if (user != null && user.getPassword().equals(password)) {
            loggedInUser = username;
            return true;
        }
        return false;
    }

    public boolean register(String username, String password, String email, String role) throws IOException {
        if (role.equals("Admin")) {
            System.out.println("Cannot register another admin.");
            return false;
        }

        if (!users.containsKey(username)) {
            User newUser = new Passenger(username, "", "", email, "", password, null, null, null, "", "");
            users.put(username, newUser);

            String[] record = {
                    username,
                    password,
                    email,
                    role
            };
            try {
                csvManager.appendRecord(record, "csv/users.csv");
            } catch (IOException e) {
                log.error("An error occurred while writing an user to the CSV file", e);
                throw e;
            }
            saveUsersToCSV();
            return true;
        } else {
            System.out.println("User already exists: " + username);
            return false; // Registrazione fallita
        }
    }

    public void logout() {
        loggedInUser = null;
    }

    public String getCurrentUserRole() {
        User user = users.get(loggedInUser);
        return user instanceof Admin ? "Admin" : "Passenger";
    }

    public String getLoggedInUser() {
        return loggedInUser;
    }

    // Serve per ottenere l'email dell'utente attualmente loggato
    public String getLoggedInUserEmail() {
        User user = users.get(loggedInUser);
        return user != null ? user.getEmail() : null;
    }

    public String getLoggedInUserPassword() {
        User user = users.get(loggedInUser);
        return user != null ? user.getPassword() : null;
    }

    // Controlla se l'utente è un amministratore
    public boolean isAdmin() {
        return "admin".equals(loggedInUser);
    }

    public Map<String, User> getUsers() {
        return users;
    }

    public boolean updateUser(String username, String newPassword, String newEmail) throws IOException {
        if (users.containsKey(username)) {
            User user = users.get(username);
            user.setPassword(newPassword);
            user.setEmail(newEmail);
            saveUsersToCSV();
            return true;
        }
        return false; // Utente non trovato
    }

    public void removeUser(String username) throws IOException {
        if (users.containsKey(username)) {
            users.remove(username);
            saveUsersToCSV();
            System.out.println("User unsubscribed: " + username);
        }
    }

    private void loadUsersFromCSV() throws IOException {
        List<String[]> records = csvManager.readAll();
        // Salta header
        for (int i = 1; i < records.size(); i++) {
            String[] record = records.get(i);
            String username = record[0];
            String password = record[1];
            String email = record[2];
            String role = record[3];

            User user;
            if ("Admin".equalsIgnoreCase(role)) {
                user = new Admin(username, password, email);
            } else {
                user = new Passenger(username, "", "", email, "", password, null, null, null, "", "");
            }
            users.put(username, user);
        }
    }

    private void saveUsersToCSV() throws IOException {
        List<String[]> records = new ArrayList<>();
        records.add(new String[]{"username", "password", "email", "role"});

        for (User user : users.values()) {
            records.add(new String[]{
                    user.getUsername(),
                    user.getPassword(),
                    user.getEmail(),
                    user instanceof Admin ? "Admin" : "Passenger"
            });
        }
        try {
            csvManager.writeAll(records, "csv/users.csv");
        } catch (IOException e) {
            log.error("An error occurred while saving users to the CSV file: " + e.getMessage());
            throw e;
        }
    }
}
