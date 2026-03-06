package it.parkio.app.manager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import it.parkio.app.event.base.Event;
import it.parkio.app.event.base.EventListener;

public class EventManager {

    private final Map<Class<? extends Event>, Set<EventListener<?>>> listeners = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T extends Event> void call(T event) {
        Set<EventListener<T>> listeners = (Set<EventListener<T>>) (Set<?>) this.listeners.get(event.getClass());

        if (listeners == null) return;

        listeners.forEach(listener -> listener.onEvent(event));
    }

    public <T extends Event> void register(Class<T> type, EventListener<T> listener) {
        listeners
            .computeIfAbsent(type, k -> new HashSet<>())
            .add(listener);
    }

}
