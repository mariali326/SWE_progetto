package domainModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NotificationPreferences {
    private final Set<NotificationType> preferredTypes;
    private final List<NotificationChannel> channels;

    public NotificationPreferences() {
        this.preferredTypes = new HashSet<>();
        this.channels = new ArrayList<>();
    }

    public void addPreference(NotificationType type) {
        preferredTypes.add(type);
    }

    public boolean isPreferred(NotificationType type) {
        return preferredTypes.contains(type);
    }

    public void addChannel(NotificationChannel channel) {
        channels.add(channel);
    }

    public List<NotificationChannel> getChannels() {
        return channels;
    }

    public Set<NotificationType> getPreferredTypes() {
        return preferredTypes;
    }
}
