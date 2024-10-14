package flightPlanner;

public class Admin extends User {
    private final String role;

    public Admin(String username, String password, String email) {
        super(username, password, email);
        this.role = "Admin";
    }

    @Override
    public void displayRole() {
        System.out.println("Role: " + role);
    }

}
