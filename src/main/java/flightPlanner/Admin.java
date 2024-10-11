package flightPlanner;

public class Admin extends User {

    public Admin(String username, String password) {
        super(username, password);
    }

    @Override
    public void displayRole() {
        System.out.println("Role: Admin");
    }

}
