package it.parkio.app.event.base;

@FunctionalInterface
public interface EventListener<T extends Event> {

    public void onEvent(T value);

}
