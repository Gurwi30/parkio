package it.parkio.app.scheduler;

import it.parkio.app.model.ParkingSpace;
import it.parkio.app.model.ParkingSpaceStatus;
import org.jetbrains.annotations.NotNull;
import org.jxmapviewer.JXMapViewer;

import javax.swing.*;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Gestisce le transizioni temporali automatiche degli stati dei posti auto.
 *
 * <p>Esempi tipici:</p>
 * <ul>
 *     <li>uno spazio riservato che deve diventare occupato all'ora di inizio;</li>
 *     <li>uno spazio occupato o riservato che deve tornare libero alla scadenza.</li>
 * </ul>
 *
 * <p>In questo modo la UI non deve controllare continuamente il tempo:
 * basta pianificare l'azione futura una sola volta.</p>
 */
public class ParkingSpaceScheduler {

    /**
     * Scheduler condiviso con un solo thread daemon.
     *
     * <p>Essendo daemon, non impedisce la chiusura dell'applicazione.</p>
     */
    private static final ScheduledExecutorService SCHEDULER =
            Executors.newScheduledThreadPool(1, r -> {
                Thread t = new Thread(r, "parking-space-scheduler");
                t.setDaemon(true);
                return t;
            });

    /**
     * Mappa dei task pianificati, indicizzati per id dello spazio.
     *
     * <p>Serve per poter annullare una pianificazione precedente
     * quando lo stato dello spazio viene modificato manualmente.</p>
     */
    private static final Map<Integer, ScheduledFuture<?>> pending = new ConcurrentHashMap<>();

    /**
     * Costruttore privato: classe utility solo statica.
     */
    private ParkingSpaceScheduler() {}

    /**
     * Pianifica il comportamento automatico futuro di uno spazio
     * in base al suo stato corrente.
     *
     * @param space     spazio da monitorare
     * @param mapViewer componente mappa da ridisegnare quando cambia stato
     */
    public static void schedule(@NotNull ParkingSpace space, @NotNull JXMapViewer mapViewer) {
        // Se esiste già una pianificazione precedente per questo spazio, la sostituiamo.
        cancel(space);

        switch (space.getStatus()) {

            case ParkingSpaceStatus.Reserved res -> {
                long msUntilStart = msUntil(res.getStart());
                long msUntilEnd   = msUntil(res.getEnd());

                // Se la riserva è già scaduta, lo spazio torna immediatamente libero.
                if (msUntilEnd <= 0) {
                    applyAndRepaint(space, ParkingSpaceStatus.free(), mapViewer);
                    return;
                }

                // Se la riserva è già iniziata, lo spazio diventa subito occupato
                // e si pianifica solo il momento di rilascio.
                if (msUntilStart <= 0) {
                    applyAndRepaint(space,
                            ParkingSpaceStatus.occupied(res.getCarPlate(), res.getStart(), res.getEnd()),
                            mapViewer);

                    scheduleRelease(space, msUntilEnd, mapViewer);
                } else {
                    // Altrimenti si pianifica il passaggio futuro da riservato a occupato.
                    ScheduledFuture<?> toOccupied = SCHEDULER.schedule(() -> {
                        applyAndRepaint(space,
                                ParkingSpaceStatus.occupied(res.getCarPlate(), res.getStart(), res.getEnd()),
                                mapViewer);

                        // Dopo il passaggio a occupato, si pianifica il ritorno a libero.
                        scheduleRelease(space, msUntil(res.getEnd()), mapViewer);
                    }, msUntilStart, TimeUnit.MILLISECONDS);

                    pending.put(space.getId(), toOccupied);
                }
            }

            case ParkingSpaceStatus.Occupied occ -> occ.getEnd().ifPresent(end -> {
                long msUntilEnd = msUntil(end);

                // Se la fine è già passata, libera subito lo spazio.
                if (msUntilEnd <= 0) {
                    applyAndRepaint(space, ParkingSpaceStatus.free(), mapViewer);
                    return;
                }

                // Altrimenti pianifica il rilascio alla scadenza.
                scheduleRelease(space, msUntilEnd, mapViewer);
            });

            // Se lo spazio è libero non serve pianificare nulla.
            default -> {}
        }
    }

    /**
     * Annulla un'eventuale pianificazione esistente per lo spazio indicato.
     *
     * @param space spazio di cui annullare il task pendente
     */
    public static void cancel(@NotNull ParkingSpace space) {
        ScheduledFuture<?> existing = pending.remove(space.getId());
        if (existing != null) existing.cancel(false);
    }

    /**
     * Arresta immediatamente lo scheduler.
     *
     * <p>Viene chiamato in fase di chiusura dell'applicazione.</p>
     */
    public static void shutdown() {
        SCHEDULER.shutdownNow();
    }

    /**
     * Pianifica il ritorno dello spazio allo stato libero dopo un certo ritardo.
     *
     * @param space     spazio da liberare
     * @param delayMs   ritardo in millisecondi
     * @param mapViewer mappa da ridisegnare
     */
    private static void scheduleRelease(@NotNull ParkingSpace space, long delayMs, @NotNull JXMapViewer mapViewer) {
        ScheduledFuture<?> toFree = SCHEDULER.schedule(() -> applyAndRepaint(space, ParkingSpaceStatus.free(), mapViewer), delayMs, TimeUnit.MILLISECONDS);
        pending.put(space.getId(), toFree);
    }

    /**
     * Applica un nuovo stato allo spazio e richiede il repaint della mappa.
     *
     * <p>Il repaint viene eseguito sul thread Swing corretto tramite {@link SwingUtilities#invokeLater(Runnable)}.</p>
     *
     * @param space     spazio da aggiornare
     * @param status    nuovo stato
     * @param mapViewer mappa da ridisegnare
     */
    private static void applyAndRepaint(@NotNull ParkingSpace space,
                                        @NotNull ParkingSpaceStatus status,
                                        @NotNull JXMapViewer mapViewer) {
        space.updateStatus(status);
        SwingUtilities.invokeLater(mapViewer::repaint);
    }

    /**
     * Calcola quanti millisecondi mancano da adesso all'istante indicato.
     *
     * @param instant istante target
     * @return differenza in millisecondi tra target e ora corrente
     */
    private static long msUntil(@NotNull Instant instant) {
        return instant.toEpochMilli() - Instant.now().toEpochMilli();
    }

}