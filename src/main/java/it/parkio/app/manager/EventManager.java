package it.parkio.app.manager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import it.parkio.app.event.base.Event;
import it.parkio.app.event.base.EventListener;
import org.jetbrains.annotations.NotNull;

/**
 * Gestore centrale degli eventi dell'applicazione.
 *
 * <p>Questa classe implementa un semplice meccanismo di publish/subscribe:
 * i componenti possono registrare listener per uno specifico tipo di evento,
 * e successivamente pubblicare un evento per notificare tutti i listener registrati.</p>
 *
 * <p>Il vantaggio principale è ridurre l'accoppiamento tra le classi:
 * un componente non deve conoscere direttamente chi reagirà a un'azione,
 * ma si limita a pubblicare un evento.</p>
 */
public class EventManager {

    /**
     * Mappa che associa ogni classe di evento all'insieme dei listener interessati.
     *
     * <p>La chiave è il tipo dell'evento, mentre il valore contiene tutti i listener
     * registrati per quel tipo specifico.</p>
     */
    private final Map<Class<? extends Event>, Set<EventListener<?>>> listeners = new HashMap<>();

    /**
     * Pubblica un evento e notifica tutti i listener registrati per la sua classe concreta.
     *
     * <p>Il cast è necessario perché i listener sono conservati in una struttura generica,
     * ma al momento della chiamata sappiamo che i listener associati a quella chiave
     * sono compatibili con il tipo reale dell'evento.</p>
     *
     * @param event evento da distribuire ai listener interessati
     * @param <T>   tipo concreto dell'evento
     */
    @SuppressWarnings("unchecked")
    public <T extends Event> void call(@NotNull T event) {
        Set<EventListener<T>> listeners = (Set<EventListener<T>>) (Set<?>) this.listeners.get(event.getClass());

        // Se nessuno ascolta questo tipo di evento, non c'è nulla da fare.
        if (listeners == null) return;

        // Notifica ogni listener passando l'evento appena pubblicato.
        listeners.forEach(listener -> listener.onEvent(event));
    }

    /**
     * Registra un listener per uno specifico tipo di evento.
     *
     * <p>Se per quel tipo non esiste ancora un insieme di listener,
     * viene creato automaticamente tramite {@code computeIfAbsent}.</p>
     *
     * @param type     classe dell'evento da ascoltare
     * @param listener listener da invocare quando verrà pubblicato un evento di quel tipo
     * @param <T>      tipo concreto dell'evento
     */
    public <T extends Event> void register(Class<T> type, EventListener<T> listener) {
        listeners
            .computeIfAbsent(type, _ -> new HashSet<>())
            .add(listener);
    }

}
