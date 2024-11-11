package domainModel;

public interface Observer {
    void update(String message, NotificationType type);
}
