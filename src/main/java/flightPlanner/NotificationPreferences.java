package flightPlanner;

import java.util.*;

public class NotificationPreferences {
    private Set<NotificationType> preferredTypes;
    private List<NotificationChannel> channels;

    public NotificationPreferences() {
        this.preferredTypes = new HashSet<>();
        this.channels = new ArrayList<>();
    }

    public void addPreference(NotificationType type){
        preferredTypes.add(type);
    }

    public void removePreference(NotificationType type){
        preferredTypes.remove(type);
    }

    public boolean isPreferred(NotificationType type){
        return preferredTypes.contains(type);
    }

    public void addChannel(NotificationChannel channel){
        channels.add(channel);
    }

    public List<NotificationChannel> getChannels(){
        return channels;
    }

    public Set<NotificationType> getPreferredTypes() {
        return preferredTypes;
    }
}
