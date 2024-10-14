import flightPlanner.AuthManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AuthManagerTest {

    private static AuthManager authManager;

    @BeforeAll
    public static void setUp() throws IOException {
        authManager = new AuthManager();
    }

    @Test
    @DisplayName("Test that checks when an user is registered, s/he can login successfully")
    public void testLoginSuccessful() {
        boolean loginResult = authManager.login("admin", "admin123");
        assertTrue(loginResult);
        assertEquals("admin", authManager.getLoggedInUser());
    }

    @Test
    @DisplayName("Test that checks when an unknown user tries to login, s/he will fail")
    public void testLoginFailure() {
        boolean loginResult = authManager.login("nonexistentUser", "password");
        assertFalse(loginResult);
        assertNull(authManager.getLoggedInUser());
    }

    @Test
    @DisplayName("Test that checks the registration of a new user")
    public void testRegisterNewUser() throws IOException {
        boolean registerResult = authManager.register("newUser", "newPass123", "newuser@example.com", "Passenger");
        assertTrue(registerResult);
        assertEquals("newPass123", authManager.getUsers().get("newUser"));
        assertEquals("newuser@example.com", authManager.getEmails().get("newUser"));
    }

    @Test
    @DisplayName("Test that checks when an user is already registered, s/he can't register again")
    public void testRegisterDuplicateUser() throws IOException {
        authManager.register("duplicateUser", "password", "duplicate@example.com", "Passenger");
        boolean registerResult = authManager.register("duplicateUser", "password", "duplicate@example.com", "Passenger");
        assertFalse(registerResult);  // Non dovrebbe permettere la registrazione
    }

    @Test
    @DisplayName("Test that checks the functionality of the method logout()")
    public void testLogout() {
        authManager.login("admin", "admin123");
        authManager.logout();
        assertNull(authManager.getLoggedInUser());
    }

    @Test
    @DisplayName("Test that checks if the role of an user is matching")
    public void testGetCurrentUserRole() {
        authManager.login("admin", "admin123");
        assertTrue(authManager.isAdmin());
        String role = authManager.getCurrentUserRole();
        assertEquals("Admin", role);
    }

    @Test
    @DisplayName("Test that checks the validity of updating the password and the email of an user")
    public void testUpdateUser() throws IOException {
        authManager.register("updateUser", "oldPassword", "oldEmail@example.com", "Passenger");
        boolean updateResult = authManager.updateUser("updateUser", "newPassword", "newEmail@example.com");
        assertTrue(updateResult);
        assertEquals("newPassword", authManager.getUsers().get("updateUser"));
        assertEquals("newEmail@example.com", authManager.getEmails().get("updateUser"));
    }

    @Test
    @DisplayName("Test that checks the validity of unsubscription of an user ")
    public void testRemoveUser() throws IOException {
        authManager.register("deleteUser", "password", "delete@example.com", "Passenger");
        authManager.removeUser("deleteUser");
        assertNull(authManager.getUsers().get("deleteUser"));
        assertNull(authManager.getEmails().get("deleteUser"));
    }

    @Test
    @DisplayName("Test that checks loading users from the file CSV works correctly")
    public void testLoadUserFromCSV() {
        Map<String, String> usernamePasswords = authManager.getUsers();
        Map<String, String> usernameEmails = authManager.getEmails();
        Map<String, String> usernameRole = authManager.getUserRoles();

        assertNotNull(usernamePasswords);
        assertFalse(usernamePasswords.isEmpty());
        assertNotNull(usernameEmails);
        assertFalse(usernameEmails.isEmpty());
        assertNotNull(usernameRole);
        assertFalse(usernameRole.isEmpty());
    }
}
