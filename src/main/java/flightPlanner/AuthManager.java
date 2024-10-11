package flightPlanner;

import java.util.HashMap;
import java.util.Map;

public class AuthManager {
    private Map<String, String> users;  // Mappa di username e password
    private Map<String, String> emails; // Mappa di username ed email
    private String loggedInUser;

    public AuthManager() {
        users = new HashMap<>();
        emails = new HashMap<>();
        // Aggiungere un amministratore predefinito
        users.put("admin", "admin123");
        emails.put("admin", "admin@example.com");
        loggedInUser = null;
    }

    public boolean login(String username, String password) {
        if (users.containsKey(username) && users.get(username).equals(password)) {
            loggedInUser = username;
            return true;
        }
        return false;
    }

    public boolean register(String username, String password, String email) {
        if (!users.containsKey(username) && !emails.containsValue(email)) {
            users.put(username, password);
            emails.put(username, email);
            return true;
        }
        return false;
    }

    public void logout() {
        loggedInUser = null;
    }

    public String getLoggedInUser() {
        return loggedInUser;
    }

    // Serve per ottenere l'email dell'utente attualmente loggato
    public String getLoggedInUserEmail() {
        return emails.get(loggedInUser);
    }

    // Controlla se l'utente è un amministratore
    public boolean isAdmin() {
        return "admin".equals(loggedInUser);
    }
}
