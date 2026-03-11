package it.parkio.app.event.base;

@FunctionalInterface
public interface EventListener<T extends Event> {

    void onEvent(T value);

}
