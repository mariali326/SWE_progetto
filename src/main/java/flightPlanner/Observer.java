package flightPlanner;

public interface Observer {
    void update(String message, NotificationType type);
}
