package domainModel;

public interface Subject {
    void notify(String message, NotificationType type);

    void subscribe(Observer observer);

    void unsubscribe(Observer observer);
}
