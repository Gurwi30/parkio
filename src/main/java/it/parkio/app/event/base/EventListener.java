package it.parkio.app.event.base;

/**
 * Rappresenta un listener generico che reagisce a un evento di tipo {@code T}.
 *
 * <p>È annotata con {@link FunctionalInterface}, quindi può essere usata
 * comodamente con lambda expression o method reference.
 * Esempio tipico:
 * <pre>{@code
 * eventManager.register(MyEvent.class, event -> {
 *     // logica da eseguire quando arriva l'evento
 * });
 * }</pre>
 * </p>
 *
 * @param <T> tipo concreto di evento che il listener è in grado di ricevere
 */
@FunctionalInterface
public interface EventListener<T extends Event> {

    /**
     * Metodo invocato quando viene pubblicato un evento del tipo associato.
     *
     * @param value evento ricevuto dal listener
     */
    void onEvent(T value);

}
