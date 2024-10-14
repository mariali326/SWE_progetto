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
    private Map<String, String> users;  // Mappa di username e password
    private Map<String, String> emails; // Mappa di username ed email
    private Map<String, String> userRoles;//Mappa di username e ruoli
    private String loggedInUser;
    private CSVManager csvManager;

    public AuthManager() throws IOException {
        String csvFilePath = "csv/users.csv";
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(csvFilePath);
        if (inputStream == null) {
            throw new FileNotFoundException("File not found in resources: " + csvFilePath);
        }
        this.csvManager = new CSVManager(new InputStreamReader(inputStream));
        users = new HashMap<>();
        emails = new HashMap<>();
        userRoles = new HashMap<>();
        loadUsersFromCSV();

        // Aggiungere un amministratore predefinito se non esiste
        if (!users.containsKey("admin")) {
            users.put("admin", "admin123");
            emails.put("admin", "admin@example.com");
            userRoles.put("admin", "Admin");
        }
        loggedInUser = null;
    }

    public boolean login(String username, String password) {
        if (users.containsKey(username) && users.get(username).equals(password)) {
            loggedInUser = username;
            return true;
        }
        return false;
    }

    public boolean register(String username, String password, String email, String role) throws IOException {
        if (!users.containsKey(username) && !emails.containsValue(email)) {
            users.put(username, password);
            emails.put(username, email);
            userRoles.put(username, role);

            String[] record = {
                    username,
                    password,
                    email,
                    role
            };
            try {
                csvManager.appendRecord(record, "csv/users.csv");
            } catch (IOException e) {
                log.error("An error occurred while writing an user on file CSV", e);
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
        return userRoles.get(loggedInUser);
    }

    public String getLoggedInUser() {
        return loggedInUser;
    }

    // Serve per ottenere l'email dell'utente attualmente loggato
    public String getLoggedInUserEmail() {
        return emails.get(loggedInUser);
    }

    public String getLoggedInUserPassword() {
        return users.get(getLoggedInUser());
    }

    // Controlla se l'utente è un amministratore
    public boolean isAdmin() {
        return "admin".equals(loggedInUser);
    }

    public Map<String, String> getUsers() {
        return users;
    }

    public Map<String, String> getEmails() {
        return emails;
    }

    public Map<String, String> getUserRoles() {
        return userRoles;
    }

    public boolean updateUser(String username, String newPassword, String newEmail) throws IOException {
        if (users.containsKey(username)) {
            users.put(username, newPassword);
            emails.put(username, newEmail);
            saveUsersToCSV();
            return true;
        }
        return false; // Utente non trovato
    }

    public void removeUser(String username) throws IOException {
        if (users.containsKey(username)) {
            users.remove(username);
            emails.remove(username);
            userRoles.remove(username);
            saveUsersToCSV();
            System.out.println("User unsubscribed: " + username);
        }
    }

    private void loadUsersFromCSV() throws IOException {
        List<String[]> records = csvManager.readAll();
        // Salta l'header
        for (int i = 1; i < records.size(); i++) {
            String[] record = records.get(i);
            String username = record[0];
            String password = record[1];
            String email = record[2];
            String role = record[3];
            users.put(username, password);
            emails.put(username, email);
            userRoles.put(username, role);

        }
    }

    public void saveUsersToCSV() throws IOException {
        List<String[]> records = new ArrayList<>();
        records.add(new String[]{"username", "password", "email", "role"});
        for (String username : users.keySet()) {
            records.add(new String[]{
                    username,
                    users.get(username),
                    emails.get(username),
                    userRoles.get(username)
            });
        }
        try {
            csvManager.writeAll(records, "csv/users.csv");
        } catch (IOException e) {
            log.error("An error occurred while saving users on file CSV: " + e.getMessage());
            throw e;
        }
    }
}
